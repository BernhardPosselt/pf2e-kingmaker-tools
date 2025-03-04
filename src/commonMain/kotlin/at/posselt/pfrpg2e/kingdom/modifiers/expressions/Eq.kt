package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class Eq(val left: Any?, val right: Any?) : Expression<Boolean> {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.evaluateExpression(left) == context.evaluateExpression(right)
}