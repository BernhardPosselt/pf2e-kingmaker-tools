package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.readText


/**
 * Given a directory, creates a file in dist/ with the same name as the directory
 * using all files in the JSON folder
 */
abstract class PackJsonFile : DefaultTask() {
    @get:InputDirectory
    abstract var targetDirectory: Directory

    @get:InputDirectory
    abstract var sourceDirectory: Directory

    private val encoder = Json {
        prettyPrint = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction
    fun action() {
        val source = sourceDirectory.asFile.toPath()
        val target = targetDirectory.asFile.toPath()
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

    @OptIn(ExperimentalSerializationApi::class)
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

