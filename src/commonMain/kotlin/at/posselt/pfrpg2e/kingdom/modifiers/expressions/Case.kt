package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class Case(val condition: Expression<Boolean>, val value: Any?): Expression<Any?> {
    override fun evaluate(context: ExpressionContext): Any? =
        if (condition.evaluate(context)) {
            context.evaluateExpression(value)
        } else {
            null
        }
}