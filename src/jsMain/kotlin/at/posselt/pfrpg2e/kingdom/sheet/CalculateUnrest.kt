package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.ChosenFeat
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.unrest.calculateUnrest
import at.posselt.pfrpg2e.kingdom.vacancies
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.roll
import js.objects.JsPlainObject
import kotlin.math.min

@JsPlainObject
private external interface ChatUnrestContext {
    val overcrowded: Int
    val secondary: Int
    val atWar: Int
    val rulerVacant: Int
    val total: Int
}

fun calculateAnarchy(feats: List<ChosenFeat>) =
    feats.sumOf { it.feat.increaseAnarchyLimit ?: 0 } + 20

suspend fun adjustUnrest(
    kingdom: KingdomData,
    settlements: List<Settlement>,
    chosenFeats: List<ChosenFeat>,
): Int {
    val unrest = calculateUnrest(kingdom.atWar, settlements, kingdom.vacancies())
    val ruler = if (unrest.rulerVacant) roll(formula = "1d4", flavor = "Ruler is vacant, gaining Unrest") else 0
    val newUnrest = unrest.war + unrest.secondaryTerritory + unrest.overcrowded + ruler
    return if (kingdom.level >= 20 && newUnrest > 0) {
        postChatMessage("Ignoring any Unrest increase due to \"Envy of the World\" Kingdom Feature")
        kingdom.unrest
    } else {
        postChatTemplate(
            templatePath = "chatmessages/unrest.hbs",
            templateContext = ChatUnrestContext(
                overcrowded = unrest.overcrowded,
                secondary = unrest.secondaryTerritory,
                atWar = unrest.war,
                rulerVacant = ruler,
                total = newUnrest,
            ),
        )
        val totalUnrest = newUnrest + kingdom.unrest
        if (totalUnrest >= 10) {
            roll(formula = "1d10", flavor = "Gaining points to Ruin (distribute as you wish)")
            if (roll(formula = "1d20", flavor = "Losing Hex on a Flat Check 11") >= 11) {
                postChatMessage("You lose one hex of your choice")
            }
        }
        val anarchyAt = calculateAnarchy(chosenFeats)
        if (totalUnrest >= anarchyAt) {
            postChatMessage("Kingdom falls into anarchy, unless you spend all fame/infamy points. Only Quell Unrest leadership activities can be performed and all checks are worsened by a degree")
        }
        min(anarchyAt, totalUnrest)
    }
}
