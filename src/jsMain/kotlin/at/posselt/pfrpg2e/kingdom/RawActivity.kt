package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.armies.findMaximumArmyTactics
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.ChosenFeature
import at.posselt.pfrpg2e.kingdom.dialogs.getValidActivitySkills
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.sheet.insertButtons
import at.posselt.pfrpg2e.utils.asSequence
import com.i18next.I18Next
import js.objects.JsPlainObject
import js.objects.Record

typealias KingdomDc = Any // number or control, custom, none, scouting

@JsPlainObject
external interface ActivityResult {
    var msg: String
    val modifiers: Array<RawModifier>
}

private fun ActivityResult.insertButtons(): ActivityResult =
    copy(
        msg = insertButtons(msg),
        modifiers = modifiers
    )

@JsPlainObject
external interface RawActivity {
    var id: String
    var actions: Int?
    var title: String
    var description: String
    var requirement: String?
    var special: String?
    var skills: Record<String, Int>
    var phase: String
    var dc: KingdomDc
    var dcAdjustment: Int?
    var enabled: Boolean
    var automationNotes: String?
    var fortune: Boolean
    var oncePerRound: Boolean
    var hint: String?
    var criticalSuccess: ActivityResult?
    var success: ActivityResult?
    var failure: ActivityResult?
    var criticalFailure: ActivityResult?
    var modifiers: Array<RawModifier>?
    val order: Int?
}

fun RawActivity.canBePerformed(
    allowCapitalInvestment: Boolean,
    kingdomSkillRanks: KingdomSkillRanks,
    kingdom: KingdomData,
    chosenFeats: List<ChosenFeat>,
): Boolean = (allowCapitalInvestment || id != "capital-investment")
        && (kingdom.settings.kingdomIgnoreSkillRequirements || getValidActivitySkills(
    ranks = kingdomSkillRanks,
    activityRanks = skillRanks(),
    ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
    expandMagicUse = kingdom.settings.expandMagicUse,
    activityId = id,
    increaseSkills = chosenFeats.map { it.feat.increasedSkills() }
).isNotEmpty())

fun RawActivity.label(
    kingdomLevel: Int,
    activity: RawActivity,
    chosenFeatures: List<ChosenFeature>,
): String {
    val claimHexAttempts = chosenFeatures.maxOfOrNull { it.feature.claimHexAttempts ?: 1 } ?: 1
    val id = activity.id
    val activityHints = if (id == "claim-hex") {
        when (claimHexAttempts) {
            1 -> "once per turn"
            2 -> "twice per turn"
            else -> "three times per turn"
        }
    } else if (id == "train-army") {
        "max ${findMaximumArmyTactics(kingdomLevel)} per army"
    } else {
        null
    }
    val skillRanks = activity.skillRanks()
    val proficiency = if (skillRanks.all { it.proficiency >= Proficiency.LEGENDARY }) {
        "legendary"
    } else if (skillRanks.all { it.proficiency >= Proficiency.MASTER }) {
        "master"
    } else if (skillRanks.all { it.proficiency >= Proficiency.EXPERT }) {
        "expert"
    } else if (skillRanks.all { it.proficiency >= Proficiency.TRAINED }) {
        "trained"
    } else {
        null
    }
    val oncePerRound = if (activity.oncePerRound) "once per turn" else null
    val hints = listOfNotNull(activityHints, oncePerRound, activity.hint, proficiency).joinToString(", ")
    return if (hints.isEmpty()) {
        activity.title
    } else {
        "${activity.title} ($hints)"
    }
}

@JsModule("./kingdom-activities.json")
private external val rawKingdomActivities: Array<RawActivity>

val disabledActivityIds = rawKingdomActivities
    .filterNot { it.enabled }
    .map { it.id }
    .toTypedArray()

private var kingdomActivities: Array<RawActivity> = emptyArray()

private fun ActivityResult.translate(i18Next: I18Next): ActivityResult =
    copy(
        msg = i18Next.t(msg),
        modifiers = modifiers
    )

private fun RawActivity.translate(i18Next: I18Next): RawActivity =
    copy(
        title = i18Next.t(title),
        description = i18Next.t(description),
        requirement = requirement?.let { i18Next.t(it) },
        special = special?.let { i18Next.t(it) },
        automationNotes = automationNotes?.let { i18Next.t(it) },
        hint = hint?.let { i18Next.t(it) },
        criticalSuccess = criticalSuccess?.translate(i18Next),
        success = success?.translate(i18Next),
        failure = failure?.translate(i18Next),
        criticalFailure = criticalFailure?.translate(i18Next),
    )

fun translateActivities(i18Next: I18Next) {
    kingdomActivities = rawKingdomActivities
        .map {
            val translated = it.translate(i18Next)
            translated.copy(
                description = insertButtons(translated.description),
                criticalSuccess = translated.criticalSuccess?.insertButtons(),
                success = translated.success?.insertButtons(),
                failure = translated.failure?.insertButtons(),
                criticalFailure = translated.criticalFailure?.insertButtons(),
            )
        }
        .toTypedArray()
}

fun KingdomData.getAllActivities(): List<RawActivity> {
    val homebrew = homebrewActivities.map { it.id }.toSet()
    return kingdomActivities.filter { it.id !in homebrew } + homebrewActivities
}

fun KingdomData.getActivity(id: String): RawActivity? =
    getAllActivities().associateBy { it.id }[id]

fun RawActivity.resolveDc(
    enemyArmyScoutingDcs: List<Int>,
    kingdomLevel: Int,
    realm: RealmData,
    rulerVacant: Boolean,
): Int? {
    val dc = when (dc) {
        "control" -> calculateControlDC(
            kingdomLevel = kingdomLevel,
            realm = realm,
            rulerVacant = rulerVacant,
        )

        "custom" -> 0
        "none" -> null
        "scouting" -> enemyArmyScoutingDcs.maxOrNull() ?: 0
        else -> dc as Int
    }
    return dc?.let { it + (dcAdjustment ?: 0) }
}

fun RawActivity.parseModifiers(): List<Modifier> =
    modifiers?.map { it.parse() }.orEmpty()

fun RawActivity.skillRanks(): Set<KingdomSkillRank> =
    skills.asSequence()
        .mapNotNull { (name, rank) ->
            KingdomSkill.fromString(name)?.let {
                KingdomSkillRank(skill = it, rank = rank)
            }
        }
        .toSet()