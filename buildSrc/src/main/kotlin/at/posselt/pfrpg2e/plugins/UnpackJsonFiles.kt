package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.file.Path
import kotlin.io.path.readText


/**
 * Takes a path to a json file containing an array and generates files from each element
 * using the fileNameProperty field value
 */
abstract class UnpackJsonFiles : DefaultTask() {
    @get:Input
    abstract val fileNameProperty: Property<String>

    @get:InputFile
    abstract val file: RegularFileProperty

    @get:OutputDirectory
    abstract val targetDirectory: DirectoryProperty

    private val encoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun action() {
        val source = file.asFile.get().toPath()
        val target = targetDirectory.asFile.get().toPath()
        val elements = parseToJsonElement(source.readText())
        if (elements is JsonArray) {
            elements.forEach { elem ->
                val field = fileNameProperty.get()
                if (elem is JsonObject && field in elem.keys) {
                    val value = elem[field]
                    if (value is JsonPrimitive && value.isString) {
                        val targetPath = target.resolve("${value.content}.json")
                        writeToFile(
                            targetPath,
                            elem,
                        )
                    }
                }
            }
        }
    }

    private fun writeToFile(target: Path, elem: JsonElement) {
        val targetFile = BufferedOutputStream(FileOutputStream(target.toFile()))
        targetFile.use {
            encoder.encodeToStream(elem, it)
        }
    }
}

