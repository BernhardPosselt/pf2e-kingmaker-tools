package at.posselt.pfrpg2e.kingdom.modifiers.bonuses

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasRollOption

fun createRepairBonus() = Modifier(
    id = "repair-structure-item-bonus",
    name = "Repair Structure",
    type = ModifierType.ITEM,
    enabled = true,
    value = 2,
    applyIf = listOf(
        HasRollOption("repair-structure")
    )
)