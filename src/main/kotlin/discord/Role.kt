package discord

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: String,
    val name: String,
    val permissions: Int,
    val position: Int,
    val color: Int,
    val hoist: Boolean,
    val managed: Boolean,
    val mentionable: Boolean,
    val tags: Tags? = null,
    val permissions_new: String
) {
    @Serializable
    data class Tags(
        val bot_id: String
    )
}