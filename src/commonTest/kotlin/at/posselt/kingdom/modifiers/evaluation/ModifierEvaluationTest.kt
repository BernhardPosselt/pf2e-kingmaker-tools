package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.actor.Proficiency.*
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill.*
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType.ABILITY
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType.PROFICIENCY
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createAbilityModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createProficiencyModifier
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.GtePredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.LtPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.WhenBranch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

val defaultContext = ExpressionContext(
    usedSkill = AGRICULTURE,
    ranks = KingdomSkillRanks(),
    level = 2,
    unrest = 0,
    flags = setOf("flag"),
    rollOptions = setOf("option"),
    phase = null,
    activity = null,
    leader = Leader.COUNSELOR,
)

class ModifierEvaluationTest {
    @Test
    fun testBasic() {
        val modifiers = listOf(
            createAbilityModifiers(
                AGRICULTURE,
                KingdomAbilityScores(11, 13, 15, 16)
            ).copy(rollOptions = setOf("option")),
            createProficiencyModifier(AGRICULTURE, proficiency = LEGENDARY, level = 3)
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(12, result.total)
        assertEquals(11, result.bonuses[PROFICIENCY])
        assertEquals(1, result.bonuses[ABILITY])
        assertEquals(modifiers, result.modifiers)
        assertEquals(setOf("option"), result.rollOptions)
    }

    @Test
    fun higherOverridesLowerInSameCategory() {
        val modifiers = listOf(
            createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3),
            createProficiencyModifier(AGRICULTURE, proficiency = TRAINED, level = 3)
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(11, result.total)
        assertEquals(11, result.bonuses[PROFICIENCY])
        assertEquals(
            listOf(
                createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3),
                createProficiencyModifier(AGRICULTURE, proficiency = TRAINED, level = 3).copy(enabled = false),
            ), result.modifiers
        )
    }

    @Test
    fun lowestOverridesLowerInSameCategory() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3).copy(value = -4)
        val second = createProficiencyModifier(AGRICULTURE, proficiency = TRAINED, level = 3).copy(value = -3)
        val modifiers = listOf(
            first,
            second
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(-4, result.total)
        assertNull(result.bonuses[PROFICIENCY])
        assertEquals(-4, result.penalties[PROFICIENCY])
        assertEquals(
            listOf(
                first,
                second.copy(enabled = false),
            ), result.modifiers
        )
    }

    @Test
    fun negativeModsStackWithPositiveInTheSameCategory() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3)
        val second = createProficiencyModifier(WARFARE, proficiency = UNTRAINED, level = 3).copy(value = -3)
        val third = createProficiencyModifier(AGRICULTURE, proficiency = TRAINED, level = 3)
        val modifiers = listOf(
            first,
            second,
            third
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(8, result.total)
        assertEquals(11, result.bonuses[PROFICIENCY])
        assertEquals(-3, result.penalties[PROFICIENCY])
        assertEquals(
            listOf(
                first,
                second,
                third.copy(enabled = false),
            ), result.modifiers
        )
    }

    @Test
    fun evaluatePredicatedValue() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3)
            .copy(
                predicatedValue = When(
                    branches = listOf(
                        WhenBranch(
                            condition = LtPredicate("@kingdomLevel", "2"),
                            value = "4",
                        ),
                        WhenBranch(
                            condition = GtePredicate("@kingdomLevel", "2"),
                            value = "3",
                        ),
                    ),
                    default = "2",
                )
            )
        val modifiers = listOf(
            first
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(3, result.total)
    }

    @Test
    fun evaluatePredicatedValueFallback() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3)
            .copy(
                predicatedValue = When(
                    branches = listOf(
                        WhenBranch(
                            condition = LtPredicate("@kingdomLevel", "2"),
                            value = "4",
                        ),
                        WhenBranch(
                            condition = GtePredicate("@kingdomLevel", "3"),
                            value = "3",
                        ),
                    ),
                    default = "2",
                )
            )
        val modifiers = listOf(
            first
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(2, result.total)
    }

    @Test
    fun untypedPenaltiesStack() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3)
            .copy(type = ModifierType.UNTYPED, value = 3)
        val second = createProficiencyModifier(AGRICULTURE, proficiency = LEGENDARY, level = 3)
            .copy(type = ModifierType.UNTYPED, value = 11)
        val third = createProficiencyModifier(FOLKLORE, proficiency = LEGENDARY, level = 3)
            .copy(type = ModifierType.UNTYPED, value = -5)
        val fourth = createProficiencyModifier(MAGIC, proficiency = LEGENDARY, level = 3)
            .copy(type = ModifierType.UNTYPED, value = -7)
        val modifiers = listOf(
            first,
            second,
            third,
            fourth
        )
        val result = evaluateModifiers(
            context = defaultContext,
            modifiers = modifiers,
        )
        assertEquals(2, result.total)
        assertEquals(modifiers, result.modifiers)
    }
}