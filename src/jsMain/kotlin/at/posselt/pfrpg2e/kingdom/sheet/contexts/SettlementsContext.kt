package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import js.objects.JsPlainObject

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
}

fun Array<RawSettlement>.toContext(
    game: Game,
    autoCalculateSettlementLevel: Boolean,
    allStructuresStack: Boolean,
    allowCapitalInvestmentInCapitalWithoutBank: Boolean,
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
            )
        }
    }.toTypedArray()
}