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
import at.posselt.pfrpg2e.kingdom.createExpressionContext
import at.posselt.pfrpg2e.kingdom.createModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierSelector
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.utils.formatAsModifier
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
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
        event = null,
        eventStage = null,
        structureIds = settlements.current
            ?.constructedStructures
            ?.map { it.id }
            ?.toSet()
            .orEmpty(),
        waterBorders = settlements.current?.waterBorders ?: 0,
    )
    val baseModifiers = kingdom.createModifiers(settlements)
    return KingdomSkill.entries.map {
        val filtered = filterModifiersAndUpdateContext(baseModifiers, context.copy(usedSkill = it),  ModifierSelector.CHECK)
        val evaluatedModifiers = evaluateModifiers(filtered)
        val rank = skillRanks.resolve(it)
        val proficiency = skillRanks.resolveProficiency(it)
        SkillChecksContext(
            skill = it.value,
            label = t(it),
            rank = rank,
            valueClass = when (proficiency) {
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
                    label = t(it),
                    hideLabel = true,
                    value = rank.toString(),
                    options = Proficiency.entries.map { SelectOption(label = t(it), value = it.rank.toString()) },
                    name = "skillRanks.${it.value}",
                    overrideType = OverrideType.NUMBER,
                    elementClasses = listOf("km-proficiency"),
                    labelClasses = listOf("km-slim-inputs"),
                ).toContext()
            },
            proficiency = t(proficiency),
            modifier = evaluatedModifiers.total.formatAsModifier(),
        )
    }.toTypedArray()
}