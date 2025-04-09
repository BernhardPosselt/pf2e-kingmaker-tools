package at.posselt.pfrpg2e.kingdom.structures

import io.github.optimumcode.json.schema.JsonSchema
import io.github.optimumcode.json.schema.ValidationError
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class StructureValidationError(
    val input: String,
    message: String,
    val errors: List<ValidationError> = emptyList(),
): Exception(message)

fun validateStructure(jsonText: String, schema: JsonElement) {
    val json = parseToJsonElement(jsonText)
    if (json !is JsonObject) {
        return throw StructureValidationError(input = jsonText, message = "Not a valid JSON Object")
    }
    if (json.containsKey("ref")) {
        val errors = validateUsingSchema(schema, json)
        if (errors.isEmpty()) {
            val ref = json["ref"].toString()
            if (structures.find { it.name == ref } != null) {
                throw StructureValidationError(input = jsonText, message = "Can not find existing structure with ref $ref")
            }
        } else {
            throw StructureValidationError(input = jsonText, message = errors.joinToString("\n"), errors = errors)
        }
    } else {
        val schema = parseToJsonElement(JSON.stringify(structureSchema))
        val errors = validateUsingSchema(schema, json)
        if (errors.isNotEmpty()) {
            throw StructureValidationError(input = jsonText, message = errors.joinToString("\n"), errors = errors)
        }
    }
}

fun validateUsingSchema(schema: JsonElement, value: JsonElement): List<ValidationError> {
    val errorCollector = mutableListOf<ValidationError>()
    val validator = JsonSchema.fromJsonElement(schema)
    validator.validate(value, errorCollector::add)
    return errorCollector
}