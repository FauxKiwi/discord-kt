package discord

import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import discord.util.SnowflakeId

@Serializable
class Message(
    val content: String,
    @SerialName("channel_id")
    @SnowflakeId
    val channelId: Long,
    val author: User,
    @SerialName("guild_id")
    @SnowflakeId
    val guildId: Long? = null
) {
    override fun toString(): String = content

    @Transient
    internal lateinit var client: Client
    //TODO
    suspend fun respond(text: String?, embeds: List<Embed> = emptyList()) {
        client.httpClient.post<Message>("$API_BASE_URL/channels/$channelId/messages") {
            this.header("Content-Type", "application/json")
            header("Authorization", client.authValue)
            this.body = Out(text, embeds.takeUnless { it.isEmpty() })
        }
    }

    @Serializable
    private data class Out(
        val content: String?,
        val embeds: List<Embed>?
    )
}