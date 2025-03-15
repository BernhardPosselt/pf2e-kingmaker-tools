package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.actor.Proficiency.*
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill.*
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType.ABILITY
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType.PROFICIENCY
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createProficiencyModifier
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createSkillAbilityModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.FilterResult
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Case
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Gte
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Lt
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.When
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
    vacancies = Vacancies(),
    structure = null,
    anarchyAt = 20,
)

class EvaluateModifiersTest {
    @Test
    fun testBasic() {
        val modifiers = listOf(
            createSkillAbilityModifiers(
                AGRICULTURE,
                KingdomAbilityScores(11, 13, 15, 16)
            ).copy(rollOptions = setOf("option")),
            createProficiencyModifier(AGRICULTURE, proficiency = LEGENDARY, level = 3)
        )
        val result = evaluateModifiers(
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
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
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        assertEquals(11, result.total)
        assertEquals(11, result.bonuses[PROFICIENCY])
        assertEquals(
            listOf(
                createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3),
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
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        assertEquals(-4, result.total)
        assertNull(result.bonuses[PROFICIENCY])
        assertEquals(-4, result.penalties[PROFICIENCY])
        assertEquals(
            listOf(
                first,
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
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        assertEquals(8, result.total)
        assertEquals(11, result.bonuses[PROFICIENCY])
        assertEquals(-3, result.penalties[PROFICIENCY])
        assertEquals(
            listOf(
                first,
                second,
            ), result.modifiers
        )
    }

    @Test
    fun evaluatevalueExpression() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3)
            .copy(
                valueExpression = When(
                    cases = listOf(
                        Case(
                            condition = Lt("@kingdomLevel", "2"),
                            value = "4",
                        ),
                        Case(
                            condition = Gte("@kingdomLevel", "2"),
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
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        assertEquals(3, result.total)
    }

    @Test
    fun evaluateValueExpressionFallback() {
        val first = createProficiencyModifier(BOATING, proficiency = LEGENDARY, level = 3)
            .copy(
                valueExpression = When(
                    cases = listOf(
                        Case(
                            condition = Lt("@kingdomLevel", 2),
                            value = 4,
                        ),
                        Case(
                            condition = Gte("@kingdomLevel", 3),
                            value = 3,
                        ),
                    ),
                    default = 2,
                )
            )
        val modifiers = listOf(
            first
        )
        val result = evaluateModifiers(
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
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
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        assertEquals(2, result.total)
        assertEquals(modifiers, result.modifiers)
    }

    @Test
    fun keepsDisabledModifiersThatAreHigherThanEnabled() {
        val first = createProficiencyModifier(AGRICULTURE, proficiency = MASTER, level = 3)
        val second = createProficiencyModifier(BOATING, proficiency = TRAINED, level = 3)
        val third = createProficiencyModifier(POLITICS, proficiency = LEGENDARY, level = 3).copy(enabled = false)
        val fourth = createProficiencyModifier(STATECRAFT, proficiency = MASTER, level = 3).copy(value = -2)
        val fifth = createProficiencyModifier(WARFARE, proficiency = TRAINED, level = 3).copy(value = -3)
        val sixth =
            createProficiencyModifier(MAGIC, proficiency = LEGENDARY, level = 3).copy(enabled = false, value = -4)
        val modifiers = listOf(
            first,
            second,
            third,
            fourth,
            fifth,
            sixth,
        )
        val result = evaluateModifiers(
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        console.log(result)
        assertEquals(6, result.total)
        assertEquals(9, result.bonuses[PROFICIENCY])
        assertEquals(-3, result.penalties[PROFICIENCY])
        assertEquals(
            listOf(
                first,
                third,
                fifth,
                sixth,
            ), result.modifiers
        )
    }

    @Test
    fun keepsOnlyOneIfBothHaveTheSameValue() {
        val first = createProficiencyModifier(AGRICULTURE, proficiency = MASTER, level = 3)
        val second = createProficiencyModifier(AGRICULTURE, proficiency = MASTER, level = 3).copy(id="blubb")
        val modifiers = listOf(
            first,
            second,
        )
        val result = evaluateModifiers(
            FilterResult(
                context = defaultContext,
                modifiers = modifiers,
            )
        )
        assertEquals(9, result.total)
        assertEquals(9, result.bonuses[PROFICIENCY])
        assertEquals(
            listOf(
                first,
            ), result.modifiers
        )
    }
}