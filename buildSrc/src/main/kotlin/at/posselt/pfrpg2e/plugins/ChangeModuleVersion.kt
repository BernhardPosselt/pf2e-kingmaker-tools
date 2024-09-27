package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.FileOutputStream

abstract class ChangeModuleVersion : DefaultTask() {
    @get:Input
    abstract val moduleVersion: Property<String>

    private val encoder = Json {
        prettyPrint = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction
    fun action() {
        val version = moduleVersion.get()
        val meta = project.projectDir.resolve("module.json")
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
            throw GradleException("Invalid module JSON format")
        }
        val output = project.layout.projectDirectory.file("module.json").asFile
        encoder.encodeToStream(new, FileOutputStream(output))
    }
}