package discord.util

import discord.abc.Snowflake

@JvmInline
value class DateTime(private val long: Long) : Snowflake {
    override val id: Long get() = long

    override fun createdAt() = this
}