package discord.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass

//actual inline fun <reified A : Annotation> KCallable<*>.findAnnotation(): A?

actual interface Logger {
    actual fun isTraceEnabled(): Boolean
    actual fun isDebugEnabled(): Boolean
    actual fun isInfoEnabled(): Boolean
    actual fun isWarnEnabled(): Boolean
    actual fun isErrorEnabled(): Boolean
    actual fun trace(message: String)
    actual fun debug(message: String)
    actual fun info(message: String)
    actual fun warn(message: String)
    actual fun error(message: String)
}

@PublishedApi
internal fun <T : Any> loggerFor(clazz: KClass<T>): Logger = LoggerImpl(clazz.simpleName ?: "null")

private class LoggerImpl(val name: String) : Logger {
    override fun isTraceEnabled() = trace

    override fun isDebugEnabled() = debug

    override fun isInfoEnabled() = info

    override fun isWarnEnabled() = warn

    override fun isErrorEnabled() = error

    override fun trace(message: String) {
        if (isTraceEnabled()) log("30;1", "TRACE", message)
    }

    override fun debug(message: String) {
        if (isDebugEnabled()) log("36", "DEBUG", message)
    }

    override fun info(message: String) {
        if (isInfoEnabled()) log("32", "INFO", message)
    }

    override fun warn(message: String) {
        if (isWarnEnabled()) log("33", "WARN", message)
    }

    override fun error(message: String) {
        if (isErrorEnabled()) log("31", "ERROR", message)
    }

    private fun log(color: String, level: String, message: String) =
        println("\u001b[${color}m${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())} - $level $name - $message\u001B[0m")
}

private const val trace = false
private const val debug = true
private const val info = true
private const val warn = true
private const val error = true