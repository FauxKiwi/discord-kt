package discord.events

import discord.Guild

class JoinGuildEvent(val guild: Guild) : EventContext()