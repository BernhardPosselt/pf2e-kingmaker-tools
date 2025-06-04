package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actor.getLoreAttributes
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.actor.Attribute
import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Perception
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.pf2e.actor.PF2ECreature
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface ActivityOutcome {
    var message: String?
    var effectUuids: Array<ActivityEffect>?
    var modifyRandomEncounterDc: ModifyEncounterDc?
    var checkRandomEncounter: Boolean?
}

@JsPlainObject
external interface ModifyEncounterDc {
    val day: Int
    val night: Int
}

@JsPlainObject
external interface ActivityEffect {
    var uuid: String
    var target: String?
    var doublesHealing: Boolean?
}

@JsPlainObject
external interface CampingSkill {
    val name: String
    val proficiency: String
    val dcType: String // zone, actorLevel , static, none
    val dc: Int?
    val validateOnly: Boolean?
    val required: Boolean?
}

@JsPlainObject
external interface CampingActivityData {
    var id: String
    var name: String
    var journalUuid: String?
    var skills: Array<CampingSkill>
    var modifyRandomEncounterDc: ModifyEncounterDc?
    var isSecret: Boolean
    var isLocked: Boolean
    var effectUuids: Array<ActivityEffect>?
    var isHomebrew: Boolean
    var criticalSuccess: ActivityOutcome?
    var success: ActivityOutcome?
    var failure: ActivityOutcome?
    var criticalFailure: ActivityOutcome?
}

fun CampingActivityData.isCookMeal() =
    id == "cook-meal"

fun CampingActivityData.isPrepareCampsite() =
    id == "prepare-campsite"

fun CampingActivityData.isHuntAndGather() =
    id == "hunt-and-gather"

fun CampingActivityData.isDiscoverSpecialMeal() =
    id == "discover-special-meal"

fun CampingActivityData.getOutcome(degreeOfSuccess: DegreeOfSuccess) =
    when (degreeOfSuccess) {
        DegreeOfSuccess.CRITICAL_FAILURE -> criticalFailure
        DegreeOfSuccess.FAILURE -> failure
        DegreeOfSuccess.SUCCESS -> success
        DegreeOfSuccess.CRITICAL_SUCCESS -> criticalSuccess
    }

fun ModifyEncounterDc.atTime(isDay: Boolean) =
    if (isDay) {
        day
    } else {
        night
    }

enum class DcType: ValueEnum, Translatable {
    ZONE,
    ACTOR_LEVEL,
    NONE,
    STATIC;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "campingActivityDcType.$value"
}

data class ParsedCampingSkill(
    val attribute: Attribute,
    val proficiency: Proficiency = Proficiency.UNTRAINED,
    val dcType: DcType,
    val dc: Int?,
    val validateOnly: Boolean = false,
    val required: Boolean = false,
)

data class ActivityAndData(
    val data: CampingActivityData,
    val result: CampingActivity,
) {
    fun done(): Boolean {
        return (data.doesNotRequireACheck() && result.actorUuid != null)
                || result.checkPerformed()
    }

    fun isCookMeal() = data.isCookMeal()

    fun isPrepareCamp() = data.isPrepareCampsite()

    fun isNotPrepareCamp() = !isPrepareCamp()
}

fun CampingActivityData.getCampingSkills(
    actor: PF2ECreature? = null,
    expandAny: Boolean = true,
): List<ParsedCampingSkill> {
    val theSkills = skills
    if (theSkills.isEmpty()) return emptyList()
    // if an actor exists, fetch all lore skills for the dropdown, otherwise go
    // with the one on the activity
    val lores: List<Attribute> = actor?.getLoreAttributes()
        ?: getLoreSkills()
    val allAttributes = Skill.entries + lores + Perception
    val anySkill = theSkills.find { it.name == "any" }
    return if (anySkill == null) {
        val activitySkills = theSkills.associateBy { Attribute.fromString(it.name) }
        allAttributes.mapNotNull { attribute ->
            activitySkills[attribute]?.let { skill ->
                ParsedCampingSkill(
                    attribute = attribute,
                    proficiency = fromCamelCase<Proficiency>(skill.proficiency) ?: Proficiency.UNTRAINED,
                    dcType = fromCamelCase<DcType>(skill.dcType) ?: DcType.NONE,
                    dc = skill.dc,
                    validateOnly = skill.validateOnly == true,
                    required = skill.required == true,
                )
            }
        }
    } else if (expandAny) {
        allAttributes.map {
            ParsedCampingSkill(
                attribute = it,
                proficiency = fromCamelCase<Proficiency>(anySkill.proficiency) ?: Proficiency.UNTRAINED,
                dcType = fromCamelCase<DcType>(anySkill.dcType) ?: DcType.NONE,
                dc = anySkill.dc,
                validateOnly = anySkill.validateOnly == true,
                required = false,
            )
        }
    } else {
        listOf(
            ParsedCampingSkill(
                attribute = Attribute.fromString("any"),
                proficiency = fromCamelCase<Proficiency>(anySkill.proficiency) ?: Proficiency.UNTRAINED,
                dcType = fromCamelCase<DcType>(anySkill.dcType) ?: DcType.NONE,
                dc = anySkill.dc,
                validateOnly = anySkill.validateOnly == true,
                required = false,
            )
        )
    }
}

fun CampingData.groupActivities(): List<ActivityAndData> {
    val activitiesById = campingActivities.associateBy { it.activityId }
    return getAllActivities().map { data ->
        val activity = activitiesById[data.id] ?: CampingActivity(
            activityId = data.id,
            actorUuid = null,
            result = null,
            selectedSkill = null,
        )
        ActivityAndData(data = data, result = activity)
    }
}

fun CampingActivityData.getLoreSkills(): List<Lore> =
    if (skills.any { it.name == "any" } != false) {
        emptyList()
    } else {
        skills.map { Attribute.fromString(it.name) }
            .filterIsInstance<Lore>()
    }

fun CampingActivityData.doesNotRequireACheck(): Boolean =
    !requiresACheck()

fun CampingActivityData.requiresACheck(): Boolean =
    skills.any { it.validateOnly != true }

@JsModule("./camping-activities.json")
private external val campingActivityData: Array<CampingActivityData>

private fun ActivityOutcome.translate() =
    ActivityOutcome.copy(
        this,
        message = message?.let { t(it) }
    )

private fun CampingActivityData.translate() =
    CampingActivityData.copy(
        this,
        name = t(name),
        criticalSuccess = criticalSuccess?.translate(),
        success = success?.translate(),
        failure = failure?.translate(),
        criticalFailure = criticalFailure?.translate(),
    )

private var translatedCampingActivities = emptyArray<CampingActivityData>()

val lockedCampingActivityIds = campingActivityData
    .filter(CampingActivityData::isLocked)
    .map(CampingActivityData::id)
    .toTypedArray()

fun translateCampingActivities() {
    translatedCampingActivities = campingActivityData
        .map { it.translate() }
        .toTypedArray()
}

fun CampingData.getAllActivities(): Array<CampingActivityData> {
    val homebrewIds = homebrewCampingActivities.map { it.id }.toSet()
    return translatedCampingActivities
        .filter { it.id !in homebrewIds }
        .toTypedArray() + homebrewCampingActivities
}