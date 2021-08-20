package base

@JvmInline
value class Color(val rgba: Int) {
    val red get() = rgba and 0xff
    val green get() = (rgba ushr 8) and 0xff
    val blue get() = (rgba ushr 16) and 0xff
    val alpha get() = (rgba ushr 24) and 0xff

    constructor(red: Int, green: Int, blue: Int, alpha: Int) :
            this(red or (green shl 8) or (blue shl 16) or (alpha shl 24))
}
