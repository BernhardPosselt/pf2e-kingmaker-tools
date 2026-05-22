package at.posselt.github

import at.posselt.utils.isPrerelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.InputStream

internal suspend fun HttpClient.uploadGithubAsset(
    user: String,
    repo: String,
    releaseId: Int,
    file: InputStream,
    githubToken: String,
    name: String,
    contentType: ContentType,
) {
    post {
        url("https://uploads.github.com/repos/") {
            appendPathSegments(user, repo, "releases", releaseId.toString(), "assets")
            parameters.append("name", name)
        }
        contentType(contentType)
        accept(ContentType.Application.Json)
        bearerAuth(githubToken)
        setBody(file.toByteReadChannel())
    }
}

internal suspend fun HttpClient.createGithubRelease(
    user: String,
    repo: String,
    githubToken: String,
    releaseVersion: String,
    body: String,
) = post {
    url("https://uploads.github.com/repos/") {
        appendPathSegments(user, repo, "releases")
    }
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