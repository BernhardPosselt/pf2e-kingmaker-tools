package at.posselt.pfrpg2e.kingdom.modifiers.expressions

sealed interface Predicate {
    fun evaluate(context: ExpressionContext): Boolean
}

