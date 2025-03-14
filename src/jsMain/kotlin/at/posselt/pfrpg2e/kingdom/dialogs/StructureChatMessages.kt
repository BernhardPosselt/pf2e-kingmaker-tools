package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.kingdom.sheet.Resource
import at.posselt.pfrpg2e.kingdom.sheet.ResourceButton
import at.posselt.pfrpg2e.kingdom.sheet.ResourceMode
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.core.ui.enrichHtml
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.ul
import kotlinx.html.li
import kotlinx.html.unsafe
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ChatStructure {
    val free: Boolean
    val name: String
    val link: String
    val slowedLink: String?
    val cost: ChatCost
    val messages: Array<String>?
}

@JsPlainObject
external interface ChatCost {
    val rp: Int
    val ore: Int
    val lumber: Int
    val stone: Int
    val luxuries: Int
    val label: String
}

private fun buildStructureHints(structure: Structure): List<String> {
    return listOfNotNull(
        structure.reduceUnrestBy?.let {
            listOfNotNull(
                generatePrefix(it.moreThanOncePerTurn),
                ResourceButton(
                    value = it.value,
                    mode = ResourceMode.LOSE,
                    resource = Resource.UNREST,
                ).toHtml(),
                it.note,
            ).joinToString(" ")
        },
        structure.reduceRuinBy?.let {
            listOfNotNull(
                generatePrefix(it.moreThanOncePerTurn),
                createButtons(it.ruin, it.value, ResourceMode.LOSE),
            ).joinToString(" ")
        },
        structure.gainRuin?.let {
            listOfNotNull(
                generatePrefix(it.moreThanOncePerTurn),
                createButtons(it.ruin, it.value, ResourceMode.GAIN),
            ).joinToString(" ")
        },
    )
}

private fun generatePrefix(moreThanOnce: Boolean) =
    if (moreThanOnce) {
        "Each time you build this structure, choose one of"
    } else {
        "The first time you build this structure each turn, choose one of"
    }

private fun createButtons(
    ruin: Ruin?,
    value: Int,
    mode: ResourceMode,
): String {
    val button = ResourceButton(
        value = "$value",
        resource = Resource.CRIME,
        mode = mode,
    )
    val buttons = when (ruin) {
        Ruin.CORRUPTION -> listOf(button.copy(resource = Resource.CORRUPTION))
        Ruin.CRIME -> listOf(button.copy(resource = Resource.CRIME))
        Ruin.DECAY -> listOf(button.copy(resource = Resource.DECAY))
        Ruin.STRIFE -> listOf(button.copy(resource = Resource.STRIFE))
        null -> listOf(
            button.copy(resource = Resource.CORRUPTION),
            button.copy(resource = Resource.CRIME),
            button.copy(resource = Resource.DECAY),
            button.copy(resource = Resource.STRIFE),
        )
    }
    return document.create.ul {
        buttons.forEach {
            li {
                unsafe {
                    +it.toHtml()
                }
            }
        }
    }.outerHTML
}

suspend fun buildDegreeMessages(
    ore: Int,
    lumber: Int,
    stone: Int,
    luxuries: Int,
    rp: Int,
    structure: Structure,
    rubble: Structure,
): DegreeMessages {
    val rubbleLink = enrichHtml(buildUuid(rubble.uuid, "Rubble"))
    val buildingLink = enrichHtml(buildUuid(structure.uuid, structure.name))
    val slowedLink = enrichHtml(buildUuid("Compendium.pf2e.conditionitems.Item.xYTAsEpcJE1Ccni3", "Slowed"))
    val isFree = ore == 0 && stone == 0 && rp == 0 && lumber == 0 && luxuries == 0
    val cost = ChatCost(
        ore = ore,
        stone = stone,
        rp = rp,
        lumber = lumber,
        luxuries = luxuries,
        label = listOf(
            "RP" to rp / 2,
            "Ore" to ore / 2,
            "Stone" to stone / 2,
            "Lumber" to lumber / 2,
            "Luxuries" to luxuries / 2,
        )
            .filter { (_, value) -> value > 0 }
            .joinToString(", ") { (label, amount) -> "$label: $amount" }
    )
    val halvedCost = ChatCost(
        ore = ore / 2,
        stone = stone / 2,
        rp = rp / 2,
        lumber = lumber / 2,
        luxuries = luxuries / 2,
        label = listOf(
            "RP" to rp,
            "Ore" to ore,
            "Stone" to stone,
            "Lumber" to lumber,
            "Luxuries" to luxuries,
        )
            .filter { (_, value) -> value > 0 }
            .joinToString(", ") { (label, amount) -> "$label: $amount" },
    )
    val messages = buildStructureHints(structure)
    return DegreeMessages(
        criticalSuccess = tpl(
            path = "chatmessages/structure-cost.hbs",
            ctx = ChatStructure(
                free = isFree,
                name = structure.name,
                link = buildingLink,
                cost = halvedCost,
                messages = messages.toTypedArray(),
            ),
        ),
        success = tpl(
            path = "chatmessages/structure-cost.hbs",
            ctx = ChatStructure(
                free = isFree,
                name = structure.name,
                link = buildingLink,
                cost = cost,
                messages = messages.toTypedArray(),
            ),
        ),
        failure = tpl(
            path = "chatmessages/structure-cost.hbs",
            ctx = ChatStructure(
                free = isFree,
                name = structure.name,
                link = buildingLink,
                slowedLink = slowedLink,
                cost = cost,
            ),
        ),
        criticalFailure = tpl(
            path = "chatmessages/structure-cost.hbs",
            ctx = ChatStructure(
                free = isFree,
                name = structure.name,
                link = rubbleLink,
                cost = cost,
            ),
        ),
    )
}