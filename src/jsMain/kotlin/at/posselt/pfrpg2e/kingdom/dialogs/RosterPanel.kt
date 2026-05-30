package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.SimpleApp
import at.posselt.pfrpg2e.kingdom.data.RawCharacter
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

/**
 * Dialog for adding a new companion/NPC to the roster.
 * Allows searching existing actors or creating a new entry.
 */
@JsPlainObject
external interface RosterAddContext : HandlebarsRenderContext {
    val actorName: String
    val isNpc: Boolean
    val speed: Int
    val plotHook: String
}

class RosterAddDialog(
    private val onAdd: (RawCharacter) -> Unit,
) : SimpleApp<RosterAddContext>(
    title = t("kingdom.roster.addCompanion"),
    template = "applications/kingdom/roster-add.hbs",
    id = "kmRosterAddDialog",
    classes = setOf("km-roster-add-dialog"),
) {
    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> buildPromise {
                val nameInput = element.querySelector("input[name='companionName']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                val name = nameInput?.value?.takeIf { it.isNotBlank() } ?: ""
                if (name.isNotBlank()) {
                    val isNpc = element.querySelector("select[name='companionRole']")
                        ?.let { it as? org.w3c.dom.HTMLSelectElement }
                        ?.value == "npc"
                    val speed = element.querySelector("input[name='companionSpeed']")
                        ?.let { it as? org.w3c.dom.HTMLInputElement }
                        ?.value?.toIntOrNull() ?: 0
                    val plotHook = element.querySelector("textarea[name='companionPlotHook']")
                        ?.let { it as? org.w3c.dom.HTMLTextAreaElement }
                        ?.value ?: ""

                    val character = RawCharacter(
                        name = name,
                    ).also {
                        it.speed = speed
                        it.plotHook = plotHook
                        it.role = if (isNpc) "npc" else "companion"
                    }
                    onAdd(character)
                    close()
                }
            }

            "cancel" -> close()

            "search-actor" -> buildPromise {
                val searchInput = element.querySelector("input[name='actorSearch']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                val query = searchInput?.value?.takeIf { it.isNotBlank() } ?: return@buildPromise
                // Search PF2ECharacter and PF2ENpc actors by name
                val actors = game.actors.filter { actor ->
                    (actor is PF2ECharacter || actor is PF2ENpc) &&
                        actor.name.contains(query, ignoreCase = true)
                }
                // Populate dropdown with results
                val dropdown = element.querySelector(".km-roster-search-results")
                    ?.let { it as? HTMLElement }
                if (dropdown != null) {
                    dropdown.innerHTML = actors.joinToString("") { actor ->
                        """<option value="${actor.uuid}">${actor.name} (${if (actor is PF2ENpc) "NPC" else "PC"})</option>"""
                    }
                }
            }

            "link-actor" -> buildPromise {
                val uuidInput = element.querySelector("select[name='searchResults']")
                    ?.let { it as? org.w3c.dom.HTMLSelectElement }
                val uuid = uuidInput?.value?.takeIf { it.isNotBlank() } ?: return@buildPromise
                // Populate the hidden actorUuid field
                val hiddenField = element.querySelector("input[name='linkedActorUuid']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                if (hiddenField != null) {
                    hiddenField.value = uuid
                    val actor = game.actors.get(uuid)
                    if (actor != null) {
                        val nameInput = element.querySelector("input[name='companionName']")
                            ?.let { it as? org.w3c.dom.HTMLInputElement }
                        if (nameInput != null) nameInput.value = actor.name
                    }
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions,
    ): Promise<RosterAddContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        RosterAddContext(
            partId = parent.partId,
            actorName = "",
            isNpc = false,
            speed = 0,
            plotHook = "",
        )
    }
}

/**
 * Dialog for editing an existing companion/NPC on the roster.
 */
@JsPlainObject
external interface RosterEditContext : HandlebarsRenderContext {
    val index: Int
    val actorName: String
    val isNpc: Boolean
    val speed: Int
    val plotHook: String
    val traveling: Boolean
    val active: Boolean
    val destinationX: Int?
    val destinationY: Int?
    val eta: Int?
}

class RosterEditDialog(
    private val index: Int,
    private val existing: RawCharacter,
    private val onSave: (Int, RawCharacter) -> Unit,
    private val onDelete: (Int) -> Unit,
) : SimpleApp<RosterEditContext>(
    title = t("kingdom.roster.editCompanion"),
    template = "applications/kingdom/roster-edit.hbs",
    id = "kmRosterEditDialog",
    classes = setOf("km-roster-edit-dialog"),
) {
    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> buildPromise {
                val name = element.querySelector("input[name='companionName']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                    ?.value?.takeIf { it.isNotBlank() } ?: existing.name

                val speed = element.querySelector("input[name='companionSpeed']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                    ?.value?.toIntOrNull() ?: existing.speed

                val plotHook = element.querySelector("textarea[name='companionPlotHook']")
                    ?.let { it as? org.w3c.dom.HTMLTextAreaElement }
                    ?.value ?: (existing.plotHook ?: "")

                val isNpc = element.querySelector("select[name='companionRole']")
                    ?.let { it as? org.w3c.dom.HTMLSelectElement }
                    ?.value == "npc"

                val updated = RawCharacter(
                    name = name,
                    actorUuid = existing.actorUuid,
                ).also {
                    it.speed = speed
                    it.plotHook = plotHook
                    it.role = if (isNpc) "npc" else "companion"
                    it.traveling = existing.traveling
                    it.active = existing.active
                    it.destinationX = existing.destinationX
                    it.destinationY = existing.destinationY
                    it.eta = existing.eta
                }
                onSave(index, updated)
                close()
            }

            "delete" -> buildPromise {
                onDelete(index)
                close()
            }

            "cancel" -> close()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions,
    ): Promise<RosterEditContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        RosterEditContext(
            partId = parent.partId,
            index = index,
            actorName = existing.name,
            isNpc = existing.role == "npc",
            speed = existing.speed,
            plotHook = existing.plotHook ?: "",
            traveling = existing.traveling,
            active = existing.active,
            destinationX = existing.destinationX,
            destinationY = existing.destinationY,
            eta = existing.eta,
        )
    }
}
