package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawExpression
import at.posselt.pfrpg2e.kingdom.RawIn
import at.posselt.pfrpg2e.kingdom.charters
import at.posselt.pfrpg2e.kingdom.data.RawAbilityBoostChoices
import at.posselt.pfrpg2e.kingdom.data.RawBonusFeat
import at.posselt.pfrpg2e.kingdom.data.RawCharterChoices
import at.posselt.pfrpg2e.kingdom.data.RawFeatureChoices
import at.posselt.pfrpg2e.kingdom.data.RawGovernmentChoices
import at.posselt.pfrpg2e.kingdom.data.RawHeartlandChoices
import at.posselt.pfrpg2e.kingdom.governments
import at.posselt.pfrpg2e.kingdom.kingdomFeats
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.typeSafeUpdate
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

class Migration13 : Migration(13) {
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
        kingdom.settings.automateStats = false
        kingdom.settings.ruinTreshold = 10
        kingdom.settings.increaseScorePicksBy = 0
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
        val governmentFeat = kingdom.government.type?.let { governmentsById[it]?.bonusFeat }
        kingdom.bonusFeats = kingdom.bonusFeats
            .mapNotNull {
                featsByName[it.id]?.let { f ->
                    RawBonusFeat(id=f.id, ruinThresholdIncreases=emptyArray())
                }
            }
            .filter { it.id != governmentFeat }
            .toTypedArray()
        kingdom.heartland = RawHeartlandChoices(
            type = heartland
        )
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
    }

    override suspend fun migrateOther(game: Game) {
        game.folders.find { it.name == "Recruitable Armies" }
            ?.typeSafeUpdate { name = "Recruitable Armies (Kingdom Sheet)" }
    }
}