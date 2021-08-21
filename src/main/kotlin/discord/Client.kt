package discord

import base.DateTime
import base.Or
import discord.abc.AbstractChannel
import discord.abc.GuildChannel
import discord.abc.PrivateChannel
import discord.events.EventContext
import discord.events.MessageCreateEvent
import discord.events.ReadyEvent
import discord.exceptions.*
import discord.interactions.Interaction
import discord.interactions.commands.Command
import discord.interactions.commands.CommandContext
import io.ktor.client.*
import io.ktor.client.engine.java.*
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.nio.ByteBuffer
import kotlin.random.Random
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType

/**
 * Represents a client connection that connects to Discord. This class is used to interact with the Discord WebSocket and API.
 * @param maxMessages The maximum number of messages to store in the internal message cache. This defaults to `1000`. Passing in `-1` disables the message cache.
 * @param coroutineScope
 * @param proxy
 * @param proxyAuth
 * @param shardId
 * @param shardCount
 * @param intents
 * @param memberCacheFlags
 * @param fetchOfflineMembers
 * @param chunkGuildsAtStartup
 * @param status
 * @param activity
 * @param allowedMentions
 * @param heartbeatTimeout
 * @param guildReadyTimeout
 * @param guildSubscriptions
 */
class Client(
    maxMessages: Int = 1000,
    val coroutineScope: CoroutineScope = GlobalScope,
    proxy: String? = null,
    proxyAuth: aiohttp.BasicAuth? = null,
    shardId: Int? = null,
    shardCount: Int = 0,
    val intents: Intents = Intents.default,
    memberCacheFlags: MemberCacheFlags = MemberCacheFlags.fromIntents(intents),
    fetchOfflineMembers: Boolean = false,
    chunkGuildsAtStartup: Boolean = intents.members,
    status: Status = Status(),
    val activity: Activity? = Activity(),
    val allowedMentions: AllowedMentions? = AllowedMentions(),
    heartbeatTimeout: Float = 60f,
    guildReadyTimeout: Float = 2f,
    guildSubscriptions: Boolean = true,
) {
    internal val httpClient = HttpClient(Java) {
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
    private var _ws: Unit? = null
    val ws get() = _ws

    private val heartbeatTimeout = (heartbeatTimeout * 1000).toLong()
    private val guildReadyTimeout = (guildReadyTimeout * 1000).toLong()

    private var _applicationId: Long = 0
    val applicationId get() = _applicationId //TODO

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
        before: DateTime? = null,
        after: DateTime? = null
    ): Sequence<Guild> {
        TODO()
    }

    private var _latency = 0f
    val latency: Float get() = _latency

    private var rateLimited = false
    val isWsRateLimited: Boolean get() = rateLimited

    private var _user: ClientUser? = null
    val user get() = _user

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

    private var errorHandler: suspend (Error) -> Unit = { System.err.println("Error: "); it.printStackTrace() }
    fun onError(eventMethod: suspend (error: Error) -> Unit) {
        errorHandler = eventMethod
    }

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
            throw GatewayNotFound()
        }
    }

    @Deprecated("This is just an alias to close(). If you want to do extraneous cleanup when subclassing, it is suggested to override close() instead", replaceWith = ReplaceWith("close()"))
    suspend fun logout() {
        close()
    }

    @Throws(GatewayNotFound::class, ConnectionClosed::class)
    suspend fun connect(reconnect: Boolean = true) {
        httpClient.wss(
            method = HttpMethod.Get,
            host = gatewayUrl.host,
            path = "/?v9=&encoding=json"
        ) {
            println("Connected to the websocket")

            val hello = json.parseToJsonElement(String(incoming.receive().data)).jsonObject
            val heartbeat = hello["d"]!!.jsonObject["heartbeat_interval"]!!.jsonPrimitive.long - 1000

            val beforeIdTime = System.currentTimeMillis()
            send(buildJsonObject {
                put("op", 2)
                put("d", buildJsonObject {
                    put("token", token)
                    put("intents", 513 /*TODO*/)
                    put("properties", buildJsonObject {
                        put("\$os", System.getProperty("os"))
                        put("\$browser", "discord-kt")
                        put("\$device", "discord-kt")
                    })
                })
            }.toString())

            val readyIncStr = String(incoming.receive().readBytes())
            _latency = (System.currentTimeMillis() - beforeIdTime) / 1000f
            val readyInc = json.parseToJsonElement(readyIncStr).jsonObject
            assert(readyInc["op"]!!.jsonPrimitive.int == 0)
            assert(readyInc["t"]?.jsonPrimitive?.contentOrNull == "READY")
            sequenceId = readyInc["s"]!!.jsonPrimitive.int

            val ready = json.decodeFromJsonElement<ReadyEvent>(readyInc["d"]!!.jsonObject)
            launch {
                delay(guildReadyTimeout)
                _applicationId = ready.application.id
                println("registering commands")
                guilds.forEach { guild ->
                    applications.forEach { it.guildCommands[guild.id]?.forEach { (_, m) -> guild.registerSlashCommand(m.findAnnotation()!!, this)} }
                }
                callEvent(ready)
            }

            var receivedHeartbeatResponse: Boolean

            launch {
                delay((heartbeat * Random(heartbeat).nextDouble()).toLong())
                while (true) {
                    send(buildJsonObject {
                        put("op", 1)
                        put("d", sequenceId.takeIf { it >= 0 })
                    }.toString())
                    val timestamp = System.currentTimeMillis()
                    println("Send HB")
                    receivedHeartbeatResponse = false
                    while (!receivedHeartbeatResponse) delay(100)
                    println("Received HBR")
                    _latency = (System.currentTimeMillis() - timestamp) / 1000f
                    delay(heartbeat)
                }
            }

            while (!shouldClose) {
                val inc = json.parseToJsonElement(String(incoming.receive().readBytes())).jsonObject
                when (inc["op"]!!.jsonPrimitive.int) {
                    11 -> {
                        println("HBR")
                        receivedHeartbeatResponse = true
                    }
                    0 -> {
                        sequenceId = inc["s"]!!.jsonPrimitive.int
                        val data = inc["d"]!!.jsonObject
                        when (inc["t"]!!.jsonPrimitive.content) {
                            "GUILD_CREATE" -> {
                                //println("Logged in to guild \"${data["name"]!!.jsonPrimitive.content}\"")
                                _guilds.add(json.decodeFromJsonElement<Guild>(data).apply { client = this@Client })
                            }
                            "MESSAGE_CREATE" -> callEvent(MessageCreateEvent(json.decodeFromJsonElement<Message>(data).apply { client = this@Client }))
                            "INTERACTION_CREATE" -> interaction(json.decodeFromJsonElement<Interaction>(data).apply { client = this@Client })
                            else -> println("Unhandled event: ${inc["t"]}")
                        }
                    }
                }
            }

            close(CloseReason(1000, "Bot closed connection"))
        }
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

    fun run(token: String, bot: Boolean = true, reconnect: Boolean = true) {
        try {
            coroutineScope.launch { start(token, bot, reconnect) }
        } catch (_: KeyboardInterrupt) {
            coroutineScope.launch { close() }
        } finally {
            //coroutineScope.close()
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

    suspend fun waitFor(event: String, check: (Event) -> Boolean, timeout: Float = Float.POSITIVE_INFINITY): Any {
        TODO()
    }

    fun event(event: String, function: suspend (Event) -> Unit) {
        TODO()
    }

    suspend fun changePresence(activity: Activity? = null, status: Status? = null, afk: Boolean = false) {
        TODO()
    }

    suspend fun fetchTemplate(@Or("template") code: String? = null, @Or("code") template: Template? = null): Template {
        TODO()
    }

    suspend fun fetchGuild(guildId: Long): Guild = fetch("/guilds/$guildId")

    suspend fun createGuild(
        name: String,
        region: VoiceRegion = VoiceRegion.US_WEST,
        icon: ByteBuffer? = null,
        code: String? = null
    ): Guild {
        TODO()
    }

    suspend fun fetchInvite(@Or("invite") url: String? = null, @Or("url") invite: Invite? = null, withCounts: Boolean = true): Invite {
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

    val applications = mutableListOf<DiscordApplication>()

    suspend fun callEvent(ctx: EventContext) = applications.forEach {
        it.eventHandlers[ctx::class.starProjectedType]?.forEach { m -> coroutineScope.launch { m.callSuspend(it, ctx) } }
    }

    private suspend fun interaction(interaction: Interaction) {
        if (applications.isEmpty()) return
        val context = interaction.context() ?: return
        applications.forEach { a ->
            a.guildCommands[interaction.guildId]!!.forEach { (n, m) -> if (n == interaction.data!!.name)m.callSuspend(a, context) }
        }
    }

    private suspend inline fun <reified T> fetch(url: String) = httpClient.get<T>(API_BASE_URL+url) {
        header("Authorization", authValue)
    }

    private suspend fun getGatewayBot() = fetch<GatewayBotResponse>("/gateway/bot")

    @Serializable
    private class GatewayBotResponse(
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
}