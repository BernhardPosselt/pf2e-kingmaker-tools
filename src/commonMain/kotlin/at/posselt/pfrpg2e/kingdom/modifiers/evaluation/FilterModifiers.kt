package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext

data class FilterResult(
    val modifiers: List<Modifier>,
    val context: ExpressionContext,
)

fun filterModifiersAndUpdateContext(
    modifiers: List<Modifier>,
    context: ExpressionContext,
): FilterResult {
    val filteredModifiers = modifiers.asSequence()
        .filter { it.predicates.all { it.evaluate(context) } }
        .toList()
    val enabledRollOptions = filteredModifiers.flatMap { it.rollOptions }.toSet()
    return FilterResult(
        context = context.copy(rollOptions = context.rollOptions + enabledRollOptions),
        modifiers = filteredModifiers,
    )
}

