import base.Color
import discord.Client
import discord.DiscordApplication
import discord.Embed
import discord.Event
import discord.events.MessageCreateEvent
import discord.events.ReadyEvent
import discord.interactions.commands.Command
import discord.interactions.commands.CommandContext
import discord.interactions.commands.CommandResponse
import discord.interactions.commands.json
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope

suspend fun main(): Unit = coroutineScope {
    App(Client(coroutineScope = this))
}

class App(client: Client) : DiscordApplication(client) {
    init {
        client.run("ODQ4OTk3MDkxMDQ3ODk5MTY2.YLUwKA.rPNtZ3bsJwpfBmPwAVxDfaokSlE")
    }

    @Event
    suspend fun onReady(event: ReadyEvent) {
        println("Ready")
        /*client.httpClient.post<HttpResponse>("https://discord.com/api/v8/applications/${client.applicationId}/guilds/581185346465824768/commands") {
            this.header("Content-Type", "application/json")
            header("Authorization", client.authValue)
            this.body = Command.Json("test", "tests")
        }.also { println(it.receive<String>()) }*/
        println(event.application.id)
        client.guilds.forEach { println(it.name) }
    }

    @Event
    suspend fun onMessageCreate(event: MessageCreateEvent) {
        if (event.message.content == "Hi")
        event.message.respond("Hi, ${event.message.author.username}")
    }

    @Command("ping", "Gets the ping", guildIds = [581185346465824768])
    suspend fun ping(ctx: CommandContext): CommandResponse {
        return ctx.send("Pong! ${client.latency * 100}ms", hidden = true)
    }

    @Command("mc-user", "Get the name and head of a minecraft user.", guildIds = [581185346465824768], options = [
        Command.Option(Command.Option.Type.String, "name", "The username")
    ])
    suspend fun getMcUser(ctx: CommandContext): CommandResponse {
        return ctx.send(embeds = listOf(Embed(color = Color(0xffff0000),
            author = Embed.Author(name = ctx["name"], iconUrl = "https://mc-heads.net/avatar/${ctx.get<String>("name")}")))
        )
    }
}