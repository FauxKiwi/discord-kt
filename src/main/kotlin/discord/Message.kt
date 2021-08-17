package discord

import kotlinx.serialization.Serializable

@Serializable
class Message(
    val content: String
) {
}