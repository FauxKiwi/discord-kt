package discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Member(
    val user: User,
    @SerialName("premium_since")
    val premiumSince: String?,
    val nick: String? = null
) : PermissionOverwrite.Target {
}