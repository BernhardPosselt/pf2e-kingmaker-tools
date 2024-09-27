package at.posselt.pfrpg2e.kingdom

import js.objects.Record
import kotlinx.js.JsPlainObject

typealias KingdomAbility = String // culture, economy, loyalty or stability
typealias AbilityScores = Record<KingdomAbility, Int>
typealias Leader = String // ruler, counselor, general, emissary, magister, treasurer, viceroy,warden
typealias Leaders = Record<Leader, LeaderValues>
typealias ModifierType = String // ability, proficiency, item, status, circumstance, vacancy, untyped
typealias LeaderType = String // pc, npc or companion
typealias GroupRelations = String  // none, diplomatic-relations, trade-agreement
typealias KingdomPhase = String  // army, civic, commerce, event, leadership, region, upkeep
typealias Heartland = String // forest-or-swamp, hill-or-plain, lake-or-river, mountain-or-ruins
typealias FameType = String  // famous or infamous
typealias Companion = String // Amiri Ekundayo Harrim Jaethal Jubilost Kalikke Kanerah Linzi Nok-Nok Octavia Regongar Tristian Valerie
typealias KingdomDc = Any // number or control, custom, none, scouting
typealias KingdomSkill = String // agriculture, arts, boating, defense, engineering, exploration, folklore, industry, intrigue, magic, politics, scholarship, statecraft, trade, warfare, wilderness
typealias SkillRanks = Record<KingdomSkill, Int>
typealias SettlementType = String // capital or settlement

@JsPlainObject
external interface Modifier {
    var type: ModifierType
    var value: Int
    var name: String
    var phases: Array<KingdomPhase>?
    var activities: Array<String>?
    var skills: Array<KingdomSkill>?
    var abilities: Array<KingdomAbility>?
    var enabled: Boolean
    var turns: Int?
    var consumeId: String?
    var rollOptions: Array<String>?
}

@JsPlainObject
external interface LeaderValues {
    var name: String
    var invested: Boolean
    var type: LeaderType
    var vacant: Boolean
}


@JsPlainObject
external interface RuinValues {
    var value: Int
    var penalty: Int
    var threshold: Int
}

@JsPlainObject
external interface Ruin {
    var corruption: RuinValues
    var crime: RuinValues
    var decay: RuinValues
    var strife: RuinValues
}

@JsPlainObject
external interface Group {
    var name: String
    var negotiationDC: Int
    var atWar: Boolean
    var relations: GroupRelations
}

@JsPlainObject
external interface WorkSite {
    var quantity: Int
    var resources: Int
}

@JsPlainObject
external interface WorkSites {
    var farmlands: WorkSite
    var lumberCamps: WorkSite
    var mines: WorkSite
    var quarries: WorkSite
    var luxurySources: WorkSite
}

@JsPlainObject
external interface Commodities {
    var food: Int
    var lumber: Int
    var luxuries: Int
    var ore: Int
    var stone: Int
}

@JsPlainObject
external interface CurrentCommodities {
    var now: Commodities
    var next: Commodities
}

@JsPlainObject
external interface Feat {
    var id: String
    var level: Int
}

@JsPlainObject
external interface MileStone {
    var name: String
    var xp: Int
    var completed: Boolean
    var homebrew: Boolean
}

@JsPlainObject
external interface OngoingEvent {
    var name: String
}

@JsPlainObject
external interface Settlement {
    var sceneId: String
    var lots: Int
    var level: Int
    var type: SettlementType
    var secondaryTerritory: Boolean
    var manualSettlementLevel: Boolean?
    var waterBorders: Int
}

@JsPlainObject
external interface BonusFeat {
    var id: String
}

@JsPlainObject
external interface Fame {
    var now: Int
    var next: Int
    var max: Int
    var type: FameType
}

@JsPlainObject
external interface Consumption {
    var armies: Int
    var now: Int
    var next: Int
}

@JsPlainObject
external interface Notes {
    var public: String
    var gm: String
}

@JsPlainObject
external interface ActivityResult {
    var msg: String
//    modifiers?: (kingdom: Kingdom) => Modifier[];
}

@JsPlainObject
external interface ActivityResults {
    var criticalSuccess: ActivityResult?
    var success: ActivityResult?
    var failure: ActivityResult?
    var criticalFailure: ActivityResult?
}

@JsPlainObject
external interface ActivityContent : ActivityResults {
    var title: String
    var description: String
    var requirement: String?
    var special: String?
    var skills: SkillRanks
    var phase: KingdomPhase
    var dc: KingdomDc
    var dcAdjustment: Int?
    var enabled: Boolean
    var companion: Companion?
    var fortune: Boolean
    var oncePerRound: Boolean
    var hint: String?
}

@JsPlainObject
external interface KingdomActivity : ActivityContent {
    val id: String
}

@JsPlainObject
external interface Resources {
    var now: Int
    var next: Int
}

@JsPlainObject
external interface KingdomSettings {
    var expandMagicUse: Boolean
}


@JsPlainObject
external interface KingdomData {
    var name: String
    var atWar: Boolean
    var charter: String
    var government: String
    var fame: Fame
    var level: Int
    var xpThreshold: Int
    var xp: Int
    var size: Int
    var unrest: Int
    var resourcePoints: Resources
    var resourceDice: Resources
    var workSites: WorkSites
    var heartland: Heartland
    var realmSceneId: String?
    var consumption: Consumption
    var notes: Notes
    var homebrewActivities: Array<KingdomActivity>
    var supernaturalSolutions: Int
    var turnsWithoutCultEvent: Int
    var creativeSolutions: Int
    var leaders: Leaders
    var settings: KingdomSettings
    var commodities: CurrentCommodities
    var groups: Array<Group>
    var feats: Array<Feat>
    var bonusFeats: Array<BonusFeat>
    var skillRanks: SkillRanks
    var abilityScores: AbilityScores
    var ruin: Ruin
    var activeSettlement: String
    var milestones: Array<MileStone>
    var ongoingEvents: Array<OngoingEvent>
    var turnsWithoutEvent: Int
    var activityBlacklist: Array<String>
    var modifiers: Array<Modifier>
    var settlements: Array<Settlement>
}
