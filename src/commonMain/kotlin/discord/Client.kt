package discord

import discord.abc.AbstractChannel
import discord.abc.GuildChannel
import discord.abc.PrivateChannel
import discord.events.*
import discord.exceptions.*
import discord.interactions.Interaction
import discord.interactions.ReceivedInteraction
import discord.interactions.commands.ApplicationCommand
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Represents a client connection that connects to Discord. This class is used to interact with the Discord WebSocket and API.
 * @param maxMessages The maximum number of messages to store in the internal message cache. This defaults to `1000`. Passing in `-1` disables the message cache.
 * @param coroutineScope
 * @param proxy
 * @param shardId
 * @param shardCount
 * @param intents
 * @param memberCacheFlags
 * @param fetchOfflineMembers
 * @param chunkGuildsAtStartup
 * @param status
 * @param activities
 * @param allowedMentions
 * @param heartbeatTimeout
 * @param guildReadyTimeout
 * @param guildSubscriptions
 */
@OptIn(ExperimentalTime::class)
class Client constructor(
    maxMessages: Int = 1000,
    val coroutineScope: CoroutineScope = GlobalScope,
    proxy: String? = null,
    //proxyAuth: BasicAuth? = null,
    shardId: Int? = null,
    shardCount: Int = 0,
    val intents: Intents = Intents.default,
    memberCacheFlags: MemberCacheFlags = MemberCacheFlags.fromIntents(intents),
    fetchOfflineMembers: Boolean = false,
    chunkGuildsAtStartup: Boolean = intents.members,
    var status: Status = Status.Online,
    var activities: List<Activity> = emptyList(),
    val allowedMentions: AllowedMentions? = null,
    private val heartbeatTimeout: Duration = Duration.seconds(60),
    private val guildReadyTimeout: Duration = Duration.seconds(2),
    guildSubscriptions: Boolean = true,
) {
    internal val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
        install(WebSockets)
    }

    private var shouldClose = false

    private var sequenceId = -1

    private lateinit var token: String
    private var bot = true
    internal lateinit var authValue: String //TODO: private

    private lateinit var gatewayUrl: Url
    private var shards = 0
    private var _ws: DefaultWebSocketSession? = null
    val ws get() = _ws

    //private val heartbeatTimeout = (heartbeatTimeout * 1000).toLong()
    //private val guildReadyTimeout = (guildReadyTimeout * 1000).toLong()
    private var _ready = false
    val ready get() = _ready

    private lateinit var _application: Application
    val applicationId get() = _application.id

    private val eventHandlers = hashMapOf<String, MutableSet<suspend (EventContext) -> Unit>>()
    private val globalApplicationCommands = hashMapOf<String, ApplicationCommand>()
    private val guildApplicationCommands = hashMapOf<Long, HashMap<String, ApplicationCommand>>()

    /**
     * Retrieves a sequence that enables receiving your guilds.
     *
     * Note:
     * Using this, you will only receive `Guild.owner`, `Guild.icon`, `Guild.id`, and `Guild.name` per Guild.
     *
     * Note:
     * This method is an API call. For general usage, consider `guilds` instead.
     * @param limit The number of guilds to retrieve. If `-1`, it retrieves every guild you have access to. Note, however, that this would make it a slow operation. Defaults to `100`.
     * @param before Retrieves guilds before this date.
     * @param after Retrieve guilds after this date or object.
     * @return The guild with the guild data parsed.
     * @throws HTTPException Getting the guilds failed
     */
    @Throws(HTTPException::class)
    suspend fun fetchGuilds(
        limit: Int = 100,
        before: Instant? = null,
        after: Instant? = null
    ): Sequence<Guild> {
        TODO()
    }

    private var _latency: Duration = Duration.INFINITE
    val latency: Duration get() = _latency

    private var rateLimited = false
    val isWsRateLimited: Boolean get() = rateLimited

    private lateinit var _user: User
    val user get() = _user

    private lateinit var sessionId: String

    private val _guilds = mutableListOf<Guild>()
    val guilds: List<Guild> get() = _guilds

    private val _emojis = mutableListOf<Emoji>()
    val emojis: List<Emoji> get() = _emojis

    private val cachedMessages = emptySequence<Message>()

    private val _privateChannels = mutableListOf<PrivateChannel>()
    val privateChannels: List<PrivateChannel> get() = _privateChannels

    private val _voiceClients = mutableListOf<VoiceProtocol>()
    val voiceClients: List<VoiceProtocol> get() = _voiceClients

    val isReady: Boolean get() = TODO()

    private var afk = -1L

    //private var exceptionHandler: suspend (Exception) -> Unit = { System.err.println("Error: "); it.printStackTrace() }
    /*fun onError(eventMethod: (exception: Throwable) -> Unit) {
        Thread.setDefaultUncaughtExceptionHandler { _, e -> eventMethod(e) }
    }*/

    @Deprecated("", replaceWith = ReplaceWith("Guild.chunk()"))
    suspend fun requestOfflineMembers(vararg guilds: Guild) {
        TODO()
    }

    suspend fun beforeIdentityHook(shardId: Int, initial: Boolean = false) {
        TODO()
    }

    @Throws(LoginFailure::class, HTTPException::class)
    suspend fun login(token: String, bot: Boolean = true) {
        this.token = token
        this.bot = bot
        if (!this.bot) {
            println("WARNING: Logging on with a user token is against the Discord Terms of Service and doing so might potentially get your account banned. Use this at your own risk.")
        }
        authValue = (if (bot) "Bot " else "Bearer ") + token

        try {
            if (bot) {
                val gatewayResponse = getGatewayBot()
                gatewayUrl = Url(gatewayResponse.url)
                shards = gatewayResponse.shards
            } else {
                gatewayUrl = Url(getGateway().url)
            }
        } catch (e: Exception) {
            throw GatewayNotFound(e)
        }
    }

    @Deprecated("This is just an alias to close(). If you want to do extraneous cleanup when subclassing, it is suggested to override close() instead", replaceWith = ReplaceWith("close()"))
    suspend fun logout() {
        close()
    }

    @OptIn(ExperimentalTime::class)
    @Throws(GatewayNotFound::class, ConnectionClosed::class)
    suspend fun connect(reconnect: Boolean = true) {
        httpClient.wss(
            method = HttpMethod.Get,
            host = gatewayUrl.host,
            path = "/?v9=&encoding=json"
        ) {
            logger.debug("Connected to the websocket")
            _ws = this

            val hello = json.parseToJsonElement(io.ktor.utils.io.core.String(incoming.receive().data)).jsonObject
            val heartbeat = hello["d"]!!.jsonObject["heartbeat_interval"]!!.jsonPrimitive.long - 1000

            val beforeIdTime = Clock.System.now()
            send(2, buildJsonObject {
                put("token", token)
                put("intents", 513 /*TODO*/)
                put("properties", buildJsonObject {
                    put("\$os", OS)
                    put("\$browser", "discord-kt")
                    put("\$device", "discord-kt")
                })
            })

            val readyIncStr = io.ktor.utils.io.core.String(incoming.receive().readBytes())
            _latency = Clock.System.now() - beforeIdTime
            val readyInc = json.parseToJsonElement(readyIncStr).jsonObject
            require(readyInc["op"]!!.jsonPrimitive.int == 0)
            require(readyInc["t"]?.jsonPrimitive?.contentOrNull == "READY")
            sequenceId = readyInc["s"]!!.jsonPrimitive.int

            val ready = json.decodeFromJsonElement<ReadyEvent>(readyInc["d"]!!.jsonObject)
            var readyTime = Clock.System.now() + guildReadyTimeout
            launch {
                changePresence(activities, status, false)
                while (Clock.System.now() <= readyTime)
                    delay(guildReadyTimeout)
                _application = ready.application
                /*guilds.forEach { guild ->
                    applications.forEach { it.guildCommands[guild.id]?.forEach { (_, m) -> guild.registerSlashCommand(m.findAnnotation(), this)} }
                }*/ //TODO
                guilds.forEach { guild ->
                    guildApplicationCommands[guild.id]?.forEach { (_, c) -> guild.registerSlashCommand(c, this) }
                }
                _ready = true
                _user = ready.user
                sessionId = ready.sessionId
                callEvent(ready)
            }

            var receivedHeartbeatResponse: Boolean

            launch {
                delay((heartbeat * Random(heartbeat).nextDouble()).toLong())
                while (true) {
                    sendHeartbeat()
                    val timestamp = Clock.System.now()
                    receivedHeartbeatResponse = false
                    while (!receivedHeartbeatResponse && Clock.System.now() - timestamp < heartbeatTimeout) delay(100)
                    _latency = Clock.System.now() - timestamp
                    delay(heartbeat)
                }
            }

            while (!shouldClose) {
                val inc = json.parseToJsonElement(io.ktor.utils.io.core.String(incoming.receive().readBytes())).jsonObject
                when (inc["op"]!!.jsonPrimitive.int) {
                    11 -> receivedHeartbeatResponse = true
                    1 -> sendHeartbeat()
                    0 -> {
                        sequenceId = inc["s"]!!.jsonPrimitive.int
                        val data = inc["d"]!!.jsonObject
                        when (inc["t"]!!.jsonPrimitive.content) {
                            "GUILD_CREATE" -> {
                                val guild = json.decodeFromJsonElement<Guild>(data).apply { client = this@Client }
                                _guilds.add(guild)
                                if (!this@Client.ready)
                                    readyTime += guildReadyTimeout
                                else
                                    callEvent(JoinGuildEvent(guild))
                            }
                            "MESSAGE_CREATE" -> callEvent(MessageCreateEvent(json.decodeFromJsonElement<Message>(data).apply { client = this@Client }))
                            "INTERACTION_CREATE" -> callInteraction(json.decodeFromJsonElement<ReceivedInteraction>(data).apply { client = this@Client })
                            else -> logger.warn("Unhandled event: ${inc["t"]}")
                        }
                    }
                }
            }

            launch {
                val closeReason = closeReason.await()
                if (closeReason == null) {
                    //reconnect
                    logger.info("Reconnecting...")
                    send(6, buildJsonObject {
                        put("token", token)
                        put("session_id", ready.sessionId)
                        put("seq", sequenceId)
                    })
                } else {
                    shouldClose = true
                    logger.warn("Connection was closed by Discord: ${closeReason.message} (${closeReason.knownReason?.let { "Reason: $it" } ?: "Code: ${closeReason.code}"}")
                }
            }

            _ws = null
            logger.debug("Closing connection")
            close(CloseReason(1000, "Bot closed connection"))
        }
    }

    private suspend fun DefaultWebSocketSession.sendHeartbeat() {
        send(1, JsonPrimitive(sequenceId.takeIf { it >= 0 }))
    }

    suspend fun close() {
        shouldClose = true
    }

    fun clear() {
        TODO()
    }

    suspend fun start(token: String, bot: Boolean = true, reconnect: Boolean = true) {
        login(token, bot)
        connect(reconnect)
    }

    fun run(token: String, bot: Boolean = true, reconnect: Boolean = true) = coroutineScope.launch {
        try {
            start(token, bot, reconnect)
        } finally {
            close()
        }
    }

    val isClosed: Boolean get() = TODO()

    private val _users = mutableListOf<User>()
    val users: List<User> get() = _users

    fun getChannel(id: Long): AbstractChannel? {
        TODO()
    }

    fun getGuild(id: Long): Guild? {
        TODO()
    }

    fun getUser(id: Long): User? {
        TODO()
    }

    fun getEmoji(id: Long): Emoji? {
        TODO()
    }

    val allChannels: Sequence<GuildChannel> get() {
        TODO()
    }

    val allMembers: Sequence<Member> get() {
        TODO()
    }

    suspend fun waitUntilReady() {
        TODO()
    }

    suspend fun <E : EventContext> waitFor(check: (E) -> Boolean, timeout: Float = Float.POSITIVE_INFINITY): Any {
        TODO()
    }

    @Suppress("unchecked_cast")
    inline fun <reified E : EventContext> event(noinline function: suspend (E) -> Unit) {
        registerEvent(E::class.simpleName!!, function as suspend (EventContext) -> Unit)
    }

    @PublishedApi
    internal fun registerEvent(name: String, function: suspend (EventContext) -> Unit) {
        eventHandlers.getOrPut(name, ::mutableSetOf).add(function)
    }

    fun interaction(interaction: Interaction) {
        when (interaction) {
            is ApplicationCommand -> {
                if (interaction.guildIds.isEmpty()) globalApplicationCommands[interaction.name] = interaction
                else interaction.guildIds.forEach { guildApplicationCommands.getOrPut(it, ::hashMapOf)[interaction.name] = interaction }
            }
        }
    }

    suspend fun changePresence(activities: List<Activity>? = null, status: Status? = null, afk: Boolean = false) {
        if (activities == null && status == null && this.afk == -1L == afk) return
        status?.let { this.status = it }
        activities?.let { this.activities = it }
        val a = activities?.onEach { it.createdAt = Clock.System.now() } ?: this.activities
        (ws ?: return).send(3, buildJsonObject {
            put("since", Clock.System.now().epochSeconds)
            put("status", json.encodeToJsonElement(this@Client.status))
            put("activities", json.encodeToJsonElement(a))
            put("afk", afk)
        })
    }

    suspend fun fetchTemplate(code: String): Template {
        TODO()
    }

    suspend fun fetchTemplate(template: Template): Template {
        TODO()
    }

    suspend fun fetchGuild(guildId: Long): Guild = fetch("/guilds/$guildId")

    suspend fun createGuild(
        name: String,
        region: VoiceRegion = VoiceRegion.US_WEST,
        icon: ByteArray? = null,
        code: String? = null
    ): Guild {
        TODO()
    }

    suspend fun fetchInvite(url: String, withCounts: Boolean = true): Invite {
        TODO()
    }

    suspend fun fetchInvite(invite: Invite, withCounts: Boolean = true): Invite {
        TODO()
    }

    suspend fun fetchWidget(guildId: Long): Widget {
        TODO()
    }

    suspend fun applicationInfo(): AppInfo {
        TODO()
    }

    suspend fun fetchUser(userId: Long): User = fetch("/users/$userId")

    @Deprecated("This can only be used by non-bot accounts.")
    suspend fun fetchUserProfile(userId: Long): Profile {
        TODO()
    }

    suspend fun fetchChannel(channelId: Long): AbstractChannel = fetch("/channels/$channelId")

    suspend fun fetchWebhook(webhookId: Long): Webhook = fetch("/webhooks/$webhookId")

    suspend fun DefaultWebSocketSession.send(op: Int, data: JsonElement?) = send(buildJsonObject {
        put("op", op)
        data?.let { put("d", it) }
    }.toString())

    suspend inline fun <reified E : EventContext> callEvent(event: E) = callEvent(E::class.simpleName!!, event)

    @PublishedApi
    internal suspend fun callEvent(name: String, event: EventContext) {
        eventHandlers[name]?.forEach { it(event) }
    }

    private suspend fun callInteraction(interaction: ReceivedInteraction) {
        when (interaction.type) {
            2 -> {
                val context = interaction.context() ?: return
                guildApplicationCommands[interaction.guildId]?.forEach { (n, m) -> if (n == interaction.data!!.name) m.executor(context) }
            }
            else -> logger.warn("Unknown interaction type: ${interaction.type}")
        }
    }

    private suspend inline fun <reified T> fetch(url: String) = httpClient.get<T>(API_BASE_URL + url) {
        header("Authorization", authValue)
    }

    private suspend fun getGatewayBot() = fetch<GatewayBotResponse>("/gateway/bot")

    @Serializable
    class GatewayBotResponse(
        val url: String,
        val shards: Int,
        val session_start_limit: SessionStartLimit
    ) {
        @Serializable
        class SessionStartLimit(
            val total: Int,
            val remaining: Int,
            val reset_after: Int,
            val max_concurrency: Int
        )
    }

    private suspend fun getGateway() = httpClient.get<GatewayResponse>("$API_BASE_URL/gateway")

    @Serializable
    class GatewayResponse(
        val url: String
    )

    private val logger = logger<Client>()
}