package discord

import discord.events.MessageCreateEvent
import discord.events.ReadyEvent
import discord.interactions.commands.ApplicationCommand
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class BotTest {
    /*@ExperimentalTime
    @Test
    fun testBot() {
        GlobalScope.launch {
            val client = Client(coroutineScope = this)
            client.event<ReadyEvent> {
                println("Ready")
            }
            client.event<MessageCreateEvent> { event ->
                println(event.message)
            }
            client.interaction(ApplicationCommand("ping", "Ping!", guildIds = longArrayOf(581185346465824768)) { ctx ->
                ctx.send("Pong! (${client.latency.inWholeMilliseconds}ms)")
            })
        }
        @Suppress("ControlFlowWithEmptyBody")
        while (true);
    }*/
}