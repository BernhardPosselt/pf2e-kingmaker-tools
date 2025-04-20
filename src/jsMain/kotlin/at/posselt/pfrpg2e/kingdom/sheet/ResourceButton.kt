package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.events.KingdomEventTrait
import at.posselt.pfrpg2e.data.kingdom.ResourceDieSize
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawKingdomEvent
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.dialogs.createOngoingEvent
import at.posselt.pfrpg2e.kingdom.dialogs.removeEvent
import at.posselt.pfrpg2e.kingdom.dialogs.requestAmount
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getEvents
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.getOngoingEvents
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.resources.calculateStorage
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.lowercaseFirst
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.uppercaseFirst
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.roll
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.Game
import com.foundryvtt.core.utils.deepClone
import js.objects.recordOf
import kotlinx.browser.document
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.js.button
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import kotlin.math.abs

enum class ResourceMode : Translatable, ValueEnum {
    GAIN,
    LOSE;

    companion object {
        fun fromString(value: String) = fromCamelCase<ResourceMode>(value)
    }

    override val i18nKey = "resourceButton.mode.$value"

    override val value: String
        get() = toCamelCase()
}

enum class Resource : Translatable, ValueEnum {
    RESOURCE_DICE,
    CRIME,
    EVENT,
    DECAY,
    CORRUPTION,
    CONSUMPTION,
    STRIFE,
    RESOURCE_POINTS,
    FOOD,
    LUXURIES,
    UNREST,
    ORE,
    LUMBER,
    FAME,
    STONE,
    XP,
    SUPERNATURAL_SOLUTION,
    CREATIVE_SOLUTION,
    ROLLED_RESOURCE_DICE;

    companion object {
        fun fromString(value: String) = entries.find { it.value == value }
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey = "resourceButton.resource.$value"

    val i18nKeyExpression = "resourceButton.resourceExpression.$value"
}

private val fromStringRegex = Regex(
    "@(?<mode>gain|lose)" +
            "(?<multiple>Multiple)?" +
            "(?<value>[0-9rd+]+)" +
            "(?<resource>${
                Resource.entries
                    .joinToString("|") { if (it == Resource.EVENT) "[a-zA-Z]+Event" else it.value.uppercaseFirst() }
            })" +
            "(?<turn>NextTurn)?"
)

fun insertButtons(source: String, events: Array<RawKingdomEvent>): String {
    return source.replace(fromStringRegex) {
        ResourceButton.fromMatch(it).toHtml(events)
    }
}

private fun parseEventId(value: String) = value
    .removeSuffix("Event")
    .split("(?=\\p{Upper})".toRegex())
    .filter { it.isNotBlank() }
    .joinToString("-") { it.lowercase() }

data class ResourceButton(
    val turn: Turn = Turn.NOW,
    val value: String,
    val mode: ResourceMode = ResourceMode.GAIN,
    val resource: Resource,
    val multiple: Boolean = false,
) {
    companion object {
        /**
         * Create a button from a string like
         * @gain1d4+3ResourcePointsNextTurn
         * @loseMultiple1rdFame
         */
        fun fromString(value: String): ResourceButton {
            val match = fromStringRegex.find(value)
            checkNotNull(match) {
                "match is null $value"
            }
            return fromMatch(match)
        }

        fun fromMatch(match: MatchResult): ResourceButton {
            val turn = if (match.groups["turn"] == null) Turn.NOW else Turn.NEXT
            val mode = if (match.groups["mode"]?.value == "gain") ResourceMode.GAIN else ResourceMode.LOSE
            val multiple = match.groups["multiple"] != null
            val value = match.groups["value"]?.value
            val resourceValue = match.groups["resource"]?.value
            val isEvent = resourceValue?.endsWith("Event") == true
            if (isEvent) console.log(
                "------------------------------------------___" + resourceValue,
                parseEventId(resourceValue!!)
            )
            val resource = if (isEvent) {
                Resource.EVENT
            } else {
                resourceValue?.let { Resource.fromString(it.lowercaseFirst()) }
            }
            checkNotNull(resource) {
                "Resource must not be null"
            }
            checkNotNull(value) {
                "Value must not be null"
            }
            return ResourceButton(
                turn = turn,
                value = if (isEvent) {
                    parseEventId(resourceValue)
                } else {
                    value
                },
                mode = mode,
                resource = resource,
                multiple = multiple,
            )
        }

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
            val multiple = target.dataset["multiple"] == "true"
            return ResourceButton(
                turn = turn,
                value = value,
                mode = mode,
                resource = resource,
                multiple = multiple,
            )
        }
    }

    fun toHtml(events: Array<RawKingdomEvent>): String {
        val isEvent = resource == Resource.EVENT
        val isRd = value.contains("rd")
        val isDiceExpression = value.contains("d")
        val resourceKey = if (isEvent) {
            "resourceButton.resource.${Resource.EVENT.value}"
        } else if (isRd) {
            "resourceButton.resourceDice.${resource.value}"
        } else if (isDiceExpression) {
            resource.i18nKeyExpression
        } else {
            resource.i18nKey
        }
        val label = t(
            resourceKey, recordOf(
                // TODO: this does not support RD expressions like 1d4rd
                (if (isDiceExpression && !isRd) "expression" else "count") to if (isRd) value.replace(
                    "rd",
                    ""
                ) else value,
                "mode" to mode.value,
                "multiple" to multiple.toString(),
                "turn" to turn.value,
                "location" to "button",
                "eventName" to if (isEvent) {
                    events.find { it.id == value }
                        ?.name
                        ?: throw IllegalStateException("Event with id $value does not exist, check your event buttons")
                } else {
                    null
                }
            )
        ).trim()
        // rename needed since value is a property on an HTML element
        val value2 = value
        val button = document.create.button {
            type = ButtonType.button
            classes = setOf("km-gain-lose")
            attributes["data-type"] = resource.value
            attributes["data-mode"] = mode.value
            attributes["data-turn"] = turn.value
            attributes["data-multiple"] = multiple.toString()
            attributes["data-value"] = value2
            +label
        }.outerHTML
        return button
    }

    private suspend fun evaluateValueExpression(
        value: String,
        resourceDieSize: ResourceDieSize,
    ): Int {
        return if ("d" in value) {
            roll(value.replace("rd", resourceDieSize.value), flavor = t("resourceButton.rolling"))
        } else {
            value.toInt()
        }
    }

    suspend fun evaluate(
        game: Game,
        kingdom: KingdomData,
        dice: ResourceDieSize,
        maximumFame: Int,
        storage: CommodityStorage,
        resourceDieSize: ResourceDieSize,
        activityId: String?,
        chosenFeats: List<ChosenFeat>,
    ) {
        val isEvent = resource == Resource.EVENT
        val event = if (isEvent) {
            kingdom.getEvents()
                .find { it.id == this.value }
        } else {
            null
        }
        val factor = if (multiple) requestAmount() else 1
        val sign = if (mode == ResourceMode.GAIN) 1 else -1
        val initialValue = if (isEvent) {
            1
        } else if (resource == Resource.ROLLED_RESOURCE_DICE) {
            val diceNum = evaluateValueExpression(value, resourceDieSize)
            roll(dice.formula(diceNum))
        } else {
            evaluateValueExpression(value, resourceDieSize)
        } * factor * sign
        val value = if (activityId != null && mode == ResourceMode.LOSE && resource == Resource.UNREST) {
            val decreases = chosenFeats.mapNotNull { it.feat.increaseActivityUnrestReductionBy }
                .filter { kingdom.unrest >= it.minimumCurrentUnrest }
                .sumOf { it.value }
            initialValue - decreases
        } else {
            initialValue
        }
        val resourceKey = if (resource == Resource.ROLLED_RESOURCE_DICE) {
            Resource.RESOURCE_POINTS.i18nKey
        } else {
            resource.i18nKey
        }
        val message = t(
            resourceKey, recordOf(
                "count" to abs(value),
                "mode" to mode.value,
                "multiple" to multiple.toString(),
                "turn" to turn.value,
                "location" to "chat",
                "eventName" to event?.name,
            )
        ).trim()
        postChatMessage(message, isHtml = true)
        val setter = when (resource) {
            Resource.CONSUMPTION -> when (turn) {
                Turn.NOW -> kingdom.consumption::now
                Turn.NEXT -> kingdom.consumption::next
            }

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
            Resource.EVENT -> null
        }
        val updatedValue = (setter?.get() ?: 0) + value
        when (resource) {
            Resource.FAME -> when (turn) {
                Turn.NOW -> setter?.set(updatedValue.coerceIn(0, maximumFame))
                Turn.NEXT -> setter?.set(updatedValue)
            }
            // values gated by capacity limit in the now column
            in listOf(
                Resource.FOOD,
                Resource.LUXURIES,
                Resource.ORE,
                Resource.LUMBER,
                Resource.STONE
            ) if turn == Turn.NEXT -> setter?.set(updatedValue)

            Resource.FOOD -> setter?.set(storage.limitFood(updatedValue))
            Resource.LUXURIES -> setter?.set(storage.limitLuxuries(updatedValue))
            Resource.ORE -> setter?.set(storage.limitOre(updatedValue))
            Resource.LUMBER -> setter?.set(storage.limitLumber(updatedValue))
            Resource.STONE -> setter?.set(storage.limitStone(updatedValue))
            // values that only can go below 0 in the next turn column
            Resource.RESOURCE_DICE,
            Resource.RESOURCE_POINTS,
            Resource.ROLLED_RESOURCE_DICE -> when (turn) {
                Turn.NOW -> setter?.set(updatedValue.coerceIn(0, Int.MAX_VALUE))
                Turn.NEXT -> setter?.set(updatedValue)
            }
            // values that should never go below 0
            Resource.CRIME,
            Resource.DECAY,
            Resource.CORRUPTION,
            Resource.STRIFE,
            Resource.UNREST,
            Resource.SUPERNATURAL_SOLUTION,
            Resource.CREATIVE_SOLUTION,
            Resource.XP -> setter?.set(updatedValue.coerceIn(0, Int.MAX_VALUE))
            // values that can be both negative in the now and next column
            Resource.CONSUMPTION -> setter?.set(updatedValue)
            // special handling based off feats
            Resource.EVENT -> {
                checkNotNull(event) {
                    "No event with id ${this.value} found"
                }
                val settlements = kingdom.getAllSettlements(game).allSettlements
                when(mode) {
                    ResourceMode.GAIN -> {
                        kingdom.ongoingEvents = kingdom.ongoingEvents + createOngoingEvent(
                            id = event.id,
                            isSettlementEvent = event.traits.contains(KingdomEventTrait.SETTLEMENT.value),
                            settlements = settlements,
                        )
                    }
                    ResourceMode.LOSE -> {
                        val events = kingdom.getOngoingEvents()
                            .filter { it.event.id == event.id }
                        if (events.isNotEmpty()) {
                            val indexToRemove = if (events.size > 1) {
                                removeEvent(events, settlements)
                            } else {
                                events.first().eventIndex
                            }
                            kingdom.ongoingEvents = kingdom.ongoingEvents
                                .filterIndexed { index, _ -> index != indexToRemove }
                                .toTypedArray()
                        }
                    }
                }
            }
        }
    }
}

suspend fun executeResourceButton(
    game: Game,
    actor: KingdomActor,
    kingdom: KingdomData,
    elem: HTMLElement,
    activityId: String?,
) {
    val previous = deepClone(kingdom)
    val button = ResourceButton.fromHtml(elem)
    val realm = game.getRealmData(actor, kingdom)
    val settlements = kingdom.getAllSettlements(game)
    val chosenFeatures = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
    val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
    val storage = calculateStorage(realm = realm, settlements = settlements.allSettlements)
    button.evaluate(
        kingdom = kingdom,
        dice = realm.sizeInfo.resourceDieSize,
        maximumFame = kingdom.settings.maximumFamePoints,
        storage = storage,
        resourceDieSize = realm.sizeInfo.resourceDieSize,
        activityId = activityId,
        chosenFeats = chosenFeats,
        game = game,
    )
    beforeKingdomUpdate(previous, kingdom)
    actor.setKingdom(kingdom)
}