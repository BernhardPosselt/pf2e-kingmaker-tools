package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.actor.Proficiency
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
    val input: FormElementContext
    val proficiency: String
    val skill: String
    val valueClass: String
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
            skill = it.value,
            label = it.label,
            rank = rank,
            valueClass = when(proficiency) {
                Proficiency.UNTRAINED -> "km-proficiency-untrained"
                Proficiency.TRAINED -> "km-proficiency-trained"
                Proficiency.EXPERT -> "km-proficiency-expert"
                Proficiency.MASTER -> "km-proficiency-master"
                Proficiency.LEGENDARY -> "km-proficiency-legendary"
            },
                    input = if (kingdom.settings.automateStats) {
                HiddenInput(
                    value = rank.toString(),
                    name = "skillRanks.${it.value}",
                    overrideType = OverrideType.NUMBER,
                ).toContext()
            } else {
                Select(
                    label = it.label,
                    hideLabel = true,
                    value = rank.toString(),
                    options = Proficiency.entries.map { SelectOption(label = it.label, value = it.rank.toString()) },
                    name = "skillRanks.${it.value}",
                    overrideType = OverrideType.NUMBER,
                    elementClasses = listOf("km-proficiency"),
                    labelClasses = listOf("km-slim-inputs"),
                ).toContext()
            },
            proficiency = proficiency.label,
            modifier = evaluatedModifiers.total.formatAsModifier(),
        )
    }.toTypedArray()
}