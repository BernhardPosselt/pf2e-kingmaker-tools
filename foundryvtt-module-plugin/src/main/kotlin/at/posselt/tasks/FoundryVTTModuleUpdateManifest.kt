package at.posselt.tasks

import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.FileOutputStream

/**
 * Generate a new module.json file with the proper versioning links
 */
@CacheableTask
abstract class FoundryVTTModuleUpdateManifest : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val moduleFile: RegularFileProperty

    @get:OutputFile
    abstract val targetModuleFile: RegularFileProperty

    @get:Input
    abstract val githubRepo: Property<String>

    @get:Input
    abstract val githubUser: Property<String>

    @get:Input
    abstract val version: Property<String>

    private val encoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun action() {
        val version = version.get()
        val meta = moduleFile.get().asFile
        val downloadUrl = URLBuilder().takeFrom("https://github.com/")
            .appendPathSegments(
                listOf(
                    githubUser.get(),
                    githubRepo.get(),
                    "releases",
                    "download",
                    version,
                    "release.zip"
                )
            )
            .build()
        val json = parseToJsonElement(meta.readText())
        val new = if (json is JsonObject) {
            json.toMutableMap().apply {
                this["version"] = JsonPrimitive(version)
                this["download"] = JsonPrimitive(downloadUrl.toString())
            }
        } else {
            throw IllegalStateException("Invalid module JSON format")
        }
        FileOutputStream(meta).use {
            encoder.encodeToStream(new, it)
        }
        FileOutputStream(targetModuleFile.get().asFile).use {
            encoder.encodeToStream(new, it)
        }
    }
}