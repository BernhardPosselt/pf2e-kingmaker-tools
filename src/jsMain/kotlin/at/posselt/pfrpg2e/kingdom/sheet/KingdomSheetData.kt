package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawCharterChoices
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.data.RawFame
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.kingdom.data.RawHeartlandChoices
import at.posselt.pfrpg2e.kingdom.data.RawLeaders
import at.posselt.pfrpg2e.kingdom.data.RawNotes
import at.posselt.pfrpg2e.kingdom.data.RawResources
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface KingdomSheetData {
    val name: String
    val atWar: Boolean
    val fame: RawFame
    val level: Int
    val xpThreshold: Int
    val xp: Int
    val size: Int
    val unrest: Int
    val resourcePoints: RawResources
    val resourceDice: RawResources
    val workSites: RawWorkSites
    val consumption: RawConsumption
    val supernaturalSolutions: Int
    val creativeSolutions: Int
    val commodities: RawCurrentCommodities
    val ruin: RawRuin
    val activeSettlement: String?
    val notes: RawNotes
    val leaders: RawLeaders
    var charter: RawCharterChoices
    var heartland: RawHeartlandChoices
    var government: RawGovernmentChoices
    var abilityBoosts: RawAbilityBoostChoices
    var features: Array<RawFeatureChoices>
    var bonusFeats: Array<RawBonusFeat>
}