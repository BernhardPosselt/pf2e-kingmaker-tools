package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.SettlementResult
import at.posselt.pfrpg2e.kingdom.checkModifiers
import at.posselt.pfrpg2e.kingdom.createExpressionContext
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.data.getChosenGovernment
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateGlobalBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.includeCapital
import at.posselt.pfrpg2e.kingdom.parseSkillRanks
import js.objects.JsPlainObject

@JsPlainObject
external interface RawModifierTotal {
    val bonus: Int
    val penalty: Int
}

@JsPlainObject
external interface RawModifierTotals {
    val item: RawModifierTotal;
    val circumstance: RawModifierTotal;
    val status: RawModifierTotal;
    val ability: RawModifierTotal;
    val proficiency: RawModifierTotal;
    val untyped: RawModifierTotal;
    val leadership: RawModifierTotal;
    val vacancyPenalty: Int
    val value: Int
}

@JsPlainObject
external interface RawSkillStats {
    val skill: String;
    val skillLabel: String;
    val ability: String;
    val abilityLabel: String;
    val rank: Int
    val total: RawModifierTotals;
}

suspend fun calculateSkillModifierBreakdown(kingdom: KingdomData, settlements: SettlementResult): Array<RawSkillStats> {
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
    val chosenFeatures = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
    val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
    val skillRanks = kingdom.parseSkillRanks(
        chosenFeatures,
        chosenFeats,
        kingdom.getChosenGovernment()
    )
    return KingdomSkill.entries.map {
        val filtered = filterModifiersAndUpdateContext(baseModifiers, context.copy(usedSkill = it))
        val evaluatedModifiers = evaluateModifiers(filtered)
        RawSkillStats(
            skill = it.value,
            skillLabel = it.value,
            ability = it.ability.value,
            abilityLabel = it.ability.label,
            rank = skillRanks.resolve(it),
            total = RawModifierTotals(
                item = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.ITEM] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.ITEM] ?: 0,
                ),
                circumstance = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.CIRCUMSTANCE] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.CIRCUMSTANCE] ?: 0,
                ),
                status = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.STATUS] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.STATUS] ?: 0,
                ),
                ability = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.ABILITY] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.ABILITY] ?: 0,
                ),
                proficiency = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.PROFICIENCY] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.PROFICIENCY] ?: 0,
                ),
                untyped = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.UNTYPED] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.UNTYPED] ?: 0,
                ),
                leadership = RawModifierTotal(
                    bonus = evaluatedModifiers.bonuses[ModifierType.LEADERSHIP] ?: 0,
                    penalty = evaluatedModifiers.penalties[ModifierType.LEADERSHIP] ?: 0,
                ),
                vacancyPenalty = evaluatedModifiers.penalties[ModifierType.VACANCY] ?: 0,
                value = evaluatedModifiers.total,
            ),
        )
    }.toTypedArray()
}