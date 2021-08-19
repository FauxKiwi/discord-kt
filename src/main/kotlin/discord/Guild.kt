package discord

import base.DateTime
import discord.abc.Snowflake
import discord.exceptions.ClientException
import discord.exceptions.Forbidden
import discord.exceptions.HTTPException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.SnowflakeId
import kotlin.jvm.Throws

/**
 * Represents a Discord guild.
 *
 * This is referred to as a “server” in the official Discord UI.
 *
 * @property name The guild name.
 * @property emojis All emojis that the guild owns.
 * @property afkTimeout The timeout to get sent to the AFK channel.
 * @property icon The guild’s icon.
 * @property id The guild's id.
 * @property ownerId The guild owner’s ID. Use `Guild.owner` instead.
 * @property unavailable Indicates if the guild is unavailable. If this is `true` then the reliability of other attributes outside of `Guild.id` is slim and they might all be `null`. It is best to not do anything with the guild if it is unavailable.
 * @property maxPresences The maximum amount of presences for the guild.
 * @property maxMembers The maximum amount of members for the guild. Note: This attribute is only available via `Client.fetch_guild()`.
 * @property maxVideoChannelUsers The maximum amount of users in a video channel.
 * @property banner The guild’s banner.
 * @property description The guild’s description.
 * @property tfaLevel Indicates the guild’s two factor authorisation level. If this value is 0 then the guild does not require 2FA for their administrative members. If the value is 1 then they do.
 * @property splash The guild’s invite splash.
 * @property premiumTier The premium tier for this guild. Corresponds to “Nitro Server” in the official UI. The number goes from 0 to 3 inclusive.
 * @property premiumSubscriptionCount The number of “boosts” this guild currently has.
 * @property preferredLocale The preferred locale for the guild. Used when filtering Server Discovery results to a specific language.
 * @property discoverySplash The guild’s discovery splash.
 */
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
    private val afkChannelId: Long?,
    @SerialName("afk_timeout")
    val afkTimeout: Int,
    @SerialName("widget_enabled")
    val widgetEnabled: Boolean = false,
    @SerialName("widget_channel_id")
    @SnowflakeId
    private val widgetChannelId: Long? = null,
    @SerialName("verification_level")
    private val _verificationLevel: Int,
    @SerialName("default_message_notifications")
    val defaultMessageNotifications: Int,
    @SerialName("explicit_content_filter")
    private val _explicitContentFilter: Int,
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
    @SerialName("premium_tier")
    val premiumTier: Int,
    @SerialName("premium_subscription_count")
    val premiumSubscriptionCount: Int? = null,
    @SerialName("preferred_locale")
    val preferredLocale: String?, //TODO: really nullable?
    val public_updates_channel_id: String?,
    @SerialName("max_video_channel_users")
    val maxVideoChannelUsers: Int = 0,
    val approximate_member_count: Int = 0,
    val approximate_presence_count: Int = 0,
    val welcome_screen: WelcomeScreen? = null,
    val nsfw_level: Int,
    val stage_instances: List<StageInstance>? = null,
    val stickers: List<Sticker>
) : Snowflake {
    /**
     * The channel that denotes the AFK channel. `null` if it doesn’t exist.
     */
    val afkChannel = if (afkChannelId != null) channels?.find { it.id == afkChannelId } else null

    /**
     * The guild’s verification level.
     */
    val verificationLevel = VerificationLevel.values()[_verificationLevel]

    val explicitContentFilter = ContentFilter.values()[_explicitContentFilter]

    @Throws(ClientException::class, HTTPException::class)
    suspend fun fetchMembers(limit: Int = 1000, after: DateTime? = null): Sequence<Member> {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun auditLogs(limit: Int = 100, before: DateTime? = null, after: DateTime? = null, oldestFirst: Boolean = after != null, user: Snowflake, action: AuditLogAction): Sequence<AuditLogEntry> {
        TODO()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Guild) return false
        return other.id == id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString() = name

    /**
     * A special feature that the guild has.
     */
    @Serializable
    enum class Feature {
        /**
         * Guild has VIP voice regions
         */
        @SerialName("VIP_REGIONS")
        VipRegions,

        /**
         * Guild can have a vanity invite URL (e.g. discord.gg/discord-api)
         */
        @SerialName("VANITY_URL")
        VanityUrl,

        /**
         * Guild’s invite page can have a special splash.
         */
        @SerialName("INVITE_SPLASH")
        InviteSplash,

        /**
         * Guild is a verified server.
         */
        @SerialName("VERIFIED")
        Verified,

        /**
         * Guild is a partnered server.
         */
        @SerialName("PARTNERED")
        Partnered,

        /**
         * Guild is allowed to have more than 50 custom emoji.
         */
        @SerialName("MORE_EMOJI")
        MoreEmoji,

        /**
         * Guild shows up in Server Discovery.
         */
        @SerialName("DISCOVERABLE")
        Discoverable,

        /**
         * Guild can be featured in Server Discovery.
         */
        @SerialName("FEATURABLE")
        Featurable,

        /**
         * Guild is a community server.
         */
        @SerialName("COMMUNITY")
        Community,

        /**
         * Guild can sell things using store channels.
         */
        @SerialName("COMMERCE")
        Commerce,

        /**
         * Guild is a public guild.
         */
        @SerialName("PUBLIC_GUILD")
        Public,

        /**
         * Guild can create news channels.
         */
        @SerialName("NEWS")
        News,

        /**
         * Guild can upload and use a banner (i.e. `banner_url()`).
         */
        @SerialName("BANNER")
        Banner,

        /**
         * Guild can upload an animated icon.
         */
        @SerialName("ANIMATED_ICON")
        AnimatedIcon,

        /**
         * Guild cannot be public.
         */
        @SerialName("PUBLIC_DISABLED")
        PublicDisabled,

        /**
         * Guild has enabled the welcome screen
         */
        @SerialName("WELCOME_SCREEN_ENABLED")
        WelcomeScreenEnabled,

        /**
         * Guild has Membership Screening enabled.
         */
        @SerialName("MEMBER_VERIFICATION_GATE_ENABLED")
        MemberVerificationGateEnabled,

        /**
         * Guild can be viewed before being accepted via Membership Screening.
         */
        @SerialName("PREVIEW_ENABLED")
        PreviewEnabled,

        @SerialName("PRIVATE_THREADS")
        PrivateThreads,
        @SerialName("THREADS_ENABLED_TESTING")
        ThreadsEnabledTesting,
        @SerialName("SEVEN_DAY_THREAD_ARCHIVE")
        SevenDayThreadArchive,
        @SerialName("THREE_DAY_THREAD_ARCHIVE")
        ThreeDayThreadArchive
    }

    /**
     * Specifies a `Guild`’s verification level, which is the criteria in which a member must meet before being able to send messages to the guild.
     */
    enum class VerificationLevel {
        /**
         * No criteria set.
         */
        None,

        /**
         * Member must have a verified email on their Discord account.
         */
        Low,

        /**
         * Member must have a verified email and be registered on Discord for more than five minutes.
         */
        Medium,

        /**
         * Member must have a verified email, be registered on Discord for more than five minutes, and be a member of the guild itself for more than ten minutes.
         */
        High,

        /**
         * Member must have a verified phone on their Discord account.
         */
        Extreme
    }

    /**
     * Specifies a `Guild`’s explicit content filter, which is the machine learning algorithms that Discord uses to detect if an image contains pornography or otherwise explicit content.
     */
    enum class ContentFilter {
        /**
         * The guild does not have the content filter enabled.
         */
        Disabled,

        /**
         * The guild has the content filter enabled for members without a role.
         */
        NoRole,

        /**
         * The guild has the content filter enabled for every member.
         */
        AllMembers
    }
}

@Serializable
class VoiceState

@Serializable
class GuildMember

@Serializable
class Channel(
    @SnowflakeId
    val id: Long
) {
}

@Serializable
class PresenceUpdate