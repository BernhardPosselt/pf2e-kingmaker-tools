package at.posselt.tasks

import at.posselt.foundryvtt.createFoundryRelease
import at.posselt.utils.ReleaseZipParser
import at.posselt.utils.isPrerelease
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
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class FoundryVTTModuleCreateRelease : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val archive: RegularFileProperty

    @get:Optional
    @get:Input
    abstract val dryRun: Property<Boolean>

    @get:Input
    abstract val githubRepo: Property<String>

    @get:Input
    abstract val githubUser: Property<String>

    @get:Input
    abstract val foundryToken: Property<String>

    @TaskAction
    fun action() {
        val parser = ReleaseZipParser()
        val zip = parser.parseZip(archive.get().asFile) ?: throw IllegalArgumentException("Not a release zip")
        val releaseVersion = zip.manifest.version

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
        val notesUrl = URLBuilder().takeFrom("https://github.com/")
            .appendPathSegments(
                listOf(
                    githubUser.get(),
                    githubRepo.get(),
                    "releases",
                    "tag",
                    releaseVersion,
                )
            )
            .build()
        runBlocking {
            httpClient.use { client ->
                if (!releaseVersion.isPrerelease()) {
                    client.createFoundryRelease(
                        foundryToken = foundryToken.get(),
                        id = zip.manifest.id,
                        releaseVersion = releaseVersion,
                        compatibility = zip.manifest.compatibility,
                        manifestUrl = Url(zip.manifest.manifest),
                        notesUrl = notesUrl,
                        dryRun = if (dryRun.isPresent) dryRun.get() else false
                    )
                }
            }
        }
    }
}