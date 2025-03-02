package at.posselt.pfrpg2e.data.kingdom.structures

data class AvailableItemsRule(
    val value: Int,
    val group: ItemGroup,
    val maximumStacks: Int,
)