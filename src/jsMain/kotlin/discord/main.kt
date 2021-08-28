package discord

import discord.util.Logger
import discord.util.loggerFor

actual val OS: String = eval("process.platform") as String

actual inline fun <reified T : Any> logger(): Logger = loggerFor(T::class)