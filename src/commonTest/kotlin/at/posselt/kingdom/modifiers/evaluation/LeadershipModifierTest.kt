package at.posselt.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.actor.SkillRanks
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.LeaderSkillRanks
import at.posselt.pfrpg2e.kingdom.modifiers.constructors.LeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.modifiers.constructors.LeaderSkills
import at.posselt.pfrpg2e.kingdom.modifiers.constructors.createLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import kotlin.test.Test
import kotlin.test.assertEquals

class LeadershipModifierTest {
    @Test
    fun testAppliesFull() {
        val modifiers = createLeadershipModifiers(
            leaderSkills = LeaderSkills(counselor = listOf(Skill.STEALTH, Skill.ACROBATICS)),
            leaderSkillRanks = LeaderSkillRanks(counselor = SkillRanks(
                stealth = 2,
                acrobatics = 3,
            )),
            leaderKingdomSkills = LeaderKingdomSkills(
                counselor = listOf(KingdomSkill.AGRICULTURE)
            ),
        )
        val filteredModifiers = filterModifiersAndUpdateContext(modifiers, defaultContext)
        console.log(filteredModifiers)
        val result = evaluateModifiers(
            context = filteredModifiers.context,
            modifiers = filteredModifiers.modifiers,
        )
        assertEquals(2, result.total)
    }
}