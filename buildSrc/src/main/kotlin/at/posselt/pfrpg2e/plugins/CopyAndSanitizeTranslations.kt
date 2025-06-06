package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

abstract class CopyAndSanitizeTranslations : DefaultTask() {
    @get:InputDirectory
    abstract val from: DirectoryProperty

    @get:OutputDirectory
    abstract val into: DirectoryProperty

    // this extra folder is needed to create a subfolder in the final dist/ folder
    // because we depend on the output directory as a resources src that folder is taken as the root
    // folder so subfolders are not picked up

    @get:Input
    abstract val targetFolderName: Property<String>

    @TaskAction
    fun action() {
        val source = from.get().asFile.toPath()
        val into = into.get().asFile.toPath()
        val folder = into.resolve(Paths.get(targetFolderName.get()))
        Files.createDirectories(folder)
        Files.walk(source)
            .parallel()
            .filter { it.isRegularFile() && it.fileName.toString().endsWith(".json") }
            .forEach {
                val target = folder.resolve(it.fileName)
                transformAndWrite(it, target)
            }
    }

    private fun transformAndWrite(source: Path, target: Path) {
        val text = source.readText()
        val whitelist = Safelist().apply {
            addTags("p", "ul", "li", "b")
        }
        val json = Json {
            ignoreUnknownKeys = true
        }
        val translations = json.parseToJsonElement(text)
        val cleaned = cleanTranslation(translations, whitelist)
        BufferedWriter(FileWriter(target.toFile())).use {
            it.write(json.encodeToString(cleaned))
        }
    }

    fun cleanTranslation(translations: JsonElement, whitelist: Safelist): JsonObject {
        // no need to take care of numerics or arrays, we're only dealing with strings and objects
        return if (translations is JsonObject) {
            JsonObject(translations.toMutableMap().map { (key, elem) ->
                if (elem is JsonPrimitive && elem.isString) {
                    key to JsonPrimitive(Jsoup.clean(elem.content, whitelist))
                } else if (elem is JsonObject) {
                    key to cleanTranslation(elem, whitelist)
                } else {
                    throw IllegalArgumentException("Received neither a string nor object as translation")
                }
            }.toMap())
        } else {
            throw IllegalArgumentException("Received neither a string nor object as root json element")
        }
    }
}