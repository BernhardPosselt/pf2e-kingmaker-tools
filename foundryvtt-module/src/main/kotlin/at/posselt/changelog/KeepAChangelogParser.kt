package at.posselt.changelog

import java.io.InputStream
import java.nio.charset.StandardCharsets

data class ChangelogVersion(
    val version: String,
    val notes: String,
)

private val versionRegex = "([^]]+)].*".toRegex()

class KeepAChangelogParser {
    /**
     * Very naive implementation. We assume that the file can fit into memory
     * and that the CHANGELOG.md file is properly formatted
     */
    fun parse(input: InputStream): List<ChangelogVersion> {
        val text = String(input.readAllBytes(), StandardCharsets.UTF_8)
        val releases = text.split("## [")
        return releases.asSequence()
            .mapNotNull {
                val lines = it.split("\n")
                lines[0].let { first ->
                    versionRegex.find(first)
                        ?.let { matchResult -> matchResult.groupValues[1] }
                        ?.let { version ->
                            val content = lines.drop(1)
                                .joinToString("\n")
                                .trim()
                            ChangelogVersion(
                                version = version,
                                notes = content,
                            )
                        }
                }
            }
            .toList()
    }
}