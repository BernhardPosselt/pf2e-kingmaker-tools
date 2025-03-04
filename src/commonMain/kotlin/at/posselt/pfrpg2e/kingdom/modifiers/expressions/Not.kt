package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class Not(val expression: Expression<Boolean>) : Expression<Boolean> {
    override fun evaluate(context: ExpressionContext): Boolean =
        !expression.evaluate(context)
}