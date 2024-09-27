package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actor.party
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2EParty
import js.array.push
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CampsitePosition {
    val x: Int
    val y: Int
    var result: String
}

@JsPlainObject
external interface ExistingCampsites {
    val positions: Array<CampsitePosition>
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
    val x: Int,
    val y: Int,
)

fun PF2EParty.getTokenPosition(scene: Scene): CampingTokenPosition? =
    scene.tokens
        .find { it.actor is PF2EParty }
        ?.let { CampingTokenPosition(it.x, it.y) }

fun findExistingCampsiteResult(game: Game, sceneId: String): DegreeOfSuccess? =
    game.scenes.get(sceneId)
        ?.let { scene ->
            game.party()
                ?.getTokenPosition(scene)
                ?.let { tokenPosition ->
                    scene.getCampsites()
                        ?.findExistingCampsite(tokenPosition)
                        ?.result
                        ?.let { fromCamelCase<DegreeOfSuccess>(it) }
                }
        }

suspend fun updateCampingPosition(game: Game, sceneId: String, result: DegreeOfSuccess) {
    val scene = game.scenes.get(sceneId)
    if (scene == null) return
    val tokenPosition = game.party()?.getTokenPosition(scene)
    if (tokenPosition == null) return
    val campsites = scene.getCampsites() ?: ExistingCampsites(positions = emptyArray())
    val existingPosition = campsites.findExistingCampsite(tokenPosition)
    if (existingPosition == null) {
        campsites.positions.push(
            CampsitePosition(
                x = tokenPosition.x,
                y = tokenPosition.y,
                result = result.toCamelCase(),
            )
        )
    } else {
        existingPosition.result = result.toCamelCase()
    }
    scene.setCampsites(campsites)
}