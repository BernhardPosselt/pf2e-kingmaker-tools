package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.utils.t
import io.github.optimumcode.json.schema.JsonSchema
import io.github.optimumcode.json.schema.ValidationError
import js.objects.recordOf
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class StructureValidationError(
    val input: String,
    message: String,
    val errors: List<ValidationError> = emptyList(),
) : Exception(message)

fun validateStructure(jsonText: String, schema: JsonElement) {
    val json = parseToJsonElement(jsonText)
    if (json !is JsonObject) {
        throw StructureValidationError(input = jsonText, message = t("kingdom.notValidJsonObject"))
    }
    if (json.containsKey("ref")) {
        val errors = validateUsingSchema(schema, json)
        if (errors.isEmpty()) {
            val ref = json["ref"].toString()
            if (translatedStructures.find { it.name == ref } != null) {
                throw StructureValidationError(
                    input = jsonText,
                    message = t("kingdom.canNotFindStructureRef", recordOf("ref" to ref))
                )
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