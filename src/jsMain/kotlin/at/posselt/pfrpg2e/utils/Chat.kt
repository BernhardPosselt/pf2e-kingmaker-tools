package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import com.foundryvtt.core.Actor
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.documents.ChatMessage
import com.foundryvtt.core.documents.GetSpeakerOptions
import com.foundryvtt.core.onRenderChatMessage
import io.kvision.jquery.get
import js.objects.ReadonlyRecord
import js.objects.jso
import js.objects.recordOf
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

suspend fun postDegreeOfSuccess(
    degreeOfSuccess: DegreeOfSuccess,
    message: String? = null,
    rollMode: RollMode? = null,
    metaHtml: String = "",
) {
    postChatMessage(
        message = tpl(
            "chatmessages/degree-of-success.hbs", recordOf(
                "isCriticalFailure" to (DegreeOfSuccess.CRITICAL_FAILURE == degreeOfSuccess),
                "isFailure" to (DegreeOfSuccess.FAILURE == degreeOfSuccess),
                "isSuccess" to (DegreeOfSuccess.SUCCESS == degreeOfSuccess),
                "isCriticalSuccess" to (DegreeOfSuccess.CRITICAL_SUCCESS == degreeOfSuccess),
                "degreeLabel" to degreeOfSuccess.toLabel(),
                "meta" to metaHtml,
                "message" to message,
            )
        ),
        rollMode = rollMode,
    )
}


suspend fun postChatTemplate(
    templatePath: String,
    templateContext: ReadonlyRecord<String, Any?> = jso(),
    rollMode: RollMode? = null,
    speaker: Actor? = null,
) {
    val message = tpl(templatePath, templateContext)
    postChatMessage(message, rollMode, speaker = speaker)
}

suspend fun postChatMessage(
    message: String,
    rollMode: RollMode? = null,
    speaker: Actor? = null
) {
    val fixedMessage = if (rollMode == RollMode.BLINDROLL) {
        "<div hidden class=\"km-hide-from-user\"></div>$message"
    } else {
        message
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