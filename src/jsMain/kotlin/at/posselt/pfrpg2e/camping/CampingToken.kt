package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actor.isKingmakerInstalled
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.isFirstGM
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.documents.onMoveToken
import com.foundryvtt.core.grid.GridOffset2D
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.kingmaker.kingmaker
import com.foundryvtt.pf2e.actor.PF2EParty
import com.pixijs.Point
import kotlinx.js.JsPlainObject
import kotlin.js.Promise
import kotlin.math.abs

fun registerCampingTokenMove(game: Game) {
    if (game.isFirstGM() && game.isKingmakerInstalled) {
        TypedHooks.onMoveToken { document, changed, _, _ ->
            val actor = document.actor
            val hexScene = game.scenes.get(stolenLandsId)
            if (actor is PF2EParty && hexScene != null && game.scenes.current?.id == stolenLandsId) {
                actor.getCamping()?.let { camping ->
                    val point = Point(x = changed.destination.x, y = changed.destination.y)
                    val offset = hexScene.grid.getOffset(point)
                    val abbreviatedZone = findKingmakerHexRegion(offset)
                    val zoneNames = kingmakerRegions[abbreviatedZone].orEmpty()
                    camping.regionSettings.regions
                        .find { it.name.trim() in zoneNames }
                        ?.let {
                            camping.currentRegion = it.name
                        }
                    buildPromise {
                        actor.setCamping(camping)
                    }
                }
            }
        }
    }
}

@JsPlainObject
external interface TokenEnterEventData {
    val token: TokenDocument
}

@JsPlainObject
external interface TokenEnterEvent {
    val data: TokenEnterEventData
}

@JsExport
fun updateCampingRegion(event: TokenEnterEvent, region: String): Promise<Unit> {
    val actor = event.data.token.actor
    return buildPromise {
        if (actor is PF2EParty) {
            actor.getCamping()?.let {
                it.currentRegion = region
                actor.setCamping(it)
            }
        }
    }
}

private fun findKingmakerHexRegion(offset: GridOffset2D): String? {
    // kingmaker hexes start at i 0 and not -1 so we need to add 1
    // furthermore, all uneven rows need to be shifted one right
    val offsetJ = if (abs(offset.i) % 2 == 1) 1 else 0
    return kingmaker.region.hexes
        .find { it.offset.i == (offset.i + 1) && it.offset.j == (offset.j + offsetJ) }
        ?.zone
        ?.id
}

private const val stolenLandsId = "AJ1k5II28u72JOmz"

private val kingmakerRegions = mapOf(
    "BV" to setOf("Zone 00", "Brevoy"),
    "RL" to setOf("Zone 01", "Rostland Hinterlands"),
    "GB" to setOf("Zone 02", "Greenbelt"),
    "TW" to setOf("Zone 03", "Tuskwater"),
    "KL" to setOf("Zone 04", "Kamelands"),
    "NM" to setOf("Zone 05", "Narlmarches"),
    "SH" to setOf("Zone 06", "Sellen Hills"),
    "DS" to setOf("Zone 07", "Dunsward"),
    "NH" to setOf("Zone 08", "Nomen Heights"),
    "LV" to setOf("Zone 09", "Tor of Levenies"),
    "HT" to setOf("Zone 10", "Hooktongue"),
    "DR" to setOf("Zone 11", "Drelev"),
    "TL" to setOf("Zone 12", "Tiger Lords"),
    "RU" to setOf("Zone 13", "Rushlight"),
    "GL" to setOf("Zone 14", "Glenebon Lowlands"),
    "PX" to setOf("Zone 15", "Pitax"),
    "GU" to setOf("Zone 16", "Glenebon Uplands"),
    "NU" to setOf("Zone 17", "Numeria"),
    "TV" to setOf("Zone 18", "Thousand Voices"),
    "BR" to setOf("Zone 19", "Branthlend Mountains"),
)