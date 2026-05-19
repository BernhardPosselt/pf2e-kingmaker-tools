package at.posselt.foundryvtt.model

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
internal data class FoundryRelease(
    val version: String,
    val manifest: Url,
    val notes: Url?,
    val compatibility: FoundryCompatibility,
)