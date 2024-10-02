package at.posselt.pfrpg2e.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.cio.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

@Serializable
private data class GetReleaseResponse(
    val id: Int,
)

@Serializable
private data class GetRelase(
    val tag_name: String,
    val target_commitish: String = "master",
    val name: String,
    val body: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val generate_release_notes: Boolean = false,
)

@Serializable
private data class FoundryReleaseVersion(
    val id: String,
    val dryRun: Boolean = false,
    val release: FoundryRelease,
) {
    @Serializable
    data class FoundryRelease(
        val version: String,
        val manifest: String,
        val notes: String,
        val compatibility: FoundryCompatibility,
    )
}

@Serializable
private data class FoundryCompatibility(
    val minimum: String,
    val verified: String,
    val maximum: String,
)

private suspend fun HttpClient.uploadGithubAsset(
    repo: String,
    releaseId: Int,
    file: File,
    githubToken: String,
    name: String,
    contentType: ContentType,
) {
    post("https://uploads.github.com/repos/$repo/releases/$releaseId/assets") {
        url {
            parameters.append("name", name)
        }
        headers {
            append(HttpHeaders.ContentLength, Files.size(file.toPath()).toString())
        }
        contentType(contentType)
        accept(ContentType.Application.Json)
        bearerAuth(githubToken)
        setBody(file.readChannel())
    }
}

private suspend fun HttpClient.createGithubRelease(
    repo: String,
    githubToken: String,
    releaseVersion: String
) = post("https://api.github.com/repos/$repo/releases") {
    contentType(ContentType.Application.Json)
    accept(ContentType.Application.Json)
    bearerAuth(githubToken)
    setBody(GetRelase(tag_name = releaseVersion, name = releaseVersion))
}.body<GetReleaseResponse>()

private suspend fun HttpClient.createFoundryRelease(
    foundryToken: String,
    id: String,
    releaseVersion: String,
    repo: String,
    compatibility: FoundryCompatibility,
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
                dryRun = false,
                release = FoundryReleaseVersion.FoundryRelease(
                    version = releaseVersion,
                    manifest = "https://github.com/BernhardPosselt/pf2e-kingmaker-tools/releases/download/$releaseVersion/module.json",
                    notes = "https://github.com/$repo/blob/master/CHANGELOG.md",
                    compatibility = compatibility
                ),
            )
        )
    }
}

@Serializable
private data class Manifest(
    val id: String,
    val compatibility: FoundryCompatibility
)

private fun parseManifest(file: File): Manifest {
    val text = file.readText()
    val json = Json {
        ignoreUnknownKeys = true
    }
    return json.decodeFromString(text)
}


abstract class ReleaseModule : DefaultTask() {
    @get:InputFile
    abstract val releaseZip: RegularFileProperty

    @get:InputFile
    abstract val releaseModuleJson: RegularFileProperty

    @get:Input
    abstract val githubRepo: Property<String>

    @TaskAction
    fun action() {
        val githubToken = System.getenv("GITHUB_TOKEN") ?: throw IllegalStateException("GITHUB_TOKEN not set")
        val foundryToken = System.getenv("FOUNDRY_TOKEN") ?: throw IllegalStateException("FOUNDRY_TOKEN not set")
        val repo = githubRepo.get()
        val archive = releaseZip.asFile.orNull
        val moduleJson = releaseModuleJson.asFile.orNull
        if (archive == null || !archive.exists()) {
            throw IllegalStateException("Archive file not found")
        }
        if (moduleJson == null || !moduleJson.exists()) {
            throw IllegalStateException("Module file not found")
        }
        val manifest = parseManifest(moduleJson)
        val releaseVersion = project.version.toString()
        exec(listOf("git", "add", "module.json", "build.gradle.kts"), ignoreErrors = true)
        exec(listOf("git", "commit", "-m", "release"), ignoreErrors = true)
        exec(listOf("git", "push"), ignoreErrors = true)
        exec(listOf("git", "tag", releaseVersion))
        exec(listOf("git", "push", "--tags"))

        val httpClient = HttpClient(Java) {
            expectSuccess = true
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }

        }
        runBlocking {
            httpClient.use { client ->
                val releaseId = client.createGithubRelease(
                    repo = repo,
                    githubToken = githubToken,
                    releaseVersion = releaseVersion,
                ).id
                client.uploadGithubAsset(
                    repo = repo,
                    releaseId = releaseId,
                    file = archive,
                    githubToken = githubToken,
                    name = "release.zip",
                    contentType = ContentType.Application.Zip,
                )
                client.uploadGithubAsset(
                    repo = repo,
                    releaseId = releaseId,
                    file = moduleJson,
                    githubToken = githubToken,
                    name = "module.json",
                    contentType = ContentType.Application.Json,
                )
                client.createFoundryRelease(
                    foundryToken = foundryToken,
                    id = manifest.id,
                    releaseVersion = releaseVersion,
                    repo = repo,
                    compatibility = manifest.compatibility,
                )
            }
        }
    }

    private fun exec(commands: List<String>, ignoreErrors: Boolean = false) {
        val exitCode = ProcessBuilder(commands)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .directory(project.projectDir)
            .start()
            .waitFor()
        if (exitCode != 0) {
            println("Failed to execute command: ${commands.joinToString(" ")}")
            if (!ignoreErrors) {
                exitProcess(exitCode)
            }
        }
    }
}