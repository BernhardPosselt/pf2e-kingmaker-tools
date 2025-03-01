package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class InPredicate(val needle: String, val haystack: Set<String>) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        haystack.map { context.evaluateExpression(it) }.contains(context.evaluateExpression(needle))
}