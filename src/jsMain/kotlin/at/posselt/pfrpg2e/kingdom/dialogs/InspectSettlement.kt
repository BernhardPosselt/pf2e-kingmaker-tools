package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.structures.calculateAvailableItems
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createTabs
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.unslugify
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.formatAsModifier
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.ui
import com.foundryvtt.core.ui.TextEditor
import js.core.Void
import js.objects.ReadonlyRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


@JsPlainObject
external interface InspectSettlementContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val blocksInput: FormElementContext
    val levelInput: FormElementContext
    val typeInput: FormElementContext
    val secondaryTerritoryInput: FormElementContext
    val manualSettlementLevelInput: FormElementContext
    val waterBordersInput: FormElementContext
    val level: Int
    val type: String
    val manualSettlementLevel: Boolean
    val lacksBridge: Boolean
    val isOvercrowded: Boolean
    val residentialLots: Int
    val consumption: Int
    val consumptionSurplus: Int
    val influence: Int
    val blocks: Int
    val maximumBlocks: String
    val maxItemBonus: Int
    val population: String
    val allowCapitalInvestment: Boolean
    val structures: ReadonlyRecord<String, Int>
    val bonuses: Array<String>
    val notes: Array<String>
    val availableItems: ReadonlyRecord<String, String>
    val currentTab: String
    val tabs: Array<NavEntryContext>
}

@JsPlainObject
external interface InspectSettlementData {
    val blocks: Int
    val level: Int
    val type: String
    val secondaryTerritory: Boolean
    val manualSettlementLevel: Boolean
    val waterBorders: Int
}

@JsExport
class InspectSettlementDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            int("blocks")
            int("level")
            enum<SettlementType>("type")
            boolean("secondaryTerritory")
            boolean("manualSettlementLevel")
            int("waterBorders")
        }
    }
}


enum class SettlementNav {
    STATUS,
    SHOPPING,
    STRUCTURES,
    NOTES,
    BONUSES;

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementNav>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}


class InspectSettlement(
    private val game: Game,
    title: String,
    private val autoCalculateSettlementLevel: Boolean,
    private val allStructuresStack: Boolean,
    private val allowCapitalInvestmentInCapitalWithoutBank: Boolean,
    settlement: RawSettlement,
    feats: List<ChosenFeat>,
    private val afterSubmit: suspend (settlement: RawSettlement) -> Unit
) : FormApp<InspectSettlementContext, InspectSettlementData>(
    title = title,
    template = "applications/kingdom/settlement.hbs",
    debug = true,
    classes = arrayOf("km-inspect-settlement"),
    dataModel = InspectSettlementDataModel::class.js,
    id = "kmInspectSettlement-${settlement.sceneId}",
    width = 500,
) {
    val magicItemLevelIncreases = feats.sumOf { it.feat.settlementMagicItemLevelIncrease ?: 0 }
    var currentNav = SettlementNav.STATUS
    var current = RawSettlement(
        sceneId = settlement.sceneId,
        lots = settlement.lots,
        level = settlement.level,
        type = settlement.type,
        secondaryTerritory = settlement.secondaryTerritory,
        manualSettlementLevel = settlement.manualSettlementLevel,
        waterBorders = settlement.waterBorders,
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "change-nav" -> {
                event.preventDefault()
                event.stopPropagation()
                target.dataset["link"]
                    ?.let { SettlementNav.fromString(it) }
                    ?.let { currentNav = it }
                render()
            }

            "km-save" -> buildPromise {
                if (isValid()) {
                    close().await()
                    afterSubmit(current)
                }
                undefined
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<InspectSettlementContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val parsed = game.scenes.get(current.sceneId)?.parseSettlement(
            rawSettlement = current,
            autoCalculateSettlementLevel = autoCalculateSettlementLevel,
            allStructuresStack = allStructuresStack,
            allowCapitalInvestmentInCapitalWithoutBank = allowCapitalInvestmentInCapitalWithoutBank,
        )
        checkNotNull(parsed) {
            val msg = "Settlement Scene deleted, closing Dialog"
            ui.notifications.error(msg)
            msg
        }
        val manualSettlementLevel = current.manualSettlementLevel == true
        val blocksInput = if (manualSettlementLevel) {
            NumberInput(
                name = "blocks",
                label = "Blocks",
                value = current.lots,
                hideLabel = true,
            )
        } else {
            HiddenInput(
                name = "blocks",
                value = current.lots.toString(),
                overrideType = OverrideType.NUMBER,
            )
        }
        val levelInput = if (manualSettlementLevel) {
            NumberInput(
                name = "level",
                label = "Level",
                value = current.level,
                hideLabel = true,
            )
        } else {
            HiddenInput(
                name = "level",
                value = current.level.toString(),
                overrideType = OverrideType.NUMBER,
            )
        }
        val typeInput = Select.fromEnum<SettlementType>(
            name = "type",
            label = "Level",
            value = SettlementType.fromString(current.type) ?: SettlementType.SETTLEMENT,
            hideLabel = true,
        )
        val secondaryTerritoryInput = CheckboxInput(
            name = "secondaryTerritory",
            label = "Secondary Territory",
            value = current.secondaryTerritory,
            hideLabel = true,
        )
        val manualSettlementLevelInput = CheckboxInput(
            name = "manualSettlementLevel",
            label = "Manual",
            value = manualSettlementLevel,
            hideLabel = true,
        )
        val waterBordersInput = NumberInput(
            name = "waterBorders",
            label = "Water Borders",
            value = current.waterBorders,
            hideLabel = true,
        )
        val settlementStructures = parsed.constructedStructures
            .groupBy { it.name }
            .map { (_, instances) ->
                async {
                    val instance = instances.first()
                    TextEditor.enrichHTML(buildUuid(instance.uuid, instance.name)).await() to instances.size
                }
            }
            .awaitAll()
            .toRecord()
        val bonuses = parsed.bonuses
            .mapNotNull { bonus ->
                val activity = bonus.activity
                val skill = bonus.skill
                val mod = bonus.value.formatAsModifier()
                if (activity != null && skill != null) {
                    "$mod to ${activity.unslugify()} using ${skill.label}"
                } else if (skill != null) {
                    "$mod to ${skill.label}"
                } else if (activity != null) {
                    "$mod to ${activity.unslugify()}"
                } else {
                    null
                }
            }
            .toTypedArray()
        val availableItems = calculateAvailableItems(
            settlementLevel = parsed.occupiedBlocks,
            preventItemLevelPenalty = parsed.preventItemLevelPenalty,
            magicalItemLevelIncrease = magicItemLevelIncreases,
            bonuses = parsed.availableItems,
        ).toEntries().map { (group, amount) ->
            group.label to if (amount >= 0) {
                "Level $amount"
            } else {
                "Not Available"
            }
        }.toRecord()
        val notes = parsed.notes.toTypedArray()
        InspectSettlementContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            manualSettlementLevel = manualSettlementLevel,
            blocksInput = blocksInput.toContext(),
            levelInput = levelInput.toContext(),
            typeInput = typeInput.toContext(),
            secondaryTerritoryInput = secondaryTerritoryInput.toContext(),
            manualSettlementLevelInput = manualSettlementLevelInput.toContext(),
            waterBordersInput = waterBordersInput.toContext(),
            blocks = parsed.occupiedBlocks,
            level = parsed.occupiedBlocks,
            type = parsed.type.label,
            lacksBridge = parsed.lacksBridge,
            isOvercrowded = parsed.isOvercrowded,
            residentialLots = parsed.residentialLots,
            consumption = parsed.consumption,
            consumptionSurplus = parsed.consumptionSurplus,
            influence = parsed.size.influence,
            maximumBlocks = parsed.size.maximumBlocks,
            maxItemBonus = parsed.size.maxItemBonus,
            population = parsed.size.population,
            allowCapitalInvestment = parsed.allowCapitalInvestment,
            structures = settlementStructures,
            bonuses = bonuses,
            notes = notes,
            availableItems = availableItems,
            currentTab = currentNav.value,
            tabs = createTabs<SettlementNav>("change-nav", currentNav)
                .filter {
                    if (it.link == SettlementNav.BONUSES.value) {
                        bonuses.isNotEmpty()
                    } else if (it.link == SettlementNav.STRUCTURES.value) {
                        parsed.constructedStructures.isNotEmpty()
                    } else if (it.link == SettlementNav.NOTES.value) {
                        parsed.notes.isNotEmpty()
                    } else {
                        true
                    }
                }
                .toTypedArray(),
        )
    }

    override fun onParsedSubmit(value: InspectSettlementData): Promise<Void> = buildPromise {
        current.lots = value.blocks
        current.level = value.level
        current.type = value.type
        current.secondaryTerritory = value.secondaryTerritory
        current.manualSettlementLevel = value.manualSettlementLevel
        current.waterBorders = value.waterBorders
        undefined
    }

}