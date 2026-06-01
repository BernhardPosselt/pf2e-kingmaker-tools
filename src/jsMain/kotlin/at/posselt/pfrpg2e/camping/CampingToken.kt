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

/**
 * A Kingmaker map zone.
 *
 * @param id the abbreviation used by the Kingmaker module's region map (e.g. "BV")
 * @param zoneNumber the padded zone number used in the default region name (e.g. "00")
 * @param zoneName the canonical Kingmaker zone name (e.g. "Brevoy")
 */
private data class KingmakerZone(
    val id: String,
    val zoneNumber: String,
    val zoneName: String,
) {
    /** Must match the default region name produced by `t("camping.zone", ...)`. */
    val regionName: String get() = "Zone $zoneNumber"
}

private val kingmakerZones = listOf(
    KingmakerZone("BV", "00", "Brevoy"),
    KingmakerZone("RL", "01", "Rostland Hinterlands"),
    KingmakerZone("GB", "02", "Greenbelt"),
    KingmakerZone("TW", "03", "Tuskwater"),
    KingmakerZone("KL", "04", "Kamelands"),
    KingmakerZone("NM", "05", "Narlmarches"),
    KingmakerZone("SH", "06", "Sellen Hills"),
    KingmakerZone("DS", "07", "Dunsward"),
    KingmakerZone("NH", "08", "Nomen Heights"),
    KingmakerZone("LV", "09", "Tor of Levenies"),
    KingmakerZone("HT", "10", "Hooktongue"),
    KingmakerZone("DR", "11", "Drelev"),
    KingmakerZone("TL", "12", "Tiger Lords"),
    KingmakerZone("RU", "13", "Rushlight"),
    KingmakerZone("GL", "14", "Glenebon Lowlands"),
    KingmakerZone("PX", "15", "Pitax"),
    KingmakerZone("GU", "16", "Glenebon Uplands"),
    KingmakerZone("NU", "17", "Numeria"),
    KingmakerZone("TV", "18", "Thousand Voices"),
    KingmakerZone("BR", "19", "Branthlend Mountains"),
)

/** Maps a zone abbreviation to the region names that should select it on token move. */
private val kingmakerRegions: Map<String, Set<String>> =
    kingmakerZones.associate { it.id to setOf(it.regionName, it.zoneName) }

/**
 * Maps a stored region name (e.g. "Zone 00") to its canonical Kingmaker zone name
 * (e.g. "Brevoy") so the camping region dropdown can show the zone name.
 */
val kingmakerZoneNamesByRegionName: Map<String, String> =
    kingmakerZones.associate { it.regionName to it.zoneName }

/**
 * Build the label shown in the camping region dropdown for a stored [regionName].
 *
 * Default Kingmaker zones (e.g. "Zone 00") are enriched with their canonical zone name
 * ("Zone 00 - Brevoy"); custom/renamed regions are returned unchanged.
 */
fun regionDropdownLabel(regionName: String): String {
    val zoneName = kingmakerZoneNamesByRegionName[regionName.trim()]
    return if (zoneName == null) regionName else "$regionName - $zoneName"
}