package at.posselt.tasks

import at.posselt.github.createGithubRelease
import at.posselt.github.uploadGithubAsset
import at.posselt.utils.ReleaseZipParser
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.FileInputStream
import kotlin.system.exitProcess

@DisableCachingByDefault
abstract class FoundryVTTModuleUploadGithubRelease : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val archive: RegularFileProperty

    @get:Input
    abstract val githubRepo: Property<String>

    @get:Input
    abstract val githubUser: Property<String>

    @get:Input
    abstract val githubToken: Property<String>

    @TaskAction
    fun action() {
        val archive = archive.asFile.orNull
        if (archive == null || !archive.exists()) {
            throw IllegalStateException("Archive file not found")
        }
        val parser = ReleaseZipParser()
        val zip = parser.parseZip(archive) ?: throw IllegalArgumentException("Not a release zip")
        val releaseVersion = zip.manifest.version
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
        val notes = zip.changelogs
            .find { it.version == releaseVersion }
            ?.notes ?: ""
        val repo = githubRepo.get()
        val user = githubUser.get()
        val ghToken = githubToken.get()
        runBlocking {
            httpClient.use { client ->
                val releaseId = client.createGithubRelease(
                    user = user,
                    repo = repo,
                    githubToken = ghToken,
                    releaseVersion = releaseVersion,
                    body = notes,
                ).id
                FileInputStream(archive).use { file ->
                    client.uploadGithubAsset(
                        user = user,
                        repo = repo,
                        releaseId = releaseId,
                        file = file,
                        githubToken = ghToken,
                        name = "release.zip",
                        contentType = ContentType.Application.Zip,
                    )
                }
                zip.manifestText.byteInputStream().use { moduleJson ->
                    client.uploadGithubAsset(
                        user = user,
                        repo = repo,
                        releaseId = releaseId,
                        file = moduleJson,
                        githubToken = ghToken,
                        name = "module.json",
                        contentType = ContentType.Application.Json,
                    )
                }
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