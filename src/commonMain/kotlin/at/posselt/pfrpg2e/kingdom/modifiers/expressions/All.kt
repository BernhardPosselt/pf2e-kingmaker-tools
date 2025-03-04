package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class All(val expressions: List<Expression<Boolean>>) : Expression<Boolean> {
    override fun evaluate(context: ExpressionContext): Boolean =
        expressions.map { it.evaluate(context) }.all { it == true }
}