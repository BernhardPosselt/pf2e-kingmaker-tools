package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.SettlementResult
import at.posselt.pfrpg2e.kingdom.checkModifiers
import at.posselt.pfrpg2e.kingdom.createExpressionContext
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateGlobalBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.includeCapital
import at.posselt.pfrpg2e.utils.formatAsModifier
import js.objects.JsPlainObject

@JsPlainObject
external interface SkillChecksContext {
    val label: String
    val modifier: String
    val rank: Int
    val proficiency: String
}

suspend fun skillChecks(
    kingdom: KingdomData,
    settlements: SettlementResult,
    skillRanks: KingdomSkillRanks,
): Array<SkillChecksContext> {
    val context = kingdom.createExpressionContext(
        phase = null,
        activity = null,
        leader = null,
        usedSkill = KingdomSkill.MAGIC,
        rollOptions = emptySet(),
        structure = null,
    )
    val allSettlements = settlements.allSettlements
    val globalBonuses = evaluateGlobalBonuses(allSettlements)
    val currentSettlement = settlements.current?.let {
        includeCapital(
            settlement = it,
            capital = settlements.capital,
            capitalModifierFallbackEnabled = kingdom.settings.includeCapitalItemModifier
        )
    }
    val baseModifiers = kingdom.checkModifiers(
        globalBonuses = globalBonuses,
        currentSettlement = currentSettlement,
        allSettlements = allSettlements,
        armyConditions = null,
    )
    return KingdomSkill.entries.map {
        val filtered = filterModifiersAndUpdateContext(baseModifiers, context.copy(usedSkill = it))
        val evaluatedModifiers = evaluateModifiers(filtered)
        val rank = skillRanks.resolve(it)
        val proficiency = skillRanks.resolveProficiency(it)
        SkillChecksContext(
            label = it.label,
            rank = rank,
            proficiency = proficiency.value,
            modifier = evaluatedModifiers.total.formatAsModifier(),
        )
    }.toTypedArray()
}