package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class GtePredicate(val left: String, val right: String) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.evaluateExpression(left).toInt() >= context.evaluateExpression(right).toInt()
}