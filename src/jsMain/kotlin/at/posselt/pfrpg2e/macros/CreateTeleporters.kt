package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.ColorInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.RegionBehavior
import com.foundryvtt.core.documents.RegionDocument
import com.foundryvtt.core.ui
import com.foundryvtt.core.utils.Color
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CreateTeleporterData {
    val name: String
    val color: String
    val targetScene: String
}

suspend fun Game.createTeleporterPair() {
    val scene = scenes.current
    if (scene == null) {
        val message = t("macros.createTeleporters.missingScene")
        ui.notifications.error(message)
        throw IllegalStateException(message)
    }
    awaitablePrompt<CreateTeleporterData, Unit>(
        title = t("macros.createTeleporters.title", recordOf("sceneName" to scene.name)),
        templatePath = "components/forms/form.hbs",
        width = 400,
        templateContext = recordOf(
            "description" to t("macros.createTeleporters.description"),
            "formRows" to formContext(
                TextInput(
                    name = "name",
                    label = t("macros.createTeleporters.name"),
                    value = "",
                    stacked = false
                ),
                ColorInput(
                    label = t("macros.createTeleporters.color"),
                    name = "color",
                    value = "#000000",
                    stacked = false
                ),
                Select(
                    name = "targetScene",
                    label = t("macros.createTeleporters.targetScene"),
                    value = scene.id!!,
                    stacked = false,
                    options = scenes.contents.map { SelectOption(label = it.name, value = it.id!!) }
                )
            )
        )
    ) { data, _ ->
        val targetScene = scenes.get(data.targetScene)
        checkNotNull(targetScene) {
            "Target Scene ${data.targetScene} was not found"
        }
        val color = Color(data.color.replace("#", "").toInt(16))
        val first = scene.createEmbeddedDocuments<RegionDocument>(
            "Region", arrayOf(
                recordOf(
                    "name" to "${data.name} 1",
                    "color" to color,
                )
            )
        ).await().first()
        val second = targetScene.createEmbeddedDocuments<RegionDocument>(
            "Region", arrayOf(
                recordOf(
                    "name" to "${data.name} 2",
                    "color" to color,
                )
            )
        ).await().first()
        first.createEmbeddedDocuments<RegionBehavior>(
            "RegionBehavior", arrayOf(
                recordOf(
                    "type" to "teleportToken",
                    "system" to recordOf(
                        "choice" to false,
                        "destination" to second.uuid,
                    ),
                )
            )
        ).await()
        second.createEmbeddedDocuments<RegionBehavior>(
            "RegionBehavior", arrayOf(
                recordOf(
                    "type" to "teleportToken",
                    "system" to recordOf(
                        "choice" to false,
                        "destination" to first.uuid,
                    ),
                )
            )
        ).await()
    }
}
