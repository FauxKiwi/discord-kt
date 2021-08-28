package discord.util

import kotlin.js.Date
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
        if (isTraceEnabled()) log("TRACE", message)
    }

    override fun debug(message: String) {
        if (isDebugEnabled()) log("DEBUG", message)
    }

    override fun info(message: String) {
        if (isInfoEnabled()) log("INFO", message)
    }

    override fun warn(message: String) {
        if (isWarnEnabled()) log("WARN", message)
    }

    override fun error(message: String) {
        if (isErrorEnabled()) log("ERROR", message)
    }

    private fun log(level: String, message: String) = println("${DateTime.now().format("HH:mm:ss.SSS")} - $level $name - $message")
}

private const val trace = false
private const val debug = true
private const val info = true
private const val warn = true
private const val error = true