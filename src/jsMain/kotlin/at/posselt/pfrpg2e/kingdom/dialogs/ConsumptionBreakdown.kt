package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.kingdom.sheet.contexts.ConsumptionBreakdownContext


suspend fun consumptionBreakdown(context: ConsumptionBreakdownContext) {
    prompt<Unit, Unit>(
        title = "Consumption Breakdown",
        templateContext = context,
        templatePath = "applications/kingdom/consumption-breakdown.hbs",
    ) {  }
}