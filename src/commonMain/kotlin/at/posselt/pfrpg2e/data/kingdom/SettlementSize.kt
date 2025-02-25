package at.posselt.pfrpg2e.data.kingdom

data class SettlementSize(
    val type: SettlementType,
    val maximumLots: String,
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
        type = SettlementType.VILLAGE,
        consumption = 1,
        influence = 0,
        maximumLots = "1",
        requiredKingdomLevel = 1,
        levelFrom = 1,
        levelTo = 1,
        maxItemBonus = 1,
        population = "400 or less",
    ), SettlementSize(
        type = SettlementType.TOWN,
        consumption = 2,
        influence = 1,
        requiredKingdomLevel = 3,
        maximumLots = "4",
        levelFrom = 2,
        levelTo = 4,
        maxItemBonus = 1,
        population = "401-2000",
    ), SettlementSize(
        type = SettlementType.CITY,
        consumption = 4,
        influence = 2,
        requiredKingdomLevel = 9,
        maximumLots = "9",
        levelFrom = 5,
        levelTo = 9,
        maxItemBonus = 2,
        population = "2001–25000",
    ), SettlementSize(
        type = SettlementType.METROPOLIS,
        consumption = 6,
        influence = 3,
        maximumLots = "10+",
        requiredKingdomLevel = 15,
        levelFrom = 10,
        maxItemBonus = 3,
        population = "25001+",
    )
)