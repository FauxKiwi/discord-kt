package discord.abc

import discord.util.DateTime

abstract class Snowflake {
    abstract val id: Long

    fun createdAt(): DateTime = DateTime(id)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Snowflake) return false
        return other.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}

fun Snowflake(id: Long): Snowflake = SnowflakeId(id)

private class SnowflakeId(override val id: Long) : Snowflake()