package at.posselt.foundryvtt

import at.posselt.foundryvtt.model.FoundryCompatibility
import at.posselt.foundryvtt.model.FoundryRelease
import at.posselt.foundryvtt.model.FoundryReleaseVersion
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

internal suspend fun HttpClient.createFoundryRelease(
    foundryToken: String,
    id: String,
    releaseVersion: String,
    compatibility: FoundryCompatibility,
    manifestUrl: Url,
    notesUrl: Url?,
    dryRun: Boolean,
) {
    post("https://api.foundryvtt.com/_api/packages/release_version/") {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
        headers {
            append(HttpHeaders.Authorization, foundryToken)
        }
        setBody(
            FoundryReleaseVersion(
                id = id,
                dryRun = dryRun,
                release = FoundryRelease(
                    version = releaseVersion,
                    manifest = manifestUrl,
                    notes = notesUrl,
                    compatibility = compatibility
                ),
            )
        )
    }
}