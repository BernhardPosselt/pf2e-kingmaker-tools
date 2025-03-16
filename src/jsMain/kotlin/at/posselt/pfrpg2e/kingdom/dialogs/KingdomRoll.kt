package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.determineDegree
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
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
        actorUuid = meta?.dataset["kingdomActorUuid"] ?: "",
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
    path = "chatmessages/roll-flavor.hbs",
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
    val actorUuid: String
}

@JsPlainObject
private external interface ChatButtonContext {
    val criticalSuccess: Boolean
    val modifiers: Array<ChatModifier>
}

suspend fun buildChatButtons(
    degree: DegreeOfSuccess,
    modifiers: Array<RawModifier>,
    actorUuid: String,
): String {
    return tpl(
        path = "chatmessages/roll-chat-buttons.hbs",
        ctx = ChatButtonContext(
            criticalSuccess = degree == DegreeOfSuccess.CRITICAL_SUCCESS,
            modifiers = modifiers.map {
                ChatModifier(
                    label = it.buttonLabel ?: it.name,
                    data = serializeB64Json(it),
                    actorUuid = actorUuid,
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
    val additionalMessages: String?
    val actorUuid: String
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
        additionalMessages = elem.dataset["additionalMessages"],
        actorUuid = elem.dataset["kingdomActorUuid"] ?: "",
    )

enum class ChangeDegree {
    UPGRADE,
    DOWNGRADE;
}

suspend fun changeDegree(rollMeta: HTMLElement, mode: ChangeDegree) {
    val meta = parseUpgradeMeta(rollMeta)
    console.log(meta)
    val degree = DegreeOfSuccess.fromString(meta.degree)
    val changed = if (mode == ChangeDegree.UPGRADE) {
        when (degree) {
            DegreeOfSuccess.CRITICAL_FAILURE -> DegreeOfSuccess.FAILURE
            DegreeOfSuccess.FAILURE -> DegreeOfSuccess.SUCCESS
            DegreeOfSuccess.SUCCESS -> DegreeOfSuccess.CRITICAL_SUCCESS
            else -> null
        }
    } else {
        when (degree) {
            DegreeOfSuccess.FAILURE -> DegreeOfSuccess.CRITICAL_FAILURE
            DegreeOfSuccess.SUCCESS -> DegreeOfSuccess.FAILURE
            DegreeOfSuccess.CRITICAL_SUCCESS -> DegreeOfSuccess.SUCCESS
            else -> null
        }
    }
    if (changed == null) {
        console.error("Can not upgrade degree $degree")
    } else {
        postActivityDegreeOfSuccess(meta, changed)
    }
}

suspend fun rollCheck(
    afterRoll: AfterRoll,
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
    degreeMessages: DegreeMessages,
): DegreeOfSuccess {
    val result = d20Check(
        dc = dc,
        modifier = if (isCreativeSolution) modifierWithCreativeSolution else modifier,
        rollMode = rollMode,
        rollTwiceKeepHighest = rollTwiceKeepHighest,
        rollTwiceKeepLowest = rollTwiceKeepLowest,
        toChat = false,
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
    // TODO: needs to add upgrades/downgrades from modifiers
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
        afterRoll(changed)
        val context = UpgradeMetaContext(
            rollMode = rollMode?.value ?: RollMode.PUBLICROLL.value,
            activityId = activity.id,
            degree = originalDegree.value,
            additionalMessages = serializeB64Json(degreeMessages),
            actorUuid = kingdomActor.uuid,
        )
        postActivityDegreeOfSuccess(context, changed)
    }
    return changed
}

private suspend fun postActivityDegreeOfSuccess(
    metaContext: UpgradeMetaContext,
    changedDegreeOfSuccess: DegreeOfSuccess,
) {
    val kingdomActor = fromUuidTypeSafe<KingdomActor>(metaContext.actorUuid) ?: return
    val kingdom = kingdomActor.getKingdom() ?: return
    val activity = kingdom.getActivity(metaContext.activityId) ?: return
    val rollMode = RollMode.fromString(metaContext.rollMode) ?: return
    val degree = DegreeOfSuccess.fromString(metaContext.degree) ?: return
    val degreeMessages = metaContext.additionalMessages
        ?.let { deserializeB64Json<DegreeMessages>(it) }
    val metaHtml = tpl(
        path = "chatmessages/upgrade-roll-meta.hbs",
        ctx = metaContext.copy(degree = changedDegreeOfSuccess.value),
    )
    val modifiers = when (changedDegreeOfSuccess) {
        DegreeOfSuccess.CRITICAL_FAILURE -> activity.criticalFailure
        DegreeOfSuccess.FAILURE -> activity.failure
        DegreeOfSuccess.SUCCESS -> activity.success
        DegreeOfSuccess.CRITICAL_SUCCESS -> activity.criticalSuccess
    }
    val messages = when (changedDegreeOfSuccess) {
        DegreeOfSuccess.CRITICAL_FAILURE -> degreeMessages?.criticalFailure
        DegreeOfSuccess.FAILURE -> degreeMessages?.failure
        DegreeOfSuccess.SUCCESS -> degreeMessages?.success
        DegreeOfSuccess.CRITICAL_SUCCESS -> degreeMessages?.criticalSuccess
    }
    val chatModifiers = modifiers?.modifiers ?: emptyArray()
    val postHtml = if (chatModifiers.isNotEmpty() || changedDegreeOfSuccess == DegreeOfSuccess.CRITICAL_SUCCESS) {
        buildChatButtons(changedDegreeOfSuccess, chatModifiers, kingdomActor.uuid)
    } else {
        ""
    }
    postDegreeOfSuccess(
        degreeOfSuccess = changedDegreeOfSuccess,
        originalDegreeOfSuccess = degree,
        title = activity.title,
        rollMode = rollMode,
        metaHtml = metaHtml,
        preHtml = "<p>${activity.description}</p>",
        postHtml = postHtml,
        message = modifiers?.msg,
    )
    if (messages != null) {
        postChatMessage(
            "$messages<span hidden=\"hidden\" data-kingdom-actor-uuid=\"${kingdomActor.uuid}\"></span>",
            rollMode = rollMode
        )
    }
}