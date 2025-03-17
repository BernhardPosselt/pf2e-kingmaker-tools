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
    // first filter applicable modifiers and get their roll options
    val rollOptions = modifiers.asSequence()
        .filter { it.applyIf.all { it.evaluate(context) } && it.enabled }
        .flatMap { it.rollOptions }
        .toSet()
    val filteredModifiers = modifiers.asSequence()
        .filter { it.applyIf.all { it.evaluate(context.copy(rollOptions = context.rollOptions + rollOptions)) } }
        .toList()
    val enabledRollOptions = filteredModifiers.flatMap { it.rollOptions }.toSet()
    return FilterResult(
        context = context.copy(rollOptions = context.rollOptions + enabledRollOptions),
        modifiers = filteredModifiers,
    )
}

