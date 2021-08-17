package discord.events

import kotlin.reflect.full.starProjectedType

open class EventContext {
}

val EventContextType = EventContext::class.starProjectedType