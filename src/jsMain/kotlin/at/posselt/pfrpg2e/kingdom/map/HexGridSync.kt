package at.posselt.pfrpg2e.kingdom.map

import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.createDrawingsResilient
import at.posselt.pfrpg2e.utils.deleteDrawingsResilient
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.unsetAppFlag
import at.posselt.pfrpg2e.utils.getRealmTileData
import at.posselt.pfrpg2e.kingdom.getKingdomActors
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.isKingdomActor
import com.foundryvtt.core.Game
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.documents.DrawingDocument
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.onUpdateActor
import com.foundryvtt.core.documents.onUpdateScene
import com.foundryvtt.core.helpers.onCanvasReady
import com.foundryvtt.kingmaker.HexState
import com.foundryvtt.kingmaker.kingmaker
import com.foundryvtt.kingmaker.onCloseKingmakerHexEdit
import js.objects.ReadonlyRecord
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import kotlin.math.sqrt

@JsPlainObject
external interface ZoneLabelData {
    val zoneId: String
    val zoneLabel: String
}

fun DrawingDocument.getZoneLabelData(): ZoneLabelData? =
    getAppFlag("zoneLabel")

suspend fun DrawingDocument.setZoneLabelData(data: ZoneLabelData) {
    setAppFlag("zoneLabel", data)
}

suspend fun DrawingDocument.unsetZoneLabelData() {
    unsetAppFlag("zoneLabel")
}

fun registerHexGridSync(game: Game) {
    console.info("[pf2e-kmt] HexGridSync build: 2026-06-02 hexagons-migrate-v7")
    TypedHooks.onCloseKingmakerHexEdit { _, _ ->
        buildPromise {
            syncHexDrawingsToNativeState(game)
            syncSettlementMarkers(game)
            syncZoneLabels(game)
        }
    }

    TypedHooks.onUpdateActor { actor, _, _, _ ->
        if (actor.isKingdomActor()) {
            buildPromise {
                syncSettlementMarkers(game)
            }
        }
    }

    TypedHooks.onUpdateScene { _, _, _, _ ->
        buildPromise {
            syncSettlementMarkers(game)
        }
    }

    TypedHooks.onCanvasReady { _ ->
        buildPromise {
            syncSettlementMarkers(game)
            syncZoneLabels(game)
        }
    }
}

/**
 * Build a Foundry polygon ("p") shape for the grid hex at [offset], matching the hex's actual
 * six corners. Points are returned relative to the shape's top-left origin (`x`/`y`), which is
 * what `DrawingDocument` expects. Returns `{ x, y, width, height, points }`.
 */
private fun hexPolygonShape(grid: dynamic, offset: dynamic): dynamic =
    js(
        """(function (verts) {
            var minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
            verts.forEach(function (v) {
                if (v.x < minX) minX = v.x;
                if (v.y < minY) minY = v.y;
                if (v.x > maxX) maxX = v.x;
                if (v.y > maxY) maxY = v.y;
            });
            var points = [];
            verts.forEach(function (v) { points.push(v.x - minX); points.push(v.y - minY); });
            return { x: minX, y: minY, width: maxX - minX, height: maxY - minY, points: points };
        })(grid.getVertices(offset))"""
    )

/**
 * Find the managed hex overlay of [type] (claimed/explored/cleared) that belongs to the native
 * Kingmaker hex identified by [hexKey]. Overlays are matched on the hex key we stamp into their
 * realmTile flag at creation time — NOT on drawing geometry — so a single hex can carry several
 * overlay types simultaneously and each one is created, updated, and deleted independently.
 */
private fun findHexOverlay(
    drawings: Array<DrawingDocument>,
    type: String,
    hexKey: String,
): DrawingDocument? =
    drawings.find {
        val data = it.getRealmTileData()
        data?.type == type && data.hexKey == hexKey
    }

suspend fun syncHexDrawingsToNativeState(game: Game) {
    val activeScene = game.scenes.active
    console.info("[pf2e-kmt] ROADDBG entry: activeScene=", activeScene != null, "hexagonal=", activeScene?.grid?.isHexagonal)
    if (activeScene == null) return
    if (!activeScene.grid.isHexagonal) return

    val kingdomActor = game.getKingdomActors().firstOrNull()
    console.info("[pf2e-kmt] ROADDBG kingdomActor found=", kingdomActor != null)
    if (kingdomActor == null) return
    val kingdom = kingdomActor.getKingdom() ?: return

    val hexes = kingmaker.state.hexes

    // Migration: delete any of our hex overlays that the per-hex logic below can no longer
    // track, so it recreates them cleanly:
    //  - non-polygon ("r") overlays from builds that drew rectangles, and
    //  - overlays with no stamped `hexKey`, from builds that matched overlays by geometry/type.
    // Re-read the drawing list afterwards so the stale ones aren't seen as "already exists".
    val overlayTypes = setOf("claimed", EXPLORED_DRAWING_TYPE, CLEARED_DRAWING_TYPE)
    val staleOverlays = activeScene.drawings.contents.filter {
        val data = it.getRealmTileData()
        data?.type in overlayTypes && (it.asDynamic().shape?.type != "p" || data?.hexKey == null)
    }
    if (staleOverlays.isNotEmpty()) {
        activeScene.deleteDrawingsResilient(staleOverlays.map { it._id }.toTypedArray())
    }

    val activeDrawings = activeScene.drawings.contents

    for (key in js("Object.keys(hexes)").unsafeCast<Array<String>>()) {
        val hexState = hexes[key] ?: continue
        val hexObj = kingmaker.region.hexes.find { it.key.toString() == key } ?: continue
        val offset = hexObj.offset

        val claimed = hexState.claimed == true
        val existingClaimed = findHexOverlay(activeDrawings, "claimed", key)

        if (claimed) {
            if (existingClaimed == null) {
                val hex = hexPolygonShape(activeScene.grid, offset)
                val drawingData = js("""
                    {
                        x: hex.x,
                        y: hex.y,
                        shape: { type: "p", width: hex.width, height: hex.height, points: hex.points },
                        fillType: 1,
                        fillColor: "#00FF00",
                        fillAlpha: 0.2,
                        strokeWidth: 2,
                        strokeColor: "#00FF00",
                        strokeAlpha: 1,
                        flags: {
                            "pf2e-kingmaker-tools": {
                                "realmTile": {
                                    "type": "claimed",
                                    "kingdomActorUuid": kingdomActor.uuid,
                                    "hexKey": key
                                }
                            }
                        }
                    }
                """)
                activeScene.createDrawingsResilient(
                    arrayOf(drawingData.unsafeCast<com.foundryvtt.core.AnyObject>())
                )
            }
        } else {
            if (existingClaimed != null) {
                activeScene.deleteDrawingsResilient(arrayOf(existingClaimed._id))
            }
        }

        val explored = shouldHaveExploredDrawing(hexState.explored)
        val existingExplored = findHexOverlay(activeDrawings, EXPLORED_DRAWING_TYPE, key)

        if (explored) {
            if (existingExplored == null) {
                val hex = hexPolygonShape(activeScene.grid, offset)
                val drawingData = js("""
                    {
                        x: hex.x,
                        y: hex.y,
                        shape: { type: "p", width: hex.width, height: hex.height, points: hex.points },
                        fillType: 1,
                        fillColor: "$EXPLORED_FILL_COLOR",
                        fillAlpha: $EXPLORED_FILL_ALPHA,
                        strokeWidth: $EXPLORED_STROKE_WIDTH,
                        strokeColor: "$EXPLORED_STROKE_COLOR",
                        strokeAlpha: 1,
                        flags: {
                            "pf2e-kingmaker-tools": {
                                "realmTile": {
                                    "type": "$EXPLORED_DRAWING_TYPE",
                                    "kingdomActorUuid": kingdomActor.uuid,
                                    "hexKey": key
                                }
                            }
                        }
                    }
                """)
                activeScene.createDrawingsResilient(
                    arrayOf(drawingData.unsafeCast<com.foundryvtt.core.AnyObject>())
                )
            } else if (activeScene.drawings.get(existingExplored._id) != null) {
                // Update path: ensure existing explored drawing has correct visual.
                // Guard against a stale reference that an earlier loop iteration already deleted.
                existingExplored.update(
                    recordOf<String, Any?>(
                        "fillType" to 1,
                        "fillColor" to EXPLORED_FILL_COLOR,
                        "fillAlpha" to EXPLORED_FILL_ALPHA,
                        "strokeWidth" to EXPLORED_STROKE_WIDTH,
                        "strokeColor" to EXPLORED_STROKE_COLOR,
                    )
                ).await()
            }
        } else {
            if (existingExplored != null) {
                activeScene.deleteDrawingsResilient(arrayOf(existingExplored._id))
            }
        }

        val cleared = shouldHaveClearedDrawing(hexState.cleared)
        val existingCleared = findHexOverlay(activeDrawings, CLEARED_DRAWING_TYPE, key)

        if (cleared) {
            if (existingCleared == null) {
                // Cleared drawing: warm orange solid tint with an outline. All hex overlays use a
                // SOLID fill (fillType 1) on a real hex polygon; distinguished from claimed (green)
                // and explored (blue) by color.
                val hex = hexPolygonShape(activeScene.grid, offset)
                val drawingData = js("""
                    {
                        x: hex.x,
                        y: hex.y,
                        shape: { type: "p", width: hex.width, height: hex.height, points: hex.points },
                        fillType: 1,
                        fillColor: "$CLEARED_FILL_COLOR",
                        fillAlpha: $CLEARED_FILL_ALPHA,
                        strokeWidth: $CLEARED_STROKE_WIDTH,
                        strokeColor: "$CLEARED_STROKE_COLOR",
                        strokeAlpha: 1,
                        flags: {
                            "pf2e-kingmaker-tools": {
                                "realmTile": {
                                    "type": "$CLEARED_DRAWING_TYPE",
                                    "kingdomActorUuid": kingdomActor.uuid,
                                    "hexKey": key
                                }
                            }
                        }
                    }
                """)
                activeScene.createDrawingsResilient(
                    arrayOf(drawingData.unsafeCast<com.foundryvtt.core.AnyObject>())
                )
            } else if (activeScene.drawings.get(existingCleared._id) != null) {
                // Update path: ensure existing cleared drawing has correct visual.
                // Guard against a stale reference that an earlier loop iteration already deleted.
                existingCleared.update(
                    recordOf<String, Any?>(
                        "fillType" to 1,
                        "fillColor" to CLEARED_FILL_COLOR,
                        "fillAlpha" to CLEARED_FILL_ALPHA,
                        "strokeWidth" to CLEARED_STROKE_WIDTH,
                        "strokeColor" to CLEARED_STROKE_COLOR,
                    )
                ).await()
            }
        } else {
            if (existingCleared != null) {
                activeScene.deleteDrawingsResilient(arrayOf(existingCleared._id))
            }
        }

    }

    // Roads are global (they connect adjacent hexes), so they're synced once over the whole
    // hex set rather than per-hex.
    syncRoadDrawings(activeScene, hexes, kingdomActor.uuid, activeDrawings)
}

/**
 * Roads sync (global, not per-hex). Roads are a per-hex feature on HexState but render as line
 * segments connecting adjacent road hexes, so this is computed once over the whole hex set. We
 * collect all road hexes, derive the canonical set of adjacent road pairs, delete drawings whose
 * pair is no longer desired, and create drawings for newly desired pairs.
 */
private suspend fun syncRoadDrawings(
    activeScene: Scene,
    hexes: ReadonlyRecord<String, HexState>,
    kingdomActorUuid: String,
    activeDrawings: Array<DrawingDocument>,
) {
    val roadKeys = mutableSetOf<String>()
    val allFeatureTypesSeen = mutableSetOf<String?>()
    for (k in js("Object.keys(hexes)").unsafeCast<Array<String>>()) {
        val hs = hexes[k]
        hs?.features?.forEach { allFeatureTypesSeen.add(it.type) }
        if (hs != null && shouldHaveRoadDrawing(hs.features?.map { it.type })) {
            roadKeys.add(k)
        }
    }
    console.info(
        "[pf2e-kmt] ROADDBG featureTypesSeen=", allFeatureTypesSeen.toTypedArray(),
        "roadKeys=", roadKeys.toTypedArray(),
    )

    // Build canonical set of road pairs ("a,b" where a < b lexicographically)
    val desiredPairs = mutableSetOf<String>()
    val pairToPoints = mutableMapOf<String, Pair<dynamic, dynamic>>()
    for (rk in roadKeys) {
        val rkHex = kingmaker.region.hexes.find { it.key.toString() == rk } ?: continue
        val neighbors = rkHex.getNeighbors()
        for (neighbor in neighbors) {
            val neighborOffset = neighbor.offset
            val neighborKey = findHexKeyByOffset(neighborOffset, hexes) ?: continue
            if (neighborKey in roadKeys && neighborKey != rk) {
                val canonicalPair = if (rk < neighborKey) "$rk,$neighborKey" else "$neighborKey,$rk"
                if (canonicalPair !in desiredPairs) {
                    desiredPairs.add(canonicalPair)
                    val fromPoint = activeScene.grid.getCenterPoint(rkHex.offset)
                    val toPoint = activeScene.grid.getCenterPoint(neighborOffset)
                    pairToPoints[canonicalPair] = Pair(fromPoint, toPoint)
                }
            }
        }
    }

    console.info("[pf2e-kmt] ROADDBG desiredPairs=", desiredPairs.toTypedArray())

    // Delete stale road drawings (those whose pair key is no longer desired)
    val existingRoadDrawings = activeDrawings.filter { it.getRealmTileData()?.type == ROAD_DRAWING_TYPE }
    for (erd in existingRoadDrawings) {
        val pairKey: String? = erd.getAppFlag("roadPairKey")
        if (pairKey == null || pairKey !in desiredPairs) {
            activeScene.deleteDrawingsResilient(arrayOf(erd._id))
        }
    }

    // Create or update road drawings for desired pairs
    val refreshedDrawings = activeScene.drawings.contents
    for (pairKey in desiredPairs) {
        val (fromPoint, toPoint) = pairToPoints[pairKey] ?: continue
        val existingRoad = refreshedDrawings.find {
            if (it.getRealmTileData()?.type != ROAD_DRAWING_TYPE) return@find false
            val roadPair: String? = it.getAppFlag("roadPairKey")
            roadPair == pairKey
        }

        // Build a thin rectangle polygon between the two hex centers. Foundry's polygon
        // shape expects `points` as a FLAT number array [x1,y1,x2,y2,...] relative to the
        // drawing's (x, y) origin — not an array of [x,y] pairs.
        val fx = fromPoint.x.unsafeCast<Double>()
        val fy = fromPoint.y.unsafeCast<Double>()
        val tx = toPoint.x.unsafeCast<Double>()
        val ty = toPoint.y.unsafeCast<Double>()
        val dx = tx - fx
        val dy = ty - fy
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0.0) continue
        val nx = -dy / len * (ROAD_STROKE_WIDTH / 2.0)
        val ny = dx / len * (ROAD_STROKE_WIDTH / 2.0)

        // Four corners of the thin rectangle in absolute scene coordinates.
        val c0x = fx + nx; val c0y = fy + ny
        val c1x = tx + nx; val c1y = ty + ny
        val c2x = tx - nx; val c2y = ty - ny
        val c3x = fx - nx; val c3y = fy - ny
        val originX = minOf(c0x, c1x, c2x, c3x)
        val originY = minOf(c0y, c1y, c2y, c3y)
        val width = maxOf(c0x, c1x, c2x, c3x) - originX
        val height = maxOf(c0y, c1y, c2y, c3y) - originY
        val flatPoints = arrayOf(
            c0x - originX, c0y - originY,
            c1x - originX, c1y - originY,
            c2x - originX, c2y - originY,
            c3x - originX, c3y - originY,
        )

        if (existingRoad == null) {
            val drawingData = recordOf(
                "x" to originX,
                "y" to originY,
                "shape" to recordOf(
                    "type" to "p",
                    "width" to width,
                    "height" to height,
                    "points" to flatPoints,
                ),
                "fillType" to 0,
                "fillAlpha" to 0.0,
                "strokeWidth" to ROAD_STROKE_WIDTH,
                "strokeColor" to ROAD_STROKE_COLOR,
                "strokeAlpha" to 1.0,  // keep the line visible so the drawing passes joint validation
                "flags" to recordOf(
                    "pf2e-kingmaker-tools" to recordOf(
                        "realmTile" to recordOf(
                            "type" to ROAD_DRAWING_TYPE,
                            "kingdomActorUuid" to kingdomActorUuid,
                        ),
                        "roadPairKey" to pairKey,
                    ),
                ),
            ).unsafeCast<com.foundryvtt.core.AnyObject>()
            console.info("[pf2e-kmt] ROADDBG creating road drawing for pair=", pairKey)
            activeScene.createDrawingsResilient(
                arrayOf(drawingData)
            )
        }
        // An existing road drawing for this pair needs no update — its stroke width/color
        // are static constants, so re-updating it only risks racing a concurrent delete.
    }
}

/**
 * Find the hex key in the hexes record that matches the given grid offset.
 */
private fun findHexKeyByOffset(
    offset: dynamic,
    hexes: dynamic,
): String? {
    val keys = js("Object.keys(hexes)").unsafeCast<Array<String>>()
    for (k in keys) {
        val hs = hexes[k]
        if (hs != null) {
            val hexObj = kingmaker.region.hexes.find { it.key.toString() == k }
            if (hexObj != null && hexObj.offset.i == offset.i && hexObj.offset.j == offset.j) {
                return k
            }
        }
    }
    return null
}

suspend fun syncSettlementMarkers(game: Game) {
    val activeScene = game.scenes.active ?: return
    if (!activeScene.grid.isHexagonal) return

    val kingdomActor = game.getKingdomActors().firstOrNull() ?: return
    val kingdom = kingdomActor.getKingdom() ?: return
    val settlements = kingdom.settlements

    val activeDrawings = activeScene.drawings.contents
    val validSettlementIds = settlements.map { it.sceneId }.toSet()

    // 1. Delete drawings for deleted settlements
    for (drawing in activeDrawings) {
        val realmData = drawing.getRealmTileData()
        if (realmData?.type == "settlement") {
            val sId = realmData.settlementId
            if (sId == null || sId !in validSettlementIds) {
                activeScene.deleteDrawingsResilient(arrayOf(drawing._id))
            }
        }
    }

    // 2. Add or update drawings for active settlements
    for (settlement in settlements) {
        val settlementScene = game.scenes.get(settlement.sceneId) ?: continue
        val settlementName = settlementScene.name

        // Text-only markers fail Foundry's joint validation when the text is empty.
        if (settlementName.isNullOrBlank()) continue

        // Find hex by matching name (case-insensitive)
        val matchingHex = kingmaker.region.hexes.find {
            it.name.lowercase().trim() == settlementName.lowercase().trim()
        } ?: continue

        val offset = matchingHex.offset
        val point = activeScene.grid.getCenterPoint(offset)

        val existingDrawing = activeDrawings.find { d ->
            val realmData = d.getRealmTileData()
            realmData?.type == "settlement" && realmData.settlementId == settlement.sceneId
        }

        val boxWidth = 300
        val boxHeight = 60
        val x = point.x - boxWidth / 2
        val y = point.y - boxHeight / 2

        if (existingDrawing == null) {
            val drawingData = recordOf(
                "shape" to recordOf(
                    "type" to "r",
                    "width" to boxWidth,
                    "height" to boxHeight,
                ),
                "height" to boxHeight,
                "width" to boxWidth,
                "locked" to true,
                "x" to x,
                "y" to y,
                "text" to settlementName,
                "textAlpha" to 1,
                "fontSize" to 24,
                "textColor" to "#FFD700",
                "strokeAlpha" to 0,
                "fillAlpha" to 0,
                "flags" to recordOf(
                    "pf2e-kingmaker-tools" to recordOf(
                        "realmTile" to recordOf(
                            "type" to "settlement",
                            "kingdomActorUuid" to kingdomActor.uuid,
                            "settlementId" to settlement.sceneId
                        )
                    )
                )
            ).unsafeCast<com.foundryvtt.core.AnyObject>()

            activeScene.createDrawingsResilient(
                arrayOf(drawingData)
            )
        } else {
            // Verify and update if name has changed
            val currentText = existingDrawing.asDynamic().text.unsafeCast<String?>()
            if (currentText != settlementName && activeScene.drawings.get(existingDrawing._id) != null) {
                existingDrawing.update(
                    recordOf<String, Any?>("text" to settlementName)
                ).await()
            }
        }
    }
}

suspend fun syncZoneLabels(game: Game) {
    val activeScene = game.scenes.active ?: return
    if (!activeScene.grid.isHexagonal) return

    val kingdomActor = game.getKingdomActors().firstOrNull() ?: return
    val kingdom = kingdomActor.getKingdom() ?: return

    val activeDrawings = activeScene.drawings.contents

    // 1. Remove existing zone label drawings
    for (drawing in activeDrawings) {
        if (drawing.getZoneLabelData() != null) {
            activeScene.deleteDrawingsResilient(arrayOf(drawing._id))
        }
    }

    // 2. Group hexes by zone and create labels at the center of each zone
    val allHexes = kingmaker.region.hexes.contents
    val grouped = allHexes.groupBy { it.zone.id }

    for ((zoneId, hexList) in grouped) {
        if (hexList.isEmpty()) continue

        val zoneLabel = hexList[0].zone.label
        val zoneColor = hexList[0].zone.color

        // A drawing with no text, no fill and no stroke fails Foundry's joint
        // validation ("must have visible text, a visible fill, or a visible line").
        // These labels render text only, so skip zones without a usable label.
        if (zoneLabel.isBlank()) continue

        // Compute center point of all hexes in this zone
        var sumX = 0.0
        var sumY = 0.0
        for (hex in hexList) {
            val point = activeScene.grid.getCenterPoint(hex.offset)
            sumX += point.x
            sumY += point.y
        }
        val centerX = sumX / hexList.size
        val centerY = sumY / hexList.size

        val labelWidth = 300.0
        val labelHeight = 50.0

        // Create zone label drawing
        val drawingData = recordOf(
            "shape" to recordOf(
                "type" to "r",
                "width" to labelWidth,
                "height" to labelHeight,
            ),
            "height" to labelHeight,
            "width" to labelWidth,
            "locked" to true,
            "x" to centerX - labelWidth / 2,
            "y" to centerY - labelHeight / 2,
            "text" to zoneLabel,
            "textAlpha" to 1,
            "fontSize" to 28,
            "fontFamily" to "Signika",
            "textColor" to zoneColor,
            "strokeAlpha" to 0,
            "fillAlpha" to 0,
            "flags" to recordOf(
                "pf2e-kingmaker-tools" to recordOf(
                    "zoneLabel" to recordOf(
                        "zoneId" to zoneId,
                        "zoneLabel" to zoneLabel,
                    )
                )
            )
        ).unsafeCast<com.foundryvtt.core.AnyObject>()

        activeScene.createDrawingsResilient(
            arrayOf(drawingData)
        )
    }
}

// Re-export from commonMain so jsMain callers and tests share the same source
// (definition lives in commonMain/kotlin/.../map/HexDrawingHelpers.kt)
