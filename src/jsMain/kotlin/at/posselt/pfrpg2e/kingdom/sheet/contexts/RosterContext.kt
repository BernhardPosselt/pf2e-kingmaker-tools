package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.kingdom.data.RawCharacter
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RosterActorContext {
    val name: String
    val role: String
    val roleLabel: String
    val speed: Int
    val destinationX: Int?
    val destinationY: Int?
    val destinationLabel: String
    val eta: Int?
    val traveling: Boolean
    val active: Boolean
    val plotHook: String?
    val actorUuid: String?
    val img: String?
}

@JsPlainObject
external interface RosterContext {
    val items: Array<RosterActorContext>
    val isGM: Boolean
}

fun Array<RawCharacter>.toRosterContext(isGM: Boolean): RosterContext =
    RosterContext(
        items = mapIndexed { index, character ->
            RosterActorContext(
                name = character.name,
                role = character.role,
                roleLabel = if (character.role == "npc") "NPC" else "Companion",
                speed = character.speed,
                destinationX = character.destinationX,
                destinationY = character.destinationY,
                destinationLabel = if (character.destinationX != null && character.destinationY != null) {
                    "(${character.destinationX}, ${character.destinationY})"
                } else {
                    "-"
                },
                eta = character.eta,
                traveling = character.traveling,
                active = character.active,
                plotHook = character.plotHook,
                actorUuid = character.actorUuid,
                img = character.img,
            )
        }.toTypedArray(),
        isGM = isGM,
    )
