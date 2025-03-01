package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext

// TODO
// filter by activities
// filter by skills
// filter by phase
// filter by roll options
// filter by flags

private fun evaluatePredicatedValue(modifier: Modifier, context: ExpressionContext): Modifier {
    val predicatedValue = modifier.predicatedValue
    return if (predicatedValue == null) {
        modifier.copy()
    } else {
        modifier.copy(value = predicatedValue.evaluate(context)?.toInt() ?: 0)
    }
}

private fun applyStackingRules(modifiers: List<Modifier>): List<Modifier> {
    val modifiersByType = modifiers.groupBy { it.type }
    return modifiersByType.flatMap {
        val highestPositive = it.value.filter { it.value >= 0 }.maxByOrNull { it.value }
        val lowestNegative = it.value.filter { it.value < 0 }.minByOrNull { it.value }
        it.value.map {
            it.copy(enabled = it == highestPositive || it == lowestNegative)
        }
    }
}


fun evaluateModifiers(
    modifiers: List<Modifier>,
    context: ExpressionContext,
): ModifierResult {
    val evaluatedModifiers = modifiers.map { evaluatePredicatedValue(it, context) }
    val filteredModifiers = applyStackingRules(evaluatedModifiers)
    val enabledModifiers = filteredModifiers.filter { it.enabled }
    return ModifierResult(
        modifiers = filteredModifiers,
        total = enabledModifiers.sumOf { it.value },
        bonuses = enabledModifiers
            .filter { it.value >= 0 }
            .associate {
                it.type to it.value
            },
        penalties = enabledModifiers
            .filter { it.value < 0 }
            .associate {
                it.type to it.value
            },
        rollOptions = enabledModifiers.flatMap { it.rollOptions }.toSet()
    )
}