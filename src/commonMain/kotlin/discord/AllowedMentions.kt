package discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AllowedMentions(
    val parse: Set<ParseType>,
    val roles: Set<Long> = emptySet(),
    val users: Set<Long> = emptySet(),
    @SerialName("replied_user")
    val mentionRepliedUser: Boolean = false
) {
    @Serializable
    enum class ParseType {
        @SerialName("roles")
        Roles,
        @SerialName("users")
        Users,
        @SerialName("everyone")
        Everyone
    }
}