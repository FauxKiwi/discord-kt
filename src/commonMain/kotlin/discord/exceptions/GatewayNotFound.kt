package discord.exceptions

class GatewayNotFound(original: Exception) : Exception(original.message, original.cause)