package discord.events

import discord.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import discord.util.SnowflakeId

@Serializable
class ReadyEvent(
    @SerialName("v")
    val gatewayVersion: Int,
    val user: User,
    val guilds: List<UnavailableGuild>,
    @SerialName("session_id")
    val sessionId: String,
    val shard: IntArray? = null,
    val application: Application
) : EventContext() {
}

@Serializable
class UnavailableGuild(

)

@Serializable
class Application(
    @SnowflakeId
    val id: Long
)