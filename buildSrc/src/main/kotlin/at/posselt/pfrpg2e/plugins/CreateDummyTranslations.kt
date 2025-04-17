package at.posselt.pfrpg2e.plugins

import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

abstract class CreateDummyTranslations : DefaultTask() {
    @get:InputFile
    abstract val moduleJson: RegularFileProperty

    @get:InputFile
    abstract val enTranslation: RegularFileProperty

    @get:OutputDirectory
    abstract val langDirectory: DirectoryProperty

    @TaskAction
    fun action() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val module = json.decodeFromString<ModuleJson>(moduleJson.get().asFile.readText())
        val langDir = langDirectory.get().asFile.toPath()
        val enTranslation = enTranslation.get().asFile.toPath()
        module.languages
            .filter { it.lang != "en" }
            .forEach {
                val target = langDir.resolve("${it.lang}.json")
                Files.copy(enTranslation, target)
            }
    }
}