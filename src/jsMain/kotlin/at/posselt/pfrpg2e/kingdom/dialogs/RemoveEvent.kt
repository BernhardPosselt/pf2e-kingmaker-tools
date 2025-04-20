package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.OngoingEvent
import at.posselt.pfrpg2e.utils.t
import kotlinx.js.JsPlainObject


@JsPlainObject
external interface RemoveEventContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface RemoveEventData {
    val index: Int
}


suspend fun removeEvent(
    events: List<OngoingEvent>,
    settlements: List<Settlement>,
): Int {
    return awaitablePrompt<RemoveEventData, Int>(
        title = t("kingdom.pickEventToRemove"),
        templateContext = RemoveEventContext(
            formRows = formContext(
                Select(
                    name = "index",
                    label = t("kingdom.event"),
                    value = events.first().eventIndex.toString(),
                    overrideType = OverrideType.NUMBER,
                    options = events.mapIndexed { index, it ->
                        val name = it.event.name
                        val settlement = it.settlementSceneId
                            ?.let { sceneId -> settlements.find { it.id == sceneId } }
                        val suffix = if (it.secretLocation || settlement == null) {
                            " ($index)"
                        } else {
                            " (${settlement.name})"
                        }
                        SelectOption(value = it.eventIndex.toString(), label = name + suffix)
                    }
                )
            )
        ),
        templatePath = "components/forms/form.hbs",
    ) { data, _ -> data.index }
}