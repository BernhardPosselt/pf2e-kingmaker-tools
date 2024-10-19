package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.FileOutputStream

/**
 * Generate a new module.json file with the proper versioning links
 */
abstract class ChangeModuleVersion : DefaultTask() {
    @get:InputFile
    abstract val sourceFile: RegularFileProperty
    @get:OutputFile
    abstract val targetFile: RegularFileProperty
    @get:Input
    abstract val moduleVersion: Property<String>

    private val encoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun action() {
        val version = moduleVersion.get()
        val meta = sourceFile.get().asFile
        val json = parseToJsonElement(meta.readText())
        val new = if (json is JsonObject) {
            json.toMutableMap().apply {
                this["version"] = JsonPrimitive(version)
                this["download"] =
                    JsonPrimitive(
                        "https://github.com/BernhardPosselt" +
                                "/pf2e-kingmaker-tools/releases/download/${version}/release.zip"
                    )
            }
        } else {
            throw IllegalStateException("Invalid module JSON format")
        }
        FileOutputStream(meta).use {
            encoder.encodeToStream(new, it)
        }
        FileOutputStream(targetFile.get().asFile).use {
            encoder.encodeToStream(new, it)
        }
    }
}