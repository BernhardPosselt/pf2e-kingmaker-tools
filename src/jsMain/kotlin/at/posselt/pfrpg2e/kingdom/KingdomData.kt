package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.SkillRanks
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.Ruins
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActor
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderKingdomSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderType
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.awaitAll
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidOfTypes
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ECreature
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.Record
import kotlinx.js.JsPlainObject

typealias AbilityScores = Record<String, Int>
typealias LeaderValue = String // ruler, counselor, general, emissary, magister, treasurer, viceroy,warden
typealias Leaders = Record<LeaderValue, LeaderValues>
typealias RawLeaderType = String // pc, regularNpc, highlyMotivatedNpc, nonPathfinderNpc
typealias GroupRelations = String  // none, diplomatic-relations, trade-agreement
typealias Heartland = String // forest-or-swamp, hill-or-plain, lake-or-river, mountain-or-ruins
typealias FameType = String  // famous or infamous
typealias Companion = String // Amiri Ekundayo Harrim Jaethal Jubilost Kalikke Kanerah Linzi Nok-Nok Octavia Regongar Tristian Valerie
typealias KingdomSkillValue = String // agriculture, arts, boating, defense, engineering, exploration, folklore, industry, intrigue, magic, politics, scholarship, statecraft, trade, warfare, wilderness
typealias SkillValue = String // acrobatics, athletics, etc
typealias RawSkillRanks = Record<KingdomSkillValue, Int>

@JsPlainObject
external interface LeaderValues {
    var uuid: String?
    var invested: Boolean
    var type: RawLeaderType
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
external interface RawWorkSite {
    var quantity: Int
    var resources: Int
}

@JsPlainObject
external interface RawWorkSites {
    var farmlands: RawWorkSite
    var lumberCamps: RawWorkSite
    var mines: RawWorkSite
    var quarries: RawWorkSite
    var luxurySources: RawWorkSite
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
external interface RawFeat {
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
external interface RawConsumption {
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
    var workSites: RawWorkSites
    var heartland: Heartland
    var realmSceneId: String?
    var consumption: RawConsumption
    var notes: Notes
    var homebrewActivities: Array<KingdomActivity>
    var supernaturalSolutions: Int
    var turnsWithoutCultEvent: Int
    var creativeSolutions: Int
    var leaders: Leaders
    var settings: KingdomSettings
    var commodities: CurrentCommodities
    var groups: Array<Group>
    var feats: Array<RawFeat>
    var bonusFeats: Array<BonusFeat>
    var skillRanks: RawSkillRanks
    var abilityScores: AbilityScores
    var ruin: Ruin
    var activeSettlement: String
    var milestones: Array<MileStone>
    var ongoingEvents: Array<OngoingEvent>
    var turnsWithoutEvent: Int
    var activityBlacklist: Array<String>
    var modifiers: Array<RawModifier>
    var settlements: Array<RawSettlement>
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

fun KingdomData.vacancies() =
    Vacancies(
        ruler = leaders[Leader.RULER.value]?.vacant == true,
        counselor = leaders[Leader.COUNSELOR.value]?.vacant == true,
        emissary = leaders[Leader.EMISSARY.value]?.vacant == true,
        general = leaders[Leader.GENERAL.value]?.vacant == true,
        magister = leaders[Leader.MAGISTER.value]?.vacant == true,
        treasurer = leaders[Leader.TREASURER.value]?.vacant == true,
        viceroy = leaders[Leader.VICEROY.value]?.vacant == true,
        warden = leaders[Leader.WARDEN.value]?.vacant == true,
    )

fun KingdomData.parseSkillRanks() =
    KingdomSkillRanks(
        agriculture = skillRanks["agriculture"] ?: 0,
        arts = skillRanks["arts"] ?: 0,
        boating = skillRanks["boating"] ?: 0,
        defense = skillRanks["defense"] ?: 0,
        engineering = skillRanks["engineering"] ?: 0,
        exploration = skillRanks["exploration"] ?: 0,
        folklore = skillRanks["folklore"] ?: 0,
        industry = skillRanks["industry"] ?: 0,
        intrigue = skillRanks["intrigue"] ?: 0,
        magic = skillRanks["magic"] ?: 0,
        politics = skillRanks["politics"] ?: 0,
        scholarship = skillRanks["scholarship"] ?: 0,
        statecraft = skillRanks["statecraft"] ?: 0,
        trade = skillRanks["trade"] ?: 0,
        warfare = skillRanks["warfare"] ?: 0,
        wilderness = skillRanks["wilderness"] ?: 0,
    )

fun KingdomData.getAllActivities(): List<KingdomActivity> {
    val homebrew = homebrewActivities.map { it.id }.toSet()
    return kingdomActivities.filter { it.id !in homebrew } + homebrewActivities
}

fun KingdomData.getActivity(id: String): KingdomActivity? =
    getAllActivities().associateBy { it.id }[id]

fun KingdomData.getAllFeats(): List<RawKingdomFeat> {
    val homebrewFeats = emptySet<String>().toSet()
    return kingdomFeats.filter { it.name !in homebrewFeats } + emptySet()
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

fun KingdomData.getEnabledFeatures(): List<KingdomFeature> {
    return kingdomFeatures
        .flatMap { it.explodeLevels() }
        .sortedWith(compareBy<ExplodedKingdomFeature> { it.level }.thenBy { it.name })
}

fun KingdomData.hasAssurance(skill: KingdomSkill) =
    getChosenFeats().any { it.feat.assuranceForSkill == skill.value }

fun Ruin.parse() = Ruins(
    decayPenalty = decay.penalty,
    strifePenalty = strife.penalty,
    corruptionPenalty = corruption.penalty,
    crimePenalty = crime.penalty,
)

fun KingdomData.parseAbilityScores() = KingdomAbilityScores(
    economy = abilityScores["economy"] ?: 10,
    stability = abilityScores["stability"] ?: 10,
    loyalty = abilityScores["loyalty"] ?: 10,
    culture = abilityScores["culture"] ?: 10
)

private fun PF2ECreature.parseSkillRanks(): SkillRanks =
    SkillRanks(
        acrobatics = skills["acrobatics"]?.rank ?: 0,
        arcana = skills["arcana"]?.rank ?: 0,
        athletics = skills["athletics"]?.rank ?: 0,
        crafting = skills["crafting"]?.rank ?: 0,
        deception = skills["deception"]?.rank ?: 0,
        diplomacy = skills["diplomacy"]?.rank ?: 0,
        intimidation = skills["intimidation"]?.rank ?: 0,
        medicine = skills["medicine"]?.rank ?: 0,
        nature = skills["nature"]?.rank ?: 0,
        occultism = skills["occultism"]?.rank ?: 0,
        performance = skills["performance"]?.rank ?: 0,
        religion = skills["religion"]?.rank ?: 0,
        society = skills["society"]?.rank ?: 0,
        stealth = skills["stealth"]?.rank ?: 0,
        survival = skills["survival"]?.rank ?: 0,
        thievery = skills["thievery"]?.rank ?: 0,
        perception = perception.rank,
        lores = skills.asSequence()
            .map { (key, value) -> Attribute.fromString(key) to value.rank }
            .mapNotNull { (attribute, rank) ->
                if (attribute is Lore) {
                    SkillRanks.LoreRank(attribute.value, rank)
                } else {
                    null
                }
            }
            .toList()
    )

suspend fun KingdomData.parseLeaderActors(): LeaderActors {
    val actorsByType = leaders.asSequence()
        .mapNotNull { (name, values) ->
            Leader.fromString(name)?.let {
                it to values
            }
        }
        .map { (leader, values) ->
            buildPromise {
                values.uuid
                    ?.let { fromUuidOfTypes(it, PF2ECharacter::class, PF2ENpc::class) }
                    ?.let { actor ->
                        val skillRanks = actor.parseSkillRanks()
                        LeaderType.fromString(actor.type)?.let { type ->
                            leader to LeaderActor(actor.level, type, skillRanks, values.invested)
                        }
                    }
            }
        }
        .toList()
        .awaitAll()
        .filterNotNull()
        .toMap()
    return LeaderActors(
        ruler = actorsByType[Leader.RULER],
        counselor = actorsByType[Leader.COUNSELOR],
        emissary = actorsByType[Leader.EMISSARY],
        general = actorsByType[Leader.GENERAL],
        magister = actorsByType[Leader.MAGISTER],
        treasurer = actorsByType[Leader.TREASURER],
        viceroy = actorsByType[Leader.VICEROY],
        warden = actorsByType[Leader.WARDEN]
    )
}

fun RawLeaderSkills.parse() = LeaderSkills(
    ruler = ruler.map { Attribute.fromString(it) },
    counselor = counselor.map { Attribute.fromString(it) },
    emissary = emissary.map { Attribute.fromString(it) },
    general = general.map { Attribute.fromString(it) },
    magister = magister.map { Attribute.fromString(it) },
    treasurer = treasurer.map { Attribute.fromString(it) },
    viceroy = viceroy.map { Attribute.fromString(it) },
    warden = warden.map { Attribute.fromString(it) },
)

fun RawLeaderKingdomSkills.parse() = LeaderKingdomSkills(
    ruler = ruler.mapNotNull { KingdomSkill.fromString(it) },
    counselor = counselor.mapNotNull { KingdomSkill.fromString(it) },
    emissary = emissary.mapNotNull { KingdomSkill.fromString(it) },
    general = general.mapNotNull { KingdomSkill.fromString(it) },
    magister = magister.mapNotNull { KingdomSkill.fromString(it) },
    treasurer = treasurer.mapNotNull { KingdomSkill.fromString(it) },
    viceroy = viceroy.mapNotNull { KingdomSkill.fromString(it) },
    warden = warden.mapNotNull { KingdomSkill.fromString(it) }
)

data class SettlementResult(
    val allSettlements: List<Settlement>,
    val capital: Settlement?,
    val current: Settlement?,
)

fun KingdomData.getAllSettlements(game: Game): SettlementResult {
    val settlementAndActive = settlements.mapNotNull { raw ->
        val scene = game.scenes.get(raw.sceneId)
        if (scene == null) {
            null
        } else {
            val active = raw.sceneId == activeSettlement
            scene.parseSettlement(
                raw,
                settings.autoCalculateSettlementLevel,
                settings.kingdomAllStructureItemBonusesStack
            ) to active
        }
    }
    val allSettlements = settlementAndActive.map { it.component1() }
    return SettlementResult(
        allSettlements = allSettlements,
        capital = allSettlements.find { it.type == SettlementType.CAPITAL },
        current = settlementAndActive.find { it.component2() }?.first,
    )
}



@Deprecated("Do not use this, this should work with more than one sheet")
fun Game.getKingdomActor(): PF2ENpc? =
    actors.contents
        .filterIsInstance<PF2ENpc>()
        .find { it.getKingdom() != null && it.name == "Kingdom Sheet" }
