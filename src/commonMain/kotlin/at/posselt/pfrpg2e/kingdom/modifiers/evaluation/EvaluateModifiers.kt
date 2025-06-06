package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.ExpressionContext


private fun evaluateValueExpression(
    modifier: Modifier,
    context: ExpressionContext
): Modifier {
    val valueExpression = modifier.valueExpression
    return if (valueExpression == null) {
        modifier.copy()
    } else {
        modifier.copy(value = valueExpression.evaluateInt(context))
    }
}

private fun applyStackingRules(modifiers: List<Modifier>): List<Modifier> {
    val modifiersByType = modifiers.groupBy { it.type }
    val untypedModifiers = modifiersByType[ModifierType.UNTYPED] ?: emptyList()
    return modifiersByType
        .filter { it.key != ModifierType.UNTYPED }
        .flatMap { mods ->
            val highest = mods.value.filter { it.enabled && it.value > 0 }.maxByOrNull { it.value }
            val lowest = mods.value.filter { it.enabled && it.value < 0 }.minByOrNull { it.value }
            val highestDisabled = mods.value.filter { !it.enabled && it.value > (highest?.value ?: 0) }
                .toSet()
            val lowestDisabled = mods.value.filter { !it.enabled && it.value < (lowest?.value ?: 0) }
                .toSet()
            mods.value
                .filter { it == highest || it == lowest || it in highestDisabled || it in lowestDisabled }
        } + untypedModifiers
}


fun evaluateModifiers(
    filterResult: FilterResult,
): ModifierResult {
    val modifiers = filterResult.modifiers
    val context = filterResult.context
    val evaluatedModifiers = modifiers
        .distinctBy { it.id }
        .map { evaluateValueExpression(it, context) }
    val filteredModifiers = applyStackingRules(evaluatedModifiers)
    val enabledModifiers = filteredModifiers.filter { it.enabled }
    return ModifierResult(
        upgradeResults = enabledModifiers
            .flatMap { it.upgradeResults }
            .toSet(),
        downgradeResults = enabledModifiers
            .flatMap { it.downgradeResults }
            .toSet(),
        rollTwiceKeepHighest = enabledModifiers.any { it.rollTwiceKeepHighest },
        rollTwiceKeepLowest = enabledModifiers.any { it.rollTwiceKeepLowest },
        fortune = enabledModifiers.any { it.fortune },
        modifiers = enabledModifiers,
        filteredModifiers = filteredModifiers,
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