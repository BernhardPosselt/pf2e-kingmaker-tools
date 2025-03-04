package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class In(val needle: Any?, val haystack: List<Any?>) : Expression<Boolean> {
    override fun evaluate(context: ExpressionContext): Boolean =
        haystack.map { context.evaluateExpression(it) }.contains(context.evaluateExpression(needle))
}