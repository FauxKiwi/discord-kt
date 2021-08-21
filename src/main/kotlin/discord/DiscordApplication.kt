package discord

import discord.events.EventContext
import discord.events.EventContextType
import discord.interactions.commands.Command
import discord.interactions.commands.CommandContextType
import discord.interactions.commands.CommandResponseType
import kotlin.reflect.KCallable
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

open class DiscordApplication(val client: Client) {
    internal val eventHandlers = hashMapOf<KType, MutableList<KCallable<*>>>()
    internal val globalCommands = hashMapOf<String, KCallable<*>>()
    internal val guildCommands = hashMapOf<Long, HashMap<String, KCallable<*>>>()

    init {
        client.applications.add(this)
        for (m in this::class.members) {
            if (m.hasAnnotation<Event>()) tryRegisterEvent(m)
            else if (m.hasAnnotation<Command>()) tryRegisterCommand(m)
        }
    }

    private fun tryRegisterEvent(m: KCallable<*>) {
        if (!m.isSuspend) return
        val params = m.valueParameters
        if (params.size != 1) return
        val ctxClass = params[0].type
        if (!ctxClass.isSubtypeOf(EventContextType)) return
        eventHandlers.getOrElse(ctxClass) {
            val methods = mutableListOf<KCallable<*>>()
            eventHandlers[ctxClass] = methods
            methods
        }.add(m)
    }

    private fun tryRegisterCommand(m: KCallable<*>) {
        if (!m.isSuspend) return
        if (!m.returnType.isSubtypeOf(CommandResponseType)) return
        val params = m.valueParameters
        if (params.size != 1) return
        if (!params[0].type.isSubtypeOf(CommandContextType)) return
        val command = m.findAnnotation<Command>()!!
        val name = command.name
        val guilds = command.guildIds
        if (guilds.isEmpty()) {
            TODO()
        }
        val maps = if (guilds.isEmpty()) listOf(globalCommands) else guilds.map {
            guildCommands.getOrElse(it) {
                val map = hashMapOf<String, KCallable<*>>()
                guildCommands[it] = map
                map
            }
        }
        for (map in maps) {
            if (map.containsKey(name)) {
                println("Warning: You attempted to register two commands with the same name")
                continue
            }
            map[name] = m
        }
    }
}