package discord.events

import discord.Message

class MessageCreate(val message: Message) : EventContext() {
}