package discord.events

import kotlin.reflect.full.starProjectedType

open class EventContext {
}

internal val EventContextType = EventContext::class.starProjectedType