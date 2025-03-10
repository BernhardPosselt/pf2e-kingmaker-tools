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
        (0..times).forEach {
            degree = degree.upgrade()
        }
        degree
    } else {
        degree
    }
    val result = if (degree in downgradesByDegree) {
        val times = downgradesByDegree[degree]?.maxBy { it.times }?.times ?: 1
        (0..times).forEach {
            resultUpgrade = resultUpgrade.downgrade()
        }
        resultUpgrade
    } else {
        degree
    }
    return DegreeChange(
        originalDegree = originalDegree,
        changedDegree = result,
    )
}