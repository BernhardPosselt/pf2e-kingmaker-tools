package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2EParty
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject
import kotlin.math.min

suspend fun updateXP(players: Array<PF2ECharacter>, amount: Int) = coroutineScope {
    players.map {
        val currentXP = it.system.details.xp.value
        val xpThreshold = it.system.details.xp.max
        val currentLevel = it.system.details.level.value
        val addLevels = (currentXP + amount) / xpThreshold
        val level = currentLevel + addLevels
        val xpGain = if (level >= 20) {
            0
        } else {
            (currentXP + amount) % xpThreshold
        }
        async {
            it.typeSafeUpdate {
                system.details.xp.value = xpGain
                system.details.level.value = min(20, level)
            }
        }
    }.awaitAll()
    postChatTemplate(
        "chatmessages/xp-result.hbs",
        recordOf(
            "characterNames" to players.map { it.name }.toTypedArray(),
            "amount" to amount,
        )
    )
}

@JsPlainObject
external interface XpFormData {
    val amount: Int
    val partyUuid: String
}

suspend fun awardXPMacro(game: Game) {
    val parties = game.actors.contents
        .filterIsInstance<PF2EParty>()
    if (parties.isEmpty()) {
        ui.notifications.error(t("macros.noPartiesFound"))
        return
    }
    prompt<XpFormData, Unit>(
        title = t("macros.xp.title"),
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                Select(
                    name = "partyUuid",
                    label = t("macros.xp.party"),
                    value = parties.first().uuid,
                    options = parties
                        .map { SelectOption(value = it.uuid, label = it.name) }
                ),
                NumberInput(
                    name = "amount",
                    label = t("macros.xp.amount"),
                ),
            )
        )
    ) {
        if (it.amount > 0) {
            fromUuidTypeSafe<PF2EParty>(it.partyUuid)
                ?.members
                ?.filterIsInstance<PF2ECharacter>()
                ?.toTypedArray()
                ?.let { players -> updateXP(players, it.amount) }

        }
    }
}