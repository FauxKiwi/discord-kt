package base

sealed interface Optional<out T> {
    @JvmInline
    value class Some<T> @PublishedApi internal constructor(private val _value: Any?) : Optional<T> {
        val value
        @Suppress("unchecked_cast")
        get() = _value as T
    }

    object None : Optional<Nothing>
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> some(value: T) = Optional.Some<T>(value)

val none = Optional.None