package discord.abc

import discord.util.DateTime

interface Snowflake {
    val id: Long

    fun createdAt(): DateTime = DateTime(id)
}

fun Snowflake(id: Long): Snowflake = SnowflakeId(id)

private class SnowflakeId(override val id: Long) : Snowflake