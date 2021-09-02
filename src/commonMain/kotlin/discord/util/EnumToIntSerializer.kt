package discord.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal open class EnumToIntSerializer<E : Enum<E>>(private val values: Array<E>, descriptor: String) : KSerializer<E> {
    override val descriptor = PrimitiveSerialDescriptor(descriptor, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: E) = encoder.encodeInt(value.ordinal)

    override fun deserialize(decoder: Decoder): E = values[decoder.decodeInt()]
}