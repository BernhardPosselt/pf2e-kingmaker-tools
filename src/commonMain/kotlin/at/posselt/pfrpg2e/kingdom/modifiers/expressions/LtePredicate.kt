package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class LtePredicate(val left: String, val right: String) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        (context.evaluateExpression(left)?.toInt() ?: 0) <= (context.evaluateExpression(right)?.toInt() ?: 0)
}