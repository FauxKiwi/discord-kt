package discord

import discord.events.EventContext
import discord.events.EventContextType
import kotlin.reflect.KCallable
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

open class DiscordApplication(val client: Client) {
    val eventHandlers = hashMapOf<KType, MutableList<KCallable<*>>>()

    init {
        client.applications.add(this)
        for (m in this::class.members) {
            if (!m.hasAnnotation<Event>()) continue
            if (!m.isSuspend) continue
            val params = m.valueParameters
            if (params.size != 1) continue
            val ctxClass = params[0].type
            if (!ctxClass.isSubtypeOf(EventContextType)) continue
            eventHandlers.getOrElse(ctxClass) {
                val methods = mutableListOf<KCallable<*>>()
                eventHandlers[ctxClass] = methods
                methods
            }.add(m)
        }
    }
}