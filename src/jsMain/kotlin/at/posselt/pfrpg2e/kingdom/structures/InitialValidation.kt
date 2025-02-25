package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.kingdom.getStructure
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.serialization.json.Json.Default.parseToJsonElement

fun validateStructures(game: Game) {
    val schema = parseToJsonElement(JSON.stringify(structureRefSchema))
    val actorsAndErrors = game.actors.contents
        .filterIsInstance<PF2ENpc>()
        .mapNotNull {
            try {
                it.getStructure()?.let { structure ->
                    validateStructure(JSON.stringify(structure), schema)
                    null
                }
            } catch (e: StructureValidationError) {
                it to e
            }
        }
    if(actorsAndErrors.isNotEmpty()) {
        val actorNames = actorsAndErrors.joinToString(", ") { it.first.name }
        ui.notifications.error("The following actor structures failed to validate; Kingdom Sheet might not work as expected: $actorNames")
        ui.notifications.error("Check console log for exact errors")
        actorsAndErrors.forEach {  (actor, error) ->
            console.error("Actor: ${actor.name}", error.message)
        }
    }
}