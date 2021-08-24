package discord.interactions

import discord.*
import discord.interactions.commands.CommandResponse
import discord.interactions.components.Component
import io.ktor.client.request.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

abstract class InteractionContext(
    protected val client: Client,
    val message: Message?,
    val interactionId: Long,
    protected val interactionToken: String,
    val guildId: Long,
    val channelId: Long
) {
    private var _deferred = false
    private var deferredHidden = false
    val deferred get() = _deferred

    private var _responded = false
    val responded get() = _responded

    val guild by lazy { client.getGuild(guildId) }

    val channel by lazy { client.getChannel(channelId) }

    val voiceClient: VoiceProtocol get() = TODO()

    suspend fun defer(hidden: Boolean = false) {
        _deferred = true
        deferredHidden = false
        invokeDefer(hidden)
    }

    protected abstract suspend fun invokeDefer(hidden: Boolean)

    abstract suspend fun send(
        content: String? = null,
        embeds: List<Embed> = emptyList(),
        tts: Boolean = false,
        files: List<File> = emptyList(),
        allowedMentions: AllowedMentions? = null,
        hidden: Boolean = false,
        components: List<Component> = emptyList()
    ): CommandResponse
}