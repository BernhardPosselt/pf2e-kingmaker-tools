package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.KingdomActivity
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.postDegreeOfSuccess
import at.posselt.pfrpg2e.utils.serializeB64Json
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.objects.JsPlainObject

@JsPlainObject
external interface ModifierPill {
    val value: String
    val label: String
}


@JsPlainObject
private external interface RollMetaContext {
    val label: String
    val dc: Int
    val activityId: String?
    val actorUuid: String
    val degree: String
    val rollMode: String
    val modifier: Int
    val pill: Array<String>
}

private suspend fun generateRollMeta(
    activity: KingdomActivity?,
    modifier: Int,
    modifierPills: Array<ModifierPill>,
    actor: PF2ENpc,
    rollMode: RollMode,
    degree: DegreeOfSuccess,
    skill: KingdomSkill,
    dc: Int,
) = tpl(
    path = "chatmessages/roll-flavor",
    ctx = RollMetaContext(
        label = activity?.title ?: skill.label,
        dc = dc,
        activityId = activity?.id,
        actorUuid = actor.uuid,
        degree = degree.value,
        rollMode = rollMode.value,
        modifier = modifier,
        pill = modifierPills.map { "${it.label} ${it.value}" }.toTypedArray(),
    ).asAnyObject(),
)

@JsPlainObject
private external interface ChatModifier {
    val label: String
    val data: String
}

@JsPlainObject
private external interface ChatButtonContext {
    val criticalSuccess: Boolean
    val modifiers: Array<ChatModifier>
}

suspend fun buildChatButtons(
    degree: DegreeOfSuccess,
    modifiers: Array<RawModifier>,
): String {
    return tpl(
        path = "chatmessages/roll-chat-buttons.hbs",
        ctx = ChatButtonContext(
            criticalSuccess = degree == DegreeOfSuccess.CRITICAL_SUCCESS,
            modifiers = modifiers.map {
                ChatModifier(
                    label = it.name,
                    data = serializeB64Json(it)
                )
            }.toTypedArray()
        ).asAnyObject(),
    )
}


@JsPlainObject
private external interface UpgradeMetaContext {
    val rollMode: String
    val activityId: String
    val degree: String
    val additionalMessages: String
}

suspend fun buildUpgradeMeta(
    additionalMessages: String?,
    degree: DegreeOfSuccess,
    activity: KingdomActivity,
    rollMode: RollMode,
): String {
    return tpl(
        path = "chatmessages/upgrade-roll-meta.hbs",
        ctx = UpgradeMetaContext(
            rollMode = rollMode.value,
            activityId = activity.id,
            degree = degree.value,
            additionalMessages = serializeB64Json(additionalMessages ?: ""),
        ).asAnyObject(),
    )
}

suspend fun rollCheck(
    afterRollMessage: AfterRollMessage,
    rollMode: RollMode?,
    activity: KingdomActivity?,
    skill: KingdomSkill,
    modifier: Int,
    modifierPills: Array<ModifierPill>,
    dc: Int,
    kingdomActor: PF2ENpc,
    upgrades: Set<DegreeOfSuccess>,
): DegreeOfSuccess {
    val result = d20Check(
        dc = dc,
        modifier = modifier,
        rollMode = rollMode,
    )
    val originalDegree = result.degreeOfSuccess
    val degree = if (originalDegree in upgrades) {
        originalDegree.upgrade()
    } else {
        originalDegree
    }
    val nonNullRollMode = rollMode ?: RollMode.PUBLICROLL
    val rollMeta = generateRollMeta(
        activity = activity,
        modifier = modifier,
        modifierPills = modifierPills,
        actor = kingdomActor,
        rollMode = nonNullRollMode,
        degree = degree,
        skill = skill,
        dc = dc,
    )
    result.toChat(rollMeta)
    if (activity == null) {
        postDegreeOfSuccess(
            degreeOfSuccess = degree,
            originalDegreeOfSuccess = originalDegree,
        )
    } else {
        val additionalMessages = afterRollMessage(degree)
        val metaHtml = buildUpgradeMeta(
            rollMode = nonNullRollMode,
            activity = activity,
            degree = degree,
            additionalMessages = additionalMessages
        )
        val modifiers = when (degree) {
            DegreeOfSuccess.CRITICAL_FAILURE -> activity.criticalFailure
            DegreeOfSuccess.FAILURE -> activity.failure
            DegreeOfSuccess.SUCCESS -> activity.success
            DegreeOfSuccess.CRITICAL_SUCCESS -> activity.criticalSuccess
        }
        val postHtml = buildChatButtons(degree, modifiers?.modifiers ?: emptyArray())
        postDegreeOfSuccess(
            degreeOfSuccess = degree,
            originalDegreeOfSuccess = originalDegree,
            title = activity.title,
            rollMode = nonNullRollMode,
            metaHtml = metaHtml,
            preHtml = "<p>${activity.description}</p>",
            postHtml = postHtml + additionalMessages
        )
    }
    return degree
}