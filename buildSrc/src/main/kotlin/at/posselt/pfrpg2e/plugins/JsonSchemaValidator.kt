package at.posselt.pfrpg2e.plugins

import io.github.optimumcode.json.schema.JsonSchema
import io.github.optimumcode.json.schema.ValidationError
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import kotlin.io.path.isRegularFile

data class SchemaSource(
    val schemaFile: RegularFile,
    var jsonFilesToValidate: Directory,
)

abstract class JsonSchemaValidator : DefaultTask() {
    private val schemaList: MutableList<SchemaSource> = mutableListOf()

    @TaskAction
    fun action() {
        schemaList.forEach { schema ->
            val schemaFile = schema.schemaFile.asFile
            val validator = JsonSchema.fromDefinition(schemaFile.readText())
            Files.walk(schema.jsonFilesToValidate.asFile.toPath())
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

    fun addSchema(file: RegularFile, filesToValidate: Directory) {
        val schema = SchemaSource(
            file, filesToValidate
        )
        schemaList.add(schema)
    }
}

