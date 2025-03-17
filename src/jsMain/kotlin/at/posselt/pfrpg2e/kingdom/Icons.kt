package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.kingdom.sheet.openOrCreateKingdomSheet
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.game
import kotlinx.browser.document
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement

fun createKingmakerIcon(
    id: String?,
    actionDispatcher: ActionDispatcher
): HTMLElement {
    val kingdomLink = document.create.a {
        classes = setOf("create-button")
        i {
            classes = setOf("fa-brands", "fa-fort-awesome")
        }
        attributes["data-tooltip"] = "PFRPG2E Kingdom Sheet"
        onClickFunction = {
            it.preventDefault()
            it.stopPropagation()
            buildPromise {
                if (id != null) {
                    game.actors.get(id)?.takeIfInstance<KingdomActor>()?.let { actor ->
                        openOrCreateKingdomSheet(game, actionDispatcher, actor)
                    }
                }
            }
        }
    }
    return kingdomLink
}
