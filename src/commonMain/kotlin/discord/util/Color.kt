package discord.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = Color.Serializer::class)
@JvmInline
value class Color(val rgba: UInt) {
    val red get() = rgba and 0xffu
    val green get() = (rgba shr 8) and 0xffu
    val blue get() = (rgba shr 16) and 0xffu
    val alpha get() = (rgba shr 24) and 0xffu

    constructor(red: UInt, green: UInt, blue: UInt, alpha: UInt) :
            this(red or (green shl 8) or (blue shl 16) or (alpha shl 24))

    internal class Serializer : KSerializer<Color> {
        override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: Color) = encoder.encodeLong(value.rgba.toLong())

        override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeLong().toUInt())
    }
}
