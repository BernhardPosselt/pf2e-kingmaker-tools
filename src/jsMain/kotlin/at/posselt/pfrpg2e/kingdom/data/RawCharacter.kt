package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

/**
 * Schema for storing companion/NPC travel and state data on native Foundry VTT Actor flags.
 * Synced to/from native character-type actors via [at.posselt.pfrpg2e.utils.setAppFlag].
 */
@JsPlainObject
external interface RawCharacter {
    /** Display name of the companion/NPC */
    var name: String
    /** Optional reference to the Foundry Actor UUID this companion is bound to */
    var actorUuid: String?
    /** Travel: destination grid X coordinate (hex column) */
    var destinationX: Int?
    /** Travel: destination grid Y coordinate (hex row) */
    var destinationY: Int?
    /** Travel: speed in hexes per day */
    var speed: Int
    /** Travel: estimated time of arrival in days (counted down daily off the world clock) */
    var eta: Int?
    /** Plot hook or GM note associated with this companion */
    var plotHook: String?
    /** Whether this companion is currently traveling */
    var traveling: Boolean
    /** Whether this companion is active (false = sidelined/inactive) */
    var active: Boolean
    /** Companion role: "companion" for player companions, "npc" for NPCs */
    var role: String
    /** Optional portrait/image path */
    var img: String?
}

/**
 * Default companion state for a new companion actor.
 */
fun RawCharacter(
    name: String,
    actorUuid: String? = null,
): RawCharacter =
    js("{ name: name, actorUuid: actorUuid, speed: 0, traveling: false, active: true, role: 'companion', plotHook: '' }")
        .unsafeCast<RawCharacter>()
