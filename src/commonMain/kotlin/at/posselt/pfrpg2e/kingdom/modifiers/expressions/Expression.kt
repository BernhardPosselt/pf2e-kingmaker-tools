package at.posselt.pfrpg2e.kingdom.modifiers.expressions

sealed interface Expression<T> {
    fun evaluate(context: ExpressionContext): T
}

