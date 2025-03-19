package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.SkillRanks
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.RuinValues
import at.posselt.pfrpg2e.data.kingdom.calculateScore
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActor
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderKingdomSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderType
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.ChosenFeature
import at.posselt.pfrpg2e.kingdom.data.MilestoneChoice
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawAbilityScores
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawCharterChoices
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.data.RawFame
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.kingdom.data.RawGroup
import at.posselt.pfrpg2e.kingdom.data.RawHeartlandChoices
import at.posselt.pfrpg2e.kingdom.data.RawLeaderValues
import at.posselt.pfrpg2e.kingdom.data.RawLeaders
import at.posselt.pfrpg2e.kingdom.data.RawNotes
import at.posselt.pfrpg2e.kingdom.data.RawResources
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawSkillRanks
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import at.posselt.pfrpg2e.kingdom.data.RuinThresholdIncreases
import at.posselt.pfrpg2e.kingdom.data.getBoosts
import at.posselt.pfrpg2e.kingdom.data.parse
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.fromUuidOfTypes
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ECreature
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface OngoingEvent {
    var name: String
}

@JsPlainObject
external interface KingdomSettings {
    var rpToXpConversionRate: Int
    var rpToXpConversionLimit: Int
    var resourceDicePerVillage: Int
    var resourceDicePerTown: Int
    var resourceDicePerCity: Int
    var ruinThreshold: Int
    var increaseScorePicksBy: Int
    var realmSceneId: String?
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
    var automateStats: Boolean
    var recruitableArmiesFolderId: String?
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
    var fame: RawFame
    var level: Int
    var xpThreshold: Int
    var xp: Int
    var size: Int
    var unrest: Int
    var resourcePoints: RawResources
    var resourceDice: RawResources
    var workSites: RawWorkSites
    var consumption: RawConsumption
    var supernaturalSolutions: Int
    var creativeSolutions: Int
    var settings: KingdomSettings
    var commodities: RawCurrentCommodities
    var ruin: RawRuin
    var activeSettlement: String?
    var turnsWithoutCultEvent: Int // set via button
    var turnsWithoutEvent: Int // set via button
    var notes: RawNotes
    var homebrewMilestones: Array<RawMilestone>
    var homebrewActivities: Array<RawActivity>
    var homebrewCharters: Array<RawCharter>
    var homebrewGovernments: Array<RawGovernment>
    var homebrewHeartlands: Array<RawHeartland>
    var homebrewFeats: Array<RawFeat>
    var activityBlacklist: Array<String>
    var featBlacklist: Array<String>
    var heartlandBlacklist: Array<String>
    var charterBlacklist: Array<String>
    var governmentBlacklist: Array<String>
    var modifiers: Array<RawModifier>  // set via dialog
    var settlements: Array<RawSettlement>  // set via dialog
    var leaders: RawLeaders
    var charter: RawCharterChoices
    var heartland: RawHeartlandChoices
    var government: RawGovernmentChoices
    var abilityBoosts: RawAbilityBoostChoices
    var features: Array<RawFeatureChoices>
    var bonusFeats: Array<RawBonusFeat>
    var ongoingEvents: Array<OngoingEvent>
    var groups: Array<RawGroup>
    var skillRanks: RawSkillRanks
    var abilityScores: RawAbilityScores
    var initialProficiencies: Array<String>
    var milestones: Array<MilestoneChoice>
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
        ruler = leaders.ruler.vacant == true || leaders.ruler.uuid == null,
        counselor = leaders.counselor.vacant == true || leaders.counselor.uuid == null,
        emissary = leaders.emissary.vacant == true || leaders.emissary.uuid == null,
        general = leaders.general.vacant == true || leaders.general.uuid == null,
        magister = leaders.magister.vacant == true || leaders.magister.uuid == null,
        treasurer = leaders.treasurer.vacant == true || leaders.treasurer.uuid == null,
        viceroy = leaders.viceroy.vacant == true || leaders.viceroy.uuid == null,
        warden = leaders.warden.vacant == true || leaders.warden.uuid == null,
    )

fun KingdomData.getTrainedSkills(
    chosenFeats: List<ChosenFeat>,
    government: RawGovernment?,
): Set<KingdomSkill> {
    val government = government?.skillProficiencies ?: emptyArray()
    val initial = initialProficiencies
    val feats = chosenFeats.mapNotNull { it.feat.trainSkill }
    return (government + feats + initial)
        .mapNotNull { KingdomSkill.fromString(it) }
        .toSet()
}

fun KingdomData.parseSkillRanks(
    chosenFeatures: List<ChosenFeature>,
    chosenFeats: List<ChosenFeat>,
    government: RawGovernment?,
) = if (settings.automateStats) {
    val skillIncreases = chosenFeatures
        .mapNotNull {
            it.choice.skillIncrease?.let { KingdomSkill.fromString(it) }
        }.groupBy { it }
    val skillTrainings = getTrainedSkills(chosenFeats, government)
    val ranks = KingdomSkill.entries.associate {
        val base = if (it in skillTrainings) 1 else 0
        val increase = skillIncreases[it]?.size ?: 0
        it to (base + increase).coerceIn(0, 4)
    }
    KingdomSkillRanks(
        agriculture = ranks[KingdomSkill.AGRICULTURE] ?: 0,
        arts = ranks[KingdomSkill.ARTS] ?: 0,
        boating = ranks[KingdomSkill.BOATING] ?: 0,
        defense = ranks[KingdomSkill.DEFENSE] ?: 0,
        engineering = ranks[KingdomSkill.ENGINEERING] ?: 0,
        exploration = ranks[KingdomSkill.EXPLORATION] ?: 0,
        folklore = ranks[KingdomSkill.FOLKLORE] ?: 0,
        industry = ranks[KingdomSkill.INDUSTRY] ?: 0,
        intrigue = ranks[KingdomSkill.INTRIGUE] ?: 0,
        magic = ranks[KingdomSkill.MAGIC] ?: 0,
        politics = ranks[KingdomSkill.POLITICS] ?: 0,
        scholarship = ranks[KingdomSkill.SCHOLARSHIP] ?: 0,
        statecraft = ranks[KingdomSkill.STATECRAFT] ?: 0,
        trade = ranks[KingdomSkill.TRADE] ?: 0,
        warfare = ranks[KingdomSkill.WARFARE] ?: 0,
        wilderness = ranks[KingdomSkill.WILDERNESS] ?: 0,
    )
} else {
    KingdomSkillRanks(
        agriculture = skillRanks.agriculture,
        arts = skillRanks.arts,
        boating = skillRanks.boating,
        defense = skillRanks.defense,
        engineering = skillRanks.engineering,
        exploration = skillRanks.exploration,
        folklore = skillRanks.folklore,
        industry = skillRanks.industry,
        intrigue = skillRanks.intrigue,
        magic = skillRanks.magic,
        politics = skillRanks.politics,
        scholarship = skillRanks.scholarship,
        statecraft = skillRanks.statecraft,
        trade = skillRanks.trade,
        warfare = skillRanks.warfare,
        wilderness = skillRanks.wilderness,
    )
}

fun KingdomData.getAllActivities(): List<RawActivity> {
    val homebrew = homebrewActivities.map { it.id }.toSet()
    return kingdomActivities.filter { it.id !in homebrew } + homebrewActivities
}

fun KingdomData.getActivity(id: String): RawActivity? =
    getAllActivities().associateBy { it.id }[id]

fun KingdomData.hasAssurance(
    chosenFeats: List<ChosenFeat>,
    skill: KingdomSkill
) = chosenFeats.any { it.feat.assuranceForSkill == skill.value }


fun KingdomData.parseRuins(
    choices: List<ChosenFeature>,
    baseThreshold: Int,
): RuinValues {
    val defaults = ruin.parse()
    return if (settings.automateStats) {
        val choiceIncreases = choices.map { it.choice }
            .flatMap { listOfNotNull(it.ruinThresholdIncreases) + it.featRuinThresholdIncreases }
        val bonusFeatIncreases = bonusFeats.flatMap { it.ruinThresholdIncreases.toList() }
        val increases = (choiceIncreases + bonusFeatIncreases)
            .map { it.parse() }
            .fold(RuinThresholdIncreases()) { prev, curr -> prev + curr }
        defaults.copy(
            decay = defaults.decay.copy(threshold = increases.decay + baseThreshold),
            strife = defaults.strife.copy(threshold = increases.strife + baseThreshold),
            corruption = defaults.corruption.copy(threshold = increases.corruption + baseThreshold),
            crime = defaults.crime.copy(threshold = increases.crime + baseThreshold),
        )
    } else {
        defaults
    }
}

fun KingdomData.parseAbilityScores(
    chosenCharter: RawCharter?,
    chosenHeartland: RawHeartland?,
    chosenGovernment: RawGovernment?,
    chosenFeatures: List<ChosenFeature>,
) = if (settings.automateStats) {
    val charterBoosts: List<String> = charter.abilityBoosts.getBoosts()
    val charterBoost: List<String> = listOfNotNull(chosenCharter?.boost)
    val charterFlaw = chosenCharter?.flaw
    val heartlandBoost: List<String> = listOfNotNull(chosenHeartland?.boost)
    val governmentBoosts: List<String> = chosenGovernment?.boosts?.toList().orEmpty()
    val governmentBoostChoices: List<String> = government.abilityBoosts.getBoosts()
    val featureBoosts: List<String> = chosenFeatures.mapNotNull { it.choice.abilityBoosts?.getBoosts() }
        .flatMap { it }
    val additionalBoosts = abilityBoosts.getBoosts()
    val boostsByAbility =
        (charterBoosts +
                charterBoost +
                heartlandBoost +
                governmentBoosts +
                governmentBoostChoices +
                featureBoosts +
                additionalBoosts)
            .mapNotNull { KingdomAbility.fromString(it) }
            .groupBy { it }
    val flawsByAbility = listOfNotNull(charterFlaw)
        .mapNotNull { KingdomAbility.fromString(it) }
        .groupBy { it }
    val boosts = KingdomAbility.entries.associate {
        it to calculateScore(boostsByAbility[it]?.size ?: 0, flawsByAbility[it]?.size ?: 0)
    }

    KingdomAbilityScores(
        economy = boosts[KingdomAbility.ECONOMY] ?: 10,
        stability = boosts[KingdomAbility.STABILITY] ?: 10,
        loyalty = boosts[KingdomAbility.LOYALTY] ?: 10,
        culture = boosts[KingdomAbility.CULTURE] ?: 10
    )
} else {
    KingdomAbilityScores(
        economy = abilityScores.economy,
        stability = abilityScores.stability,
        loyalty = abilityScores.loyalty,
        culture = abilityScores.culture,
    )
}

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
                settings.kingdomAllStructureItemBonusesStack,
                settings.capitalInvestmentInCapital,
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


fun Game.getKingdomActors(): List<KingdomActor> =
    actors.contents
        .filterIsInstance<KingdomActor>()
        .filter { it.getKingdom() != null }