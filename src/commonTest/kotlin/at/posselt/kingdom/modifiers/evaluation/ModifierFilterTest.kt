package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.createProficiencyModifier
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate
import kotlin.test.Test
import kotlin.test.assertEquals

private val filterMod = createProficiencyModifier(KingdomSkill.AGRICULTURE, proficiency = Proficiency.LEGENDARY, level = 3)

class ModifierFilterTest {
    @Test
    fun testPermutations() {
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod),
            context = defaultContext,
            activity = null,
            phase = null,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(skills = listOf(KingdomSkill.WARFARE))),
            context = defaultContext,
            activity = null,
            phase = null,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(activities = listOf("test"))),
            context = defaultContext,
            activity = null,
            phase = null,
        ).modifiers.size)
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(activities = listOf("test"))),
            context = defaultContext,
            activity = "test",
            phase = null,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(phases = listOf(KingdomPhase.ARMY))),
            context = defaultContext,
            activity = null,
            phase = null,
        ).modifiers.size)
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(phases = listOf(KingdomPhase.ARMY))),
            context = defaultContext,
            activity = null,
            phase = KingdomPhase.ARMY,
        ).modifiers.size)
    }

    @Test
    fun testPredicates() {
        assertEquals(1, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(predicates = listOf(
                EqPredicate(left = "@kingdomLevel", "2")
            ))),
            context = defaultContext,
            activity = null,
            phase = null,
        ).modifiers.size)
        assertEquals(0, filterModifiersAndUpdateContext(
            modifiers = listOf(filterMod.copy(predicates = listOf(
                EqPredicate(left = "@kingdomLevel", "3")
            ))),
            context = defaultContext,
            activity = null,
            phase = null,
        ).modifiers.size)
    }

    @Test
    fun collectsRollOptionsFromModifiers() {
        val result = filterModifiersAndUpdateContext(
            modifiers = listOf(
                filterMod.copy(
                    predicates = listOf(
                        EqPredicate(left = "@kingdomLevel", "2")
                    ),
                    rollOptions = setOf("mod")
                )
            ),
            context = defaultContext,
            activity = null,
            phase = null,
        )
        assertEquals(1, result.modifiers.size)
        assertEquals(setOf("mod", "option"), result.context.rollOptions)
    }
}
