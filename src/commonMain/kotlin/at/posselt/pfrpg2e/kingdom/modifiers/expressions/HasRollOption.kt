package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class HasRollOption(val option: String) : Expression<Boolean> {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.rollOptions.contains(option)
}