package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.kingdom.ResourceDieSize
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.dialogs.requestAmount
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
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
            "(?<resource>${Resource.entries.joinToString("|") { it.value.uppercaseFirst() }})" +
            "(?<turn>NextTurn)?"
)

fun insertButtons(source: String): String {
    return source.replace(fromStringRegex) {
        ResourceButton.fromMatch(it).toHtml()
    }
}

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
            val resource = match.groups["resource"]
                ?.value?.let { Resource.fromString(it.lowercaseFirst()) }
            checkNotNull(resource) {
                "Resource must not be null"
            }
            checkNotNull(value) {
                "Value must not be null"
            }
            return ResourceButton(
                turn = turn,
                value = value,
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

    fun toHtml(): String {
        val isRd = value.contains("rd")
        val isDiceExpression = value.contains("d")
        val resourceLabel = if (isRd) {
            t("resourceButton.resourceDice.${resource.value}", recordOf("count" to value.replace("rd", "")))
        } else if(isDiceExpression) {
            t(resource.i18nKeyExpression, recordOf("expression" to value))
        } else {
            t(resource.i18nKey, recordOf("count" to value))
        }
        val turnLabel = if (turn == Turn.NEXT) " ${t(turn)}" else ""
        val label = "${t(mode)} ${resourceLabel}$turnLabel"
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
        kingdom: KingdomData,
        dice: ResourceDieSize,
        maximumFame: Int,
        storage: CommodityStorage,
        resourceDieSize: ResourceDieSize,
        activityId: String?,
        chosenFeats: List<ChosenFeat>,
    ) {
        val factor = if (multiple) requestAmount() else 1
        val sign = if (mode == ResourceMode.GAIN) 1 else -1
        val initialValue = if (resource == Resource.ROLLED_RESOURCE_DICE) {
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
        val turnLabel = if (turn == Turn.NEXT) " ${t(turn)}" else ""
        val mode = if (mode == ResourceMode.GAIN) "resourceButton.mode.gaining" else "resourceButton.mode.losing"
        val resourceKey =
            if (resource == Resource.ROLLED_RESOURCE_DICE) Resource.RESOURCE_POINTS.i18nKey else resource.i18nKey
        val message = "${t(mode)} ${t(resourceKey, recordOf("count" to abs(value)))}$turnLabel"
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
        }
        val updatedValue = setter.get() + value
        when (resource) {
            Resource.FAME -> when (turn) {
                Turn.NOW -> setter.set(updatedValue.coerceIn(0, maximumFame))
                Turn.NEXT -> setter.set(updatedValue)
            }
            // values gated by capacity limit in the now column
            in listOf(
                Resource.FOOD,
                Resource.LUXURIES,
                Resource.ORE,
                Resource.LUMBER,
                Resource.STONE
            ) if turn == Turn.NEXT -> setter.set(updatedValue)

            Resource.FOOD -> setter.set(storage.limitFood(updatedValue))
            Resource.LUXURIES -> setter.set(storage.limitLuxuries(updatedValue))
            Resource.ORE -> setter.set(storage.limitOre(updatedValue))
            Resource.LUMBER -> setter.set(storage.limitLumber(updatedValue))
            Resource.STONE -> setter.set(storage.limitStone(updatedValue))
            // values that only can go below 0 in the next turn column
            Resource.RESOURCE_DICE,
            Resource.RESOURCE_POINTS,
            Resource.ROLLED_RESOURCE_DICE -> when (turn) {
                Turn.NOW -> setter.set(updatedValue.coerceIn(0, Int.MAX_VALUE))
                Turn.NEXT -> setter.set(updatedValue)
            }
            // values that should never go below 0
            Resource.CRIME,
            Resource.DECAY,
            Resource.CORRUPTION,
            Resource.STRIFE,
            Resource.UNREST,
            Resource.SUPERNATURAL_SOLUTION,
            Resource.CREATIVE_SOLUTION,
            Resource.XP -> setter.set(updatedValue.coerceIn(0, Int.MAX_VALUE))
            // values that can be both negative in the now and next column
            Resource.CONSUMPTION -> setter.set(updatedValue)
            // special handling based off feats

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
    )
    beforeKingdomUpdate(previous, kingdom)
    actor.setKingdom(kingdom)
}