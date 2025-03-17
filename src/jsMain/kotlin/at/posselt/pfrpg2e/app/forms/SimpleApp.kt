package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.app.App
import at.posselt.pfrpg2e.app.HandlebarsFormApplicationOptions
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.MenuControl
import at.posselt.pfrpg2e.utils.resolveTemplatePath
import com.foundryvtt.core.applications.api.ApplicationHeaderControlsEntry
import com.foundryvtt.core.applications.api.ApplicationPosition
import com.foundryvtt.core.applications.api.HandlebarsTemplatePart
import com.foundryvtt.core.applications.api.Window
import com.foundryvtt.core.game
import js.objects.Record
import js.objects.recordOf

abstract class SimpleApp<T : HandlebarsRenderContext>(
    title: String,
    template: String,
    controls: Array<MenuControl> = emptyArray(),
    classes: Array<String> = emptyArray(),
    scrollable: Array<String> = emptyArray(),
    width: Int? = undefined,
    height: Int? = null,
    id: String? = null,
    resizable: Boolean? = undefined,
): App<T>(
    HandlebarsFormApplicationOptions(
        window = Window(
            title = title,
            resizable = resizable,
            controls = controls.map {
                ApplicationHeaderControlsEntry(
                    label = it.label,
                    icon = it.icon,
                    action = it.action,
                    visible = !it.gmOnly || game.user.isGM,
                )
            }.toTypedArray()
        ),
        position = if (height == null) {
            ApplicationPosition(
                width = width,
            )
        } else {
            ApplicationPosition(
                width = width,
                height = height,
            )
        },
        classes = classes,
        tag = "div",
        parts = recordOf(
            "div" to HandlebarsTemplatePart(
                template = resolveTemplatePath(template),
                scrollable = scrollable,
            )
        )
    ).apply {
        id?.let { this.unsafeCast<Record<String, Any>>()["id"] = it }
    }
)