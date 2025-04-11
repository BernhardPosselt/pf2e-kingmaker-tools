package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawExpression
import at.posselt.pfrpg2e.kingdom.RawIn
import at.posselt.pfrpg2e.kingdom.charters
import at.posselt.pfrpg2e.kingdom.data.MilestoneChoice
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawAbilityScores
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawCharterChoices
import at.posselt.pfrpg2e.kingdom.data.RawCommodities
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.kingdom.data.RawHeartlandChoices
import at.posselt.pfrpg2e.kingdom.data.RawRuin
import at.posselt.pfrpg2e.kingdom.data.RawRuinValues
import at.posselt.pfrpg2e.kingdom.data.RawSkillRanks
import at.posselt.pfrpg2e.kingdom.data.RawWorkSite
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import at.posselt.pfrpg2e.kingdom.governments
import at.posselt.pfrpg2e.kingdom.kingdomFeats
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.asAnyObject
import com.foundryvtt.core.Game
import io.github.uuidjs.uuid.v4
import js.array.tupleOf
import js.reflect.Reflect
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface RawFeat {
    var id: String
    var level: Int
}

class Migration13 : Migration(13, true) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        val featsByName = kingdomFeats.associateBy { it.name }
        val governmentsByName = governments.associateBy { it.name.lowercase() }
        val governmentsById = governments.associateBy { it.id }
        val chartersByName = charters.associateBy { it.name.lowercase() }
        val heartland = kingdom.heartland.unsafeCast<String?>()
        val government = kingdom.government.unsafeCast<String?>()
        val charter = kingdom.charter.unsafeCast<String?>()
        val feats: Array<RawFeat> = kingdom.asDynamic().feats.unsafeCast<Array<RawFeat>>()
        kingdom.features = feats.mapNotNull { f ->
            featsByName[f.id]?.let {
                RawFeatureChoices(
                    id = "kingdom-feat-level-${f.level}",
                    featId = it.id,
                    featRuinThresholdIncreases = emptyArray(),
                )
            }
        }.toTypedArray()
        kingdom.abilityBoosts = RawAbilityBoostChoices(
            culture = false,
            economy = false,
            loyalty = false,
            stability = false,
        )
        kingdom.homebrewCharters = emptyArray()
        kingdom.homebrewGovernments = emptyArray()
        kingdom.homebrewHeartlands = emptyArray()
        kingdom.homebrewFeats = emptyArray()
        kingdom.featBlacklist = emptyArray()
        kingdom.settings.automateStats = false
        kingdom.settings.ruinThreshold = 10
        kingdom.settings.increaseScorePicksBy = 0
        kingdom.settings.realmSceneId = kingdom.asDynamic().realmSceneId
        kingdom.charter = RawCharterChoices(
            type = chartersByName[charter?.lowercase() ?: ""]?.id,
            abilityBoosts = RawAbilityBoostChoices(
                culture = false,
                economy = false,
                loyalty = false,
                stability = false,
            )
        )
        kingdom.government = RawGovernmentChoices(
            type = governmentsByName[government?.lowercase() ?: ""]?.id,
            abilityBoosts = RawAbilityBoostChoices(
                culture = false,
                economy = false,
                loyalty = false,
                stability = false,
            )
        )
        // prevent these things from being null from the previous sheet
        kingdom.workSites = RawWorkSites(
            farmlands = RawWorkSite(
                quantity = kingdom.workSites.farmlands.quantity ?: 0,
                resources = kingdom.workSites.farmlands.resources ?: 0,
            ),
            lumberCamps = RawWorkSite(
                quantity = kingdom.workSites.lumberCamps.quantity ?: 0,
                resources = kingdom.workSites.lumberCamps.resources ?: 0,
            ),
            mines = RawWorkSite(
                quantity = kingdom.workSites.mines.quantity ?: 0,
                resources = kingdom.workSites.mines.resources ?: 0,
            ),
            quarries = RawWorkSite(
                quantity = kingdom.workSites.quarries.quantity ?: 0,
                resources = kingdom.workSites.quarries.resources ?: 0,
            ),
            luxurySources = RawWorkSite(
                quantity = kingdom.workSites.luxurySources.quantity ?: 0,
                resources = kingdom.workSites.luxurySources.resources ?: 0,
            ),
        )
        kingdom.supernaturalSolutions = kingdom.supernaturalSolutions ?: 0
        kingdom.creativeSolutions = kingdom.creativeSolutions ?: 0
        kingdom.fame.now = kingdom.fame.now ?: 0
        kingdom.fame.next = kingdom.fame.next ?: 0
        kingdom.commodities = RawCurrentCommodities(
            now = RawCommodities(
                food = kingdom.commodities.now.food ?: 0,
                lumber = kingdom.commodities.now.lumber ?: 0,
                luxuries = kingdom.commodities.now.luxuries ?: 0,
                ore = kingdom.commodities.now.ore ?: 0,
                stone = kingdom.commodities.now.stone ?: 0,
            ),
            next = RawCommodities(
                food = kingdom.commodities.next.food ?: 0,
                lumber = kingdom.commodities.next.lumber ?: 0,
                luxuries = kingdom.commodities.next.luxuries ?: 0,
                ore = kingdom.commodities.next.ore ?: 0,
                stone = kingdom.commodities.next.stone ?: 0,
            ),
        )
        kingdom.resourcePoints.now = kingdom.resourcePoints.now ?: 0
        kingdom.resourcePoints.next = kingdom.resourcePoints.next ?: 0
        kingdom.resourceDice.now = kingdom.resourceDice.now ?: 0
        kingdom.resourceDice.next = kingdom.resourceDice.next ?: 0
        kingdom.unrest = kingdom.unrest ?: 0
        kingdom.abilityScores = RawAbilityScores(
            economy = kingdom.abilityScores.economy ?: 0,
            stability = kingdom.abilityScores.stability ?: 0,
            loyalty = kingdom.abilityScores.loyalty ?: 0,
            culture = kingdom.abilityScores.culture ?: 0,
        )
        kingdom.ruin = RawRuin(
            corruption = RawRuinValues(
                value = kingdom.ruin.corruption.value ?: 0,
                penalty = kingdom.ruin.corruption.penalty ?: 0,
                threshold = kingdom.ruin.corruption.threshold ?: 10,
            ),
            crime = RawRuinValues(
                value = kingdom.ruin.crime.value ?: 0,
                penalty = kingdom.ruin.crime.penalty ?: 0,
                threshold = kingdom.ruin.crime.threshold ?: 10,
            ),
            decay = RawRuinValues(
                value = kingdom.ruin.decay.value ?: 0,
                penalty = kingdom.ruin.decay.penalty ?: 0,
                threshold = kingdom.ruin.decay.threshold ?: 10,
            ),
            strife = RawRuinValues(
                value = kingdom.ruin.strife.value ?: 0,
                penalty = kingdom.ruin.strife.penalty ?: 0,
                threshold = kingdom.ruin.strife.threshold ?: 10,
            ),
        )
        kingdom.consumption.now = kingdom.consumption.now ?: 0
        kingdom.consumption.next = kingdom.consumption.next ?: 0
        kingdom.size = kingdom.size ?: 0
        // end nullability fixes
        kingdom.initialProficiencies = emptyArray()
        kingdom.groups = kingdom.groups.map {
            it.copy(
                relations = when (it.relations) {
                    "diplomatic-relations" -> Relations.DIPLOMATIC_RELATIONS.value
                    "trade-agreement" -> Relations.TRADE_AGREEMENT.value
                    else -> Relations.NONE.value
                }
            )
        }.toTypedArray()
        val governmentFeat = kingdom.government.type?.let { governmentsById[it]?.bonusFeat }
        kingdom.bonusFeats = kingdom.bonusFeats
            .mapNotNull {
                featsByName[it.id]?.let { f ->
                    RawBonusFeat(id = f.id, ruinThresholdIncreases = emptyArray())
                }
            }
            .filter { it.id != governmentFeat }
            .toTypedArray()
        kingdom.heartland = RawHeartlandChoices(
            type = heartland
        )
        kingdom.skillRanks = RawSkillRanks(
            agriculture = kingdom.skillRanks.agriculture ?: 0,
            arts = kingdom.skillRanks.arts ?: 0,
            boating = kingdom.skillRanks.boating ?: 0,
            defense = kingdom.skillRanks.defense ?: 0,
            engineering = kingdom.skillRanks.engineering ?: 0,
            exploration = kingdom.skillRanks.exploration ?: 0,
            folklore = kingdom.skillRanks.folklore ?: 0,
            industry = kingdom.skillRanks.industry ?: 0,
            intrigue = kingdom.skillRanks.intrigue ?: 0,
            magic = kingdom.skillRanks.magic ?: 0,
            politics = kingdom.skillRanks.politics ?: 0,
            scholarship = kingdom.skillRanks.scholarship ?: 0,
            statecraft = kingdom.skillRanks.statecraft ?: 0,
            trade = kingdom.skillRanks.trade ?: 0,
            warfare = kingdom.skillRanks.warfare ?: 0,
            wilderness = kingdom.skillRanks.wilderness ?: 0,
        )
        kingdom.heartlandBlacklist = emptyArray()
        kingdom.charterBlacklist = emptyArray()
        kingdom.governmentBlacklist = emptyArray()
        kingdom.homebrewMilestones = emptyArray()
        kingdom.milestones = kingdom.milestones.map {
            MilestoneChoice(
                id = it.asDynamic().name.unsafeCast<String>().slugify(),
                completed = it.completed,
                enabled = if (kingdom.settings.vanceAndKerensharaXP) true else it.asDynamic().homebrew == true
            )
        }.toTypedArray()
        kingdom.settings.recruitableArmiesFolderId = game.folders.find { it.name == "Recruitable Armies" }?.id
        kingdom.modifiers.forEach {
            val predicates = it.applyIf?.toMutableList() ?: mutableListOf<RawExpression<Boolean>>()
            if (it.asAnyObject()["consumeId"] == "") {
                it.isConsumedAfterRoll = true
            }
            val skills = it.asDynamic().skills as Array<String>?
            val activities = it.asDynamic().activities as Array<String>?
            val abilities = it.asDynamic().abilities as Array<String>?
            val phases = it.asDynamic().phases as Array<String>?
            if (it.asDynamic().consumeId == "") {
                it.isConsumedAfterRoll = true
            }
            it.id = v4()
            if (skills != null) {
                predicates.add(RawIn(`in` = tupleOf("@skill", skills)))
            }
            if (activities != null) {
                predicates.add(RawIn(`in` = tupleOf("@activity", activities)))
            }
            if (abilities != null) {
                predicates.add(RawIn(`in` = tupleOf("@ability", abilities)))
            }
            if (phases != null) {
                predicates.add(RawIn(`in` = tupleOf("@phase", phases)))
            }
            Reflect.deleteProperty(it, "skills")
            Reflect.deleteProperty(it, "activities")
            Reflect.deleteProperty(it, "abilities")
            Reflect.deleteProperty(it, "phases")
            it.applyIf = predicates.toTypedArray()
        }
        Reflect.deleteProperty(kingdom, "feats")
        Reflect.deleteProperty(kingdom, "realmSceneId")
    }

    override suspend fun migrateOther(game: Game) {

    }
}