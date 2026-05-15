package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.actor.isKingmakerInstalled
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.kingdom.settlements.Block
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.SettlementTerrain
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.SettlementData
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateSettlement
import at.posselt.pfrpg2e.kingdom.scenes.GridType
import at.posselt.pfrpg2e.kingdom.scenes.toRectangle
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.DrawingDocument
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.ShapeData
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

private fun isSlowedOrInfrastructure(actor: StructureActor): Boolean {
    val structure = actor.parseStructure() ?: return false
    return structure.slowed || structure.isInfrastructure
}

private fun Scene.getNonInfrastructureBlocks(): List<Block> {
    val sceneGridSize = grid.size.toDouble()
    val structuresAndRectangles = structureTokens()
        .mapNotNull {
            val actor = it.actor
            if (actor !is StructureActor) return@mapNotNull null
            val structure = actor.parseStructure() ?: return@mapNotNull null
            if (structure.isInfrastructure) return@mapNotNull null
            structure to it.toRectangle(sceneGridSize, sceneGridSize)
        }
    val blockTiles = getBlockTiles()
        .map { it.toRectangle(GridType.SQUARE).applyTolerance(50.0) }
    return blockTiles.map { blockTile ->
        val structuresInBlock = structuresAndRectangles
            .filter { it.component2() in blockTile }
            .map { it.component1() }
        Block(
            delayedStructures = structuresInBlock.filter { it.slowed },
            constructedStructures = structuresInBlock.filter { !it.slowed && it.rpPaid },
            structuresUnderConstruction = structuresInBlock.filter { !it.slowed && !it.rpPaid },
        )
    }.toList()
}

fun Scene.parseSettlement(
    rawSettlement: RawSettlement,
    autoCalculateSettlementLevel: Boolean,
    allStructuresStack: Boolean,
    allowCapitalInvestmentInCapitalWithoutBank: Boolean,
    capStructureBonusAtKingdomLevel: Boolean,
    kingdomLevel: Int,
): Settlement {
    val blocks = getNonInfrastructureBlocks()
    val occupiedBlocks = if (autoCalculateSettlementLevel && rawSettlement.manualSettlementLevel != true) max(
        0,
        blocks.filter { it.isOccupied }.size
    ) else rawSettlement.lots
    val structures = getStructures()
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
        structures = structures,
        allStructuresStack = allStructuresStack,
        allowCapitalInvestmentInCapitalWithoutBank = allowCapitalInvestmentInCapitalWithoutBank,
        capStructureBonusAtKingdomLevel = capStructureBonusAtKingdomLevel,
        kingdomLevel = kingdomLevel,
        blocks = blocks,
    )
}

suspend fun Game.importSettlementScene(
    sceneName: String,
    terrain: SettlementTerrain,
    waterBorders: Int,
): Scene {
    val bg = if (isKingmakerInstalled) {
        val type = if (waterBorders == 1) "c" else "a"
        "modules/pf2e-kingmaker/assets/maps-settlements/maps/${terrain.value}-${waterBorders}$type.webp"
    } else {
        "modules/pf2e-kingmaker-tools/img/settlements/backgrounds/${terrain.value}-${waterBorders}.webp"
    }
    return createScene(sceneName, bg, 4000, 4000, waterBorders)
}

interface BlockShape {
    val background: String
    val squaresX: Int
    val squaresY: Int
}


enum class SettlementBlockShape(
    override val background: String,
    override val squaresX: Int,
    override val squaresY: Int,
) : Translatable, ValueEnum, BlockShape {
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

enum class InfrastructureBlockShape(
    override val background: String,
    override val squaresX: Int,
    override val squaresY: Int,
) : BlockShape {
    ONE_BY_ONE("modules/pf2e-kingmaker-tools/img/settlements/1x1.webp", 1, 1),
    THREE_BY_ONE("modules/pf2e-kingmaker-tools/img/settlements/3x1.webp", 1, 3),
    ONE_BY_THREE("modules/pf2e-kingmaker-tools/img/settlements/1x3.webp", 3, 1),
    TWO_BY_ONE("modules/pf2e-kingmaker-tools/img/settlements/2x1.webp", 1, 2),
    ONE_BY_TWO("modules/pf2e-kingmaker-tools/img/settlements/1x2.webp", 2, 1);
}

suspend fun Scene.createSettlementBlocks(blocks: List<BlockTile>): TileDocument {
    val data = blocks.map {
        val tile = it.shape
        val squareX = it.x
        val squareY = it.y
        recordOf(
            "x" to squareX * grid.size,
            "y" to squareY * grid.size,
            "width" to grid.size * tile.squaresX,
            "height" to grid.size * tile.squaresY,
            "levels" to JsSet(arrayOf("defaultLevel0000")),
            "locked" to true,
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
    }.toTypedArray()
    return createEmbeddedDocuments<TileDocument>("Tile", data).await().first()
}

suspend fun Scene.createSettlementLegends(legends: List<Legend>): DrawingDocument {
    val data = legends.map {
        val squareX = it.x
        val squareY = it.y
        recordOf(
            "shape" to ShapeData(
                type = "r",
                width = 500,
                height = 100,
            ),
            "height" to 100,
            "width" to 500,
            "locked" to true,
            "x" to squareX * grid.size,
            "y" to squareY * grid.size,
            "levels" to JsSet(arrayOf("defaultLevel0000")),
            "text" to it.label,
            "textAlpha" to 1,
            "fontSize" to 30,
            "rotation" to it.rotation,
            "strokeAlpha" to 0,
        ).unsafeCast<AnyObject>()
    }.toTypedArray()
    return createEmbeddedDocuments<DrawingDocument>("Drawing", data).await().first()
}

data class BlockTile(
    val shape: BlockShape,
    val x: Int,
    val y: Int,
)

data class Legend(
    val label: String,
    val x: Int,
    val y: Int,
    val rotation: Int = 0,
)

private suspend fun Scene.createInfrastructureBlocks(waterBorders: Int) {
    val hasWaterBorders = waterBorders > 0
    val shapes = listOf(
        // infrastructure
        BlockTile(
            shape = InfrastructureBlockShape.ONE_BY_THREE,
            x = 11,
            y = 9,
        ),
    )
    val waterBorderShapes = if (hasWaterBorders) {
        listOf(
            BlockTile(
                shape = InfrastructureBlockShape.ONE_BY_THREE,
                x = 18,
                y = 36,
            )
        )
    } else {
        emptyList()
    }
    val sideShapes = listOf(
        // top
        BlockTile(
            if (waterBorders >= 4) InfrastructureBlockShape.ONE_BY_TWO else InfrastructureBlockShape.ONE_BY_ONE,
            19,
            7
        ),
        // left
        BlockTile(
            if (waterBorders >= 3) InfrastructureBlockShape.TWO_BY_ONE else InfrastructureBlockShape.ONE_BY_ONE,
            7,
            19
        ),
        // bottom
        BlockTile(
            if (waterBorders >= 1) InfrastructureBlockShape.ONE_BY_TWO else InfrastructureBlockShape.ONE_BY_ONE,
            19,
            32
        ),
        // right
        BlockTile(
            if (waterBorders >= 2) InfrastructureBlockShape.TWO_BY_ONE else InfrastructureBlockShape.ONE_BY_ONE,
            32,
            19
        ),
    )
    createSettlementBlocks(shapes + sideShapes + waterBorderShapes)
    val legends = listOf(Legend(t("structureTrait.infrastructure"), 10, 8))
    val waterBorderLegends = if (hasWaterBorders) {
        listOf(Legend(t("kingdom.fishingFleets"), 17, 37))
    } else {
        emptyList()
    }
    val wall = t("kingdom.wall")
    val wallAndBridge = t("kingdom.wallAndBridge")
    val sideLegends = listOf(
        // top
        Legend(
            if (waterBorders >= 4) wallAndBridge else wall,
            17,
            6
        ),
        // left
        Legend(
            if (waterBorders >= 3) wallAndBridge else wall,
            4,
            19,
            90,
        ),
        // bottom
        Legend(
            if (waterBorders >= 1) wallAndBridge else wall,
            17,
            33
        ),
        // right
        Legend(
            if (waterBorders >= 2) wallAndBridge else wall,
            31,
            19,
            270,
        ),
    )
    createSettlementLegends(legends + sideLegends + waterBorderLegends)
}

suspend fun createScene(
    name: String,
    background: String,
    height: Int,
    width: Int,
    waterBorders: Int,
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
    scene.createSettlementBlocks(
        listOf(
            BlockTile(
                SettlementBlockShape.TWO_BY_TWO,
                11,
                11,
            )
        )
    )
    scene.createInfrastructureBlocks(waterBorders)
    val thumbnail = scene.createThumbnail().await()
    scene.typeSafeUpdate { thumb = thumbnail.thumb }
    return scene
}