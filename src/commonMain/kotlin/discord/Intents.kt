package discord

class Intents {
    val members = false

    companion object {
        val all = Intents()
        val default = Intents()
        val none = Intents()
    }
}