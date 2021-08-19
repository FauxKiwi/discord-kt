package util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class IdSerializer : KSerializer<Long> {
    override val descriptor = PrimitiveSerialDescriptor("Id", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Long) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Long {println("h"); return decoder.decodeString().toLong()}
}

@Serializable(with = IdSerializer::class)
annotation class SnowflakeId