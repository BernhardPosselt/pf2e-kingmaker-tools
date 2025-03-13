package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import kotlinx.browser.document
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.js.button

enum class Turn {
    NOW,
    NEXT;

    companion object {
        fun fromString(value: String) = fromCamelCase<Turn>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

enum class ResourceMode {
    GAIN,
    LOSE;

    companion object {
        fun fromString(value: String) = fromCamelCase<ResourceMode>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

enum class Resource(val value: String) {
    RESOURCE_DICE("resource-dice"),
    CRIME("crime"),
    DECAY("decay"),
    CORRUPTION("corruption"),
    STRIFE("strife"),
    RESOURCE_POINTS("resource-points"),
    FOOD("food"),
    LUXURIES("luxuries"),
    UNREST("unrest"),
    ORE("ore"),
    LUMBER("lumber"),
    FAME("fame"),
    STONE("stone"),
    XP("xp"),
    SUPERNATURAL_SOLUTION("supernatural-solution"),
    CREATIVE_SOLUTION("creative-solution"),
    ROLLED_RESOURCE_DICE("rolled-resource-dice");

    val label: String
        get() = value.split("-").joinToString(" ") { it.toLabel() }
}

fun createResourceButton(
    turn: Turn = Turn.NOW,
    value: String,
    mode: ResourceMode = ResourceMode.GAIN,
    resource: Resource,
    hints: String? = null,
    multiple: Boolean = false,
): String {
    val turnLabel = if (turn == Turn.NEXT) " Next Turn" else ""
    val hints = hints?.let { " ($it)" } ?: ""
    val label = "${mode.label} $value ${resource.label}$turnLabel$hints"
    return document.create.button {
        type = ButtonType.button
        classes = setOf("km-gain-lose")
        attributes["type"] = resource.value
        attributes["mode"] = mode.value
        attributes["turn"] = turn.value
        attributes["multiple"] = multiple.toString()
        attributes["value"] = value
        +label
    }.outerHTML
}