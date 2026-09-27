package at.posselt.foundryvtt.model

import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val id: String,
    val compatibility: FoundryCompatibility,
    val manifest: String,
    val version: String,
)
