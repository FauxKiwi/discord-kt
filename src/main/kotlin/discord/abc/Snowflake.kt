package discord.abc

import base.DateTime

interface Snowflake {
    val id: Long

    fun createdAt(): DateTime = DateTime(id)
}