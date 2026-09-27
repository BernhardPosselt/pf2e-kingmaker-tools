package at.posselt.github

import at.posselt.utils.isPrerelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files

internal suspend fun HttpClient.uploadGithubAsset(
    user: String,
    repo: String,
    releaseId: Int,
    input: InputStream,
    size: Long,
    githubToken: String,
    name: String,
    contentType: ContentType,
) {
    post("https://uploads.github.com/repos/") {
        url {
            appendPathSegments(user, repo, "releases", releaseId.toString(), "assets")
            parameters.append("name", name)
        }
        headers {
            append(HttpHeaders.ContentLength, size.toString())
        }
        contentType(contentType)
        accept(ContentType.Application.Json)
        bearerAuth(githubToken)
        setBody(input.toByteReadChannel())
    }
}

internal suspend fun HttpClient.uploadGithubAsset(
    user: String,
    repo: String,
    releaseId: Int,
    file: File,
    githubToken: String,
    name: String,
    contentType: ContentType,
) {
    withContext(Dispatchers.IO) {
        FileInputStream(file).use {
            uploadGithubAsset(
                user = user,
                repo = repo,
                releaseId = releaseId,
                input = it,
                githubToken = githubToken,
                name = name,
                contentType = contentType,
                size = Files.size(file.toPath()),
            )
        }
    }
}

internal suspend fun HttpClient.createGithubRelease(
    user: String,
    repo: String,
    githubToken: String,
    releaseVersion: String,
    body: String,
) = post("https://api.github.com/repos/") {
    url {
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