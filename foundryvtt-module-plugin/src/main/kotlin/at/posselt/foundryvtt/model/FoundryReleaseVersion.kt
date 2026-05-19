package at.posselt.foundryvtt.model

import kotlinx.serialization.Serializable

@Serializable
internal data class FoundryReleaseVersion(
    val id: String,
    val dryRun: Boolean = false,
    val release: FoundryRelease,
)

