package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate

fun armyConditionPenalties(
    armyName: String,
    armyUuid: String,
    miredValue: Int,
    wearyValue: Int
): List<Modifier> {
    val modifiers = mutableListOf<Modifier>()
    if (miredValue > 0) {
        modifiers.add(Modifier(
            name = "$armyName (Mired $miredValue)",
            type = ModifierType.CIRCUMSTANCE,
            value = -miredValue,
            id = "mired-$armyUuid",
            predicates = listOf(
                EqPredicate("@activity", "deploy-army"),
            )
        ))
    }
    if (wearyValue > 0) {
        modifiers.add(Modifier(
            name = "$armyName (Weary $wearyValue)",
            type = ModifierType.CIRCUMSTANCE,
            value = -wearyValue,
            id = "weary-$armyUuid",
            predicates = listOf(
                EqPredicate("@phase", KingdomPhase.ARMY.value),
            )
        ))
    }
    return modifiers
}