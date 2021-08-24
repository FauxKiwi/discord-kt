package discord

import discord.abc.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import discord.util.SnowflakeId

@Serializable
class User(
    @SnowflakeId
    override val id: Long,
    val username: String,
    val discriminator: String,
    val avatar: String?,
    val bot: Boolean = false,
    val system: Boolean = false,
    @SerialName("mfa_enabled")
    val tfaEnabled: Boolean = false,
    val locale: String? = null,
    val verified: Boolean = false,
    val email: String? = null,
    val flags: Int = 0,
    @SerialName("premium_type")
    val nitroSubscription: Int = 0,
    @SerialName("public_flags")
    val publicFlags: Int = 0,
) : Snowflake {
}