@file:JvmName("DateTimeJvm")

package discord.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@JvmInline
actual value class DateTime(val millis: Long) {
    actual companion object {
        actual fun now(): DateTime = DateTime(System.currentTimeMillis())
    }

    private fun toJavaLocalDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), timeZoneId)

    actual override fun toString(): String = toJavaLocalDateTime().toString()

    actual fun format(format: String): String = toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(format))

    actual fun year(): Int = toJavaLocalDateTime().year

    actual fun month(): Int = toJavaLocalDateTime().month.value

    actual fun day(): Int = toJavaLocalDateTime().dayOfMonth

    actual fun hour(): Int = toJavaLocalDateTime().hour

    actual fun minute(): Int = toJavaLocalDateTime().minute

    actual fun second(): Int = (millis % (60 * 1000) / 1000).toInt()

    actual fun millisecond(): Int = (millis % 1000).toInt()
}

private val timeZoneId = TimeZone.getDefault().toZoneId()