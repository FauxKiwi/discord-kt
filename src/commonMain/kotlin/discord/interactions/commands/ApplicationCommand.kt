package discord.interactions.commands

import discord.Channel
import discord.Role
import discord.User
import discord.interactions.Interaction
import kotlinx.serialization.Serializable
//import org.intellij.lang.annotations.Pattern

//@Target(AnnotationTarget.FUNCTION)
//@Suppress("unused")
class ApplicationCommand(
    //@Pattern("^[\\w-]{1,32}\$")
    val name: String,
    //@Pattern("^.{1,100}\$")
    val description: String,
    val options: List<Option<*>> = emptyList(),
    val permissions: List<Permission> = emptyList(),
    val guildIds: LongArray = noGuildIds,
    val executor: suspend (CommandContext) -> CommandResponse
) : Interaction() {
    class Option<T>(
        val type: Type<T>,
        //@Pattern("^[\\w-]{1,32}\$")
        val name: String,
        //@Pattern("^.{1,100}\$")
        val description: String,
        val default: Boolean = false,
        val required: Boolean = true,
        val choices: List<Choice<T>> = emptyList(),
        val options: List<Option<*>> = emptyList()
    ) {
        class Type<T>(val ordinal: Int) {
            companion object {
                val SubCommand = Type<Nothing>(0) //TODO
                val SubCommandGroup = Type<Nothing>(1) //TODO
                val String = Type<String>(2)
                val Integer = Type<Int>(3)
                val Boolean = Type<Boolean>(4)
                val User = Type<User>(5)
                val Channel = Type<Channel>(6)
                val Role = Type<Role>(7)
            }
        }


        sealed class Choice<T> constructor(
            //@Pattern("^.{1,32}\$")
            val name: String,
            val value: T
        )

        class StringChoice(name: String, value: String) : Choice<String>(name, value)
        class IntChoice(name: String, value: String) : Choice<String>(name, value)
    }

    class Permission(
        val guildId: Long,
        val permissions: Array<Entry>
    ) {
        annotation class Entry(
            val id: Long,
            val type: Type,
            val allow: Boolean
        ) {
            enum class Type {
                User, Role
            }
        }
    }
}

@Serializable
internal class CommandJson(
    val name: String,
    val description: String,
    val options: List<Option>
) {
    constructor(command: ApplicationCommand) :
            this(command.name, command.description, command.options.map { option -> Option(
                option.name, option.description, option.type.ordinal + 1, option.required
            ) })

    @Serializable
    class Option(
        val name: String,
        val description: String,
        val type: Int,
        val required: Boolean,
        //TODO
    )
}

/*fun valueOfChoice(choice: Command.Option.Choice) = if (choice.stringValue != "\u0000" && choice.intValue == Int.MIN_VALUE) choice.intValue else
    if (choice.stringValue == "\u0000" && choice.intValue != Int.MIN_VALUE) false else error("No valid choice value")*/

internal val ApplicationCommand.json get() = CommandJson(this)

private val noGuildIds = LongArray(0)