package discord

import discord.abc.Snowflake
import kotlinx.serialization.Serializable
import discord.util.SnowflakeId
import kotlinx.serialization.SerialName

@Serializable
data class Role(
    @SnowflakeId
    override val id: Long,
    val name: String,
    val permissions: Int,
    val position: Int,
    val color: Int,
    val hoist: Boolean,
    val managed: Boolean,
    val mentionable: Boolean,
    val tags: Tags? = null,
    val permissions_new: String
) : Snowflake(), PermissionOverwrite.Target {
    @Serializable
    data class Tags(
        @SerialName("bot_id")
        @SnowflakeId
        val botId: Long? = null,
        @SerialName("integration_id")
        @SnowflakeId
        val integrationId: Long? = null,
        @SerialName("premium_subscriber")
        val premiumSubscriberRole: Boolean? = null
    )
}
