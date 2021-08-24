package discord.interactions

import discord.Client
import discord.Message
import discord.User
import discord.interactions.commands.ClientCommand
import discord.interactions.commands.CommandContext
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import discord.util.SnowflakeId

@Serializable
internal class Interaction(
    @SnowflakeId
    val id: Long,
    @SerialName("application_id")
    @SnowflakeId
    val applicationId: Long,
    val type: Int,
    val data: Data? = null,
    @SerialName("guild_id")
    @SnowflakeId
    val guildId: Long = 0,
    @SerialName("channel_id")
    @SnowflakeId
    val channelId: Long = 0,
    val user: User? = null,
    val token: String,
    val version: Int = 1,
    val message: Message? = null
) {
    @Transient
    internal lateinit var client: Client

    internal fun context() = when (type) {
        Type.ApplicationCommand -> CommandContext(client, id, token, guildId, channelId,
            ClientCommand(data!!.name, data.options?.associate { it.name to ClientCommand.Argument(it.value ?: it.options!!) } ?: emptyMap())
        )
        else -> null
    }

    object Type {
        const val Ping = 1
        const val ApplicationCommand = 2
        const val MessageComponent = 3
    }

    @Serializable
    class Data(
        @SnowflakeId
        val id: Long,
        val name: String,
        val type: Int,
        val resolved: Resolved? = null,
        val options: Array<Option>? = null,
        @SerialName("custom_id")
        val customId: String? = null,
        @SerialName("component_type")
        val componentType: Int = 0,
        val values: Array<Select>? = null,
        @SerialName("target_id")
        @SnowflakeId
        val targetId: Long = 0
    ) {
        @Serializable
        class Resolved

        @Serializable
        class Option(
            val name: String,
            val type: Int,
            @SerialName("value")
            private val _value: JsonPrimitive? = null,
            val options: List<Option>? = null
        ) {
            @Transient
            val value = if (_value == null) null else when (type) {
                3 -> _value.content
                4 -> _value.int
                5 -> _value.boolean
                else -> TODO()
            }
        }

        @Serializable
        class Select
    }
}