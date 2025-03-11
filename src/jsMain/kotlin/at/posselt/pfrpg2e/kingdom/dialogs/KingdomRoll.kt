package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.determineDegree
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postDegreeOfSuccess
import at.posselt.pfrpg2e.utils.serializeB64Json
import at.posselt.pfrpg2e.utils.tpl
import js.objects.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.math.max

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
    val pills: Array<String>
    val creativeSolutionPills: Array<String>
    val fortune: Boolean
    val isCreativeSolution: Boolean
    val modifierWithCreativeSolution: Int
}

private fun parseRollMeta(rollElement: HTMLElement): RollMetaContext {
    val meta = rollElement.querySelector(".km-roll-meta") as HTMLElement?
    val pills = rollElement.querySelectorAll(".km-modifier-pill.km-creative-solution-pills").asList()
        .map { it.textContent ?: "" }
        .toTypedArray()
    val creativePills = rollElement.querySelectorAll(".km-modifier-pill:not(.km-creative-solution-pills)").asList()
        .map { it.textContent ?: "" }
        .toTypedArray()
    return RollMetaContext(
        label = meta?.dataset["label"] ?: "",
        dc = meta?.dataset["dc"]?.toInt() ?: 0,
        activityId = meta?.dataset["activityId"] ?: "",
        actorUuid = meta?.dataset["actorUuid"] ?: "",
        degree = meta?.dataset["degree"] ?: "",
        rollMode = meta?.dataset["rollMode"] ?: "",
        modifier = meta?.dataset["modifier"]?.toInt() ?: 0,
        pills = pills,
        creativeSolutionPills = creativePills,
        fortune = meta?.dataset["fortune"] == "true",
        isCreativeSolution = meta?.dataset["isCreativeSolution"] == "true",
        modifierWithCreativeSolution = meta?.dataset["modifierWithCreativeSolution"]?.toInt() ?: 0,
    )
}

private suspend fun generateRollMeta(
    activity: RawActivity?,
    modifier: Int,
    modifierPills: Array<ModifierPill>,
    actor: KingdomActor,
    rollMode: RollMode,
    degree: DegreeOfSuccess,
    skill: KingdomSkill,
    dc: Int,
    fortune: Boolean,
    creativeSolutionPills: Array<ModifierPill>,
    modifierWithCreativeSolution: Int,
    isCreativeSolution: Boolean,
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
        fortune = fortune,
        modifierWithCreativeSolution = modifierWithCreativeSolution,
        pills = modifierPills.map { "${it.label} ${it.value}" }.toTypedArray(),
        creativeSolutionPills = creativeSolutionPills.map { "${it.label} ${it.value}" }.toTypedArray(),
        isCreativeSolution = isCreativeSolution,
    ),
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
        ),
    )
}


@JsPlainObject
private external interface UpgradeMetaContext {
    val rollMode: String
    val activityId: String
    val degree: String
    val additionalMessages: String
}

private suspend fun buildUpgradeMeta(
    additionalMessages: String?,
    degree: DegreeOfSuccess,
    activity: RawActivity,
    rollMode: RollMode,
): String {
    return tpl(
        path = "chatmessages/upgrade-roll-meta.hbs",
        ctx = UpgradeMetaContext(
            rollMode = rollMode.value,
            activityId = activity.id,
            degree = degree.value,
            additionalMessages = serializeB64Json(additionalMessages ?: ""),
        ),
    )
}

// TODO: re-roll with creative solution
//if (data.creativeSolution && !data.assurance) {
//    postChatMessage("Reduced Creative Solutions by 1")
//    kingdom.creativeSolutions = max(0, kingdom.creativeSolutions - 1)
//}

private fun parseUpgradeMeta(elem: HTMLElement) =
    UpgradeMetaContext(
        rollMode = elem.dataset["rollMode"] ?: "",
        activityId = elem.dataset["activityId"] ?: "",
        degree = elem.dataset["degree"] ?: "",
        additionalMessages = deserializeB64Json<String>(elem.dataset["additionalMessages"] ?: ""),
    )

suspend fun rollCheck(
    afterRoll: AfterRollMessage,
    rollMode: RollMode?,
    activity: RawActivity?,
    skill: KingdomSkill,
    modifier: Int,
    modifierWithCreativeSolution: Int,
    fortune: Boolean,
    modifierPills: Array<ModifierPill>,
    dc: Int,
    kingdomActor: KingdomActor,
    upgrades: Set<UpgradeResult>,
    rollTwiceKeepHighest: Boolean,
    rollTwiceKeepLowest: Boolean,
    creativeSolutionPills: Array<ModifierPill>,
    isCreativeSolution: Boolean = false,
    downgrades: Set<DowngradeResult>,
): DegreeOfSuccess {
    val result = d20Check(
        dc = dc,
        modifier = if (isCreativeSolution) modifierWithCreativeSolution else modifier,
        rollMode = rollMode,
        rollTwiceKeepHighest=rollTwiceKeepHighest,
        rollTwiceKeepLowest=rollTwiceKeepLowest,
    )

    if (isCreativeSolution) {
        kingdomActor.getKingdom()?.let {
            postChatMessage("Reduced Creative Solutions by 1")
            it.creativeSolutions = max(0, it.creativeSolutions - 1)
            kingdomActor.setKingdom(it)
        }
    }

    val degreeResult = determineDegree(result.degreeOfSuccess, upgrades, downgrades)
    val originalDegree = degreeResult.originalDegree
    val changed = degreeResult.changedDegree
    val nonNullRollMode = rollMode ?: RollMode.PUBLICROLL
    // TODO: needs to add upgrades/downgrades
    val rollMeta = generateRollMeta(
        activity = activity,
        modifier = modifier,
        modifierPills = modifierPills,
        actor = kingdomActor,
        rollMode = nonNullRollMode,
        degree = changed,
        skill = skill,
        dc = dc,
        fortune = fortune || isCreativeSolution,
        creativeSolutionPills = creativeSolutionPills,
        modifierWithCreativeSolution = modifierWithCreativeSolution,
        isCreativeSolution = isCreativeSolution,
    )
    result.toChat(rollMeta)
    if (activity == null) {
        postDegreeOfSuccess(
            degreeOfSuccess = changed,
            originalDegreeOfSuccess = originalDegree,
        )
    } else {
        val additionalMessages = afterRoll(changed)
        val metaHtml = buildUpgradeMeta(
            rollMode = nonNullRollMode,
            activity = activity,
            degree = changed,
            additionalMessages = additionalMessages
        )
        val modifiers = when (changed) {
            DegreeOfSuccess.CRITICAL_FAILURE -> activity.criticalFailure
            DegreeOfSuccess.FAILURE -> activity.failure
            DegreeOfSuccess.SUCCESS -> activity.success
            DegreeOfSuccess.CRITICAL_SUCCESS -> activity.criticalSuccess
        }
        val chatModifiers = modifiers?.modifiers ?: emptyArray()
        val postHtml = buildChatButtons(changed, chatModifiers)
        postDegreeOfSuccess(
            degreeOfSuccess = changed,
            originalDegreeOfSuccess = originalDegree,
            title = activity.title,
            rollMode = nonNullRollMode,
            metaHtml = metaHtml,
            preHtml = "<p>${activity.description}</p>",
            postHtml = postHtml + additionalMessages
        )
    }
    return changed
}