package discord

import kotlinx.serialization.json.Json

data class VersionInfo(
    val major: Int,
    val minor: Int,
    val micro: Int,
    val releaseLevel: ReleaseLevel,
    val serial: Int = 0
) {
    enum class ReleaseLevel {
        ALPHA, BETA, CANDIDATE, FINAL
    }

    override fun toString(): String = when (releaseLevel) {
        ReleaseLevel.ALPHA -> "${versionString()}-alpha"
        ReleaseLevel.BETA -> "${versionString()}-beta"
        ReleaseLevel.CANDIDATE -> "${versionString()}-rc$serial"
        ReleaseLevel.FINAL -> versionString()
    }

    private fun versionString() = "$major.$minor.$micro"
}

val versionInfo = VersionInfo(0, 0, 1, VersionInfo.ReleaseLevel.ALPHA, 0)

val version = versionInfo.toString()

const val API_BASE_URL = "https://discord.com/api"

internal val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = false
    this.useAlternativeNames = false
}