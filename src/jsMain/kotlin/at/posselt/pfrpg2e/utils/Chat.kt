package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.GetSpeakerOptions
import js.objects.jso
import js.objects.recordOf
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

suspend fun postDegreeOfSuccess(
    degreeOfSuccess: DegreeOfSuccess,
    originalDegreeOfSuccess: DegreeOfSuccess? = null,
    message: String? = null,
    rollMode: RollMode? = null,
    metaHtml: String = "",
    preHtml: String = "",
    postHtml: String = "",
    title: String = "",
) {
    val original = if (originalDegreeOfSuccess != null && originalDegreeOfSuccess != degreeOfSuccess) {
        t(originalDegreeOfSuccess)
    } else {
        null
    }
    postChatTemplate(
        "chatmessages/degree-of-success.hbs",
        recordOf(
            "isCriticalFailure" to (DegreeOfSuccess.CRITICAL_FAILURE == degreeOfSuccess),
            "isFailure" to (DegreeOfSuccess.FAILURE == degreeOfSuccess),
            "isSuccess" to (DegreeOfSuccess.SUCCESS == degreeOfSuccess),
            "isCriticalSuccess" to (DegreeOfSuccess.CRITICAL_SUCCESS == degreeOfSuccess),
            "degree" to degreeOfSuccess.toLabel(),
            "meta" to metaHtml,
            "message" to message,
            "original" to original,
            "postHtml" to postHtml,
            "preHtml" to preHtml,
            "title" to title,
        ),
        rollMode = rollMode,
    )
}


suspend fun postChatTemplate(
    templatePath: String,
    templateContext: Any? = jso(),
    rollMode: RollMode? = null,
    speaker: Actor? = null,
) {
    val message = tpl(templatePath, templateContext)
    postChatMessage(message, rollMode, speaker = speaker, isHtml = true)
}

suspend fun postChatMessage(
    message: String,
    rollMode: RollMode? = null,
    speaker: Actor? = null,
    isHtml: Boolean = false,
) {
    val value = if (isHtml) message else escapeHtml(message)
    val fixedMessage = if (rollMode == RollMode.BLINDROLL) {
        "<div hidden class=\"km-hide-from-user\"></div>$value"
    } else {
        value
    }
    val data = recordOf<String, Any?>(
        "content" to fixedMessage
    )
    if (speaker != null) {
        data["speaker"] = ChatMessage.getSpeaker(GetSpeakerOptions(actor = speaker))
    }
    rollMode?.let { ChatMessage.applyRollMode(data, it.toCamelCase()) }
    ChatMessage.create(data).await()
}

fun fixVisibility(game: Game, html: HTMLElement, message: ChatMessage) {
    if (!game.user.isGM
        && message.blind
        && html.querySelector(".km-hide-from-user") != null
    ) {
        html.hidden = true
    }
}

fun bindChatClick(selector: String, callback: (Event, HTMLElement) -> Unit) {
    document.getElementById("chat-log")
        ?.addEventListener("click", { event ->
            event.target
                ?.takeIfInstance<HTMLElement>()
                ?.closest(selector)
                ?.takeIfInstance<HTMLElement>()
                ?.let { callback(event, it) }
        })
}