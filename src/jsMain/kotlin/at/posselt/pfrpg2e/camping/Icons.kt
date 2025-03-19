package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.SheetType
import at.posselt.pfrpg2e.utils.createPartyActorIcon
import com.foundryvtt.core.game


fun createCampingIcon(
    id: String,
    actionDispatcher: ActionDispatcher
) = createPartyActorIcon(
    id = id,
    icon = setOf("fa-solid", "fa-tent"),
    toolTip = "PFRPG2E Camping Sheet",
    macroName = "Open Camping Sheet",
    macroImg = "icons/magic/fire/flame-burning-campfire-orange.webp",
    sheetType = SheetType.CAMPING,
    onClick = {
        game.actors.get(it)?.takeIfInstance<CampingActor>()?.let { actor ->
            openOrCreateCampingSheet(game, actionDispatcher, actor)
        }
    },
)
