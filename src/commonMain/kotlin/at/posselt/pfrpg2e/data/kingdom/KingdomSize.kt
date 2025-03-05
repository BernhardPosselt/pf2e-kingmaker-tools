package at.posselt.pfrpg2e.data.kingdom

data class KingdomSize(
    val sizeFrom: Int,
    val type: KingdomSizeType,
    val resourceDieSize: ResourceDieSize,
    val controlDCModifier: Int,
    val commodityCapacity: Int,
    val sizeTo: Int? = null,
)

val kingdomSizeData = listOf(
    KingdomSize(
        sizeFrom = 0,
        sizeTo = 9,
        type = KingdomSizeType.TERRITORY,
        resourceDieSize = ResourceDieSize.D4,
        controlDCModifier = 0,
        commodityCapacity = 4,
    ), KingdomSize(
        sizeFrom = 10,
        sizeTo = 24,
        type = KingdomSizeType.PROVINCE,
        resourceDieSize = ResourceDieSize.D6,
        controlDCModifier = 1,
        commodityCapacity = 8,
    ), KingdomSize(
        sizeFrom = 25,
        sizeTo = 49,
        type = KingdomSizeType.STATE,
        resourceDieSize = ResourceDieSize.D8,
        controlDCModifier = 2,
        commodityCapacity = 12,
    ), KingdomSize(
        sizeFrom = 50,
        sizeTo = 99,
        type = KingdomSizeType.COUNTRY,
        resourceDieSize = ResourceDieSize.D10,
        controlDCModifier = 3,
        commodityCapacity = 16,
    ), KingdomSize(
        sizeFrom = 100,
        type = KingdomSizeType.DOMINION,
        resourceDieSize = ResourceDieSize.D12,
        controlDCModifier = 4,
        commodityCapacity = 20,
    )
)

fun findKingdomSize(size: Int) =
    kingdomSizeData.find { it.sizeFrom <= size
            && (it.sizeTo?.let { to -> to >= size } != false) }
        ?: kingdomSizeData.first()