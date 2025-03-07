package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawKingdomFeat
import at.posselt.pfrpg2e.kingdom.kingdomFeats
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawFeat {
    var id: String
    var level: Int
}

data class ChosenFeat(
    val takenAtLevel: Int,
    val feat: RawKingdomFeat,
)

fun KingdomData.getChosenFeats(): List<ChosenFeat> {
    val featsByName = getAllFeats().associateBy { it.name }
    return feats.mapNotNull { feat ->
        featsByName[feat.id]?.let {
            ChosenFeat(takenAtLevel = feat.level, it)
        }
    }
}

fun KingdomData.getAllFeats(): List<RawKingdomFeat> {
    val homebrewFeats = emptySet<String>().toSet()
    return kingdomFeats.filter { it.name !in homebrewFeats } + emptySet()
}
