package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.array.JsTuple2
import js.objects.Record
import kotlinx.js.JsPlainObject

typealias KingdomAbility = String // culture, economy, loyalty or stability
typealias AbilityScores = Record<KingdomAbility, Int>
typealias LeaderValue = String // ruler, counselor, general, emissary, magister, treasurer, viceroy,warden
typealias Leaders = Record<LeaderValue, LeaderValues>
typealias LeaderType = String // pc, regularNpc, highlyMotivatedNpc, nonPathfinderNpc
typealias GroupRelations = String  // none, diplomatic-relations, trade-agreement
typealias KingdomPhase = String  // army, civic, commerce, event, leadership, region, upkeep
typealias Heartland = String // forest-or-swamp, hill-or-plain, lake-or-river, mountain-or-ruins
typealias FameType = String  // famous or infamous
typealias Companion = String // Amiri Ekundayo Harrim Jaethal Jubilost Kalikke Kanerah Linzi Nok-Nok Octavia Regongar Tristian Valerie
typealias KingdomSkillValue = String // agriculture, arts, boating, defense, engineering, exploration, folklore, industry, intrigue, magic, politics, scholarship, statecraft, trade, warfare, wilderness
typealias SkillValue = String // acrobatics, athletics, etc
typealias SkillRanks = Record<KingdomSkillValue, Int>

sealed external interface RawPredicate

@JsPlainObject
external interface RawGtePredicate: RawPredicate {
    val gte: JsTuple2<String, String>
}

@JsPlainObject
external interface RawGtPredicate: RawPredicate {
    val gt: JsTuple2<String, String>
}

@JsPlainObject
external interface RawLtePredicate: RawPredicate {
    val lte: JsTuple2<String, String>
}

@JsPlainObject
external interface RawInPredicate: RawPredicate {
    val `in`: JsTuple2<String, Array<String>>
}

@JsPlainObject
external interface RawLtPredicate: RawPredicate {
    val lt: JsTuple2<String, String>
}

@JsPlainObject
external interface RawEqPredicate: RawPredicate {
    val eq: JsTuple2<String, String>
}

@JsPlainObject
external interface RawOrPredicate: RawPredicate {
    val or: JsTuple2<RawPredicate, RawPredicate>
}

@JsPlainObject
external interface RawAndPredicate: RawPredicate {
    val and: JsTuple2<RawPredicate, RawPredicate>
}

@JsPlainObject
external interface RawNotPredicate: RawPredicate {
    val not: RawPredicate
}

@JsPlainObject
external interface RawHasFlagPredicate: RawPredicate {
    val hasFlag: String
}

@JsPlainObject
external interface RawHasRollOptionPredicate: RawPredicate {
    val hasRollOption: String
}

@JsPlainObject
external interface RawWhenPredicate {
    val `when`: JsTuple2<RawPredicate, String>
}

@JsPlainObject
external interface RawModifier {
    var type: String
    var value: Int
    var predicatedValue: RawWhenPredicate?
    var name: String
    var enabled: Boolean
    var turns: Int?
    var isConsumedAfterRoll: Boolean?
    var rollOptions: Array<String>?
    var predicates: Array<RawPredicate>?
}

@JsPlainObject
external interface LeaderValues {
    var uuid: String?
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
    var type: String
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
external interface Resources {
    var now: Int
    var next: Int
}

@JsPlainObject
external interface KingdomSettings {
    var rpToXpConversionRate: Int
    var rpToXpConversionLimit: Int
    var resourceDicePerVillage: Int
    var resourceDicePerTown: Int
    var resourceDicePerCity: Int
    var resourceDicePerMetropolis: Int
    var xpPerClaimedHex: Int
    var includeCapitalItemModifier: Boolean
    var cultOfTheBloomEvents: Boolean
    var autoCalculateSettlementLevel: Boolean
    var vanceAndKerensharaXP: Boolean
    var capitalInvestmentInCapital: Boolean
    var reduceDCToBuildLumberStructures: Boolean
    var kingdomSkillIncreaseEveryLevel: Boolean
    var kingdomAllStructureItemBonusesStack: Boolean
    var kingdomIgnoreSkillRequirements: Boolean
    var autoCalculateArmyConsumption: Boolean
    var enableLeadershipModifiers: Boolean
    var expandMagicUse: Boolean
    var kingdomEventRollMode: String
    var automateResources: String
    var proficiencyMode: String
    var kingdomEventsTable: String?
    var kingdomCultTable: String?
    var maximumFamePoints: Int
    var leaderKingdomSkills: RawLeaderKingdomSkills
    var leaderSkills: RawLeaderSkills
}

@JsPlainObject
external interface RawLeaderKingdomSkills {
    var ruler: Array<KingdomSkillValue>
    var counselor: Array<KingdomSkillValue>
    var emissary: Array<KingdomSkillValue>
    var general: Array<KingdomSkillValue>
    var magister: Array<KingdomSkillValue>
    var treasurer: Array<KingdomSkillValue>
    var viceroy: Array<KingdomSkillValue>
    var warden: Array<KingdomSkillValue>
}

@JsPlainObject
external interface RawLeaderSkills {
    var ruler: Array<SkillValue>
    var counselor: Array<SkillValue>
    var emissary: Array<SkillValue>
    var general: Array<SkillValue>
    var magister: Array<SkillValue>
    var treasurer: Array<SkillValue>
    var viceroy: Array<SkillValue>
    var warden: Array<SkillValue>
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
    var modifiers: Array<RawModifier>
    var settlements: Array<Settlement>
}

fun RawLeaderKingdomSkills.hasSkill(leader: Leader, skill: KingdomSkill) =
    when (leader) {
        Leader.RULER -> ruler.contains(skill.value)
        Leader.COUNSELOR -> counselor.contains(skill.value)
        Leader.EMISSARY -> emissary.contains(skill.value)
        Leader.GENERAL -> general.contains(skill.value)
        Leader.MAGISTER -> magister.contains(skill.value)
        Leader.TREASURER -> treasurer.contains(skill.value)
        Leader.VICEROY -> viceroy.contains(skill.value)
        Leader.WARDEN -> warden.contains(skill.value)
    }

fun RawLeaderSkills.hasAttribute(leader: Leader, attribute: Attribute) =
    when (leader) {
        Leader.RULER -> ruler.contains(attribute.value)
        Leader.COUNSELOR -> counselor.contains(attribute.value)
        Leader.EMISSARY -> emissary.contains(attribute.value)
        Leader.GENERAL -> general.contains(attribute.value)
        Leader.MAGISTER -> magister.contains(attribute.value)
        Leader.TREASURER -> treasurer.contains(attribute.value)
        Leader.VICEROY -> viceroy.contains(attribute.value)
        Leader.WARDEN -> warden.contains(attribute.value)
    }

fun RawLeaderSkills.deleteLore(attribute: Attribute) = RawLeaderSkills(
    ruler = ruler.filter { it != attribute.value }.toTypedArray(),
    counselor = counselor.filter { it != attribute.value }.toTypedArray(),
    emissary = emissary.filter { it != attribute.value }.toTypedArray(),
    general = general.filter { it != attribute.value }.toTypedArray(),
    magister = magister.filter { it != attribute.value }.toTypedArray(),
    treasurer = treasurer.filter { it != attribute.value }.toTypedArray(),
    viceroy = viceroy.filter { it != attribute.value }.toTypedArray(),
    warden = warden.filter { it != attribute.value }.toTypedArray(),
)

fun Game.getKingdomActor(): PF2ENpc? =
    actors.contents
        .filterIsInstance<PF2ENpc>()
        .find { it.getKingdom() != null && it.name == "Kingdom Sheet"}
