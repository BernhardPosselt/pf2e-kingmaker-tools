package at.posselt.kingdom.modifiers.expressions

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.All
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Gte
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasFlag
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasRollOption
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Lt
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Not
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Some
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionTest {
    @Test
    fun predicate() {
        val context = ExpressionContext(
            usedSkill = KingdomSkill.AGRICULTURE,
            ranks = KingdomSkillRanks(
                agriculture = 1,
            ),
            level = 2,
            unrest = 1,
            rollOptions = setOf("option"),
            flags = setOf("flag"),
            leader = Leader.COUNSELOR,
            activity = null,
            phase = null,
            vacancies = Vacancies(),
            structure = null,
            anarchyAt = 20,
        )
        val result = Not(
            All(listOf(
                All(listOf(HasFlag("flag"),  HasRollOption("option"))),
                All(listOf(
                    Gte("@kingdomLevel", "@unrest"),
                    Some(
                        listOf(
                            Eq(left = "2", right = "1"),
                            Lt("@agricultureRank", "2"),
                        )
                    ),
                ))
            )
        )).evaluate(context)
        assertEquals(false, result)
    }
}