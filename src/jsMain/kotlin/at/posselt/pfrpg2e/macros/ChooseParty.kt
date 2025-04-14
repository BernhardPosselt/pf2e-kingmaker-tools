package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.t
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
    val first = parties.firstOrNull()
    if (first == null) {
        val message = t("macros.noPartiesFound")
        ui.notifications.error(message)
        throw IllegalStateException(message)
    }
    if (parties.size > 1) {
        return awaitablePrompt<PartyFormData, PF2EParty>(
            title = t("macros.chooseParty"),
            templatePath = "components/forms/form.hbs",
            templateContext = recordOf(
                "formRows" to formContext(
                    Select(
                        name = "partyUuid",
                        label = t("macros.party"),
                        value = parties.first().uuid,
                        options = parties
                            .map { SelectOption(value = it.uuid, label = it.name) }
                    ),
                )
            )
        ) { it, _ ->
            val party = fromUuidTypeSafe<PF2EParty>(it.partyUuid)
            checkNotNull(party) {
                t("macros.noPartyChosen")
            }
            party
        }
    } else {
        return first
    }
}