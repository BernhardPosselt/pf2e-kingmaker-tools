package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.readText


/**
 * Concatenates all json files ending in .json in sourceDirectory into a single
 * JSON file in targetDirectory. The file name is the name of the sourceDirectory + .json
 */
abstract class CombineJsonFiles : DefaultTask() {
    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val targetDirectory: DirectoryProperty

    private val encoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun action() {
        val source = sourceDirectory.asFile.get().toPath()
        val target = targetDirectory.asFile.get().toPath()
        Files.walk(source, 1)
            .filter { Files.isDirectory(it) }
            .filter { it != source }
            .forEach { directory ->
                val name = directory.fileName
                val jsonFiles = Files.walk(directory)
                    .filter { Files.isRegularFile(it) }
                    .filter { it.toString().endsWith(".json") }
                    .collect(Collectors.toList())
                writeToFile(
                    target.resolve(Paths.get("$name.json")),
                    jsonFiles,
                )
            }
    }

    private fun writeToFile(target: Path, jsonFiles: List<Path>) {
        val result = buildJsonArray {
            jsonFiles.asSequence()
                .map { parseToJsonElement(it.readText()) }
                .forEach { add(it) }
        }
        val targetFile = BufferedOutputStream(FileOutputStream(target.toFile()))
        targetFile.use {
            encoder.encodeToStream(result, it)
        }
    }
}

