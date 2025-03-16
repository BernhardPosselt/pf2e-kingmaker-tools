package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.ResourceDieSize
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.dialogs.requestAmount
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.roll
import com.foundryvtt.core.Game
import com.foundryvtt.core.utils.deepClone
import kotlinx.browser.document
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.js.button
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import kotlin.math.abs

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

    companion object {
        fun fromString(value: String) = entries.find { it.value == value }
    }

    val label: String
        get() = value.split("-").joinToString(" ") { it.toLabel() }
}

data class ResourceButton(
    val turn: Turn = Turn.NOW,
    val value: String,
    val mode: ResourceMode = ResourceMode.GAIN,
    val resource: Resource,
    val hints: String? = null,
    val multiple: Boolean = false,
) {
    companion object {
        fun fromHtml(target: HTMLElement): ResourceButton {
            val turn = target.dataset["turn"]
                ?.let { Turn.fromString(it) }
                ?: Turn.NOW
            val mode = target.dataset["mode"]
                ?.let { ResourceMode.fromString(it) }
                ?: ResourceMode.GAIN
            val resource = target.dataset["type"]
                ?.let { Resource.fromString(it) }
                ?: Resource.RESOURCE_DICE
            val value = target.dataset["value"] ?: ""
            val hints = target.dataset["hints"]
            val multiple = target.dataset["multiple"] == "true"
            return ResourceButton(
                turn = turn,
                value = value,
                mode = mode,
                resource = resource,
                hints = hints,
                multiple = multiple,
            )
        }
    }

    fun toHtml(): String {
        val turnLabel = if (turn == Turn.NEXT) " Next Turn" else ""
        val hints = hints?.let { " ($it)" } ?: ""
        val label = "${mode.label} $value ${resource.label}$turnLabel$hints"
        val value2 = value
        return document.create.button {
            type = ButtonType.button
            classes = setOf("km-gain-lose")
            attributes["data-type"] = resource.value
            attributes["data-mode"] = mode.value
            attributes["data-turn"] = turn.value
            attributes["data-multiple"] = multiple.toString()
            attributes["data-value"] = value2
            +label
        }.outerHTML
    }

    private suspend fun evaluateValueExpression(value: String): Int {
        return if ("d" in value) {
            roll(value, flavor = "Rolling Resources")
        } else {
            value.toInt()
        }
    }

    suspend fun evaluate(
        kingdom: KingdomData,
        dice: ResourceDieSize,
        maximumFame: Int,
        storage: CommodityStorage,
    ) {
        val factor = if (multiple) requestAmount() else 1
        val sign = if (mode == ResourceMode.GAIN) 1 else -1
        val value = if (resource == Resource.ROLLED_RESOURCE_DICE) {
            val diceNum = evaluateValueExpression(value)
            roll(dice.formula(diceNum))
        } else {
            evaluateValueExpression(value)
        } * factor * sign
        val turnLabel = if (turn == Turn.NEXT) " Next Turn" else ""
        val hints = hints?.let { " ($it)" } ?: ""
        val mode = if (mode == ResourceMode.GAIN) "Gaining" else "Losing"
        val message = "$mode ${abs(value)} ${resource.label}$turnLabel$hints"
        postChatMessage(message)
        val setter = when (resource) {
            Resource.RESOURCE_DICE -> when (turn) {
                Turn.NOW -> kingdom.resourceDice::now
                Turn.NEXT -> kingdom.resourceDice::next
            }

            Resource.RESOURCE_POINTS, Resource.ROLLED_RESOURCE_DICE -> when (turn) {
                Turn.NOW -> kingdom.resourcePoints::now
                Turn.NEXT -> kingdom.resourcePoints::next
            }

            Resource.XP -> kingdom::xp
            Resource.UNREST -> kingdom::unrest
            Resource.CRIME -> kingdom.ruin.crime::value
            Resource.DECAY -> kingdom.ruin.decay::value
            Resource.CORRUPTION -> kingdom.ruin.corruption::value
            Resource.STRIFE -> kingdom.ruin.strife::value
            Resource.FOOD -> when (turn) {
                Turn.NOW -> kingdom.commodities.now::food
                Turn.NEXT -> kingdom.commodities.next::food
            }

            Resource.LUXURIES -> when (turn) {
                Turn.NOW -> kingdom.commodities.now::luxuries
                Turn.NEXT -> kingdom.commodities.next::luxuries
            }

            Resource.ORE -> when (turn) {
                Turn.NOW -> kingdom.commodities.now::ore
                Turn.NEXT -> kingdom.commodities.next::ore
            }

            Resource.LUMBER -> when (turn) {
                Turn.NOW -> kingdom.commodities.now::lumber
                Turn.NEXT -> kingdom.commodities.next::lumber
            }

            Resource.STONE -> when (turn) {
                Turn.NOW -> kingdom.commodities.now::stone
                Turn.NEXT -> kingdom.commodities.next::stone
            }

            Resource.FAME -> when (turn) {
                Turn.NOW -> kingdom.fame::now
                Turn.NEXT -> kingdom.fame::next
            }

            Resource.SUPERNATURAL_SOLUTION -> kingdom::supernaturalSolutions
            Resource.CREATIVE_SOLUTION -> kingdom::creativeSolutions
        }
        val updatedValue = setter.get() + value
        when (resource) {
            Resource.FAME -> when(turn) {
                Turn.NOW -> setter.set(updatedValue.coerceIn(0, maximumFame))
                Turn.NEXT -> setter.set(updatedValue)
            }
            // values gated by capacity limit in the now column
            in listOf(Resource.FOOD,
            Resource.LUXURIES,
            Resource.ORE,
            Resource.LUMBER,
            Resource.STONE) if turn == Turn.NEXT -> setter.set(updatedValue)
            Resource.FOOD -> setter.set(storage.limitFood(updatedValue))
            Resource.LUXURIES -> setter.set(storage.limitLuxuries(updatedValue))
            Resource.ORE -> setter.set(storage.limitOre(updatedValue))
            Resource.LUMBER -> setter.set(storage.limitLumber(updatedValue))
            Resource.STONE -> setter.set(storage.limitStone(updatedValue))
            // values that only can go below 0 in the next turn column
            Resource.RESOURCE_DICE,
            Resource.RESOURCE_POINTS,
            Resource.ROLLED_RESOURCE_DICE,
            Resource.SUPERNATURAL_SOLUTION,
            Resource.CREATIVE_SOLUTION -> when (turn) {
                Turn.NOW -> setter.set(updatedValue.coerceIn(0, Int.MAX_VALUE))
                Turn.NEXT -> setter.set(updatedValue)
            }
            // values that should never go below 0
            Resource.CRIME,
            Resource.DECAY,
            Resource.CORRUPTION,
            Resource.STRIFE,
            Resource.UNREST,
            Resource.XP -> setter.set(updatedValue.coerceIn(0, Int.MAX_VALUE))
        }
    }
}

suspend fun executeResourceButton(
    game: Game,
    actor: KingdomActor,
    kingdom: KingdomData,
    elem: HTMLElement
) {
    val previous = deepClone(kingdom)
    val button = ResourceButton.fromHtml(elem)
    val realm = game.getRealmData(actor, kingdom)
    val settlements = kingdom.getAllSettlements(game)
    val storage = calculateStorage(realm = realm, settlements = settlements.allSettlements)
    button.evaluate(
        kingdom = kingdom,
        dice = realm.sizeInfo.resourceDieSize,
        maximumFame = kingdom.settings.maximumFamePoints,
        storage = storage,
    )
    beforeKingdomUpdate(previous, kingdom)
    actor.setKingdom(kingdom)
}