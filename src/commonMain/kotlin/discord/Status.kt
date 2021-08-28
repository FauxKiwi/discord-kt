package discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    @SerialName("online")
    Online,
    @SerialName("dnd")
    DoNotDisturb,
    @SerialName("idle")
    Idle,
    @SerialName("invisible")
    Invisible,
    @SerialName("offline")
    Offline
}