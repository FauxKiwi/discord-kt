package discord.util

import kotlin.js.Date

actual value class DateTime(val millis: Long) {
    actual companion object {
        actual fun now(): DateTime = DateTime(Date.now().toLong())
    }

    private fun toJsDate() = Date(millis)

    actual override fun toString(): String = Date(millis - timeZoneOffset).toISOString().substring(0, 23)

    actual fun format(format: String) = "${hour()}:${minute()}:${second()}:${millisecond()}"

    actual fun year(): Int = toJsDate().getFullYear()

    actual fun month(): Int = toJsDate().getMonth()

    actual fun day(): Int = toJsDate().getDay()

    actual fun hour(): Int = toJsDate().getHours()

    actual fun minute(): Int = toJsDate().getMinutes()

    actual fun second(): Int = (millis % (60 * 1000) / 1000).toInt()

    actual fun millisecond(): Int = (millis % 1000).toInt()
}

private val timeZoneOffset = Date().getTimezoneOffset() * 60 * 1000