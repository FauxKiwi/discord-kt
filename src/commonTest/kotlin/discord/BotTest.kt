package discord

import discord.events.MessageCreateEvent
import discord.interactions.commands.ApplicationCommand
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class BotTest {
    /*@Test
    fun testBot() {
        GlobalScope.launch {
            val client = Client(coroutineScope = this)
            client.event<MessageCreateEvent> { event ->
                println(event.message)
            }
            client.interaction(ApplicationCommand("ping", "Ping!", guildIds = longArrayOf(581185346465824768)) { ctx ->
                ctx.send("Pong! (${client.latency * 1000}ms)")
            })
            client.run("")
        }
        @Suppress("ControlFlowWithEmptyBody")
        while (true);
    }*/
}