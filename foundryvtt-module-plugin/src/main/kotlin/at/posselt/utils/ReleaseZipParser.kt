package at.posselt.utils

import at.posselt.changelog.KeepAChangelogParser
import at.posselt.changelog.ChangelogVersion
import at.posselt.foundryvtt.model.Manifest
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import kotlin.io.path.inputStream
import kotlin.streams.asSequence

private val moduleRegex = "^/([0-9a-zA-Z-_]+)/module.json$".toRegex()
private val changelogRegex = "^/([0-9a-zA-Z-_]+)/CHANGELOG.md$".toRegex()

data class ManifestAndChangelog(
    val manifest: Manifest,
    val changelogs: List<ChangelogVersion>,
    val manifestText: String
)

private data class FFile(
    val path: Path,
    val type: ConfigFileType
) {
    enum class ConfigFileType {
        MANIFEST,
        CHANGELOG;
    }
}

class ReleaseZipParser {

    fun parseZip(file: File): ManifestAndChangelog? {
        return FileSystems.newFileSystem(file.toPath(), Collections.emptyMap<String, Any>()).use { fs ->
            fs.rootDirectories.mapNotNull { root ->
                val result = Files.walk(root).asSequence()
                    .mapNotNull { path ->
                        if (moduleRegex.matches(path.toString())) {
                            FFile(path = path, type = FFile.ConfigFileType.MANIFEST)
                        } else if (changelogRegex.matches(path.toString())) {
                            FFile(path = path, type = FFile.ConfigFileType.CHANGELOG)
                        } else {
                            null
                        }
                    }
                    .toList()
                val manifestText = result
                    .find { it.type == FFile.ConfigFileType.MANIFEST }
                    ?.path
                    ?.inputStream()
                    ?.readAllBytes()
                    ?.let { String(it, StandardCharsets.UTF_8) }
                val manifest = manifestText
                    ?.let(::parseManifest)
                    ?: return@mapNotNull null
                val changelogs = result.find { it.type == FFile.ConfigFileType.CHANGELOG }
                    ?.let {
                        val parser = KeepAChangelogParser()
                        it.path.inputStream().use(parser::parse)
                    }.orEmpty()
                ManifestAndChangelog(
                    manifestText = manifestText,
                    manifest = manifest,
                    changelogs = changelogs,
                )
            }
        }.firstOrNull()
    }

}