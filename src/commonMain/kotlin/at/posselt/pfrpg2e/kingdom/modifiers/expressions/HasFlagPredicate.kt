package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class HasFlagPredicate(val flag: String) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.flags.contains(flag)
}