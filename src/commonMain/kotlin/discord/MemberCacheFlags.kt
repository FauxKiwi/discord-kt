package discord

import kotlin.experimental.and
import kotlin.jvm.JvmInline

@JvmInline
value class MemberCacheFlags(val value: Byte) {
    val online
        get() = (value and 1) > 0
    val voice
        get() = (value and 0b10) > 0
    val joined
        get() = (value and 0b100) > 0

    companion object {
        val all = MemberCacheFlags(0b111)
        val none = MemberCacheFlags(0)

        fun fromIntents(intents: Intents) = MemberCacheFlags(0) //TODO
    }
}