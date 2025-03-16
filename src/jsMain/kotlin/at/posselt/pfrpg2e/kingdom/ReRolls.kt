package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.dialogs.DegreeMessages
import at.posselt.pfrpg2e.kingdom.dialogs.rollCheck
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.serializeB64Json
import at.posselt.pfrpg2e.utils.tpl
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.collections.isNotEmpty
import kotlin.text.toInt


@JsPlainObject
external interface UpOrDowngrade {
    val degree: String
    val times: Int
}


@JsPlainObject
private external interface RollMetaContext {
    val label: String
    val dc: Int
    val skill: String
    val activityId: String?
    val actorUuid: String
    val degree: String
    val rollMode: String
    val modifier: Int
    val pills: Array<String>
    val creativeSolutionPills: Array<String>
    val fortune: Boolean
    val modifierWithCreativeSolution: Int
    val upgrades: String?
    val downgrades: String?
    val additionalChatMessages: String?
    val isCreativeSolution: Boolean
}

private fun parseRollMeta(rollElement: HTMLElement): RollMetaContext {
    val meta = rollElement.querySelector(".km-roll-meta") as HTMLElement?
    val creativePills = rollElement.querySelectorAll(".km-modifier-pill.km-creative-solution-pills").asList()
        .map { it.textContent ?: "" }
        .toTypedArray()
    val pills = rollElement.querySelectorAll(".km-modifier-pill:not(.km-creative-solution-pills)").asList()
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
        skill = meta?.dataset["skill"] ?: "",
        pills = pills,
        additionalChatMessages = meta?.dataset["additionalChatMessages"],
        upgrades = meta?.dataset["upgrades"],
        downgrades = meta?.dataset["downgrades"],
        creativeSolutionPills = creativePills,
        fortune = meta?.dataset["fortune"] == "true",
        modifierWithCreativeSolution = meta?.dataset["modifierWithCreativeSolution"]?.toInt() ?: 0,
        isCreativeSolution = false,
    )
}

suspend fun generateRollMeta(
    activity: RawActivity?,
    modifier: Int,
    modifierPills: Array<String>,
    actor: KingdomActor,
    rollMode: RollMode,
    degree: DegreeOfSuccess,
    skill: KingdomSkill,
    dc: Int,
    fortune: Boolean,
    creativeSolutionPills: Array<String>,
    modifierWithCreativeSolution: Int,
    isCreativeSolution: Boolean,
    additionalChatMessages: DegreeMessages?,
    upgrades: Set<UpgradeResult>,
    downgrades: Set<DowngradeResult>,
): String {
    val upgradeData = upgrades
        .map { UpOrDowngrade(degree = it.upgrade.value, times = it.times) }
        .toTypedArray()
    val downgradeData = downgrades
        .map { UpOrDowngrade(degree = it.downgrade.value, times = it.times) }
        .toTypedArray()

    return tpl(
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
            skill = skill.value,
            additionalChatMessages = additionalChatMessages?.let { serializeB64Json(it) },
            upgrades = upgradeData.takeIf { it.isNotEmpty() }?.let { serializeB64Json(it) },
            downgrades = downgradeData.takeIf { it.isNotEmpty() }?.let { serializeB64Json(it) },
            modifierWithCreativeSolution = modifierWithCreativeSolution,
            pills = modifierPills,
            creativeSolutionPills = creativeSolutionPills,
            isCreativeSolution = isCreativeSolution,
        ),
    )
}

enum class ReRollMode {
    ROLL_TWICE_KEEP_HIGHEST,
    ROLL_TWICE_KEEP_LOWEST,
    DEFAULT,
    FAME_OR_INFAMY,
    CREATIVE_SOLUTION;
}

suspend fun reRoll(chatMessage: HTMLElement, mode: ReRollMode) {
    val meta = parseRollMeta(chatMessage)
    val rollMode = RollMode.fromString(meta.rollMode) ?: return
    val actor = fromUuidTypeSafe<KingdomActor>(meta.actorUuid) ?: return
    val kingdom = actor.getKingdom() ?: return
    val skill = KingdomSkill.fromString(meta.skill) ?: return
    val activity = meta.activityId?.let { kingdom.getActivity(it) }
    val upgrades = meta.upgrades?.let { deserializeB64Json<Array<UpOrDowngrade>>(it) }.orEmpty()
    val downgrades = meta.downgrades?.let { deserializeB64Json<Array<UpOrDowngrade>>(it) }.orEmpty()
    val degreeMessages = meta.additionalChatMessages?.let { deserializeB64Json<DegreeMessages>(it) }
    console.log(upgrades, downgrades)
    rollCheck(
        afterRoll = {},
        rollMode = rollMode,
        activity = activity,
        skill = skill,
        modifier = meta.modifier,
        modifierWithCreativeSolution = meta.modifierWithCreativeSolution,
        fortune = meta.fortune,
        modifierPills = meta.pills,
        creativeSolutionPills = meta.creativeSolutionPills,
        dc = meta.dc,
        kingdomActor = actor,
        isCreativeSolution = mode == ReRollMode.CREATIVE_SOLUTION,
        upgrades = upgrades.mapNotNull { result ->
            DegreeOfSuccess.fromString(result.degree)?.let { degree ->
                UpgradeResult(upgrade = degree, times = result.times)
            }
        }.toSet(),
        downgrades = downgrades.mapNotNull { result ->
            DegreeOfSuccess.fromString(result.degree)?.let { degree ->
                DowngradeResult(downgrade = degree, times = result.times)
            }
        }.toSet(),
        degreeMessages = degreeMessages,
        rollTwiceKeepHighest = mode == ReRollMode.ROLL_TWICE_KEEP_HIGHEST,
        rollTwiceKeepLowest = mode == ReRollMode.ROLL_TWICE_KEEP_LOWEST,
        useFameInfamy = mode == ReRollMode.FAME_OR_INFAMY,
        assurance = false,
    )
}