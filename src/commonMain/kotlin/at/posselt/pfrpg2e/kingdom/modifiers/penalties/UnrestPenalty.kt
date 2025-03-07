package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

fun createUnrestModifier(unrest: Int): Modifier? {
    val value = calculateUnrestPenalty(unrest)
    return if (value == 0) {
        null
    } else {
        Modifier(
            id = "unrest",
            value = -value,
            type = ModifierType.STATUS,
            name = "Unrest"
        )
    }
}

fun calculateUnrestPenalty(unrest: Int) =
    if (unrest >= 15) {
        4
    } else if (unrest >= 10) {
        3
    } else if (unrest >= 5) {
        2
    } else if (unrest >= 1) {
        1
    } else {
        0
    }