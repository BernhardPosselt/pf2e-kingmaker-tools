package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.actions.ActionDispatcher
import at.posselt.pfrpg2e.camping.createCampingIcon
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.createKingmakerIcon
import at.posselt.pfrpg2e.kingdom.dialogs.ActorActions
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.toCamelCase
import com.foundryvtt.core.Game
import com.foundryvtt.core.directories.onRenderActorDirectory
import com.foundryvtt.core.documents.Macro
import com.foundryvtt.core.game
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.core.helpers.onHotBarDrop
import com.foundryvtt.pf2e.actor.PF2EParty
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
import org.w3c.dom.asList
import org.w3c.dom.get


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
}

fun createPartyActorIcon(
    id: String,
    icon: Set<String>,
    toolTip: String,
    sheetType: SheetType,
    macroName: String? = null,
    macroImg: String? = null,
    onClick: suspend (id: String) -> Unit,
): HTMLElement {
    val kingdomLink = document.create.a {
        classes = setOf("create-button")
        i {
            classes = icon
        }
        attributes["data-tooltip"] = toolTip
        attributes["draggable"] = "true"
        if (macroImg != null && macroName != null) {
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
    TypedHooks.onHotBarDrop { bar, data, slot ->
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

fun registerIcons(actionDispatcher: ActionDispatcher) {
    TypedHooks.onRenderActorDirectory { _, html, _ ->
        html.querySelectorAll(".folder[data-party]")
            .asList()
            .filterIsInstance<HTMLElement>()
            .forEach {
                val id = it.dataset["entryId"]
                if (id != null) {
                    val insertAfter = it.querySelector(".folder-name")
                    if (game.user.isGM) {
                        insertAfter?.insertAdjacentElement(
                            "afterend", createPartyActorIcon(
                                id = id,
                                icon = setOf("fa-solid", "fa-ellipsis-vertical"),
                                toolTip = t("applications.actorActions.tooltip"),
                                sheetType = SheetType.KINGDOM,
                                onClick = {
                                    game.actors.get(id)?.takeIfInstance<PF2EParty>()?.let { actor ->
                                        ActorActions(actor = actor).launch()
                                    }
                                },
                            )
                        )
                    }
                    insertAfter?.insertAdjacentElement("afterend", createCampingIcon(id, actionDispatcher))
                    insertAfter?.insertAdjacentElement("afterend", createKingmakerIcon(id, actionDispatcher))
                    if (game.settings.pfrpg2eKingdomCampingWeather.getHideBuiltinKingdomSheet()) {
                        it.querySelector(".fa-crown")
                            ?.parentElement
                            ?.takeIfInstance<HTMLElement>()
                            ?.let { elem ->
                                elem.hidden = true
                            }
                    }
                }
            }
    }
}