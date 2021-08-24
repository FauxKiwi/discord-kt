package discord.interactions.commands

import discord.*
import discord.interactions.InteractionContext
import discord.interactions.components.Component
import io.ktor.client.request.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import java.io.File
import kotlin.reflect.full.starProjectedType

class CommandContext(
    client: Client,
    interactionId: Long,
    interactionToken: String,
    guildId: Long,
    channelId: Long,
    val command: ClientCommand
) : InteractionContext(client, null, interactionId, interactionToken, guildId, channelId) {
    val name: String get() = command.name

    val args: Map<String, ClientCommand.Argument> get() = command.arguments

    inline operator fun <reified T> get(arg: String): T? = command.getArgument(arg) as? T?

    val subcommandName: String get() = TODO()
    val subcommandGroup: Nothing get() = TODO()

    val cog: Nothing get() = TODO()

    suspend fun invoke(): Nothing = TODO()

    override suspend fun send(
        content: String?,
        embeds: List<Embed>,
        tts: Boolean,
        files: List<File>,
        allowedMentions: AllowedMentions?,
        hidden: Boolean,
        components: List<Component>
    ): CommandResponse {
        client.httpClient.post<Unit>("$API_BASE_URL/v8/interactions/$interactionId/$interactionToken/callback") {
            this.header("Content-Type", "application/json")
            header("Authorization", client.authValue)
            this.body = buildJsonObject {
                put("type", 4)
                put("data", buildJsonObject {
                    if (tts) put("tts", tts)
                    if (content != null) put("content", content)
                    if (embeds.isNotEmpty()) put("embeds", json.encodeToJsonElement(embeds))
                    if (allowedMentions != null) put("allowed_mentions", json.encodeToJsonElement(allowedMentions))
                    var flags = 0u
                    if (hidden) flags /* |= */ = flags or (1u shl 6)
                    if (flags != 0u) put("flags", flags.toLong())
                    if (components.isNotEmpty()) put("components", json.encodeToJsonElement(components))
                })
            }
        }
        return CommandResponse()
    }

    override suspend fun invokeDefer(hidden: Boolean) {
        TODO("Not yet implemented")
    }
}

internal val CommandContextType = CommandContext::class.starProjectedType