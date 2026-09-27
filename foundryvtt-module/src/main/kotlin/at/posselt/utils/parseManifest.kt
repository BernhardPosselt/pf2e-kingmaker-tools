package at.posselt.utils

import at.posselt.foundryvtt.model.Manifest
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets

internal fun parseManifest(text: String): Manifest {
    val json = Json {
        ignoreUnknownKeys = true
    }
    return json.decodeFromString(text)
}

internal fun parseManifest(input: InputStream): Manifest {
    val text = String(input.readAllBytes(), StandardCharsets.UTF_8);
    return parseManifest(text)
}