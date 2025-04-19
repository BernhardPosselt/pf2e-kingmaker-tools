package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierSelector
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface ModifierContext {
    val name: String
    val description: String
    val turns: String
    val selector: String
    val isConsumedAfterRoll: Boolean
}


fun Array<RawModifier>.toContext(): Array<ModifierContext> {
    return map {
        val name = if(it.requiresTranslation != false) t(it.name) else it.name
        val buttonLabel = it.buttonLabel
        val label = if(it.requiresTranslation != false && buttonLabel != null) t(buttonLabel) else buttonLabel
        ModifierContext(
            name = name,
            selector = t(it.selector
                ?.let { value -> ModifierSelector.fromString(value) }
                ?: ModifierSelector.CHECK),
            description = label ?: name,
            turns = if ((it.turns ?: 0) > 0) "${it.turns}" else t("kingdom.indefinite"),
            isConsumedAfterRoll = it.isConsumedAfterRoll == true,
        )
    }.toTypedArray()
}