package at.posselt.pfrpg2e.kingdom.modifiers.expressions

data class When(val branches: List<WhenBranch>, val default: String) {
    fun evaluate(context: ExpressionContext) =
        branches.asSequence()
            .mapNotNull { it.evaluate(context) }
            .firstOrNull()
            ?: default
}