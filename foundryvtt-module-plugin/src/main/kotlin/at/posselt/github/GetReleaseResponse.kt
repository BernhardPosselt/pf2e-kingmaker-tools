package at.posselt.github

import kotlinx.serialization.Serializable

@Serializable
internal data class GetReleaseResponse(
    val id: Int,
)