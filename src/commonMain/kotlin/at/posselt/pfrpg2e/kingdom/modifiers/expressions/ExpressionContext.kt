package at.posselt.pfrpg2e.kingdom.modifiers.expressions

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.toCamelCase

data class ExpressionContext(
    val usedSkill: KingdomSkill,
    val ranks: KingdomSkillRanks,
    val leader: Leader,
    val activity: String?,
    val phase: KingdomPhase?,
    val level: Int,
    val unrest: Int,
    val flags: Set<String>,
    val rollOptions: Set<String>,
) {
    fun evaluateExpression(expression: String): String? {
        return when (expression) {
            "@leader" -> leader.value
            "@phase" -> phase?.value
            "@activity" -> activity
            "@unrest" -> unrest.toString()
            "@agricultureRank" -> ranks.agriculture.toString()
            "@artsRank" -> ranks.arts.toString()
            "@boatingRank" -> ranks.boating.toString()
            "@defenseRank" -> ranks.defense.toString()
            "@engineeringRank" -> ranks.engineering.toString()
            "@explorationRank" -> ranks.exploration.toString()
            "@folkloreRank" -> ranks.folklore.toString()
            "@industryRank" -> ranks.industry.toString()
            "@intrigueRank" -> ranks.intrigue.toString()
            "@magicRank" -> ranks.magic.toString()
            "@politicsRank" -> ranks.politics.toString()
            "@scholarshipRank" -> ranks.scholarship.toString()
            "@statecraftRank" -> ranks.statecraft.toString()
            "@tradeRank" -> ranks.trade.toString()
            "@warfareRank" -> ranks.warfare.toString()
            "@wildernessRank" -> ranks.wilderness.toString()
            "@kingdomLevel" -> level.toString()
            "@skillRank" -> ranks.resolve(usedSkill).toString()
            "@skill" -> usedSkill.toCamelCase()
            else -> expression
        }
    }
}