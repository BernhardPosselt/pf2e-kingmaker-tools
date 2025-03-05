package at.posselt.pfrpg2e.data.kingdom.settlements

data class SettlementSize(
    val type: SettlementSizeType,
    val maximumBlocks: String,
    val requiredKingdomLevel: Int,
    val population: String,
    val consumption: Int,
    val maxItemBonus: Int,
    val influence: Int,
    val levelFrom: Int,
    val levelTo: Int? = null,
)

val settlementSizeData = listOf(
    SettlementSize(
        type = SettlementSizeType.VILLAGE,
        consumption = 1,
        influence = 0,
        maximumBlocks = "1",
        requiredKingdomLevel = 1,
        levelFrom = 1,
        levelTo = 1,
        maxItemBonus = 1,
        population = "400 or less",
    ), SettlementSize(
        type = SettlementSizeType.TOWN,
        consumption = 2,
        influence = 1,
        requiredKingdomLevel = 3,
        maximumBlocks = "4",
        levelFrom = 2,
        levelTo = 4,
        maxItemBonus = 1,
        population = "401-2000",
    ), SettlementSize(
        type = SettlementSizeType.CITY,
        consumption = 4,
        influence = 2,
        requiredKingdomLevel = 9,
        maximumBlocks = "9",
        levelFrom = 5,
        levelTo = 9,
        maxItemBonus = 2,
        population = "2001–25000",
    ), SettlementSize(
        type = SettlementSizeType.METROPOLIS,
        consumption = 6,
        influence = 3,
        maximumBlocks = "10+",
        requiredKingdomLevel = 15,
        levelFrom = 10,
        maxItemBonus = 3,
        population = "25001+",
    )
)

fun findSettlementSize(level: Int) =
    settlementSizeData.find { it.levelFrom <= level
            && (it.levelTo?.let { to -> to >= level } != false) }
        ?: settlementSizeData.first()