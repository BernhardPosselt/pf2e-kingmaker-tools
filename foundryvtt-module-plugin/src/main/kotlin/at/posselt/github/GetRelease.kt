package at.posselt.github

import kotlinx.serialization.Serializable

@Serializable
internal data class GetRelease(
    @Suppress("PropertyName")
    val tag_name: String,
    @Suppress("PropertyName")
    val target_commitish: String = "master",
    val name: String,
    val body: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @Suppress("PropertyName")
    val generate_release_notes: Boolean = false,
)