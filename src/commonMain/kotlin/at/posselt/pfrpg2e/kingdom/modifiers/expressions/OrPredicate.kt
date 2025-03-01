package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class OrPredicate(val left: Predicate, val right: Predicate) : Predicate {
    override fun evaluate(context: ExpressionContext): Boolean =
        left.evaluate(context) || right.evaluate(context)
}