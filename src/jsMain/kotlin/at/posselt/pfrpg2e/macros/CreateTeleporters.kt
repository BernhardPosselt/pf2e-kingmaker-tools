package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import js.objects.recordOf
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CreateTeleporterData {
    val name: Int
}

suspend fun createTeleporterPair(game: Game) {
    awaitablePrompt<CreateTeleporterData, Unit>(
        title = t("macros.createTeleporters.title"), // TODO: translate
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                TextInput(
                    name = "amount",
                    label = t("macros.createTeleporters.name"), // TODO: translate
                    value = ""
                )
            )
        )
    ) { _, _ ->

        console.log("hi")
    }
}
