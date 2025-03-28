package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.RawNote
import at.posselt.pfrpg2e.kingdom.UpgradeMetaContext
import at.posselt.pfrpg2e.kingdom.generateRollMeta
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.Note
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.determineDegree
import at.posselt.pfrpg2e.kingdom.serialize
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.utils.d20Check
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.postDegreeOfSuccess
import at.posselt.pfrpg2e.utils.serializeB64Json
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
    isCreativeSolution: Boolean = false,
    downgrades: Set<DowngradeResult>,
    degreeMessages: DegreeMessages?,
    useFameInfamy: Boolean,
    assurance: Boolean,
    notes: Set<Note>,
): DegreeOfSuccess {
    val result = d20Check(
        dc = dc,
        modifier = if (isCreativeSolution) modifierWithCreativeSolution else modifier,
        rollMode = rollMode,
        rollTwiceKeepHighest = rollTwiceKeepHighest,
        rollTwiceKeepLowest = rollTwiceKeepLowest,
        toChat = false,
        assurance = assurance,
    )

    if (isCreativeSolution) {
        kingdomActor.getKingdom()?.let {
            postChatMessage("Reduced Creative Solutions by 1")
            it.creativeSolutions = max(0, it.creativeSolutions - 1)
            kingdomActor.setKingdom(it)
        }
    }

    if (useFameInfamy) {
        kingdomActor.getKingdom()?.let {
            val fameType = if (it.fame.type == "famous") "Fame" else "Infamy"
            postChatMessage("Reducing $fameType by 1")
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
            additionalChatMessages = serializeB64Json(degreeMessages),
            actorUuid = kingdomActor.uuid,
            notes = serializeB64Json(notes.map { it.serialize() }.toTypedArray()),
        )
        postActivityDegreeOfSuccess(context, changed)
    }
    return changed
}

suspend fun postActivityDegreeOfSuccess(
    metaContext: UpgradeMetaContext,
    changedDegreeOfSuccess: DegreeOfSuccess,
) {
    val kingdomActor = fromUuidTypeSafe<KingdomActor>(metaContext.actorUuid) ?: return
    val kingdom = kingdomActor.getKingdom() ?: return
    val activity = kingdom.getActivity(metaContext.activityId) ?: return
    val rollMode = RollMode.fromString(metaContext.rollMode) ?: return
    val degree = DegreeOfSuccess.fromString(metaContext.degree) ?: return
    val degreeMessages = metaContext.additionalChatMessages
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
    val notesContext = metaContext.notes
        ?.let { deserializeB64Json<Array<RawNote>>(it) }
        ?.filter { it.degree == null || it.degree == changedDegreeOfSuccess.value }
        ?.toTypedArray()
        ?: emptyArray()
    val notesHtml = tpl(
        path = "chatmessages/roll-notes.hbs",
        ctx = recordOf("notes" to notesContext)
    )
    postDegreeOfSuccess(
        degreeOfSuccess = changedDegreeOfSuccess,
        originalDegreeOfSuccess = degree,
        title = activity.title,
        rollMode = rollMode,
        metaHtml = metaHtml,
        preHtml = "<p>${activity.description}</p>",
        postHtml = notesHtml + postHtml,
        message = modifiers?.msg,
    )
    if (messages != null) {
        postChatMessage(
            "$messages<span hidden=\"hidden\" data-kingdom-actor-uuid=\"${kingdomActor.uuid}\"></span>",
            rollMode = rollMode
        )
    }
}