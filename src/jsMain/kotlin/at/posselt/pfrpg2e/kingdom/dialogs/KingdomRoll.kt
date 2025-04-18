package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.events.KingdomEvent
import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.data.kingdom.FameType
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.RawNote
import at.posselt.pfrpg2e.kingdom.UpgradeMetaContext
import at.posselt.pfrpg2e.kingdom.generateRollMeta
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.getEvent
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.Note
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.determineDegree
import at.posselt.pfrpg2e.kingdom.serialize
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.insertButtons
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postDegreeOfSuccess
import at.posselt.pfrpg2e.utils.serializeB64Json
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.tpl
import js.objects.JsPlainObject
import js.objects.recordOf
import kotlin.math.max


@Suppress("unused")
@JsPlainObject
private external interface ChatModifier {
    val label: String
    val data: String
    val actorUuid: String
}

@Suppress("unused")
@JsPlainObject
private external interface ChatButtonContext {
    val criticalSuccess: Boolean
    val eventId: String?
    val eventIndex: Int
    val modifiers: Array<ChatModifier>
}

suspend fun buildChatButtons(
    degree: DegreeOfSuccess,
    modifiers: Array<RawModifier>,
    actorUuid: String,
    eventIndex: Int,
    eventId: String?,
): String {
    return tpl(
        path = "chatmessages/roll-chat-buttons.hbs",
        ctx = ChatButtonContext(
            criticalSuccess = degree == DegreeOfSuccess.CRITICAL_SUCCESS,
            eventIndex = eventIndex,
            eventId = eventId,
            modifiers = modifiers.map {
                val name = if(it.requiresTranslation != false) t(it.name) else it.name
                val buttonLabel = it.buttonLabel
                val label = if(it.requiresTranslation != false && buttonLabel != null) t(buttonLabel) else buttonLabel
                ChatModifier(
                    label = label ?: name,
                    data = serializeB64Json(it),
                    actorUuid = actorUuid,
                )
            }.toTypedArray()
        ),
    )
}


suspend fun rollCheck(
    afterRoll: AfterRoll,
    rollMode: RollMode?,
    activity: RawActivity?,
    skill: KingdomSkill,
    modifier: Int,
    modifierWithCreativeSolution: Int,
    fortune: Boolean,
    modifierPills: Array<String>,
    dc: Int,
    kingdomActor: KingdomActor,
    upgrades: Set<UpgradeResult>,
    rollTwiceKeepHighest: Boolean,
    rollTwiceKeepLowest: Boolean,
    creativeSolutionPills: Array<String>,
    freeAndFairPills: Array<String>,
    isCreativeSolution: Boolean = false,
    downgrades: Set<DowngradeResult>,
    degreeMessages: DegreeMessages?,
    useFameInfamy: Boolean,
    assurance: Boolean,
    notes: Set<Note>,
    eventStageIndex: Int,
    event: KingdomEvent?,
    eventIndex: Int,
    isFreeAndFair: Boolean,
    modifierWithoutFreeAndFair: Int,
): DegreeOfSuccess {
    val result = d20Check(
        dc = dc,
        modifier = if (isCreativeSolution) {
            modifierWithCreativeSolution
        } else if (isFreeAndFair) {
            modifierWithoutFreeAndFair
        } else {
            modifier
        },
        rollMode = rollMode,
        rollTwiceKeepHighest = rollTwiceKeepHighest,
        rollTwiceKeepLowest = rollTwiceKeepLowest,
        toChat = false,
        assurance = assurance,
    )

    if (isCreativeSolution) {
        kingdomActor.getKingdom()?.let {
            postChatMessage(t("kingdom.reducingCreativeSolutions"))
            it.creativeSolutions = max(0, it.creativeSolutions - 1)
            kingdomActor.setKingdom(it)
        }
    }

    if (isFreeAndFair) {
        kingdomActor.getKingdom()?.let {
            postChatMessage(t("kingdom.losing2Rp"))
            it.resourcePoints.now = max(0, it.resourcePoints.now - 2)
            kingdomActor.setKingdom(it)
        }
    }

    if (useFameInfamy) {
        kingdomActor.getKingdom()?.let {
            val fameType = if (it.fame.type == "famous") t(FameType.FAMOUS) else t(FameType.INFAMOUS)
            postChatMessage(t("kingdom.reducingFame", recordOf("type" to fameType)))
            it.fame.now = max(0, it.fame.now - 1)
            kingdomActor.setKingdom(it)
        }
    }

    val degreeResult = determineDegree(result.degreeOfSuccess, upgrades, downgrades)
    val originalDegree = degreeResult.originalDegree
    val changed = degreeResult.changedDegree
    val nonNullRollMode = rollMode ?: RollMode.PUBLICROLL
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
        additionalChatMessages = degreeMessages,
        upgrades = upgrades,
        downgrades = downgrades,
        notes = notes,
        eventId = event?.id,
        eventIndex = eventIndex,
        eventStageIndex = eventStageIndex,
        freeAndFairPills = freeAndFairPills,
        modifierWithoutFreeAndFair = modifierWithoutFreeAndFair,
        isFreeAndFair = isFreeAndFair,
    )
    result.toChat(rollMeta, isHtml = true)
    if (activity == null && event == null) {
        postDegreeOfSuccess(
            degreeOfSuccess = changed,
            originalDegreeOfSuccess = originalDegree,
        )
    } else {
        afterRoll(changed)
        val context = UpgradeMetaContext(
            rollMode = rollMode?.value ?: RollMode.PUBLICROLL.value,
            activityId = activity?.id,
            degree = originalDegree.value,
            additionalChatMessages = serializeB64Json(degreeMessages),
            actorUuid = kingdomActor.uuid,
            eventId = event?.id,
            eventIndex = eventIndex,
            eventStageIndex = eventStageIndex,
            notes = serializeB64Json(notes.map { it.serialize() }.toTypedArray()),
        )
        postComplexDegreeOfSuccess(context, changed)
    }
    return changed
}

suspend fun postComplexDegreeOfSuccess(
    metaContext: UpgradeMetaContext,
    changedDegreeOfSuccess: DegreeOfSuccess,
) {
    val kingdomActor = fromUuidTypeSafe<KingdomActor>(metaContext.actorUuid) ?: return
    val kingdom = kingdomActor.getKingdom() ?: return
    val rollMode = RollMode.fromString(metaContext.rollMode) ?: return
    val degree = DegreeOfSuccess.fromString(metaContext.degree) ?: return
    val eventIndex = metaContext.eventIndex
    val activity = metaContext.activityId?.let { kingdom.getActivity(it) }
    val event = metaContext.eventId?.let { kingdom.getEvent(it) }
    val eventStageIndex = metaContext.eventStageIndex
    val stage = event?.stages[eventStageIndex]
    val resolveEventOn = event?.resolvedOn?.mapNotNull { DegreeOfSuccess.fromString(it) }?.toSet().orEmpty()
    val degreeMessages = metaContext.additionalChatMessages
        ?.let { deserializeB64Json<DegreeMessages>(it) }
    val metaHtml = tpl(
        path = "chatmessages/upgrade-roll-meta.hbs",
        ctx = metaContext.copy(degree = changedDegreeOfSuccess.value),
    )
    val chatModifiers = when (changedDegreeOfSuccess) {
        DegreeOfSuccess.CRITICAL_FAILURE -> activity?.criticalFailure?.modifiers ?: stage?.criticalFailure?.modifiers
        DegreeOfSuccess.FAILURE -> activity?.failure?.modifiers ?: stage?.failure?.modifiers
        DegreeOfSuccess.SUCCESS -> activity?.success?.modifiers ?: stage?.success?.modifiers
        DegreeOfSuccess.CRITICAL_SUCCESS -> activity?.criticalSuccess?.modifiers ?: stage?.criticalSuccess?.modifiers
    } ?: emptyArray()
    val message = when (changedDegreeOfSuccess) {
        DegreeOfSuccess.CRITICAL_FAILURE -> activity?.criticalFailure?.msg ?: stage?.criticalFailure?.msg
        DegreeOfSuccess.FAILURE -> activity?.failure?.msg ?: stage?.failure?.msg
        DegreeOfSuccess.SUCCESS -> activity?.success?.msg ?: stage?.success?.msg
        DegreeOfSuccess.CRITICAL_SUCCESS -> activity?.criticalSuccess?.msg ?: stage?.criticalSuccess?.msg
    }
    val additionalMessages = when (changedDegreeOfSuccess) {
        DegreeOfSuccess.CRITICAL_FAILURE -> degreeMessages?.criticalFailure
        DegreeOfSuccess.FAILURE -> degreeMessages?.failure
        DegreeOfSuccess.SUCCESS -> degreeMessages?.success
        DegreeOfSuccess.CRITICAL_SUCCESS -> degreeMessages?.criticalSuccess
    }
    val buttonEvent = event?.takeIf {
        val isLastStage = it.stages.size == eventStageIndex + 1
        val resolvedOnDegree = changedDegreeOfSuccess in resolveEventOn
        val isNotContinuousEvent = KingdomEventTrait.CONTINUOUS.value !in event.traits
        (resolvedOnDegree || isNotContinuousEvent) && isLastStage
    }
    val postHtml = if (chatModifiers.isNotEmpty()
        || buttonEvent != null
        || changedDegreeOfSuccess == DegreeOfSuccess.CRITICAL_SUCCESS
    ) {
        buildChatButtons(
            degree = changedDegreeOfSuccess,
            modifiers = chatModifiers,
            actorUuid = kingdomActor.uuid,
            eventIndex = eventIndex,
            eventId = buttonEvent?.id,
        )
    } else {
        ""
    }
    val notesContext = metaContext.notes
        ?.let { deserializeB64Json<Array<RawNote>>(it) }
        ?.filter { it.degree == null || it.degree == changedDegreeOfSuccess.value }
        ?.map { insertButtons(it.note) }
        ?.toTypedArray()
        ?: emptyArray()
    val notesHtml = tpl(
        path = "chatmessages/roll-notes.hbs",
        ctx = recordOf("notes" to notesContext)
    )
    postDegreeOfSuccess(
        degreeOfSuccess = changedDegreeOfSuccess,
        originalDegreeOfSuccess = degree,
        title = activity?.title ?: event?.name ?: "",
        rollMode = rollMode,
        metaHtml = metaHtml,
        preHtml = "${activity?.description ?: event?.description}",
        postHtml = notesHtml + postHtml,
        message = message,
    )
    if (additionalMessages != null) {
        postChatMessage(
            "$additionalMessages<span hidden=\"hidden\" data-kingdom-actor-uuid=\"${kingdomActor.uuid}\"></span>",
            rollMode = rollMode
        )
    }
}