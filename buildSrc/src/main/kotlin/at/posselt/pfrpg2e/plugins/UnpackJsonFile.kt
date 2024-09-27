package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Given a file, creates a directory with the same name as the file
 * but without the json suffix, then iterates over all contents and
 * creates files using a name property in the new folder
 */
abstract class UnpackJsonFile : DefaultTask() {
    @get:InputFile
    abstract var targetFile: RegularFile

    @get:InputDirectory
    abstract var outputDirectory: Directory

    @get:Input
    var fileNameProperty: String = "name"

    private val encoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun action() {
        val file = targetFile.asFile
        val json = parseToJsonElement(file.readText())
        val targetDirectory = outputDirectory.asFile.toPath().resolve(
            Paths.get(file.name.removeSuffix(".json"))
        )
        recreateDirectory(targetDirectory)
        if (json is JsonArray) {
            json.forEach {
                writeToFile(it, targetDirectory)
            }
        } else {
            throw GradleException("Not an array")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeToFile(element: JsonElement, targetDirectory: Path) {
        if (element is JsonObject) {
            val name = element[fileNameProperty]?.jsonPrimitive?.content
            if (name == null) {
                throw GradleException("Can not find $fileNameProperty property in nested object")
            } else {
                val targetFile = targetDirectory.resolve(Paths.get("$name.json"))
                val target = BufferedOutputStream(FileOutputStream(targetFile.toFile()))
                target.use {
                    encoder.encodeToStream(element, it)
                }
            }
        }
    }

    private fun recreateDirectory(targetDirectory: Path) {
        if (Files.isDirectory(targetDirectory)) {
            Files.walk(targetDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete)
        }
        Files.createDirectory(targetDirectory)
    }
}

