package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierSelector
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext

data class FilterResult(
    val modifiers: List<Modifier>,
    val context: ExpressionContext,
)

fun filterModifiersAndUpdateContext(
    modifiers: List<Modifier>,
    context: ExpressionContext,
    selector: ModifierSelector,
): FilterResult {
    // only keep modifiers that match selector
    val selectedMods = modifiers.filter { it.selector == selector }
    // first filter applicable modifiers and get their roll options
    val rollOptions = selectedMods.asSequence()
        .filter { it.applyIf.all { it.evaluate(context) } && it.enabled }
        .flatMap { it.rollOptions }
        .toSet()
    val filteredModifiers = selectedMods.asSequence()
        .filter { it.applyIf.all { it.evaluate(context.copy(rollOptions = context.rollOptions + rollOptions)) } }
        .toList()
    val enabledRollOptions = filteredModifiers.flatMap { it.rollOptions }.toSet()
    return FilterResult(
        context = context.copy(rollOptions = context.rollOptions + enabledRollOptions),
        modifiers = filteredModifiers,
    )
}

