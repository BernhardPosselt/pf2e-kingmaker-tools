package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.SkillRanks
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActor
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderKingdomSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderType
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.data.RawFame
import at.posselt.pfrpg2e.kingdom.data.RawFeat
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.kingdom.data.RawLeaderValues
import at.posselt.pfrpg2e.kingdom.data.RawLeaders
import at.posselt.pfrpg2e.kingdom.data.RawNotes
import at.posselt.pfrpg2e.kingdom.data.RawResources
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.fromUuidOfTypes
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ECreature
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.Record
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface RawMileStone {
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
    var ruler: Array<String>
    var counselor: Array<String>
    var emissary: Array<String>
    var general: Array<String>
    var magister: Array<String>
    var treasurer: Array<String>
    var viceroy: Array<String>
    var warden: Array<String>
}

@JsPlainObject
external interface RawLeaderSkills {
    var ruler: Array<String>
    var counselor: Array<String>
    var emissary: Array<String>
    var general: Array<String>
    var magister: Array<String>
    var treasurer: Array<String>
    var viceroy: Array<String>
    var warden: Array<String>
}

@JsPlainObject
external interface KingdomData {
    var name: String
    var atWar: Boolean
    var charter: String // TODO
    var government: String // TODO
    var fame: RawFame
    var level: Int
    var xpThreshold: Int
    var xp: Int
    var size: Int
    var unrest: Int
    var resourcePoints: RawResources
    var resourceDice: RawResources
    var workSites: RawWorkSites
    var heartland: String // TODO
    var realmSceneId: String?
    var consumption: RawConsumption
    var supernaturalSolutions: Int
    var creativeSolutions: Int
    var settings: KingdomSettings
    var commodities: RawCurrentCommodities
    var ruin: RawRuin
    var activeSettlement: String?
    var turnsWithoutCultEvent: Int
    var turnsWithoutEvent: Int
    var notes: RawNotes
    var homebrewActivities: Array<KingdomActivity>
    var homebrewCharters: Array<RawCharter>
    var homebrewGovernments: Array<RawGovernment>
    var homebrewHeartlands: Array<RawHeartland>
    var leaders: RawLeaders
    var groups: Array<RawGroup>  // TODO
    var feats: Array<RawFeat>  // TODO
    var bonusFeats: Array<BonusFeat>  // TODO
    var skillRanks: Record<String, Int>  // TODO
    var abilityScores: Record<String, Int>  // TODO
    var milestones: Array<RawMileStone>  // TODO
    var ongoingEvents: Array<OngoingEvent>  // TODO
    var activityBlacklist: Array<String>  // TODO
    var modifiers: Array<RawModifier>  // TODO
    var settlements: Array<RawSettlement>  // TODO
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
        ruler = leaders.ruler.vacant == true,
        counselor = leaders.counselor.vacant == true,
        emissary = leaders.emissary.vacant == true,
        general = leaders.general.vacant == true,
        magister = leaders.magister.vacant == true,
        treasurer = leaders.treasurer.vacant == true,
        viceroy = leaders.viceroy.vacant == true,
        warden = leaders.warden.vacant == true,
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

fun KingdomData.getEnabledFeatures(): List<KingdomFeature> {
    return kingdomFeatures
        .flatMap { it.explodeLevels() }
        .sortedWith(compareBy<ExplodedKingdomFeature> { it.level }.thenBy { it.name })
}

fun KingdomData.hasAssurance(skill: KingdomSkill) =
    getChosenFeats().any { it.feat.assuranceForSkill == skill.value }

fun KingdomData.parseAbilityScores() = KingdomAbilityScores(
    economy = abilityScores["economy"] ?: 10,
    stability = abilityScores["stability"] ?: 10,
    loyalty = abilityScores["loyalty"] ?: 10,
    culture = abilityScores["culture"] ?: 10
)

fun KingdomData.hasLeaderUuid(uuid: String) =
    leaders.ruler.uuid == uuid ||
            leaders.counselor.uuid == uuid ||
            leaders.emissary.uuid == uuid ||
            leaders.general.uuid == uuid ||
            leaders.magister.uuid == uuid ||
            leaders.treasurer.uuid == uuid ||
            leaders.viceroy.uuid == uuid ||
            leaders.warden.uuid == uuid

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

private suspend fun RawLeaderValues.parseActor(): LeaderActor? =
    uuid?.let { fromUuidOfTypes(it, PF2ECharacter::class, PF2ENpc::class) }
        ?.let { actor ->
            val skillRanks = actor.parseSkillRanks()
            LeaderType.fromString(type)?.let { type ->
                LeaderActor(
                    level = actor.level,
                    type = type,
                    ranks = skillRanks,
                    invested = invested,
                    uuid = actor.uuid,
                    img = actor.img,
                    name = actor.name,
                )
            }
        }

suspend fun KingdomData.parseLeaderActors() = LeaderActors(
    ruler = leaders.ruler.parseActor(),
    counselor = leaders.counselor.parseActor(),
    emissary = leaders.emissary.parseActor(),
    general = leaders.general.parseActor(),
    magister = leaders.magister.parseActor(),
    treasurer = leaders.treasurer.parseActor(),
    viceroy = leaders.viceroy.parseActor(),
    warden = leaders.warden.parseActor(),
)

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
