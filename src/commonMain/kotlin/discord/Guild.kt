package discord

import discord.abc.GuildChannel
import discord.abc.Snowflake
import discord.exceptions.ClientException
import discord.exceptions.Forbidden
import discord.exceptions.HTTPException
import discord.exceptions.InvalidData
import discord.interactions.commands.ApplicationCommand
import discord.interactions.commands.json
import discord.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.NoSuchElementException
import kotlin.jvm.JvmInline

/**
 * Represents a Discord guild.
 *
 * This is referred to as a “server” in the official Discord UI.
 */
@Serializable
class Guild(
    /**
     * The guild's id.
     */
    @SnowflakeId
    override val id: Long,
    /**
     * The guild name.
     */
    val name: String,
    /**
     * The guild’s icon.
     */
    val icon: String?,
    @SerialName("icon_hash")
    val iconHash: String? = null,
    /**
     * The guild’s invite splash.
     */
    val splash: String?,
    /**
     * The guild’s discovery splash.
     */
    @SerialName("discovery_splash")
    val discoverySplash: String?,
    @SerialName("owner")
    val isOwner: Boolean? = null,
    /**
     * The guild owner’s ID. Use `Guild.owner` instead.
     */
    @SerialName("owner_id")
    @SnowflakeId
    val ownerId: Long,
    val permissions: String? = null,
    @Deprecated("This field is deprecated and will be removed in v9 and is replaced by rtc_region")
    val region: String? = null,
    @SerialName("afk_channel_id")
    @SnowflakeId
    private val afkChannelId: Long?,
    /**
     * The timeout to get sent to the AFK channel.
     */
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
    /**
     * Returns a list of the guild’s roles in hierarchy order.
     *
     * The first element of this list will be the lowest role in the hierarchy.
     */
    val roles: List<Role>,
    /**
     * All emojis that the guild owns.
     */
    val emojis: List<Emoji>,
    val features: List<Feature>,
    /**
     * Indicates the guild’s two factor authorisation level. If this value is 0 then the guild does not require 2FA for their administrative members. If the value is 1 then they do.
     */
    @SerialName("mfa_level")
    val tfaLevel: Int,
    @SerialName("application_id")
    @SnowflakeId
    private val applicationId: Long?,
    @SerialName("system_channel_id")
    @SnowflakeId
    private val systemChannelId: Long?,
    @SerialName("system_channel_flags")
    private val _systemChannelFlags: Int,
    @SerialName("rules_channel_id")
    @SnowflakeId
    private val rulesChannelId: Long?,
    val joined_at: String? = null,
    /**
     * Indicates if the guild is a ‘large’ guild.
     *
     * A large guild is defined as having more than large_threshold count members, which for this library is set to the maximum of 250.
     */
    val large: Boolean = false,
    /**
     * Indicates if the guild is unavailable. If this is `true` then the reliability of other attributes outside of `Guild.id` is slim and they might all be `null`. It is best to not do anything with the guild if it is unavailable.
     */
    val unavailable: Boolean = false,
    /**
     * Returns the true member count regardless of it being loaded fully or not.
     *
     * Warning:
     * Due to a Discord limitation, in order for this attribute to remain up-to-date and accurate, it requires `Intents.members` to be specified.
     */
    @SerialName("member_count")
    val memberCount: Int = 0,
    val voice_states: List<VoiceState>? = null,
    /**
     * A list of members that belong to this guild.
     */
    val members: List<Member>? = null,
    val channels: List<Channel>? = null,
    val threads: List<Channel>? = null,
    val presences: List<PresenceUpdate>? = null,
    /**
     * The maximum amount of presences for the guild.
     */
    @SerialName("max_presences")
    val maxPresences: Int? = null,
    /**
     * The maximum amount of members for the guild. Note: This attribute is only available via `Client.fetch_guild()`.
     */
    @SerialName("max_members")
    val maxMembers: Int,
    @SerialName("vanity_url_code")
    val vanityUrlCode: String?,
    /**
     * The guild’s description.
     */
    val description: String?,
    /**
     * The guild’s banner.
     */
    val banner: String?,
    /**
     * The premium tier for this guild. Corresponds to “Nitro Server” in the official UI. The number goes from 0 to 3 inclusive.
     */
    @SerialName("premium_tier")
    val premiumTier: Int,
    /**
     * The number of “boosts” this guild currently has.
     */
    @SerialName("premium_subscription_count")
    val premiumSubscriptionCount: Int? = null,
    /**
     * The preferred locale for the guild. Used when filtering Server Discovery results to a specific language.
     */
    @SerialName("preferred_locale")
    val preferredLocale: String?, //TODO: really nullable?
    @SerialName("public_updates_channel_id")
    @SnowflakeId
    private val publicUpdatesChannelId: Long?,
    /**
     * The maximum amount of users in a video channel.
     */
    @SerialName("max_video_channel_users")
    val maxVideoChannelUsers: Int = 0,
    val approximate_member_count: Int = 0,
    val approximate_presence_count: Int = 0,
    val welcome_screen: WelcomeScreen? = null,
    val nsfw_level: Int,
    val stage_instances: List<StageInstance>? = null,
    val stickers: List<Sticker>
) : Snowflake() {
    @Transient
    internal lateinit var client: Client

    /**
     * The channel that denotes the AFK channel. `null` if it doesn’t exist.
     */
    val afkChannel by lazy { if (afkChannelId != null) channels?.find { it.id == afkChannelId } else null }

    /**
     * The guild’s verification level.
     */
    @Transient
    val verificationLevel = VerificationLevel.values()[_verificationLevel]

    /**
     * The guild’s explicit content filter.
     */
    @Transient
    val explicitContentFilter = ContentFilter.values()[_explicitContentFilter]

    /**
     * Retrieves a sequence that enables receiving the guild’s members. In order to use this, `Intents.members()` must be enabled.
     * @param limit The number of members to retrieve. Defaults to 1000. Pass `-1` to fetch all members. Note that this is potentially slow.
     * @param after Retrieve members after this date.
     * @return The members with the member data parsed.
     * @throws ClientException The members intent is not enabled.
     * @throws HTTPException Getting the members failed.
     */
    @Throws(ClientException::class, HTTPException::class)
    suspend fun fetchMembers(limit: Int = 1000, after: DateTime? = null): Sequence<Member> {
        TODO()
    }

    /**
     * Returns a sequence that enables receiving the guild’s audit logs.
     *
     * You must have the `view_audit_log` permission to use this.
     * @param limit The number of entries to retrieve. If `-1` retrieve all entries.
     * @param before Retrieve entries before this date.
     * @param after Retrieve entries after this date.
     * @param oldestFirst If set to `true`, return entries in oldest->newest order. Defaults to `true` if `after` is specified, otherwise `false`
     * @param user The moderator to filter entries from.
     * @param action The action to filter with.
     * @return The audit log entries
     * @throws Forbidden You are not allowed to fetch audit logs
     * @throws HTTPException An error occurred while fetching the audit logs.
     */
    @Throws(Forbidden::class, HTTPException::class)
    suspend fun auditLogs(limit: Int = 100, before: DateTime? = null, after: DateTime? = null, oldestFirst: Boolean = after != null, user: Snowflake, action: AuditLogAction): Sequence<AuditLogEntry> {
        TODO()
    }

    /**
     * A list of voice channels that belongs to this guild.
     *
     * This is sorted by the position and are in UI order from top to bottom.
     */
    val voiceChannels by lazy { channels?.filter { it.isVoice }?.sortedBy { it.position } }

    /**
     * A list of stage channels that belongs to this guild.
     *
     * This is sorted by the position and are in UI order from top to bottom.
     */
    val stageChannels by lazy { channels?.filter { it.isVoice }?.sortedBy { it.position } }

    /**
     * Similar to `Client.user` except an instance of `Member`. This is essentially used to get the member version of yourself.
     */
    val me by lazy { applicationId?.let { getMember(it) } }

    /**
     * Returns the VoiceProtocol associated with this guild, if any.
     */
    @Transient
    val voiceClient = null //TODO

    /**
     * A list of text channels that belongs to this guild.
     * This is sorted by the position and are in UI order from top to bottom.
     */
    val textChannels by lazy { channels?.filter { it.isText }?.sortedBy { it.position } }

    /**
     * A list of categories that belongs to this guild.
     * This is sorted by the position and are in UI order from top to bottom.
     */
    val categories by lazy { channels?.filter { it.isCategory }?.sortedBy { it.position } }

    /**
     * Returns every `CategoryChannel` and their associated channels.
     *
     * These channels and categories are sorted in the official Discord UI order.
     *
     * If the channels do not have a category, then the first element of the pair is `null`.
     * @return The categories and their associated channels.
     */
    fun byCategory(): List<Pair<CategoryChannel?, List<GuildChannel>>> {
        TODO()
    }

    /**
     * Returns a channel with the given ID.
     * @param channelId The ID to search for.
     * @return The returned channel or `null` if not found.
     */
    fun getChannel(channelId: Long) = channels?.find { it.id == channelId }

    /**
     * Returns the guild’s channel used for system messages.
     * If no channel is set, then this returns `null`.
     */
    val systemChannel by lazy { if (systemChannelId != null) channels?.find { it.id == systemChannelId } else null }

    /**
     * Returns the guild’s system channel settings.
     */
    @Transient
    val systemChannelFlags = SystemChannelFlags(_systemChannelFlags)

    /**
     * Return’s the guild’s channel used for the rules. The guild must be a Community guild.
     *
     * If no channel is set, then this returns `null`.
     */
    val rulesChannel by lazy { if (rulesChannelId != null) channels?.find { it.id == rulesChannelId } else null }

    /**
     * Return’s the guild’s channel where admins and moderators of the guilds receive notices from Discord. The guild must be a Community guild.
     *
     * If no channel is set, then this returns `null`.
     */
    val publicUpdatesChannel  by lazy { if (publicUpdatesChannelId != null) channels?.find { it.id == publicUpdatesChannelId } else null }

    /**
     * The maximum number of emoji slots this guild has.
     */
    //@Transient
    val emojiLimit: Int get() = TODO()

    /**
     * The maximum bitrate for voice channels this guild can have.
     */
    //@Transient
    val bitrateLimit: Float get() = TODO()

    /**
     * The maximum number of bytes files can have when uploaded to this guild.
     */
    //@Transient
    val fileSizeLimit: Int get() = TODO()

    /**
     * Returns a member with the given ID.
     * @param userId The ID to search for.
     * @return The member or `null` if not found.
     */
    fun getMember(userId: Long) = members?.find { it.user.id == userId }

    /**
     * A list of members who have “boosted” this guild.
     */
    val premiumSubscribers by lazy { members?.filter { it.premiumSince != null } }

    /**
     * Returns a role with the given ID.
     * @param roleId The ID to search for.
     * @return The role or `null` if not found.
     */
    fun getRole(roleId: Long) = roles.find { it.id == roleId }

    /**
     * Gets the @everyone role that all members have by default.
     */
    val defaultRole by lazy { roles.find { it.name == "@everyone" } }

    /**
     * Gets the premium subscriber role, AKA “boost” role, in this guild.
     */
    val premiumSubscriberRole: Role by lazy<Role> { TODO() }

    /**
     * Gets the role associated with this client’s user, if any.
     */
    val selfRole: Role by lazy<Role> { TODO() }

    /**
     * The member that owns the guild.
     */
    val owner by lazy { getMember(ownerId) }

    /**
     * Returns the guild’s icon asset.
     */
    val iconAsset: Asset by lazy<Asset> { TODO() }

    /**
     * Returns `true` if the guild has an animated icon.
     */
    val isIconAnimated get() = Feature.AnimatedIcon in features

    /**
     * Returns an `Asset` for the guild’s icon.
     *
     * The format must be one of ‘webp’, ‘jpeg’, ‘jpg’, ‘png’ or ‘gif’, and ‘gif’ is only valid for animated avatars. The size must be a power of 2 between 16 and 4096.
     * @param format The format to attempt to convert the icon to. If the format is `null`, then it is automatically detected into either ‘gif’ or static_format depending on the icon being animated or not.
     * @param staticFormat Format to attempt to convert only non-animated icons to.
     * @param size The size of the image to display.
     * @return The resulting CDN asset.
     * @throws IllegalArgumentException Bad image format passed to `format` or invalid `size`.
     */
    @Throws(IllegalArgumentException::class)
    fun iconAssetAs(format: String? = null, staticFormat: String = "webp", size: Int = 1024): Asset {
        val useFormat = format ?: if (isIconAnimated) "gif" else staticFormat
        TODO()
    }

    /**
     * Returns the guild’s banner asset.
     */
    val bannerAsset: Asset by lazy<Asset> { TODO() }

    /**
     * Returns an `Asset` for the guild’s banner.
     *
     * The format must be one of ‘webp’, ‘jpeg’, or ‘png’. The size must be a power of 2 between 16 and 4096.
     * @param format The format to attempt to convert the banner to.
     * @param size The size of the image to display.
     * @return The resulting CDN asset.
     * @throws IllegalArgumentException Bad image format passed to `format` or invalid `size`.
     */
    @Throws(IllegalArgumentException::class)
    fun bannerAssetAs(format: String = "webp", size: Int = 2048): Asset {
        TODO()
    }

    /**
     * Returns the guild’s invite splash asset.
     */
    val splashAsset: Asset by lazy<Asset> { TODO() }

    /**
     * Returns an `Asset` for the guild’s invite splash.
     *
     * The format must be one of ‘webp’, ‘jpeg’, or ‘png’. The size must be a power of 2 between 16 and 4096.
     * @param format The format to attempt to convert the splash to.
     * @param size The size of the image to display.
     * @return The resulting CDN asset.
     * @throws IllegalArgumentException Bad image format passed to `format` or invalid `size`.
     */
    @Throws(IllegalArgumentException::class)
    fun splashAssetAs(format: String = "webp", size: Int = 2048): Asset {
        TODO()
    }

    /**
     * Returns the guild’s discovery splash asset.
     */
    val discoverySplashAsset: Asset by lazy<Asset> { TODO() }

    /**
     * Returns an `Asset` for the guild’s discovery splash.
     *
     * The format must be one of ‘webp’, ‘jpeg’, or ‘png’. The size must be a power of 2 between 16 and 4096.
     * @param format The format to attempt to convert the splash to.
     * @param size The size of the image to display.
     * @return The resulting CDN asset.
     * @throws IllegalArgumentException Bad image format passed to `format` or invalid `size`.
     */
    @Throws(IllegalArgumentException::class)
    fun discoverySplashAssetAs(format: String = "webp", size: Int = 2048): Asset {
        TODO()
    }

    /**
     * Returns a boolean indicating if the guild is “chunked”.
     *
     * A chunked guild means that `member_count` is equal to the number of members stored in the internal `members` cache.
     *
     * If this value returns `false`, then you should request for offline members.
     */
    val chunked: Boolean get() = TODO()

    /**
     * Returns the shard ID for this guild if applicable.
     */
    @Transient
    val shardId: Int? = null

    /**
     * Returns the first member found that matches the name provided.
     *
     * The name can have an optional discriminator argument, e.g. “Jake#0001” or “Jake” will both do the lookup. However the former will give a more precise result. Note that the discriminator must have all 4 digits for this to work.
     *
     * If a nickname is passed, then it is looked up via the nickname. Note however, that a nickname + discriminator combo will not lookup the nickname but rather the username + discriminator combo due to nickname + discriminator not being unique.
     *
     * If no member is found, `null` is returned.
     * @param name The name of the member to lookup with an optional discriminator.
     * @return The member in this guild with the associated name. If not found then `null` is returned.
     */
    fun getMemberNamed(name: String): Member? {
        if (members == null) return null
        val split = name.split('#')
        val discriminator = split.takeIf { it.size >= 2 }?.last()
        val searchName = if (discriminator != null) split.subList(0, split.size - 1).joinToString("#") else name
        var foundName: Member? = null
        var foundNickname: Member? = null
        for (m in members) {
            if (m.user.discriminator == discriminator && m.user.username == searchName) return m
            if (discriminator == null && m.user.username == name) foundName = m
            if (discriminator == null && m.nick == name) foundNickname = m
        }
        return foundName ?: foundNickname
    }

    /**
     * Creates a `TextChannel` for the guild.
     *
     * Note that you need the `manage_channels` permission to create the channel.
     *
     * The `overwrites` parameter can be used to create a ‘secret’ channel upon creation. This parameter expects a map of overwrites with the target (either a `Member` or a `Role`) as the key and a `PermissionOverwrite` as the value.
     *
     * Note:
     * Creating a channel of a specified position will not update the position of other channels to follow suit. A follow-up call to `edit()` will be required to update the position of the channel in the channel list.
     * @param name The channel’s name.
     * @param overwrites A map of target (either a role or a member) to `PermissionOverwrite` to apply upon creation of a channel. Useful for creating secret channels.
     * @param category The category to place the newly created channel under. The permissions will be automatically synced to category if no overwrites are provided.
     * @param position The position in the channel list. This is a number that starts at 0. e.g. the top channel is position 0.
     * @param topic The new channel’s topic.
     * @param slowModeDelay Specifies the slowmode rate limit for user in this channel, in seconds. The maximum value possible is *21600*.
     * @param nsfw To mark the channel as NSFW or not.
     * @param reason The reason for creating this channel. Shows up on the audit log.
     * @return The channel that was just created.
     * @throws Forbidden You do not have the proper permissions to create this channel.
     * @throws HTTPException Creating the channel failed.
     * @throws IllegalArgumentException The permission overwrite information is not in proper form.
     */
    @Throws(Forbidden::class, HTTPException::class, IllegalArgumentException::class)
    suspend fun createTextChannel(
        name: String,
        overwrites: Map<PermissionOverwrite.Target, PermissionOverwrite> = emptyMap(),
        category: CategoryChannel? = null,
        position: Int = 0,
        topic: String? = null,
        slowModeDelay: Int = -1,
        nsfw: Boolean = false,
        reason: String? = null
    ): TextChannel {
        TODO()
    }

    /**
     * Creates a `VoiceChannel` for the guild.
     *
     * Note that you need the `manage_channels` permission to create the channel.
     *
     * The `overwrites` parameter can be used to create a ‘secret’ channel upon creation. This parameter expects a map of overwrites with the target (either a `Member` or a `Role`) as the key and a `PermissionOverwrite` as the value.
     *
     * Note:
     * Creating a channel of a specified position will not update the position of other channels to follow suit. A follow-up call to `edit()` will be required to update the position of the channel in the channel list.
     * @param name The channel’s name.
     * @param overwrites A map of target (either a role or a member) to `PermissionOverwrite` to apply upon creation of a channel. Useful for creating secret channels.
     * @param category The category to place the newly created channel under. The permissions will be automatically synced to category if no overwrites are provided.
     * @param position The position in the channel list. This is a number that starts at 0. e.g. the top channel is position 0.
     * @param topic The new channel’s topic.
     * @param slowModeDelay Specifies the slowmode rate limit for user in this channel, in seconds. The maximum value possible is *21600*.
     * @param nsfw To mark the channel as NSFW or not.
     * @param bitrate The channel’s preferred audio bitrate in bits per second.
     * @param userLimit The channel’s limit for number of members that can be in a voice channel.
     * @param rtcRegion The region for the voice channel’s voice communication. A value of `null` indicates automatic voice region detection.
     * @param reason The reason for creating this channel. Shows up on the audit log.
     * @return The channel that was just created.
     * @throws Forbidden You do not have the proper permissions to create this channel.
     * @throws HTTPException Creating the channel failed.
     * @throws IllegalArgumentException The permission overwrite information is not in proper form.
     */
    @Throws(Forbidden::class, HTTPException::class, IllegalArgumentException::class)
    suspend fun createVoiceChannel(
        name: String,
        overwrites: Map<PermissionOverwrite.Target, PermissionOverwrite> = emptyMap(),
        category: CategoryChannel? = null,
        position: Int = 0,
        topic: String? = null,
        slowModeDelay: Int = -1,
        nsfw: Boolean = false,
        bitrate: Int = 32,
        userLimit: Int = -1,
        rtcRegion: VoiceRegion? = null,
        reason: String? = null
    ): VoiceChannel {
        TODO()
    }

    /**
     * Creates a `StageChannel` for the guild.
     *
     * Note that you need the `manage_channels` permission to create the channel.
     *
     * The `overwrites` parameter can be used to create a ‘secret’ channel upon creation. This parameter expects a map of overwrites with the target (either a `Member` or a `Role`) as the key and a `PermissionOverwrite` as the value.
     *
     * Note:
     * Creating a channel of a specified position will not update the position of other channels to follow suit. A follow-up call to `edit()` will be required to update the position of the channel in the channel list.
     * @param name The channel’s name.
     * @param overwrites A map of target (either a role or a member) to `PermissionOverwrite` to apply upon creation of a channel. Useful for creating secret channels.
     * @param category The category to place the newly created channel under. The permissions will be automatically synced to category if no overwrites are provided.
     * @param position The position in the channel list. This is a number that starts at 0. e.g. the top channel is position 0.
     * @param topic The new channel’s topic.
     * @param reason The reason for creating this channel. Shows up on the audit log.
     * @return The channel that was just created.
     * @throws Forbidden You do not have the proper permissions to create this channel.
     * @throws HTTPException Creating the channel failed.
     * @throws IllegalArgumentException The permission overwrite information is not in proper form.
     */
    @Throws(Forbidden::class, HTTPException::class, IllegalArgumentException::class)
    suspend fun createStageChannel(
        name: String,
        overwrites: Map<PermissionOverwrite.Target, PermissionOverwrite> = emptyMap(),
        category: CategoryChannel? = null,
        position: Int = 0,
        topic: String? = null,
        reason: String? = null
    ): StageChannel {
        TODO()
    }

    /**
     * Creates a `CategoryChannel` for the guild.
     *
     * Note that you need the `manage_channels` permission to create the channel.
     *
     * The `overwrites` parameter can be used to create a ‘secret’ channel upon creation. This parameter expects a map of overwrites with the target (either a `Member` or a `Role`) as the key and a `PermissionOverwrite` as the value.
     *
     * Note:
     * Creating a channel of a specified position will not update the position of other channels to follow suit. A follow-up call to `edit()` will be required to update the position of the channel in the channel list.
     * @param name The channel’s name.
     * @param overwrites A map of target (either a role or a member) to `PermissionOverwrite` to apply upon creation of a channel. Useful for creating secret channels.
     * @param position The position in the channel list. This is a number that starts at 0. e.g. the top channel is position 0.
     * @param topic The new channel’s topic.
     * @param slowModeDelay Specifies the slowmode rate limit for user in this channel, in seconds. The maximum value possible is *21600*.
     * @param nsfw To mark the channel as NSFW or not.
     * @param reason The reason for creating this channel. Shows up on the audit log.
     * @return The channel that was just created.
     * @throws Forbidden You do not have the proper permissions to create this channel.
     * @throws HTTPException Creating the channel failed.
     * @throws IllegalArgumentException The permission overwrite information is not in proper form.
     */
    @Throws(Forbidden::class, HTTPException::class, IllegalArgumentException::class)
    suspend fun createCategoryChannel(
        name: String,
        overwrites: Map<PermissionOverwrite.Target, PermissionOverwrite> = emptyMap(),
        position: Int = 0,
        topic: String? = null,
        slowModeDelay: Int = -1,
        nsfw: Boolean = false,
        reason: String? = null
    ): CategoryChannel {
        TODO()
    }

    /**
     * Leaves the guild.
     *
     * Note:
     * You cannot leave the guild that you own, you must delete it instead via `delete()`.
     * @throws HTTPException Leaving the guild failed.
     */
    @Throws(HTTPException::class)
    suspend fun leave() {
        TODO()
    }

    /**
     * Deletes the guild. You must be the guild owner to delete the guild.
     * @throws HTTPException Deleting the guild failed.
     * @throws Forbidden You do not have permissions to delete the guild.
     */
    @Throws(HTTPException::class, Forbidden::class)
    suspend fun delete() {
        TODO()
    }

    /**
     * Edits the guild.
     *
     * You must have the manage_guild permission to edit the guild.
     * @param name The new name of the guild.
     * @param description The new description of the guild. This is only available to guilds that contain `PUBLIC` in `Guild.features`.
     * @param icon A byte-array representing the icon. Only PNG/JPEG is supported. GIF is only available to guilds that contain `ANIMATED_ICON` in `Guild.features` . Could be `null` to denote removal of the icon.
     * @param banner A byte-array representing the banner. Could be `null` to denote removal of the banner.
     * @param splash A byte-array representing the invite splash. Could be `null` to denote removal of the banner. This is only available to guilds that contain `INVITE_SPLASH` in `Guild.features`.
     * @param region The new region for the guild’s voice communication.
     * @param afkChannel The new channel that is the AFK channel. Could be `null` for no AFK channel.
     * @param afkTimeout The number of seconds until someone is moved to the AFK channel.
     * @param owner The new owner of the guild to transfer ownership to. Note that you must be owner of the guild to do this.
     * @param verificationLevel The new verification level for the guild.
     * @param defaultMessageNotifications The new default notification level for the guild.
     * @param explicitContentFilter The new explicit content filter for the guild.
     * @param vanityCode The new vanity code for the guild.
     * @param systemChannel The new channel that is used for the system channel. Could be `null` for no system channel.
     * @param systemChannelFlags The new system channel settings to use with the new system channel.
     * @param preferredLocale The new preferred locale for the guild. Used as the primary language in the guild.
     * @param rulesChannel The new channel that is used for rules. This is only available to guilds that contain `PUBLIC` in `Guild.features`. Could be `null` for no rules channel.
     * @param publicUpdatesChannel The new channel that is used for public updates from Discord. This is only available to guilds that contain `PUBLIC` in `Guild.features`. Could be `null` for no public updates channel.
     * @param reason The reason for editing this guild. Shows up on the audit log.
     * @throws Forbidden You do not have permissions to edit the guild.
     * @throws HTTPException Editing the guild failed.
     * @throws IllegalArgumentException The image format passed in to `icon` is invalid. It must be PNG or JPG. This is also raised if you are not the owner of the guild and request an ownership transfer.
     */
    @Throws(Forbidden::class, HTTPException::class, IllegalArgumentException::class)
    suspend fun edit(
        name: String? = null,
        description: String? = null,
        icon: Optional<ByteArray?> = none,
        banner: Optional<ByteArray?> = none,
        splash: Optional<ByteArray?> = none,
        region: VoiceRegion? = null,
        afkChannel: Optional<VoiceChannel?> = none,
        afkTimeout: Int? = null,
        owner: Member? = null,
        verificationLevel: VerificationLevel? = null,
        defaultMessageNotifications: Int? = null,
        explicitContentFilter: ContentFilter? = null,
        vanityCode: String? = null,
        systemChannel: Optional<TextChannel> = none,
        systemChannelFlags: SystemChannelFlags? = null,
        preferredLocale: Locale? = null,
        rulesChannel: Optional<TextChannel>? = none,
        publicUpdatesChannel: Optional<TextChannel>? = none,
        reason: String? = null
    ) {
        TODO()
    }

    /**
     * Retrieves all `abc.GuildChannel` that the guild has.
     *
     * Note:
     * This method is an API call. For general usage, consider `channels` instead.
     * @return All channels in the guild.
     * @see channels
     */
    @Throws(InvalidData::class, HTTPException::class)
    suspend fun fetchChannels(): List<GuildChannel> {
        TODO()
    }

    /**
     * Retrieves a Member from a guild ID, and a member ID.
     *
     * Note:
     * This method is an API call. If you have `Intents.members` and member cache enabled, consider `get_member()` instead.
     * @param memberId The member’s ID to fetch from.
     * @return The member from the member ID.
     * @throws Forbidden You do not have access to the guild.
     * @throws HTTPException You do not have access to the guild.
     * @see getMember
     */
    @Throws(Forbidden::class, HTTPException::class)
    suspend fun fetchMember(memberId: Long): Member {
        TODO()
    }

    /**
     * Retrieves the `BanEntry` for a user.
     *
     * You must have the `ban_members` permission to get this information.
     * @param user The user to get ban information from.
     * @return The `BanEntry` object for the specified user.
     * @throws Forbidden You do not have proper permissions to get the information.
     * @throws NoSuchElementException This user is not banned.
     * @throws HTTPException An error occurred while fetching the information.
     */
    @Throws(Forbidden::class, NoSuchElementException::class, HTTPException::class)
    suspend fun fetchBan(user: Snowflake): BanEntry {
        TODO()
    }

    /**
     * Retrieves all the users that are banned from the guild as a list of `BanEntry`.
     *
     * You must have the `ban_members` permission to get this information.
     * @return A list of `BanEntry` objects.
     * @throws Forbidden You do not have proper permissions to get the information.
     * @throws HTTPException An error occurred while fetching the information.
     */
    @Throws(Forbidden::class, HTTPException::class)
    suspend fun bans(): List<BanEntry> {
        TODO()
    }

    /**
     * Prunes the guild from its inactive members.
     *
     * The inactive members are denoted if they have not logged on in `days` number of days and they have no roles.
     *
     * You must have the `kick_members` permission to use this.
     *
     * To check how many members you would prune without actually pruning, see the `estimatePrunedMembers()` function.
     *
     * To prune members that have specific roles see the `roles` parameter.
     * @param days The number of days before counting as inactive.
     * @param reason The reason for doing this action. Shows up on the audit log.
     * @param computePruneCount Whether to compute the prune count. This defaults to `true` which makes it prone to timeouts in very large guilds. In order to prevent timeouts, you must set this to `false`. If this is set to `false`, then this function will always return *0*.
     * @param roles A list of `abc.Snowflake` that represent roles to include in the pruning process. If a member has a role that is not specified, they’ll be excluded.
     * @return The number of members pruned. If `computePruneCount` is `false` then this returns *0*.
     * @throws Forbidden You do not have permissions to prune members.
     * @throws HTTPException An error occurred while pruning members.
     */
    @Throws(Forbidden::class, HTTPException::class)
    suspend fun pruneMembers(days: Int, reason: String?, computePruneCount: Boolean = true, roles: List<Snowflake>? = null): Int {
       TODO()
    }

    /**
     * Gets the list of templates from this guild.
     *
     * Requires `manage_guild` permissions.
     * @return The templates for this guild.
     * @throws Forbidden You don’t have permissions to get the templates.
     */
    @Throws(Forbidden::class)
    suspend fun templates(): List<Template> {
        TODO()
    }

    /**
     * Gets the list of webhooks from this guild.
     *
     * Requires `manage_webhooks` permissions.
     * @return The webhooks for this guild.
     * @throws Forbidden You don’t have permissions to get the webhooks.
     */
    @Throws(Forbidden::class)
    suspend fun webhooks(): List<Webhook> {
        TODO()
    }

    /**
     * Similar to `pruneMembers()` except instead of actually pruning members, it returns how many members it would prune from the guild had it been called.
     * @param days The number of days before counting as inactive.
     * @param roles A list of `abc.Snowflake` that represent roles to include in the estimate. If a member has a role that is not specified, they’ll be excluded.
     * @return The number of members estimated to be pruned.
     * @throws Forbidden You do not have permissions to prune members.
     * @throws HTTPException An error occurred while fetching the prune members estimate.
     */
    @Throws(Forbidden::class, HTTPException::class)
    suspend fun estimatePruneMembers(days: Int, roles: List<Snowflake>?): Int {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun invites(): List<Invite> {
        TODO()
    }

    suspend fun createTemplate(name: String, description: String?) {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun createIntegration(type: String, id: Long) {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun integrations(): List<Integration> {
        TODO()
    }

    @Throws(HTTPException::class)
    suspend fun fetchEmojis(): List<Emoji> {
        TODO()
    }

    @Throws(NoSuchElementException::class, HTTPException::class)
    suspend fun fetchEmoji(emojiId: Long): Emoji {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun createCustomEmoji(name: String, image: ByteArray, roles: List<Role>?, reason: String?): Emoji {
        TODO()
    }

    @Throws(HTTPException::class)
    suspend fun fetchRoles(): List<Role> {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun createRole(name: String, permissions: Permissions, color: Color, hoist: Boolean, mentionable: Boolean, reason: String?): Role {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun editRolePositions(positions: Map<Role, Int>, reason: String?): List<Role> {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun kick(user: Snowflake, reason: String?) {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun ban(user: Snowflake, deleteMessageDays: Int, reason: String?) {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun unban(user: Snowflake, reason: String?) {
        TODO()
    }

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun vanityInvite(): Invite {
        TODO()
    }

    /*@Deprecated("This API should not be used as a non-bot user, as it is against the Discord TOS.")
    @Throws(HTTPException::class, ClientException::class)
    suspend fun ack() {
        TODO()
    }*/

    @Throws(Forbidden::class, HTTPException::class)
    suspend fun widget(): Widget {
        TODO()
    }

    @Throws(ClientException::class)
    suspend fun chunk(cache: Boolean = true) {
        TODO()
    }

    @Throws(ConnectTimeoutException::class, IllegalArgumentException::class, ClientException::class)
    suspend fun queryMembers(query: String? = null, limit: Int = 5, presences: Boolean = false, cache: Boolean = true, userIds: LongArray? = null): List<Member> {
        TODO()
    }

    suspend fun changeVoiceState(channel: VoiceChannel?, selfMute: Boolean = false, selfDeaf: Boolean = false) {
        TODO()
    }

    override fun toString() = name

    internal suspend fun registerSlashCommand(command: ApplicationCommand, scope: CoroutineScope) = scope.async {
        //println("Guild $name is registering command ${command.name}")
        client.httpClient.post<HttpResponse>("https://discord.com/api/v8/applications/${client.applicationId}/guilds/$id/commands") {
            this.header("Content-Type", "application/json")
            header("Authorization", client.authValue)
            this.body = command.json
        }.also { commands[json.parseToJsonElement(it.receive()).jsonObject["id"]!!.jsonPrimitive.content.toLong()] = command }
    }

    @Transient
    internal val commands = hashMapOf<Long, ApplicationCommand>()

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
        @SerialName("THREADS_ENABLED")
        ThreadsEnabled,
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

    @JvmInline
    value class SystemChannelFlags(val value: Int) {
        inline val suppressJoinNotifications get() = value and 0b1 != 0
        inline val suppressPremiumSubscriptions get() = value and 0b10 != 0
        inline val suppressGuildReminderNotifications get() = value and 0b100 != 0
    }
}

@Serializable
class VoiceState

@Serializable
class Channel(
    @SnowflakeId
    val id: Long
) {
    @Transient val isVoice: Boolean = false
    @Transient val isStage: Boolean = false
    @Transient val isText: Boolean = false
    @Transient val isCategory: Boolean = false
    val position: Int = 0
}

@Serializable
class PresenceUpdate
