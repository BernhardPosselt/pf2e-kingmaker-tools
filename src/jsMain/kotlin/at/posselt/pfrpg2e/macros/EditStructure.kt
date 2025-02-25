package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.kingdom.*
import at.posselt.pfrpg2e.kingdom.structures.StructureData
import at.posselt.pfrpg2e.kingdom.structures.StructureValidationError
import at.posselt.pfrpg2e.kingdom.structures.structureRefSchema
import at.posselt.pfrpg2e.kingdom.structures.validateStructure
import at.posselt.pfrpg2e.takeIfInstance
import com.foundryvtt.core.Actor
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.jso
import js.objects.recordOf
import kotlinx.js.JsPlainObject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json.Default.parseToJsonElement

@JsPlainObject
external interface StructureMacroData {
    val data: String
}

suspend fun editStructureMacro(actor: Actor?) {
    val npcActor = actor
        ?.takeIfInstance<PF2ENpc>()
        ?.parent.unsafeCast<PF2ENpc?>()
        ?.baseActor.unsafeCast<PF2ENpc?>()
    if (npcActor == null) {
        ui.notifications.error("Please select an NPC actor")
        return
    }
    val existing = npcActor.getStructure() ?: jso()
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
            } else {
                try {
                    validateStructure(data.data, parseToJsonElement(JSON.stringify(structureRefSchema)))
                    val value = JSON.parse<StructureData>(data.data)
                    npcActor.setStructureData(value)
                } catch (e: StructureValidationError) {
                    ui.notifications.error(e.message ?: "Failed to validate structure")
                    console.error(e.message, e.errors)
                }
            }
        } catch (e: SerializationException) {
            ui.notifications.error("${data.data}: ${e.message}")
        }
    }
}

