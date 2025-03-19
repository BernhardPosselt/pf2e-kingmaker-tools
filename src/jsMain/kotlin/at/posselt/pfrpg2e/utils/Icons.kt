package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import com.foundryvtt.core.Game
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.documents.Macro
import com.foundryvtt.core.onHotBarDrop
import js.objects.Object
import js.objects.recordOf
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onDragStartFunction
import kotlinx.js.JsPlainObject
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLElement


@JsPlainObject
external interface MacroData {
    val name: String
    val img: String
    val command: String
    val type: String
}

enum class SheetType {
    CAMPING,
    KINGDOM;

    companion object {
        fun fromString(value: String) = fromCamelCase<KingdomAbility>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

fun createPartyActorIcon(
    id: String,
    icon: Set<String>,
    toolTip: String,
    sheetType: SheetType,
    macroName: String,
    macroImg: String,
    onClick: suspend (id: String) -> Unit,
): HTMLElement {
    val kingdomLink = document.create.a {
        classes = setOf("create-button")
        i {
            classes = icon
        }
        attributes["data-tooltip"] = toolTip
        attributes["draggable"] = "true"
        onDragStartFunction = {
            it.stopPropagation()
            val ev = it as DragEvent
            val data = MacroData(
                name = macroName,
                img = macroImg,
                type = sheetType.value,
                // language=javascript
                command = """
                    game.pf2eKingmakerTools.macros.openSheet('${sheetType.value}', '$id');
                """.trimIndent(),
            )
            ev.dataTransfer!!.setData("text/plain", JSON.stringify(data))
        }
        onClickFunction = {
            it.preventDefault()
            it.stopPropagation()
            buildPromise {
                onClick(id)
            }
        }
    }
    return kingdomLink
}

fun registerMacroDropHooks(game: Game) {
    Hooks.onHotBarDrop { bar, data, slot ->
        buildPromise {
            if (Object.hasOwn(data, "type") && (data["type"] == "camping" || data["type"] == "kingdom")) {
                val macroData = data.unsafeCast<MacroData>()
                val data = recordOf(
                    "name" to macroData.name,
                    "img" to macroData.img,
                    "type" to "script",
                    "command" to macroData.command
                )
                val macro = Macro.create(data).await()
                game.user.assignHotbarMacro(macro, slot).await()
            }
        }
        undefined
    }
}