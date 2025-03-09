package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.getFeats

data class ChosenFeat(
    val takenAtLevel: Int,
    val feat: RawKingdomFeat,
)

fun KingdomData.getChosenFeats(
    chosenFeatures: List<ChosenFeature>
): List<ChosenFeat> {
    val featsById = getFeats().associateBy { it.id }
    val bonusFeatId = getChosenGovernment()?.bonusFeat
    val bonusFeat = featsById[bonusFeatId]?.let { ChosenFeat(takenAtLevel = 0, it) }
    val explodedFeatureById = getExplodedFeatures().associateBy { it.id }
    return chosenFeatures
        .map { it.choice }
        .mapNotNull { feature ->
            feature.featId?.let { featId ->
                featsById[featId]?.let { feat ->
                    explodedFeatureById[feature.id]?.let {
                        ChosenFeat(takenAtLevel = it.level, feat)
                    }
                }
            }
        } + listOfNotNull(bonusFeat)
}
