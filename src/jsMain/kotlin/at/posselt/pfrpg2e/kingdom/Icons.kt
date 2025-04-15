package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.kingdom.sheet.openOrCreateKingdomSheet
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.SheetType
import at.posselt.pfrpg2e.utils.createPartyActorIcon
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.game

fun createKingmakerIcon(
    id: String,
    actionDispatcher: ActionDispatcher
) = createPartyActorIcon(
    id = id,
    icon = setOf("fa-brands", "fa-fort-awesome"),
    toolTip = t("kingdom.macroTooltip"),
    macroName = t("kingdom.openSheet"),
    macroImg = "icons/equipment/head/crown-gold-blue.webp",
    sheetType = SheetType.KINGDOM,
    onClick = {
        game.actors.get(id)?.takeIfInstance<KingdomActor>()?.let { actor ->
            openOrCreateKingdomSheet(game, actionDispatcher, actor)
        }
    },
)

