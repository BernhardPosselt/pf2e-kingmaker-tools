package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.kingdom.settlements.settlementSizeData
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.asAnyObject
import js.objects.JsPlainObject

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
        title = "Settlement Size",
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
                    val influence = if (it.influence == 1) {
                        "1 hex"
                    } else {
                        "${it.influence} hexes"
                    }
                    SSizeContext(
                        type = it.type.toLabel(),
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