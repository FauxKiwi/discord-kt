# discord-kt
[![](https://jitpack.io/v/FauxKiwi/discord-kt.svg)](https://jitpack.io/#FauxKiwi/discord-kt)
[![](https://img.shields.io/website?label=docs&up_message=available&url=https%3A%2F%2Ffauxkiwi.github.io%2Fdiscord-kt)](https://fauxkiwi.github.io/discord-kt)

Discord bot library for kotlin inspired by discord.py

## Dependency
Add this to your repositories:
```groovy
maven { url 'https://jitpack.io' }
```
Add this to your dependencies:
```groovy
implementation 'com.github.FauxKiwi:discord-kt:latest'
```

## Usage
### Basic Bot
The minimum steps for running a bot is creating an app and running the client:
```kotlin
suspend fun main(): Unit = coroutineScope {
    App(Client(coroutineScope = this))
}

class App(client: Client) : DiscordApplication(client) {
    init {
        client.run("<your bot token>")
    }
}
```
You can run the app in every coroutine scope, if you use the global scope the app will be cancelled when the rest of the program finishes.
### Events
You can register events by adding the `@Event` annotation to a suspend method in a `DiscordApplication` which takes an event as parameter:
```kotlin
//...
@Event
suspend fun onReady(event: ReadyEvent) {
    println("Ready")
}
//...
```
### Commands
You can register slash commands by adding the `@Command(...)` annotation to a suspend method in a `DiscordApplication` which takes a `CommandContext` as parameter and returns a `CommandResponse`:
```kotlin
//...
@Command("ping", "Gets the ping", guildIds = [/*<your guild id for testing>*/])
suspend fun ping(ctx: CommandContext): CommandResponse {
    return ctx.send("Pong! ${client.latency * 100}ms", hidden = true)
}
//...
```
