package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.pf2e.actor.PF2ECharacter
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject

private suspend fun updateXP(players: Array<PF2ECharacter>, amount: Int) = coroutineScope {
    players.map {
        val currentXP = it.system.details.xp.value
        val xpThreshold = it.system.details.xp.max
        val currentLevel = it.system.details.level.value
        val addLevels = (currentXP + amount) / xpThreshold
        val xpGain = (currentXP + amount) % xpThreshold
        async {
            it.typeSafeUpdate {
                system.details.xp.value = xpGain
                system.details.level.value = currentLevel + addLevels
            }
        }
    }.awaitAll()
    postChatMessage("Players gained $amount XP!")
}

@JsPlainObject
external interface XpFormData {
    val amount: Int
}

suspend fun awardXPMacro(players: Array<PF2ECharacter>) {
    prompt<XpFormData, Unit>(
        title = "Award Party XP",
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                NumberInput(
                    name = "amount",
                    label = "Amount",
                )
            )
        )
    ) {
        if (it.amount > 0) {
            updateXP(players, it.amount)
        }
    }
}