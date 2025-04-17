package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.Serializable

@Serializable
data class ModuleLanguage(
    val lang: String,
    val name: String,
    val path: String,
)

@Serializable
data class ModuleJson(
    val version: String,
    val download: String,
    val languages: List<ModuleLanguage>
)