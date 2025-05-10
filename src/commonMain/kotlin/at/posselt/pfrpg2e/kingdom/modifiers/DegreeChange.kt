package at.posselt.pfrpg2e.kingdom.modifiers

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess

data class DegreeChange(
    val originalDegree: DegreeOfSuccess,
    val changedDegree: DegreeOfSuccess
)


fun determineDegree(
    originalDegree: DegreeOfSuccess,
    upgradeDegrees: Set<UpgradeResult>,
    downgradeDegrees: Set<DowngradeResult>
): DegreeChange {
    val upgradesByDegree = upgradeDegrees.groupBy { it.upgrade }
    val downgradesByDegree = downgradeDegrees.groupBy { it.downgrade }
    var degree = originalDegree
    var resultUpgrade = if (degree in upgradesByDegree) {
        val times = upgradesByDegree[degree]?.maxBy { it.times }?.times ?: 1
        repeat(times) {
            degree = degree.upgrade()
        }
        degree
    } else {
        degree
    }
    val result = if (resultUpgrade in downgradesByDegree) {
        val times = downgradesByDegree[resultUpgrade]?.maxBy { it.times }?.times ?: 1
        repeat(times) {
            resultUpgrade = resultUpgrade.downgrade()
        }
        resultUpgrade
    } else {
        resultUpgrade
    }
    return DegreeChange(
        originalDegree = originalDegree,
        changedDegree = result,
    )
}