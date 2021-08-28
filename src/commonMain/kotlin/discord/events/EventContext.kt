package discord.events

open class EventContext {
    internal fun name() = this::class.simpleName
}

//internal val EventContextType = EventContext::class.starProjectedType