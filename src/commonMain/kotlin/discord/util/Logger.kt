package discord.util

expect interface Logger {
    fun isTraceEnabled(): Boolean
    fun isDebugEnabled(): Boolean
    fun isInfoEnabled(): Boolean
    fun isWarnEnabled(): Boolean
    fun isErrorEnabled(): Boolean

    fun trace(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}

inline fun Logger.debug(lazyMessage: () -> String) {
    if (isDebugEnabled()) debug(lazyMessage())
}