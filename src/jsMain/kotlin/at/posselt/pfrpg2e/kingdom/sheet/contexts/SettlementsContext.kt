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
            )
        }
    }.sortedWith(compareBy<SettlementsContext> { !it.isCapital }.thenBy { it.name })
        .toTypedArray()
}