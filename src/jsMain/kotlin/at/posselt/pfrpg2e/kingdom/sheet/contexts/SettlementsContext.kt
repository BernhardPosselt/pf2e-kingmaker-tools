package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementLayoutType
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface SettlementsContext {
    val id: String
    val isCapital: Boolean
    val name: String
    val level: Int
    val size: String
    val residentialLots: Int
    val isSecondaryTerritory: Boolean
    val isOvercrowded: Boolean
    val lacksBridge: Boolean
    val canLevelUpTo: String?
    val nextLevelUp: String?
    val isRigid: Boolean
    // Detailed matrix fields
    val population: String
    val blocks: Int
    val lots: Int
    val maxItemBonus: Int
    val influence: Int
    val consumption: Int
    val baseItemLevel: Int
    val alchemicalItemLevel: Int
    val magicItemLevel: Int
    val arcaneItemLevel: Int
    val divineItemLevel: Int
    val primalItemLevel: Int
    val luxuryItemLevel: Int
}

fun Array<RawSettlement>.toContext(
    game: Game,
    autoCalculateSettlementLevel: Boolean,
    allStructuresStack: Boolean,
    allowCapitalInvestmentInCapitalWithoutBank: Boolean,
    capStructureBonusAtKingdomLevel: Boolean,
    capitalCanGrowOneSizeLarger: Boolean,
    kingdomLevel: Int,
): Array<SettlementsContext> {
    val scenesById = game.scenes.contents
        .filter { it.id != null }
        .associateBy { it.id }
    return mapNotNull { settlement ->
        scenesById[settlement.sceneId]?.let { scene ->
            val parsed = scene.parseSettlement(
                rawSettlement = settlement,
                autoCalculateSettlementLevel = autoCalculateSettlementLevel,
                allStructuresStack = allStructuresStack,
                allowCapitalInvestmentInCapitalWithoutBank = allowCapitalInvestmentInCapitalWithoutBank,
                capStructureBonusAtKingdomLevel = capStructureBonusAtKingdomLevel,
                kingdomLevel = kingdomLevel,
            )
            val itemBonusCap = parsed.size.maxItemBonus
            SettlementsContext(
                id = parsed.id,
                isCapital = parsed.type == SettlementType.CAPITAL,
                name = parsed.name,
                size = t(parsed.size.type),
                level = parsed.level,
                residentialLots = parsed.residentialLots,
                isSecondaryTerritory = parsed.isSecondaryTerritory,
                isOvercrowded = parsed.isOvercrowded,
                lacksBridge = parsed.lacksBridge,
                canLevelUpTo = parsed.canLevelUp(kingdomLevel, capitalCanGrowOneSizeLarger)?.value,
                nextLevelUp = parsed.nextLevelUp()?.let { t(it) },
                isRigid = parsed.layoutType == SettlementLayoutType.RIGID,
                // Detailed matrix fields
                population = parsed.size.population,
                blocks = parsed.occupiedBlocks,
                lots = parsed.blocks.sumOf { it.occupiedLots },
                maxItemBonus = itemBonusCap,
                influence = parsed.size.influence,
                consumption = parsed.consumption,
                baseItemLevel = parsed.availableItems.other.coerceAtMost(itemBonusCap),
                alchemicalItemLevel = parsed.availableItems.alchemical.coerceAtMost(itemBonusCap),
                magicItemLevel = parsed.availableItems.magical.coerceAtMost(itemBonusCap),
                arcaneItemLevel = parsed.availableItems.arcane.coerceAtMost(itemBonusCap),
                divineItemLevel = parsed.availableItems.divine.coerceAtMost(itemBonusCap),
                primalItemLevel = parsed.availableItems.primal.coerceAtMost(itemBonusCap),
                luxuryItemLevel = parsed.availableItems.luxury.coerceAtMost(itemBonusCap),
            )
        }
    }.sortedWith(compareBy<SettlementsContext> { !it.isCapital }.thenBy { it.name })
        .toTypedArray()
}
