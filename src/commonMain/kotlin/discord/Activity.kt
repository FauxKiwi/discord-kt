package discord

import discord.util.EnumToIntSerializer
import discord.util.SnowflakeId
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
//import org.intellij.lang.annotations.Pattern

@Serializable
class Activity(
    val name: String,
    val type: Type,
    val url: String? = null,
    val timestamps: Timestamps? = null,
    @SerialName("application_id")
    @SnowflakeId
    val applicationId: Long? = null,
    val details: String? = null,
    val state: String? = null,
    val emoji: Emoji? = null,
    val party: Party? = null,
    val assets: Assets? = null,
    val secrets: Secrets? = null,
    val instance: Boolean? = null,
    /*
    INSTANCE	1 << 0
    JOIN	1 << 1
    SPECTATE	1 << 2
    JOIN_REQUEST	1 << 3
    SYNC	1 << 4
    PLAY	1 << 5
     */
    val flags: UInt? = null,
    val buttons: Array<Button>? = null
) {
    internal var createdAt = Clock.System.now()

    @Serializable(with = Type.Serializer::class)
    enum class Type {
        Game,
        Streaming,
        Listening,
        Watching,
        Custom,
        Competing;

        internal class Serializer : EnumToIntSerializer<Type>(values(), "Activity.Type")
    }

    @Serializable
    class Timestamps(
        val start: Int? = null,
        val end: Int? = null,
    )

    @Serializable
    class Emoji(
        val name: String,
        @SnowflakeId
        val id: Long? = null,
        val animated: Boolean = false
    )

    @Serializable
    class Party(
        val id: String? = null,
        val size: IntArray? = null,
    )

    @Serializable
    class Assets(
        @SerialName("large_image")
        val largeImageKey: String? = null,
        @SerialName("large_text")
        val largeImageText: String? = null,
        @SerialName("small_image")
        val smallImageKey: String? = null,
        @SerialName("small_text")
        val smallImageText: String? = null,
    )

    @Serializable
    class Secrets(
        val join: String? = null,
        val spectate: String? = null,
        val match: String? = null,
    )

    @Serializable
    class Button(
        //@Pattern("^.{1,32}\$")
        val label: String,
        //@Pattern("^.{1,512}\$")
        val url: String
    )
}