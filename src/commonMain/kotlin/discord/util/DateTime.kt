package discord.util

import kotlin.jvm.JvmInline

@JvmInline
expect value class DateTime(val millis: Long) {
    companion object {
        fun now(): DateTime
    }

    override fun toString(): String

    fun format(format: String): String

    fun year(): Int
    fun month(): Int
    fun day(): Int
    fun hour(): Int
    fun minute(): Int
    fun second(): Int
    fun millisecond(): Int
}