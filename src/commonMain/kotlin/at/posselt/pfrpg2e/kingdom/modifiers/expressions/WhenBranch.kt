package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class WhenBranch(val condition: Predicate, val value: String) {
    fun evaluate(context: ExpressionContext): String? =
        if (condition.evaluate(context)) {
            context.evaluateExpression(value)
        } else {
            null
        }
}