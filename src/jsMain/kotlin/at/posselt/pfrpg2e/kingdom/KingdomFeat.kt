package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.kingdom.data.RawAbilityScores
import at.posselt.pfrpg2e.kingdom.data.RawSkillRanks
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.utils.asSequence
import js.objects.JsPlainObject
import js.objects.Record


@JsPlainObject
external interface RawUpgradeResult {
    val upgrade: String
    val times: Int?
}


fun RawUpgradeResult.parse() =
    DegreeOfSuccess.fromString(upgrade)?.let { degree ->
        UpgradeResult(
            upgrade = degree,
            times = times ?: 1
        )
    }

@JsPlainObject
external interface RawDowngradeResult {
    val downgrade: String
    val times: Int?
}


fun RawDowngradeResult.parse() =
    DegreeOfSuccess.fromString(downgrade)?.let { degree ->
        DowngradeResult(
            downgrade = degree,
            times = times ?: 1,
        )
    }

@JsPlainObject
external interface RawRuinThresholdIncreases {
    val amount: Int
    val increase: Int
}

@JsPlainObject
external interface RawFeatRequirements {
    val skillRanks: RawSkillRanks?
    val abilityScores: RawAbilityScores?
    val featIds: Array<String>?
}

@JsPlainObject
external interface IncreaseActivityUnrestReductionBy {
    val value: Int
    val minimumCurrentUnrest: Int
}

@JsPlainObject
external interface RawFeat {
    val id: String
    val name: String
    val level: Int
    val text: String
    val automationNotes: String?
    val modifiers: Array<RawModifier>?
    val resourceDice: Int?
    val settlementMagicItemLevelIncrease: Int?
    val trainSkill: String?
    val assuranceForSkill: String?
    val increaseUsableSkills: Record<String, Array<String>>?
    val rollOptions: Array<String>?
    val increaseAnarchyLimit: Int?
    val ruinThresholdIncreases: Array<RawRuinThresholdIncreases>?
    val increaseGainedLuxuriesOncePerTurnBy: Int?
    val requirements: RawFeatRequirements?
    val removeLeaderVacancyPenalty: Boolean?
    val isFreeAndFair: Boolean?
    val increaseActivityUnrestReductionBy: IncreaseActivityUnrestReductionBy?
}

fun RawFeat.satisfiesRequirements(
    chosenFeatIds: Set<String>,
    skillRanks: KingdomSkillRanks,
    abilityScores: KingdomAbilityScores,
): Boolean {
    val featsValid = requirements?.featIds?.all { it in chosenFeatIds } != false
    val scoreRecord = requirements?.abilityScores.unsafeCast<Record<String, Int?>?>()
    val skillRecord = requirements?.skillRanks.unsafeCast<Record<String, Int?>?>()
    val scoresValid = KingdomAbility.entries
        .mapNotNull {ability ->
            scoreRecord?.get(ability.value)?.let {
                abilityScores.resolve(ability) >= it
            }
        }
        .none { it == false }
    val ranksValid = KingdomSkill.entries
        .mapNotNull {skill ->
            skillRecord?.get(skill.value)?.let {
                skillRanks.resolve(skill) >= it
            }
        }
        .none { it == false }
    return featsValid && scoresValid && ranksValid
}

fun RawFeatRequirements.formatRequirements(allFeats: Array<RawFeat>): String {
    val featsById = allFeats.associate { it.id to it.name }
    val scoreRecord = abilityScores.unsafeCast<Record<String, Int?>?>()
    val skillRecord = skillRanks.unsafeCast<Record<String, Int?>?>()
    val feats = featIds?.mapNotNull { featsById[it] }.orEmpty()
    val scores = KingdomAbility.entries
        .mapNotNull {ability ->
            scoreRecord?.get(ability.value)?.let {
                "${ability.label}: $it"
            }
        }
    val ranks = KingdomSkill.entries
        .mapNotNull {skill ->
            skillRecord?.get(skill.value)?.let { rank ->
                "${skill.label} (${Proficiency.fromRank(rank).label})"
            }
        }
    return (feats + scores + ranks).joinToString(", ")
}

fun RawFeat.increasedSkills(): Map<KingdomSkill, Set<KingdomSkill>> =
    increaseUsableSkills?.asSequence()
        ?.mapNotNull { (skill, skills) ->
            KingdomSkill.fromString(skill)?.let { kingdomSkill ->
                kingdomSkill to skills.mapNotNull { s -> KingdomSkill.fromString(s) }.toSet()
            }
        }
        ?.toMap()
        ?: emptyMap()

fun KingdomData.getFeats(): Array<RawFeat> {
    val overrides = homebrewFeats.map { it.id }.toSet()
    return homebrewFeats + kingdomFeats.filter { it.id !in overrides }
}

@JsModule("./feats.json")
external val kingdomFeats: Array<RawFeat>

