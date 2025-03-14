package at.posselt.pfrpg2e.data.kingdom.structures

data class CommodityStorage(
    val ore: Int = 0,
    val food: Int = 0,
    val lumber: Int = 0,
    val stone: Int = 0,
    val luxuries: Int = 0,
) {
    operator fun plus(other: CommodityStorage): CommodityStorage =
        CommodityStorage(
            ore = ore + other.ore,
            food = food + other.food,
            lumber = lumber + other.lumber,
            stone = stone + other.stone,
            luxuries = luxuries + other.luxuries,
        )

    fun limitOre(value: Int) = value.coerceIn(0, ore)
    fun limitLuxuries(value: Int) = value.coerceIn(0, luxuries)
    fun limitLumber(value: Int) = value.coerceIn(0, lumber)
    fun limitStone(value: Int) = value.coerceIn(0, stone)
    fun limitFood(value: Int) = value.coerceIn(0, food)
    fun isNotEmpty() = ore != 0 || food != 0 || lumber != 0 || stone != 0 || food != 0
}