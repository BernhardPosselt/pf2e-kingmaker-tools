package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class EqPredicate(val left: String, val right: String) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.evaluateExpression(left) == context.evaluateExpression(right)
}