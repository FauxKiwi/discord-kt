package discord.interactions.commands

import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Pattern

@Target(AnnotationTarget.FUNCTION)
@Suppress("unused")
annotation class Command(
    @Pattern("^[\\w-]{1,32}\$")
    val name: String,
    @Pattern("^.{1,100}\$")
    val description: String,
    val options: Array<Option> = [],
    val permissions: Array<Permission> = [],
    val guildIds: LongArray = []
) {
    @Target()
    annotation class Option(
        val type: Type,
        @Pattern("^[\\w-]{1,32}\$")
        val name: String,
        @Pattern("^.{1,100}\$")
        val description: String,
        val default: Boolean = false,
        val required: Boolean = true,
        val choices: Array<Choice> = [],
        val options: Array<Option> = []
    ) {
        enum class Type {
            SubCommand,
            SubCommandGroup,
            String,
            Integer,
            Boolean,
            User,
            Channel,
            Role
        }

        @Target()
        annotation class Choice(
            @Pattern("^.{1,32}\$")
            val name: String,
            val stringValue: String = "\u0000",
            val intValue: Int = Int.MIN_VALUE,
        )
    }

    @Target(AnnotationTarget.FUNCTION)
    annotation class Permission(
        val guildId: Long,
        val permissions: Array<Entry>
    ) {
        @Target()
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

    @Serializable
    class Json(
        val name: String,
        val description: String,
        val options: List<Option>
    ) {
        constructor(command: Command) :
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
}

fun valueOfChoice(choice: Command.Option.Choice) = if (choice.stringValue != "\u0000" && choice.intValue == Int.MIN_VALUE) choice.intValue else
    if (choice.stringValue == "\u0000" && choice.intValue != Int.MIN_VALUE) false else error("No valid choice value")

val Command.json get() = Command.Json(this)