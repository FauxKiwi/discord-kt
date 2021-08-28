package discord

/**
 * Represents the type of action being done for a `AuditLogEntry`, which is retrievable via `Guild.audit_logs()`.
 */
enum class AuditLogAction {
    /**
     * The guild has updated. Things that trigger this include:
     * * Changing the guild vanity URL
     * * Changing the guild invite splash
     * * Changing the guild AFK channel or timeout
     * * Changing the guild voice server region
     * * Changing the guild icon
     * * Changing the guild moderation settings
     * * Changing things related to the guild widget
     *
     * When this is the action, the type of `target` is the `Guild`.
     *
     * Possible attributes for `AuditLogDiff`:
     * * `afk_channel`
     * * `system_channel`
     * * `afk_timeout`
     * * `default_message_notifications`
     * * `explicit_content_filter`
     * * `mfa_level`
     * * `name`
     * * `owner`
     * * `splash`
     * * `vanity_url_code`
     */
    GuildUpdate
}

class AuditLogEntry(
    /**
     * a
     */
    val action: AuditLogAction,

    )