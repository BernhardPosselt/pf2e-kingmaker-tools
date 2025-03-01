package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class HasRollOptionPredicate(val option: String) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        context.rollOptions.contains(option)
}