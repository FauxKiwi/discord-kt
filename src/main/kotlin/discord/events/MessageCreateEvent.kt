package discord.events

import discord.Message

class MessageCreateEvent(val message: Message) : EventContext() {
}