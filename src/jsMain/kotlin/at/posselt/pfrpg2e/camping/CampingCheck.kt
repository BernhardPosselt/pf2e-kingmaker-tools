package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.actor.resolveAttribute
import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.camping.dialogs.RegionSetting
import at.posselt.pfrpg2e.data.actor.*
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.checks.getLevelBasedDC
import at.posselt.pfrpg2e.fromOrdinal
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.postDegreeOfSuccess
import com.foundryvtt.pf2e.actions.CheckDC
import com.foundryvtt.pf2e.actor.PF2ECreature
import com.foundryvtt.pf2e.actor.StatisticRollParameters
import js.array.push
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface AskDcData {
    val dc: Int
}

suspend fun askDc(activity: String): Int {
    return awaitablePrompt<AskDcData, Int>(
        title = "$activity: Select DC",
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(Select.dc())
        ),
    ) {
        it.dc
    }
}

fun PF2ECreature.satisfiesSkillRequirement(
    skill: ParsedCampingSkill,
): Boolean {
    val rank = resolveAttribute(skill.attribute)?.rank ?: 0
    return skill.proficiency.let { rank >= it.ordinal }
}

fun PF2ECreature.satisfiesAnyActivitySkillRequirement(
    activity: CampingActivityData,
    disableSkillRequirements: Boolean,
): Boolean {
    val campingSkills = activity.getCampingSkills()
    return if (disableSkillRequirements == true || campingSkills.isEmpty()) {
        true
    } else {
        campingSkills.any { satisfiesSkillRequirement(it) }
                && campingSkills.filter { it.required }.all { satisfiesSkillRequirement(it) }
    }
}

fun PF2ECreature.hasAnyActivitySkill(
    activity: CampingActivityData,
): Boolean =
    findCampingActivitySkills(activity, true)
        .map { it.attribute }
        .any { resolveAttribute(it) != null }

fun PF2ECreature.findCampingActivitySkills(
    activity: CampingActivityData,
    disableSkillRequirements: Boolean,
): List<ParsedCampingSkill> {
    return activity.getCampingSkills(this).filter {
        if (disableSkillRequirements) {
            true
        } else {
            satisfiesSkillRequirement(it)
        }
    }
}

data class CampingCheckData(
    val region: RegionSetting,
    val activityData: ActivityAndData,
    val skill: ParsedCampingSkill,
)

fun PF2ECreature.getCampingCheckData(camping: CampingData, activityName: String): CampingCheckData? {
    val region = camping.findCurrentRegion()
    val data = camping.groupActivities().find { it.data.name == activityName && it.result.actorUuid == uuid }
    val skill = data?.result?.selectedSkill
        ?.let { Attribute.fromString(it) }
        ?.let { attr -> data.data.getCampingSkills(this).find { it.attribute == attr } }
    return if (skill != null && region != null) {
        CampingCheckData(
            region = region,
            activityData = data,
            skill = skill,
        )
    } else {
        null
    }
}


/**
 * @throws Error if a popup asking for a skill or dc is closed
 */
suspend fun PF2ECreature.campingActivityCheck(
    data: CampingCheckData,
    overrideDc: Int? = null,
): DegreeOfSuccess? {
    val activity = data.activityData.data
    val activityName = activity.name
    val skill = data.skill
    val extraRollOptions = arrayOf("action:${activityName.slugify()}")
    val dc = overrideDc ?: when (skill.dcType) {
        DcType.ACTOR_LEVEL -> getLevelBasedDC(level)
        DcType.ZONE -> data.region.zoneDc
        DcType.NONE -> askDc(activityName)
        DcType.STATIC -> skill.dc ?: askDc(activityName)
    }

    val result = performCampingCheck(
        attribute = skill.attribute,
        isSecret = activity.isSecret,
        extraRollOptions = extraRollOptions,
        dc = dc,
    )
    if (result != null) {
        val config = when (result) {
            DegreeOfSuccess.CRITICAL_FAILURE -> activity.criticalFailure
            DegreeOfSuccess.FAILURE -> activity.failure
            DegreeOfSuccess.SUCCESS -> activity.success
            DegreeOfSuccess.CRITICAL_SUCCESS -> activity.criticalSuccess
        }
        val rollMode = if (data.activityData.data.isSecret) RollMode.BLINDROLL else null
        postDegreeOfSuccess(
            degreeOfSuccess = result,
            message = config?.message,
            rollMode = rollMode,
        )
    }
    return result
}


suspend fun PF2ECreature.performCampingCheck(
    attribute: Attribute,
    isSecret: Boolean = false,
    isWatch: Boolean = false,
    extraRollOptions: Array<String> = emptyArray(),
    dc: Int,
): DegreeOfSuccess? {
    val data = StatisticRollParameters(
        rollMode = if (isSecret) "blindroll" else undefined,
        dc = CheckDC(value = dc),
        extraRollOptions = arrayOf("camping") + extraRollOptions
    )
    if (isWatch) {
        data.extraRollOptions?.push("watch")
    }
    return resolveAttribute(attribute)
        ?.roll(data)
        ?.await()
        ?.let { fromOrdinal<DegreeOfSuccess>(it.degreeOfSuccess) }
}


