package discord

import base.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Embed(
    val title: String? = null,
    val type: Type? = Type.RichText,
    val description: String? = null,
    val url: String? = null,
    val timestamp: String? = null, //TODO
    val color: Color? = null,
    val footer: Footer? = null,
    val image: Image? = null,
    val thumbnail: Thumbnail? = null,
    val video: Video? = null,
    val provider: Provider? = null,
    val author: Author? = null,
    val fields: List<Field>? = null
) {
    @Serializable
    enum class Type {
        @SerialName("rich")
        RichText
    }

    @Serializable
    class Footer //TODO

    @Serializable
    class Image //TODO

    @Serializable
    class Thumbnail //TODO

    @Serializable
    class Video //TODO

    @Serializable
    class Provider //TODO

    @Serializable
    class Author(
        val name: String? = null,
        val url: String? = null,
        @SerialName("icon_url")
        val iconUrl: String? = null,
        @SerialName("proxy_icon_url")
        val proxyIconUrl: String? = null,
    )

    @Serializable
    class Field //TODO
}
