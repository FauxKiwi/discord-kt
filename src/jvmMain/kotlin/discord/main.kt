@file:JvmName("MainJvm")

package discord

import discord.util.Logger
import org.slf4j.LoggerFactory

actual val OS: String = System.getProperty("os.name")

actual inline fun <reified T : Any> logger(): Logger = LoggerFactory.getLogger(T::class.java)