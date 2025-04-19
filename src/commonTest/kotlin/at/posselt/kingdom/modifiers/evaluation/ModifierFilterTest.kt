package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierSelector
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createProficiencyModifier
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.In
import kotlin.test.Test
import kotlin.test.assertEquals

private val filterMod = createProficiencyModifier(KingdomSkill.AGRICULTURE, proficiency = Proficiency.LEGENDARY, level = 3)

class ModifierFilterTest {
    @Test
    fun testPermutations() {
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod),
            context = defaultContext,
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(applyIf = listOf(In("@activity", listOf("test"))))),
            context = defaultContext,
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(applyIf = listOf(In("@activity", listOf("test"))))),
            context = defaultContext.copy(activity = "test"),
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(applyIf = listOf(In("@phase", listOf("army"))))),
            context = defaultContext,
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(applyIf = listOf(In("@phase", listOf("army"))))),
            context = defaultContext.copy(phase = KingdomPhase.ARMY),
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
    }

    @Test
    fun testPredicates() {
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(applyIf = listOf(
                Eq(left = "@kingdomLevel", 2)
            ))),
            context = defaultContext,
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(applyIf = listOf(
                Eq(left = "@kingdomLevel", 3)
            ))),
            context = defaultContext,
            selector = ModifierSelector.CHECK,
        ).modifiers.size)
    }

    @Test
    fun collectsRollOptionsFromModifiers() {
        val result = filterModifiersAndUpdateContext(
            modifiers = listOf(
                filterMod.copy(
                    applyIf = listOf(
                        Eq(left = "@kingdomLevel", 2)
                    ),
                    rollOptions = setOf("mod")
                )
            ),
            context = defaultContext,
            selector = ModifierSelector.CHECK,
        )
        assertEquals(1, result.modifiers.size)
        assertEquals(setOf("mod", "option"), result.context.rollOptions)
    }
}
