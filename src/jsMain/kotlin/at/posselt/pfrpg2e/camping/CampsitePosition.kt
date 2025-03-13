package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2EParty
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CampsitePosition {
    val x: Double
    val y: Double
    var result: String
}

@JsPlainObject
external interface ExistingCampsites {
    var positions: Array<CampsitePosition>
}

fun ExistingCampsites.findExistingCampsite(position: CampingTokenPosition) =
    positions.find { it.x == position.x && it.y == position.y }

fun Scene.getCampsites(): ExistingCampsites? =
    getAppFlag<Scene, ExistingCampsites?>("existing-campsites")
        ?.let(::deepClone)

suspend fun Scene.setCampsites(data: ExistingCampsites) {
    setAppFlag("existing-campsites", data)
}

suspend fun Scene.resetCampsites() {
    setCampsites(ExistingCampsites(positions = emptyArray()))
}


data class CampingTokenPosition(
    val x: Double,
    val y: Double,
)

fun PF2EParty.getTokenPosition(scene: Scene): CampingTokenPosition? =
    scene.tokens
        .find { it.actor is PF2EParty }
        ?.let { CampingTokenPosition(it.x, it.y) }

fun findExistingCampsiteResult(game: Game, sceneId: String, party: PF2EParty?): DegreeOfSuccess? =
    game.scenes.get(sceneId)
        ?.let { scene ->
            party
                ?.getTokenPosition(scene)
                ?.let { tokenPosition ->
                    scene.getCampsites()
                        ?.findExistingCampsite(tokenPosition)
                        ?.result
                        ?.let { fromCamelCase<DegreeOfSuccess>(it) }
                }
        }

suspend fun updateCampingPosition(game: Game, sceneId: String, result: DegreeOfSuccess, party: PF2EParty?) {
    val scene = game.scenes.get(sceneId)
    if (scene == null) return
    val tokenPosition = party?.getTokenPosition(scene)
    if (tokenPosition == null) return
    val campsites = scene.getCampsites() ?: ExistingCampsites(positions = emptyArray())
    val existingPosition = campsites.findExistingCampsite(tokenPosition)
    if (existingPosition == null) {
        campsites.positions = campsites.positions + CampsitePosition(
            x = tokenPosition.x,
            y = tokenPosition.y,
            result = result.toCamelCase(),
        )
    } else {
        existingPosition.result = result.toCamelCase()
    }
    scene.setCampsites(campsites)
}