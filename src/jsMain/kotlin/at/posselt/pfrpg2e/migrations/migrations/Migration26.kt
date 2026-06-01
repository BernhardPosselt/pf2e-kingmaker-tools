package at.posselt.pfrpg2e.migrations.migrations

import com.foundryvtt.core.Game

/**
 * Converts the [watchSlots] field from a flat `Array<String>` (one actor UUID per slot,
 * empty string for an empty slot) to a nested `Array<Array<String>>` so that each watch slot
 * can hold multiple actors.
 *
 * Old format: `["Actor.abc", "", "Actor.def"]`
 * New format: `[["Actor.abc"], [], ["Actor.def"]]`
 *
 * Uses the `dynamic` receiver because the field's static type changed; operating on the raw
 * JS value lets us inspect the persisted (old) shape regardless of the current Kotlin type.
 */
class Migration26 : Migration(26) {

    override suspend fun migrateCamping(game: Game, camping: dynamic) {
        val slots = camping.watchSlots
        if (slots == null) {
            camping.watchSlots = emptyArray<Array<String>>()
            return
        }
        val length = slots.length as? Int ?: return
        val converted = (0 until length).map { index ->
            val entry: Any? = slots[index]
            when {
                jsTypeOf(entry) == "string" -> {
                    // Old flat entry: a non-empty UUID becomes a single-element slot.
                    val uuid = entry.unsafeCast<String>()
                    if (uuid.isNotEmpty()) arrayOf(uuid) else emptyArray()
                }
                // Already migrated (nested array) — keep as-is.
                entry is Array<*> -> entry.unsafeCast<Array<String>>()
                // Unexpected shape (object/null/undefined) — drop it.
                else -> emptyArray()
            }
        }.toTypedArray()
        camping.watchSlots = converted
    }
}
