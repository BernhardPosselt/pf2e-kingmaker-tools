package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.kingdom.settlements.settlementSizeData
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject
import js.objects.recordOf

@Suppress("unused")
@JsPlainObject
private external interface SSizeContext {
    val type: String
    val blocks: String
    val population: String
    val level: String
    val consumption: String
    val maxItemBonus: String
    val influence: String
}

@Suppress("unused")
@JsPlainObject
private external interface SettlementSizeContext {
    val data: Array<SSizeContext>
}

suspend fun settlementSizeHelp() {
    prompt<Unit, Unit>(
        title = t("kingdom.settlementSize"),
        templatePath = "applications/kingdom/settlement-size-help.hbs",
        buttonLabel = "Close",
        templateContext = SettlementSizeContext(
            data = settlementSizeData
                .map {
                    val level = if(it.levelTo != null) {
                       if (it.levelFrom != it.levelTo) {
                           "${it.levelFrom}-${it.levelTo}"
                       } else {
                           it.levelFrom.toString()
                       }
                    } else {
                        "${it.levelFrom}+"
                    }
                    val influence = t("kingdom.hex", recordOf("count" to it.influence))
                    SSizeContext(
                        type = t(it.type),
                        blocks = it.maximumBlocks,
                        population = it.population,
                        level = level,
                        consumption = it.consumption.toString(),
                        maxItemBonus = "+${it.maxItemBonus}",
                        influence = influence,
                    )
                }
                .toTypedArray()
        ).asAnyObject()
    ) {
    }
}