package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.kingdom.sheet.Resource
import at.posselt.pfrpg2e.kingdom.sheet.ResourceButton
import at.posselt.pfrpg2e.kingdom.sheet.ResourceMode
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.tpl
import com.foundryvtt.core.applications.ux.TextEditor.enrichHtml
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.ul
import kotlinx.html.li
import kotlinx.html.unsafe
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface ChatStructure {
    val free: Boolean
    val name: String
    val link: String
    val slowedLink: String?
    val cost: ChatCost
    val messages: Array<String>?
    val actorUuid: String
    val addFamePoint: Boolean
    val initialRp: Int?
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
                ).toHtml(emptyArray()),
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
        t("kingdom.eachTimeYouBuild")
    } else {
        t("kingdom.theFirstTimeYouBuild")
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
                    +it.toHtml(emptyArray())
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
    actorUuid: String,
    initialRp: Int?,
    addFamePoint: Boolean,
): DegreeMessages {
    val rubbleLink = enrichHtml(buildUuid(rubble.uuid, t("kingdom.rubble")))
    val buildingLink = enrichHtml(buildUuid(structure.uuid, structure.name))
    val slowedLink = enrichHtml(buildUuid("Compendium.pf2e.conditionitems.Item.xYTAsEpcJE1Ccni3", t("kingdom.slowed")))
    val isFree = ore == 0 && stone == 0 && rp == 0 && lumber == 0 && luxuries == 0
    val cost = ChatCost(
        ore = ore,
        stone = stone,
        rp = rp,
        lumber = lumber,
        luxuries = luxuries,
        label = listOf(
            t("kingdom.rp") to rp,
            t("kingdom.ore") to ore,
            t("kingdom.stone") to stone,
            t("kingdom.lumber") to lumber,
            t("kingdom.luxuries") to luxuries,
        )
            .filter { (_, value) -> value > 0 }
            .joinToString(", ") { (label, amount) -> "$label: $amount" }
    )
    val halvedCost = ChatCost(
        ore = ore / 2,
        stone = stone / 2,
        rp = rp,
        lumber = lumber / 2,
        luxuries = luxuries / 2,
        label = listOf(
            t("kingdom.rp") to rp,
            t("kingdom.ore") to ore / 2,
            t("kingdom.stone") to stone / 2,
            t("kingdom.lumber") to lumber / 2,
            t("kingdom.luxuries") to luxuries / 2,
        )
            .filter { (_, value) -> value > 0 }
            .joinToString(", ") { (label, amount) -> "$label: $amount" },
    )
    val messages = buildStructureHints(structure)
    val criticalSuccess = tpl(
        path = "chatmessages/structure-cost.hbs",
        ctx = ChatStructure(
            free = isFree,
            name = structure.name,
            link = buildingLink,
            cost = halvedCost,
            actorUuid = actorUuid,
            messages = messages.toTypedArray(),
            addFamePoint = addFamePoint,
            initialRp = initialRp,
        ),
    )
    val success = tpl(
        path = "chatmessages/structure-cost.hbs",
        ctx = ChatStructure(
            free = isFree,
            name = structure.name,
            link = buildingLink,
            cost = cost,
            actorUuid = actorUuid,
            messages = messages.toTypedArray(),
            addFamePoint = addFamePoint,
            initialRp = initialRp,
        ),
    )
    val failure = tpl(
        path = "chatmessages/structure-cost.hbs",
        ctx = ChatStructure(
            free = isFree,
            name = structure.name,
            link = buildingLink,
            slowedLink = slowedLink,
            actorUuid = actorUuid,
            cost = cost,
            addFamePoint = addFamePoint,
            initialRp = initialRp,
        ),
    )
    val criticalFailure = tpl(
        path = "chatmessages/structure-cost.hbs",
        ctx = ChatStructure(
            free = isFree,
            name = structure.name,
            link = rubbleLink,
            actorUuid = actorUuid,
            cost = cost,
            addFamePoint = false,
        ),
    )
    return DegreeMessages(
        criticalSuccess = criticalSuccess,
        success = success,
        failure = failure,
        criticalFailure = criticalFailure,
    )
}