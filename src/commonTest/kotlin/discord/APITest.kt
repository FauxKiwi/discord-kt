package discord

import discord.events.MessageCreateEvent
import kotlin.test.Test

class APITest {
    @Test
    fun testInlineFun() {
        val client = Client()
        client.event<MessageCreateEvent> { println(it.message) }
    }
}