package discord.interactions.commands

class ClientCommand(
    val name: String,
    val arguments: Map<String, Argument>
) {
    fun getArgument(name: String) = arguments[name]?.value

    class Argument(
        val value: Any
    )
}