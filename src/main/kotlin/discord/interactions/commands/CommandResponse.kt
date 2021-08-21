package discord.interactions.commands

import kotlin.reflect.full.starProjectedType

class CommandResponse internal constructor() {
}

internal val CommandResponseType = CommandResponse::class.starProjectedType