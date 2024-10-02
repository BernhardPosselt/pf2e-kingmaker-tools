package at.posselt.pfrpg2e.plugins

import io.github.optimumcode.json.schema.JsonSchema
import io.github.optimumcode.json.schema.ValidationError
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import kotlin.io.path.isRegularFile

abstract class JsonSchemaValidator : DefaultTask() {
    @get:InputFile
    abstract val schema: RegularFileProperty

    @get:InputDirectory
    abstract val files: DirectoryProperty

    @TaskAction
    fun action() {
        val schemaFile = schema.asFile.get()
        val validator = JsonSchema.fromDefinition(schemaFile.readText())
        Files.walk(files.asFile.get().toPath())
            .filter { it.isRegularFile() }
            .filter { it.toString().endsWith(".json") }
            .map { it.toFile() }
            .forEach { jsonFile ->
                println("Validating JSON file ${jsonFile.absolutePath} using schema ${schemaFile.absoluteFile}")
                val json = parseToJsonElement(jsonFile.readText())
                val errorCollector = mutableListOf<ValidationError>()
                validator.validate(json, errorCollector::add)
                if (errorCollector.isNotEmpty()) {
                    val msg =
                        "Failed to validate ${jsonFile.absolutePath} using schema ${schemaFile.absoluteFile}: " +
                                errorCollector.joinToString("\n") { it.message }
                    throw GradleException(msg)
                }
            }
    }
}

