package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.MergedSettlement
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.SettlementData
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateSettlement
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.includeCapital
import at.posselt.pfrpg2e.kingdom.scenes.toRectangle
import at.posselt.pfrpg2e.utils.getAppFlag
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.TileDocument
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlin.math.max
import kotlin.math.min

private fun Scene.structureTokens(): List<TokenDocument> =
    tokens.contents
        .asSequence()
        .filter {
            val actor = it.actor
            actor is PF2ENpc && actor.isStructure()
        }
        .toList()

private fun Scene.getStructures(): List<Structure> =
    tokens.contents
        .asSequence()
        .mapNotNull { it.actor }
        .filterIsInstance<PF2ENpc>()
        .mapNotNull { it.parseStructure() }
        .toList()

private val TileDocument.isBlock: Boolean
    get() = getAppFlag<TileDocument, Any?>("settlementBlockDrawing") != null

private fun Scene.getBlockTiles() =
    tiles.contents
        .asSequence()
        .filterNot { it.isBlock }

private fun Scene.calculateOccupiedBlocks(): Int {
    val structures = structureTokens().map { it.toRectangle() }
    val blocks = getBlockTiles().map { it.toRectangle().applyTolerance(50.0) }
    return blocks
        .filter { block -> structures.any { it in block } }
        .count()
}

fun Scene.parseSettlement(
    rawSettlement: RawSettlement,
    autoCalculateSettlementLevel: Boolean,
    allStructuresStack: Boolean,
): Settlement {
    val occupiedBlocks = if (autoCalculateSettlementLevel) max(1, calculateOccupiedBlocks()) else rawSettlement.lots
    val settlementLevel = if (autoCalculateSettlementLevel) min(20, occupiedBlocks) else rawSettlement.level
    return evaluateSettlement(
        data = SettlementData(
            name = name,
            occupiedBlocks = occupiedBlocks,
            level = settlementLevel,
            type = SettlementType.fromString(rawSettlement.type)
                ?: SettlementType.SETTLEMENT,
            isSecondaryTerritory = rawSettlement.secondaryTerritory,
            waterBorders = rawSettlement.waterBorders,
        ),
        structures = getStructures(),
        allStructuresStack = allStructuresStack,
    )
}

fun Scene.getMergedStructures(
    rawSettlement: RawSettlement,
    rawCapital: RawSettlement,
    capitalScene: Scene?,
    // settings
    capitalModifierFallbackEnabled: Boolean,
    autoCalculateSettlementLevel: Boolean,
    allStructuresStack: Boolean,
): MergedSettlement {
    val settlement = parseSettlement(
        rawSettlement,
        allStructuresStack = allStructuresStack,
        autoCalculateSettlementLevel = autoCalculateSettlementLevel
    )
    return if (capitalScene == this || capitalScene == null) {
        MergedSettlement(settlement)
    } else {
        includeCapital(
            settlement = settlement,
            capital = parseSettlement(
                rawCapital,
                allStructuresStack = allStructuresStack,
                autoCalculateSettlementLevel = autoCalculateSettlementLevel,
            ),
            capitalModifierFallbackEnabled = capitalModifierFallbackEnabled,
        )
    }
}
