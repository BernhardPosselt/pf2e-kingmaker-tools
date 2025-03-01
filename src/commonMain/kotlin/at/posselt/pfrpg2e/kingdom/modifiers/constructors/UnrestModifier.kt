package at.posselt.pfrpg2e.kingdom.modifiers.constructors

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType

fun createUnrestModifier(unrest: Int): Modifier {
    val value = if (unrest >= 15) {
        -4
    } else if (unrest >= 10) {
        -3
    } else if (unrest >= 5) {
        -2
    } else if (unrest >= 1) {
        -1
    } else {
        0
    }
    return Modifier(
        id = "unrest",
        value = value,
        type = ModifierType.STATUS,
        name = "Unrest"
    )
}