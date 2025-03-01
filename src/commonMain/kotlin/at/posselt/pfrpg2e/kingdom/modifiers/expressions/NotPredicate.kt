package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class NotPredicate(val predicate: Predicate) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        !predicate.evaluate(context)
}