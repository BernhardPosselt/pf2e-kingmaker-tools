package at.posselt.pfrpg2e.data.kingdom

fun calculateControlDC(
    kingdomLevel: Int,
    realm: RealmData,
    rulerVacant: Boolean
): Int {
    val sizeModifier = findKingdomSize(realm.size).controlDCModifier
    val adjustedLevel = if (kingdomLevel < 5) kingdomLevel - 1 else kingdomLevel
    val vacancyPenalty = if (rulerVacant) 2 else 0
    return 14 + adjustedLevel + adjustedLevel / 3 + sizeModifier + vacancyPenalty
}