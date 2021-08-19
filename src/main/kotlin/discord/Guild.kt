package discord

import discord.abc.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.SnowflakeId

@Serializable
class Guild(
    @SnowflakeId
    override val id: Long,
    val name: String,
    val icon: String?,
    @SerialName("icon_hash")
    val iconHash: String? = null,
    val splash: String?,
    @SerialName("discovery_splash")
    val discoverySplash: String?,
    val owner: Boolean? = null,
    @SerialName("owner_id")
    @SnowflakeId
    val ownerId: Long,
    val permissions: String? = null,
    @Deprecated("This field is deprecated and will be removed in v9 and is replaced by rtc_region")
    val region: String? = null,
    @SerialName("afk_channel_id")
    @SnowflakeId
    val afkChannelId: Long?,
    @SerialName("afk_timeout")
    val afkTimeout: Int,
    @SerialName("widget_enabled")
    val widgetEnabled: Boolean = false,
    @SerialName("widget_channel_id")
    @SnowflakeId
    private val widgetChannelId: Long? = null,
    @SerialName("verification_level")
    val verificationLevel: Int,
    @SerialName("default_message_notifications")
    val defaultMessageNotifications: Int,
    @SerialName("explicit_content_filter")
    val explicitContentFilter: Int,
    val roles: List<Role>,
    val emojis: List<Emoji>,
    val features: List<Feature>,
    @SerialName("mfa_level")
    val tfaLevel: Int,
    @SerialName("application_id")
    @SnowflakeId
    private val applicationId: Long?,
    @SerialName("system_channel_id")
    @SnowflakeId
    private val systemChannelId: Long?,
    @SerialName("system_channel_flags")
    val systemChannelFlags: Int,
    @SerialName("rules_channel_id")
    @SnowflakeId
    private val rulesChannelId: Long?,
    val joined_at: String? = null,
    val large: Boolean = false,
    val unavailable: Boolean = false,
    val member_count: Int = 0,
    val voice_states: List<VoiceState>? = null,
    val members: List<GuildMember>? = null,
    val channels: List<Channel>? = null,
    val threads: List<Channel>? = null,
    val presences: List<PresenceUpdate>? = null,
    @SerialName("max_presences")
    val maxPresences: Int? = null,
    @SerialName("max_members")
    val maxMembers: Int,
    @SerialName("vanity_url_code")
    val vanityUrlCode: String?,
    val description: String?,
    val banner: String?,
    val premium_tier: Int,
    val premium_subscription_count: Int? = null,
    val preferred_locale: String,
    val public_updates_channel_id: String?,
    val max_video_channel_users: Int = 0,
    val approximate_member_count: Int = 0,
    val approximate_presence_count: Int = 0,
    val welcome_screen: WelcomeScreen? = null,
    val nsfw_level: Int,
    val stage_instances: List<StageInstance>? = null,
    val stickers: List<Sticker>
) : Snowflake {
    enum class Feature {
        WELCOME_SCREEN_ENABLED, NEWS, COMMUNITY, PRIVATE_THREADS, THREADS_ENABLED_TESTING,
        SEVEN_DAY_THREAD_ARCHIVE, THREE_DAY_THREAD_ARCHIVE
    }
}

@Serializable
class VoiceState

@Serializable
class GuildMember

@Serializable
class Channel

@Serializable
class PresenceUpdate