package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2EParty
import js.objects.recordOf
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface PartyFormData {
    val partyUuid: String
}

suspend fun chooseParty(game: Game): PF2EParty {
    val parties = game.actors.contents.filterIsInstance<PF2EParty>()
    if (parties.isEmpty()) {
        ui.notifications.error("No parties found")
        throw IllegalStateException("No parties found")
    }
    val first = parties.firstOrNull()
    if (first == null) {
        return awaitablePrompt<PartyFormData, PF2EParty>(
            title = "Choose Party",
            templatePath = "components/forms/form.hbs",
            templateContext = recordOf(
                "formRows" to formContext(
                    Select(
                        name = "partyUuid",
                        label = "Party",
                        value = parties.first().uuid,
                        options = parties
                            .map { SelectOption(value = it.uuid, label = it.name) }
                    ),
                )
            )
        ) {
            val party = fromUuidTypeSafe<PF2EParty>(it.partyUuid)
            checkNotNull(party) {
                "No party chosen"
            }
            party
        }
    } else {
        return first
    }
}