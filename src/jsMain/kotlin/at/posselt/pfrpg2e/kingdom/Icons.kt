package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.kingdom.sheet.openOrCreateKingdomSheet
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import com.foundryvtt.core.game
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.browser.document
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement

fun createKingmakerIcon(
    uuid: String?,
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
                if (uuid != null) {
                    fromUuidTypeSafe<PF2ENpc>(uuid)?.let { actor ->
                        openOrCreateKingdomSheet(game, actionDispatcher, actor)
                    }
                }
            }
        }
    }
    return kingdomLink
}
