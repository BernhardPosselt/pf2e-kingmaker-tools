package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class HasFlag(val flag: String) : Expression<Boolean> {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.flags.contains(flag)
}