import discord.Client
import discord.DiscordApplication
import discord.Event
import discord.Guild
import discord.events.MessageCreateEvent
import discord.events.ReadyEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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
        delay(1000)
        client.guilds.forEach { println(it.name) }
    }

    @Event
    suspend fun onMessageCreate(event: MessageCreateEvent) {
        println(event.message.content)
        println("Current latency: ${client.latency}")
    }
}

/*suspend fun main() {
    val client = HttpClient(Java) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(WebSockets)
    }

    client.wss(
        method = HttpMethod.Get,
        host = "gateway.discord.gg",
        path = "/?v9=&encoding=json"
    ) {
        val hello = Json.parseToJsonElement(String(incoming.receive().data)).jsonObject
        println(hello.toString())
        val heartbeat = hello["d"]!!.jsonObject["heartbeat_interval"]!!.jsonPrimitive.long - 1000
        println(heartbeat)

        send(buildJsonObject {
            put("op", 2)
            put("d", buildJsonObject {
                put("token", "ODQ4OTk3MDkxMDQ3ODk5MTY2.YLUwKA.rPNtZ3bsJwpfBmPwAVxDfaokSlE")
                put("intents", 513)
                put("properties", buildJsonObject {
                    put("\$os", "windows")
                    put("\$browser", "disco")
                    put("\$device", "disco")
                })
            })
        }.toString())

        println(String(incoming.receive().readBytes()))

        launch {
            delay((heartbeat * Random(heartbeat).nextDouble()).toLong())
            while (true) {
                send(buildJsonObject {
                    put("op", 1)
                    put("d", null as Number?)
                }.toString())
                println(String(incoming.receive().readBytes()))
                delay(heartbeat)
            }
        }

        while (true) {
            val inc = String(incoming.receive().readBytes())
            println(inc)
        }
    }
}*/

/*suspend fun main() {
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    /*val response: HttpResponse = client.get("https://discord.com/api/guilds/846756269078872065") {
        header("Authorization", "Bot ODQ4OTk3MDkxMDQ3ODk5MTY2.YLUwKA.rPNtZ3bsJwpfBmPwAVxDfaokSlE")
    }

    println(response.readText())*/

    val response: Guild = client.get("https://discord.com/api/guilds/846756269078872065") {
        header("Authorization", "Bot ODQ4OTk3MDkxMDQ3ODk5MTY2.YLUwKA.rPNtZ3bsJwpfBmPwAVxDfaokSlE")
    }

    println(response.ownerId)
}*/