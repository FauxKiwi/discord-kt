package base

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Color.Serializer::class)
@JvmInline
value class Color(val rgba: Int) {
    val red get() = rgba and 0xff
    val green get() = (rgba ushr 8) and 0xff
    val blue get() = (rgba ushr 16) and 0xff
    val alpha get() = (rgba ushr 24) and 0xff

    constructor(red: Int, green: Int, blue: Int, alpha: Int) :
            this(red or (green shl 8) or (blue shl 16) or (alpha shl 24))

    constructor(rgba: Long) : this(rgba.toInt())

    class Serializer : KSerializer<Color> {
        override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.rgba)

        override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt())
    }
}
