package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class When(
    val cases: List<Case>,
    val default: Any?,
) : Expression<Any?> {
    override fun evaluate(context: ExpressionContext): Any? =
        cases.asSequence()
            .mapNotNull { it.evaluate(context) }
            .firstOrNull()
            ?: context.evaluateExpression(default)

    fun evaluateInt(context: ExpressionContext): Int =
        parseIntOr0(evaluate(context))

}