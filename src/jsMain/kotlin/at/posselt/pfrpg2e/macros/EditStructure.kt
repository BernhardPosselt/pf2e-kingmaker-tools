package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.kingdom.*
import at.posselt.pfrpg2e.takeIfInstance
import com.foundryvtt.core.Actor
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2ENpc
import io.github.optimumcode.json.schema.JsonSchema
import io.github.optimumcode.json.schema.ValidationError
import js.objects.jso
import js.objects.recordOf
import kotlinx.js.JsPlainObject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@JsPlainObject
external interface StructureMacroData {
    val data: String
}

suspend fun editStructureMacro(actor: Actor?) {
    val npcActor = actor
        ?.takeIfInstance<PF2ENpc>()
    if (npcActor == null) {
        ui.notifications.error("Please select an NPC actor")
        return
    }
    val existing = npcActor.getStructureData() ?: jso()
    prompt<StructureMacroData, Unit>(
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                TextArea(
                    label = "Data",
                    name = "data",
                    value = JSON.stringify(existing, null, 2),
                    help = "Leave completely empty to remove structure data",
                    required = false,
                    elementClasses = listOf("larger-textarea")
                ),
            )
        ),
        title = "Edit Structure Data",
        width = 600,
    ) { data ->
        try {
            if (data.data.isBlank()) {
                ui.notifications.info("Removed Structure Data from Actor")
                npcActor.unsetStructureData()
                return@prompt
            }
            val json = parseToJsonElement(data.data)
            console.log(json)
            if (json !is JsonObject) {
                ui.notifications.error("${data.data} is not a JSON element!")
                return@prompt
            }
            val errors = if (json.containsKey("ref")) {
                val schema = parseToJsonElement(JSON.stringify(structureRefSchema))
                validate(schema, json)
            } else {
                val schema = parseToJsonElement(JSON.stringify(structureSchema))
                validate(schema, json)
            }
            if (errors.isNotEmpty()) {
                ui.notifications.error("${data.data}: ${errors.joinToString("\n") { it.message }}")
                return@prompt
            }
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            npcActor.setStructureData(JSON.parse(data.data) as StructureData)
        } catch (e: SerializationException) {
            ui.notifications.error("${data.data}: ${e.message}")
            return@prompt
        }
    }
}

fun validate(schema: JsonElement, value: JsonElement): List<ValidationError> {
    val errorCollector = mutableListOf<ValidationError>()
    val validator = JsonSchema.fromJsonElement(schema)
    validator.validate(value, errorCollector::add)
    return errorCollector
}