package at.posselt.pfrpg2e.kingdom.map

import at.posselt.pfrpg2e.utils.buildPromise
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
import com.foundryvtt.core.documents.onUpdateActor
import com.foundryvtt.core.documents.onUpdateScene
import com.foundryvtt.core.helpers.onCanvasReady
import com.foundryvtt.kingmaker.kingmaker
import com.foundryvtt.kingmaker.onCloseKingmakerHexEdit
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

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

suspend fun syncHexDrawingsToNativeState(game: Game) {
    val activeScene = game.scenes.active ?: return
    if (!activeScene.grid.isHexagonal) return

    val kingdomActor = game.getKingdomActors().firstOrNull() ?: return
    val kingdom = kingdomActor.getKingdom() ?: return

    val hexes = kingmaker.state.hexes
    val activeDrawings = activeScene.drawings.contents

    for (key in js("Object.keys(hexes)").unsafeCast<Array<String>>()) {
        val hexState = hexes[key] ?: continue
        val hexObj = kingmaker.region.hexes.find { it.key.toString() == key } ?: continue
        val offset = hexObj.offset

        val point = activeScene.grid.getCenterPoint(offset)
        val drawing = activeDrawings.find { d ->
            val dOffset = activeScene.grid.getOffset(js("{ x: d.x, y: d.y }"))
            dOffset.i == offset.i && dOffset.j == offset.j
        }

        val claimed = hexState.claimed == true

        if (claimed) {
            if (drawing == null) {
                val shapeData = js("""
                    {
                        type: "p",
                        width: activeScene.grid.size,
                        height: activeScene.grid.size
                    }
                """)
                val drawingData = js("""
                    {
                        x: point.x - activeScene.grid.size / 2,
                        y: point.y - activeScene.grid.size / 2,
                        shape: shapeData,
                        fillType: 1,
                        fillColor: "#00FF00",
                        fillAlpha: 0.2,
                        strokeWidth: 2,
                        strokeColor: "#00FF00",
                        flags: {
                            "pf2e-kingmaker-tools": {
                                "realmTile": {
                                    "type": "claimed",
                                    "kingdomActorUuid": kingdomActor.uuid
                                }
                            }
                        }
                    }
                """)
                activeScene.createEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(drawingData.unsafeCast<com.foundryvtt.core.AnyObject>())
                ).await()
            }
        } else {
            if (drawing != null && drawing.getRealmTileData()?.type == "claimed") {
                activeScene.deleteEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(drawing._id)
                ).await()
            }
        }

        val explored = shouldHaveExploredDrawing(hexState.explored)

        if (explored) {
            val existingExplored = activeDrawings.find { it.getRealmTileData()?.type == EXPLORED_DRAWING_TYPE }
            if (existingExplored == null) {
                val shapeData = js("""
                    {
                        type: "p",
                        width: activeScene.grid.size,
                        height: activeScene.grid.size
                    }
                """)
                val drawingData = js("""
                    {
                        x: point.x - activeScene.grid.size / 2,
                        y: point.y - activeScene.grid.size / 2,
                        shape: shapeData,
                        fillType: 2,
                        fillColor: "$EXPLORED_FILL_COLOR",
                        fillAlpha: $EXPLORED_FILL_ALPHA,
                        strokeWidth: $EXPLORED_STROKE_WIDTH,
                        strokeColor: "$EXPLORED_STROKE_COLOR",
                        strokeDashArray: [8, 4],
                        flags: {
                            "pf2e-kingmaker-tools": {
                                "realmTile": {
                                    "type": "$EXPLORED_DRAWING_TYPE",
                                    "kingdomActorUuid": kingdomActor.uuid
                                }
                            }
                        }
                    }
                """)
                activeScene.createEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(drawingData.unsafeCast<com.foundryvtt.core.AnyObject>())
                ).await()
            } else {
                // Update path: ensure existing explored drawing has correct visual
                existingExplored.update(
                    recordOf<String, Any?>(
                        "fillType" to 2,
                        "fillColor" to EXPLORED_FILL_COLOR,
                        "fillAlpha" to EXPLORED_FILL_ALPHA,
                        "strokeWidth" to EXPLORED_STROKE_WIDTH,
                        "strokeColor" to EXPLORED_STROKE_COLOR,
                    )
                ).await()
            }
        } else {
            val existingExplored = activeDrawings.find { it.getRealmTileData()?.type == EXPLORED_DRAWING_TYPE }
            if (existingExplored != null) {
                activeScene.deleteEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(existingExplored._id)
                ).await()
            }
        }

        val cleared = shouldHaveClearedDrawing(hexState.cleared)

        if (cleared) {
            val existingCleared = activeDrawings.find { it.getRealmTileData()?.type == CLEARED_DRAWING_TYPE }
            if (existingCleared == null) {
                val shapeData = js("""
                    {
                        type: "p",
                        width: activeScene.grid.size,
                        height: activeScene.grid.size
                    }
                """)
                // Cleared drawing: warm orange with dotted outline (strokeDashArray [2,4]).
                // Distinct from claimed (solid green fill, fillType 1) and
                // explored (dashed blue outline, fillType 2, strokeDashArray [8,4]).
                val drawingData = js("""
                    {
                        x: point.x - activeScene.grid.size / 2,
                        y: point.y - activeScene.grid.size / 2,
                        shape: shapeData,
                        fillType: 2,
                        fillColor: "$CLEARED_FILL_COLOR",
                        fillAlpha: $CLEARED_FILL_ALPHA,
                        strokeWidth: $CLEARED_STROKE_WIDTH,
                        strokeColor: "$CLEARED_STROKE_COLOR",
                        strokeDashArray: [2, 4],
                        flags: {
                            "pf2e-kingmaker-tools": {
                                "realmTile": {
                                    "type": "$CLEARED_DRAWING_TYPE",
                                    "kingdomActorUuid": kingdomActor.uuid
                                }
                            }
                        }
                    }
                """)
                activeScene.createEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(drawingData.unsafeCast<com.foundryvtt.core.AnyObject>())
                ).await()
            } else {
                // Update path: ensure existing cleared drawing has correct visual
                existingCleared.update(
                    recordOf<String, Any?>(
                        "fillType" to 2,
                        "fillColor" to CLEARED_FILL_COLOR,
                        "fillAlpha" to CLEARED_FILL_ALPHA,
                        "strokeWidth" to CLEARED_STROKE_WIDTH,
                        "strokeColor" to CLEARED_STROKE_COLOR,
                    )
                ).await()
            }
        } else {
            val existingCleared = activeDrawings.find { it.getRealmTileData()?.type == CLEARED_DRAWING_TYPE }
            if (existingCleared != null) {
                activeScene.deleteEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(existingCleared._id)
                ).await()
            }
        }
    }
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
                activeScene.deleteEmbeddedDocuments<DrawingDocument>(
                    "Drawing",
                    arrayOf(drawing._id)
                ).await()
            }
        }
    }

    // 2. Add or update drawings for active settlements
    for (settlement in settlements) {
        val settlementScene = game.scenes.get(settlement.sceneId) ?: continue
        val settlementName = settlementScene.name

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

            activeScene.createEmbeddedDocuments<DrawingDocument>(
                "Drawing",
                arrayOf(drawingData)
            ).await()
        } else {
            // Verify and update if name has changed
            val currentText = existingDrawing.asDynamic().text.unsafeCast<String?>()
            if (currentText != settlementName) {
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
            activeScene.deleteEmbeddedDocuments<DrawingDocument>(
                "Drawing",
                arrayOf(drawing._id)
            ).await()
        }
    }

    // 2. Group hexes by zone and create labels at the center of each zone
    val allHexes = kingmaker.region.hexes.contents
    val grouped = allHexes.groupBy { it.zone.id }

    for ((zoneId, hexList) in grouped) {
        if (hexList.isEmpty()) continue

        val zoneLabel = hexList[0].zone.label
        val zoneColor = hexList[0].zone.color

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

        activeScene.createEmbeddedDocuments<DrawingDocument>(
            "Drawing",
            arrayOf(drawingData)
        ).await()
    }
}

// Re-export from commonMain so jsMain callers and tests share the same source
// (definition lives in commonMain/kotlin/.../map/HexDrawingHelpers.kt)
