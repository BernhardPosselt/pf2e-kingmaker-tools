package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface ModifierContext {
    val name: String
    val description: String
    val turns: String
    val isConsumedAfterRoll: Boolean
}


fun Array<RawModifier>.toContext(): Array<ModifierContext> {
    return map {
        ModifierContext(
            name = it.name,
            description = it.buttonLabel ?: it.name,
            turns = if ((it.turns ?: 0) > 0) "${it.turns}" else t("kingdom.indefinite"),
            isConsumedAfterRoll = it.isConsumedAfterRoll == true,
        )
    }.toTypedArray()
}