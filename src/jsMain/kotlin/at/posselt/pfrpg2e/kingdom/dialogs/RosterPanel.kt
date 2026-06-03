package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.SimpleApp
import at.posselt.pfrpg2e.kingdom.data.RawCharacter
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidOfTypes
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.game
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
    private val onAdd: suspend (RawCharacter) -> Unit,
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

                    val actorUuid = element.querySelector("input[name='linkedActorUuid']")
                        ?.let { it as? org.w3c.dom.HTMLInputElement }
                        ?.value?.takeIf { it.isNotBlank() }
                    val img = actorUuid?.let { fromUuidOfTypes(it, PF2ECharacter::class, PF2ENpc::class)?.img }

                    val character = RawCharacter(
                        name = name,
                        actorUuid = actorUuid,
                    ).also {
                        it.speed = speed
                        it.plotHook = plotHook
                        it.role = if (isNpc) "npc" else "companion"
                        it.img = img
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
                    val actor = fromUuidOfTypes(uuid, PF2ECharacter::class, PF2ENpc::class)
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
    private val onSave: suspend (Int, RawCharacter) -> Unit,
    private val onDelete: suspend (Int) -> Unit,
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

                val active = element.querySelector("input[name='companionActive']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                    ?.checked ?: existing.active

                // Number fields: when the input is present we honor its value
                // (empty string -> null, i.e. cleared); when absent we keep existing.
                val destinationXField = element.querySelector("input[name='companionDestinationX']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                val destinationX = if (destinationXField != null) destinationXField.value.toIntOrNull() else existing.destinationX

                val destinationYField = element.querySelector("input[name='companionDestinationY']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                val destinationY = if (destinationYField != null) destinationYField.value.toIntOrNull() else existing.destinationY

                val etaField = element.querySelector("input[name='companionEta']")
                    ?.let { it as? org.w3c.dom.HTMLInputElement }
                val eta = if (etaField != null) etaField.value.toIntOrNull() else existing.eta

                // A companion with a remaining ETA (>= 1 day) is en route. Derive this from
                // the ETA so there is no separate "traveling" toggle to forget — setting a
                // destination + ETA is all it takes to send them on their way.
                val traveling = eta != null && eta >= 1

                val updated = RawCharacter(
                    name = name,
                    actorUuid = existing.actorUuid,
                ).also {
                    it.speed = speed
                    it.plotHook = plotHook
                    it.role = if (isNpc) "npc" else "companion"
                    it.traveling = traveling
                    it.active = active
                    it.destinationX = destinationX
                    it.destinationY = destinationY
                    it.eta = eta
                    it.img = existing.img
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
