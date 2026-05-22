package at.posselt.foundryvtt.model

import kotlinx.serialization.Serializable

@Serializable
data class FoundryCompatibility(
    val minimum: String,
    val verified: String,
    val maximum: String? = null,
)