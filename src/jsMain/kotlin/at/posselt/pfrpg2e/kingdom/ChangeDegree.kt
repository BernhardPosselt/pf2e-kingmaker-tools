package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.kingdom.dialogs.postActivityDegreeOfSuccess
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

@JsPlainObject
external interface UpgradeMetaContext {
    val rollMode: String
    val activityId: String
    val degree: String
    val additionalChatMessages: String?
    val notes: String?
    val actorUuid: String
}

private fun parseUpgradeMeta(elem: HTMLElement) =
    UpgradeMetaContext(
        rollMode = elem.dataset["rollMode"] ?: "",
        activityId = elem.dataset["activityId"] ?: "",
        degree = elem.dataset["degree"] ?: "",
        additionalChatMessages = elem.dataset["additionalChatMessages"],
        notes = elem.dataset["notes"],
        actorUuid = elem.dataset["kingdomActorUuid"] ?: "",
    )

enum class ChangeDegree {
    UPGRADE,
    DOWNGRADE;
}

suspend fun changeDegree(rollMeta: HTMLElement, mode: ChangeDegree) {
    val meta = parseUpgradeMeta(rollMeta)
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