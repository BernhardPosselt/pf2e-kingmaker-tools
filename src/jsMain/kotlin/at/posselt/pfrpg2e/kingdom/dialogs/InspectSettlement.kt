package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.kingdom.settlements.SettlementType
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.calculateAvailableItems
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createTabs
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.structures.isStructure
import at.posselt.pfrpg2e.kingdom.structures.parseSettlement
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.formatAsModifier
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.applications.ux.TextEditor.TextEditor
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.documents.onCreateTile
import com.foundryvtt.core.documents.onCreateToken
import com.foundryvtt.core.documents.onDeleteScene
import com.foundryvtt.core.documents.onDeleteTile
import com.foundryvtt.core.documents.onDeleteToken
import com.foundryvtt.core.documents.onUpdateTile
import com.foundryvtt.core.documents.onUpdateToken
import com.foundryvtt.core.helpers.onApplyTokenStatusEffect
import com.foundryvtt.core.ui
import js.core.Void
import js.objects.ReadonlyRecord
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface LabelValueContext {
    val label: String
    val value: Int
}


@Suppress("unused")
@JsPlainObject
external interface InspectSettlementContext : ValidatedHandlebarsContext {
    val blocksInput: FormElementContext
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
    val structures: Array<LabelValueContext>
    val bonuses: Array<String>
    val notes: Array<String>
    val availableItems: ReadonlyRecord<String, String>
    val currentTab: String
    val tabs: Array<NavEntryContext>
    val storage: Array<LabelValueContext>
    val settlementActions: Int
}

@JsPlainObject
external interface InspectSettlementData {
    val blocks: Int
    val type: String
    val secondaryTerritory: Boolean
    val manualSettlementLevel: Boolean
    val waterBorders: Int
}

@JsExport
class InspectSettlementDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            int("blocks")
            enum<SettlementType>("type")
            boolean("secondaryTerritory")
            boolean("manualSettlementLevel")
            int("waterBorders")
        }
    }
}

@Suppress("unused")
enum class SettlementNav : Translatable, ValueEnum {
    STATUS,
    SHOPPING,
    STRUCTURES,
    STORAGE,
    NOTES,
    BONUSES;

    companion object {
        fun fromString(value: String) = fromCamelCase<SettlementNav>(value)
    }

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "settlementNav.$value"
}


class InspectSettlement(
    private val game: Game,
    private val kingdom: KingdomData,
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
    classes = setOf("km-inspect-settlement"),
    dataModel = InspectSettlementDataModel::class.js,
    id = "kmInspectSettlement-${settlement.sceneId}",
    width = 700,
) {
    init {
        appHook.onDeleteScene { _, _, _ -> render() }
        appHook.onCreateTile { _, _, _ -> render() }
        appHook.onUpdateTile { _, _, _, _ -> render() }
        appHook.onDeleteTile { _, _, _ -> render() }
        appHook.onDeleteToken { token, _, _ ->
            if (token.isStructure()) {
                render()
            }
        }
        appHook.onUpdateToken { token, _, _, _ ->
            if (token.isStructure()) {
                render()
            }
        }
        appHook.onCreateToken { token, _, _ ->
            if (token.isStructure()) {
                render()
            }
        }
        appHook.onApplyTokenStatusEffect { _, _, _ -> render() }
    }

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
            val msg = t("kingdom.settlementSceneDeleted")
            ui.notifications.error(msg)
            msg
        }
        val manualSettlementLevel = current.manualSettlementLevel == true
        val blocksInput = if (manualSettlementLevel) {
            NumberInput(
                name = "blocks",
                label = t("kingdom.blocks"),
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
        val typeInput = Select.fromEnum<SettlementType>(
            name = "type",
            value = SettlementType.fromString(current.type) ?: SettlementType.SETTLEMENT,
            hideLabel = true,
        )
        val secondaryTerritoryInput = CheckboxInput(
            name = "secondaryTerritory",
            label = t("kingdom.secondaryTerritory"),
            value = current.secondaryTerritory,
            hideLabel = true,
        )
        val manualSettlementLevelInput = CheckboxInput(
            name = "manualSettlementLevel",
            label = t("kingdom.manualManagement"),
            value = manualSettlementLevel,
            hideLabel = true,
        )
        val waterBordersInput = NumberInput(
            name = "waterBorders",
            label = t("kingdom.waterBorders"),
            value = current.waterBorders,
            hideLabel = true,
        )
        val settlementStructures = parsed.constructedStructures
            .groupBy { it.id }
            .values
            .sortedBy { it.first().name }
            .map { instances ->
                async {
                    val instance = instances.first()
                    TextEditor.enrichHTML(buildUuid(instance.uuid, instance.name)).await() to instances.size
                }
            }
            .awaitAll()
            .map { LabelValueContext(label = it.first, value = it.second) }
            .toTypedArray()
        val activitiesById = kingdom.getAllActivities().associateBy { it.id }
        val bonuses = parsed.highestUniqueBonuses
            .mapNotNull { bonus ->
                val activity = bonus.activity
                val skill = bonus.skill
                val mod = bonus.value.formatAsModifier()
                if (activity != null && skill != null) {
                    t(
                        "kingdom.bonusToActivityUsingSkill", recordOf(
                            "modifier" to mod,
                            "activity" to (activitiesById[activity]?.title ?: ""),
                            "skill" to t(skill)
                        )
                    )
                } else if (skill != null) {
                    t(
                        "kingdom.bonusTo", recordOf(
                            "modifier" to mod,
                            "selector" to t(skill),
                        )
                    )
                } else if (activity != null) {
                    t(
                        "kingdom.bonusTo", recordOf(
                            "modifier" to mod,
                            "selector" to (activitiesById[activity]?.title ?: ""),
                        )
                    )
                } else {
                    null
                }
            }
            .toTypedArray()
        val parsedStorage = parsed.constructedStructures.map { it.storage }
            .fold(CommodityStorage()) { prev, curr -> prev + curr }
        val storage = listOf(
            t("kingdom.food") to parsedStorage.food,
            t("kingdom.lumber") to parsedStorage.lumber,
            t("kingdom.luxuries") to parsedStorage.luxuries,
            t("kingdom.ore") to parsedStorage.ore,
            t("kingdom.stone") to parsedStorage.stone,
        )
            .filter { it.second > 0 }
            .map { LabelValueContext(label = it.first, value = it.second) }
            .toTypedArray()
        val availableItems = calculateAvailableItems(
            settlementLevel = parsed.occupiedBlocks,
            preventItemLevelPenalty = parsed.preventItemLevelPenalty,
            magicalItemLevelIncrease = magicItemLevelIncreases,
            bonuses = parsed.availableItems,
        ).toEntries().map { (group, amount) ->
            t(group) to if (amount >= 0) {
                t("kingdom.availableItemLevels", recordOf("itemLevel" to amount))
            } else {
                t("kingdom.notAvailable")
            }
        }.toRecord()
        val notes = parsed.notes.toTypedArray()
        InspectSettlementContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            manualSettlementLevel = manualSettlementLevel,
            blocksInput = blocksInput.toContext(),
            typeInput = typeInput.toContext(),
            secondaryTerritoryInput = secondaryTerritoryInput.toContext(),
            manualSettlementLevelInput = manualSettlementLevelInput.toContext(),
            waterBordersInput = waterBordersInput.toContext(),
            blocks = parsed.occupiedBlocks,
            level = parsed.level,
            type = t(parsed.type),
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
            settlementActions = parsed.settlementActions,
            availableItems = availableItems,
            currentTab = currentNav.value,
            storage = storage,
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
        current.type = value.type
        current.secondaryTerritory = value.secondaryTerritory
        current.manualSettlementLevel = value.manualSettlementLevel
        current.waterBorders = value.waterBorders
        undefined
    }

}