package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.game
import kotlinx.browser.document
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement

fun createCampingIcon(
    id: String?,
    actionDispatcher: ActionDispatcher
): HTMLElement {
    val kingdomLink = document.create.a {
        classes = setOf("create-button")
        i {
            classes = setOf("fa-solid", "fa-tent")
        }
        attributes["data-tooltip"] = "PFRPG2E Camping Sheet"
        onClickFunction = {
            it.preventDefault()
            it.stopPropagation()
            buildPromise {
                if (id != null) {
                    game.actors.get(id)?.takeIfInstance<CampingActor>()?.let { actor ->
                        CampingSheet(game, actor, actionDispatcher).launch()
                    }
                }
            }
        }
    }
    return kingdomLink
}
