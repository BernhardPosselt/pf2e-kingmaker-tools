package at.posselt.github

import at.posselt.utils.isPrerelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.InputStream

internal suspend fun HttpClient.uploadGithubAsset(
    repo: String,
    releaseId: Int,
    file: InputStream,
    githubToken: String,
    name: String,
    contentType: ContentType,
) {
    post("https://uploads.github.com/repos/$repo/releases/$releaseId/assets") {
        url {
            parameters.append("name", name)
        }
        contentType(contentType)
        accept(ContentType.Application.Json)
        bearerAuth(githubToken)
        setBody(file.toByteReadChannel())
    }
}

internal suspend fun HttpClient.createGithubRelease(
    repo: String,
    githubToken: String,
    releaseVersion: String,
    body: String,
) = post("https://api.github.com/repos/$repo/releases") {
    contentType(ContentType.Application.Json)
    accept(ContentType.Application.Json)
    bearerAuth(githubToken)
    setBody(
        GetRelease(
            tag_name = releaseVersion,
            name = releaseVersion,
            body = body,
            prerelease = releaseVersion.isPrerelease(),
        )
    )
}.body<GetReleaseResponse>()