package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext

data class FilterResult(
    val modifiers: List<Modifier>,
    val context: ExpressionContext,
)

fun filterModifiersAndUpdateContext(
    modifiers: List<Modifier>,
    context: ExpressionContext,
    activity: String? = null,
    phase: KingdomPhase? = null,
): FilterResult {
    val filteredModifiers = modifiers.asSequence()
        .filter { it.phases.isEmpty() || it.phases.contains(phase) }
        .filter { it.activities.isEmpty() || it.activities.contains(activity) }
        .filter { it.skills.isEmpty() || it.skills.contains(context.usedSkill) }
        .filter { it.abilities.isEmpty() || it.abilities.contains(context.usedSkill.ability) }
        .filter { it.predicates.all { it.evaluate(context) } }
        .toList()
    val enabledRollOptions = filteredModifiers.flatMap { it.rollOptions }.toSet()
    return FilterResult(
        context = context.copy(rollOptions = context.rollOptions + enabledRollOptions),
        modifiers = filteredModifiers,
    )
}

