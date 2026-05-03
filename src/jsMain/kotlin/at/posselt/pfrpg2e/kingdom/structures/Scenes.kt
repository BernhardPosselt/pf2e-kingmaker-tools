package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.isKingmakerInstalled
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.SettlementTerrain
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.SettlementData
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateSettlement
import at.posselt.pfrpg2e.kingdom.scenes.toRectangle
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.TextureData
import com.foundryvtt.core.documents.TileDocument
import com.foundryvtt.core.documents.TokenDocument
import js.collections.JsSet
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlin.math.max

private fun Scene.structureTokens(): List<TokenDocument> =
    tokens.contents
        .asSequence()
        .filter {
            val actor = it.actor
            actor is StructureActor && actor.isStructure() && !it.hidden
        }
        .toList()

private fun Scene.getStructures(): List<Structure> =
    tokens.contents
        .asSequence()
        .mapNotNull { it.actor }
        .filterIsInstance<StructureActor>()
        .mapNotNull { it.parseStructure() }
        .toList()

private val TileDocument.isBlock: Boolean
    get() = getAppFlag<TileDocument, Any?>("settlementBlockDrawing") != null

private fun Scene.getBlockTiles() =
    tiles.contents
        .asSequence()
        .filter { it.isBlock && !it.hidden }

private fun Scene.calculateOccupiedBlocks(): Int {
    val sceneGridSize = grid.size.toDouble()
    val structures = structureTokens()
        .filterNot {
            val actor = it.actor
            actor is StructureActor && actor.isSlowed()
        }
        .map { it.toRectangle(sceneGridSize, sceneGridSize) }
    val blocks = getBlockTiles().map { it.toRectangle().applyTolerance(50.0) }
    return blocks
        .filter { block -> structures.any { it in block } }
        .count()
}

fun Scene.parseSettlement(
    rawSettlement: RawSettlement,
    autoCalculateSettlementLevel: Boolean,
    allStructuresStack: Boolean,
    allowCapitalInvestmentInCapitalWithoutBank: Boolean,
    capStructureBonusAtKingdomLevel: Boolean,
    kingdomLevel: Int,
): Settlement {
    val occupiedBlocks = if (autoCalculateSettlementLevel && rawSettlement.manualSettlementLevel != true) max(
        0,
        calculateOccupiedBlocks()
    ) else rawSettlement.lots
    return evaluateSettlement(
        data = SettlementData(
            id = rawSettlement.sceneId,
            name = name,
            occupiedBlocks = occupiedBlocks,
            type = SettlementType.fromString(rawSettlement.type)
                ?: SettlementType.SETTLEMENT,
            isSecondaryTerritory = rawSettlement.secondaryTerritory,
            waterBorders = rawSettlement.waterBorders,
        ),
        structures = getStructures(),
        allStructuresStack = allStructuresStack,
        allowCapitalInvestmentInCapitalWithoutBank = allowCapitalInvestmentInCapitalWithoutBank,
        capStructureBonusAtKingdomLevel = capStructureBonusAtKingdomLevel,
        kingdomLevel = kingdomLevel,
    )
}

suspend fun Game.importSettlementScene(
    uuid: String,
    sceneName: String,
    terrain: SettlementTerrain,
    waterBorders: Int,
): Scene {
    val bg = if (isKingmakerInstalled) {
        "modules/pf2e-kingmaker/assets/maps-settlements/maps/${terrain.value}-${waterBorders}a.webp"
    } else {
        "modules/pf2e-kingmaker-tools/img/settlements/backgrounds/${terrain.value}-${waterBorders}.webp"
    }
    return createScene(sceneName, bg, 4000, 4000)
}

enum class SettlementBlockShape(
    val background: String,
    val squaresX: Int,
    val squaresY: Int,
): Translatable, ValueEnum {
    TWO_BY_TWO("modules/pf2e-kingmaker-tools/img/settlements/block.webp", 2, 2),
    ONE_BY_FOUR("modules/pf2e-kingmaker-tools/img/settlements/4x1.webp", 1, 4),
    FOUR_BY_ONE("modules/pf2e-kingmaker-tools/img/settlements/1x4.webp", 4, 1);

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementBlockShape>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "settlementBlockShape.$value"
}

suspend fun Scene.createSettlementBlock(
    tile: SettlementBlockShape,
    squareX: Int = 11,
    squareY: Int = 11,
): TileDocument {
    val data = recordOf(
        "x" to squareX * grid.size,
        "y" to squareY * grid.size,
        "width" to grid.size * tile.squaresX,
        "height" to grid.size * tile.squaresY,
        "levels" to JsSet(arrayOf("defaultLevel0000")),
        "texture" to TextureData(
            src = tile.background,
            anchorX = 0,
            anchorY = 0,
            tint = 16777215,
            alphaThreshold = .75,
            fit = "fill",
            scaleX = 1.0,
            scaleY = 1.0,
        ),
        "flags" to recordOf(
            Config.moduleId to recordOf(
                "settlementBlockDrawing" to recordOf(
                    "isSettlementBlock" to true,
                )
            )
        )
    ).unsafeCast<AnyObject>()
    return createEmbeddedDocuments<TileDocument>("Tile", arrayOf(data)).await().first()
}

suspend fun createScene(
    name: String,
    background: String,
    height: Int,
    width: Int,
): Scene {
    val scene = Scene.create(
        recordOf(
            "name" to name,
            "permission" to 0,
            "navigation" to true,
            "ownership" to recordOf("default" to 2),
            "width" to width,
            "height" to height,
            "padding" to 0,
        )
    ).await()
    scene.firstLevel.update(recordOf("background.src" to background)).await()
    scene.createSettlementBlock(SettlementBlockShape.TWO_BY_TWO)
    val thumbnail = scene.createThumbnail().await()
    scene.typeSafeUpdate { thumb = thumbnail.thumb }
    return scene
}