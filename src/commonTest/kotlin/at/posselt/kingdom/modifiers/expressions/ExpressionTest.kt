package at.posselt.kingdom.modifiers.expressions

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.AndPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.EqPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.GtePredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasFlagPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.HasRollOptionPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.LtPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.NotPredicate
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.OrPredicate
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
            isVacant = false,
        )
        val result = NotPredicate(
            AndPredicate(
                left = AndPredicate(left = HasFlagPredicate("flag"), right = HasRollOptionPredicate("option")),
                right = AndPredicate(
                    right = GtePredicate("@kingdomLevel", "@unrest"),
                    left = OrPredicate(EqPredicate(left = "2", right = "1"), LtPredicate("@agricultureRank", "2")),
                )
            )
        ).evaluate(context)
        assertEquals(false, result)
    }
}