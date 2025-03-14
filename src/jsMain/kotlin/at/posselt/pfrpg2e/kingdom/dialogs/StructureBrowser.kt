package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.RangeInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createNavEntries
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.unslugify
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

enum class StructureBrowserNav {
    BUILDABLE,
    REPAIRABLE,
    UPGRADEABLE,
    FREE;

    companion object {
        fun fromString(value: String) = fromCamelCase<StructureBrowserNav>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

@JsPlainObject
external interface StructureBrowserContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val activeSettlement: FormElementContext
    val maxLevel: FormElementContext
    val minLots: FormElementContext
    val maxLots: FormElementContext
    val search: FormElementContext
    val mainFilters: Array<FormElementContext>
    val activityFilters: Array<FormElementContext>
    val nav: Array<NavEntryContext>
    val currentNav: String
}

@JsPlainObject
external interface FilterContext {
    val id: String
    val enabled: Boolean
}

@JsPlainObject
external interface StructureBrowserData {
    val activeSettlement: String?
    val search: String?
    val minLots: Int
    val maxLots: Int
    val maxLevel: Int
    val mainFilters: Array<FilterContext>
    val activityFilters: Array<FilterContext>
}

@JsExport
class StructureBrowserDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            string("activeSettlement", nullable = true)
            string("search", nullable = true)
            int("minLots")
            int("maxLots")
            int("maxLevel")
            array("activityFilters") {
                schema {
                    boolean("enabled")
                    string("id")
                }
            }
            array("mainFilters") {
                schema {
                    boolean("enabled")
                    enum<MainFilters>("id")
                }
            }
        }
    }
}

enum class MainFilters {
    IGNORE_PROFICIENCY,
    IGNORE_BUILDING_COST,
    REDUCES_RUIN,
    REDUCES_UNREST,
    REDUCES_CONSUMPTION,
    HOUSING,
    DOWNTIME,
    SHOPPING,
    AFFECTS_EVENTS,
    INCREASES_CAPACITY,
    CHEAPER_WHEN_UPGRADED,
    UPGRADEABLE;


    companion object {
        fun fromString(value: String) = fromCamelCase<MainFilters>(value)
    }

    val value: String
        get() = toCamelCase()

    val label: String
        get() = toLabel()
}

class StructureBrowser(
    private val game: Game,
    private val actor: KingdomActor,
    private val kingdom: KingdomData,
    private val structures: List<Structure>,
) : FormApp<StructureBrowserContext, StructureBrowserData>(
    title = "Structure Browser",
    template = "applications/kingdom/structure-browser.hbs",
    debug = true,
    dataModel = StructureBrowserDataModel::class.js,
    id = "kmStructureBrowser-${actor.uuid}"
) {
    var currentNav: StructureBrowserNav = StructureBrowserNav.BUILDABLE
    var minLots = 0
    var maxLots = 4
    var maxLevel = kingdom.level
    var search: String? = null
    var mainFilters = setOf<MainFilters>()
    var activityFilters = setOf<String>()

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "clear-filters" -> {
                minLots = 0
                maxLots = 4
                maxLevel = kingdom.level
            }

            "build-structure" -> {

            }

            "upgrade-structure" -> {

            }

            "repair-structure" -> {

            }

            "change-nav" -> {
                currentNav = target.dataset["link"]
                    ?.let { StructureBrowserNav.fromString(it) }
                    ?: StructureBrowserNav.BUILDABLE
                render()
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<StructureBrowserContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val settlements = kingdom.getAllSettlements(game)
        val activeSettlement = Select(
            label = "Active Settlement",
            value = kingdom.activeSettlement,
            options = settlements.allSettlements.map { SelectOption(label = it.name, value = it.id) },
            required = false,
            name = "activeSettlement",
        )
        val maxLevelInput = RangeInput(
            value = maxLevel,
            name = "maxLevel",
            label = "Max Level",
            min = 1,
            max = 20,
        )
        val maxLotsInput = RangeInput(
            value = maxLots,
            name = "maxLots",
            label = "Max Lots",
            min = 0,
            max = 4,
        )
        val minLotsInput = RangeInput(
            value = minLots,
            name = "minLots",
            label = "Min Lots",
            min = 0,
            max = 4,
        )
        val searchInput = TextInput(
            name = "search",
            value = search ?: "",
            label = "Name",
            required = false,
        )
        val buildable = 0
        val upgradeable = 0
        val free = 0
        val mainFilters = MainFilters.entries.flatMapIndexed { index, it ->
            listOf(
                HiddenInput(
                    name = "mainFilters.$index.id",
                    value = it.value,
                ).toContext(),
                CheckboxInput(
                    name = "mainFilters.$index.checked",
                    label = it.label,
                    value = it in mainFilters,
                ).toContext()
            )
        }.toTypedArray()
        val activityFilters = structures
            .flatMap { it.bonuses.mapNotNull { b -> b.activity } }
            .flatMapIndexed { index, it ->
                listOf(
                    HiddenInput(
                        name = "activityFilters.$index.id",
                        value = it,
                    ).toContext(),
                    CheckboxInput(
                        name = "activityFilters.$index.checked",
                        label = it.unslugify(),
                        value = it in activityFilters,
                    ).toContext()
                )
            }
            .sortedBy { it.label }
            .toTypedArray()
        val nav = createNavEntries<StructureBrowserNav>(currentNav)
            .map {
                when (it.link) {
                    StructureBrowserNav.BUILDABLE.value -> it.copy(label = "${it.label} ($buildable)")
                    StructureBrowserNav.UPGRADEABLE.value -> it.copy(label = "${it.label} ($upgradeable)")
                    StructureBrowserNav.FREE.value -> it.copy(label = "${it.label} ($free)")
                    else -> it
                }
            }
            .toTypedArray()
        StructureBrowserContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            activeSettlement = activeSettlement.toContext(),
            currentNav = currentNav.value,
            maxLots = maxLotsInput.toContext(),
            minLots = minLotsInput.toContext(),
            maxLevel = maxLevelInput.toContext(),
            search = searchInput.toContext(),
            activityFilters = activityFilters,
            mainFilters = mainFilters,
            nav = nav,
        )
    }

    override fun onParsedSubmit(value: StructureBrowserData): Promise<Void> = buildPromise {
        kingdom.activeSettlement = value.activeSettlement
        maxLots = value.maxLots
        minLots = value.minLots
        maxLevel = value.maxLevel
        search = value.search
        mainFilters = value.activityFilters
            .filter { it.enabled }
            .mapNotNull { MainFilters.fromString(it.id) }
            .toSet()
        activityFilters = value.activityFilters
            .filter { it.enabled }
            .map { it.id }
            .toSet()
        actor.setKingdom(kingdom)
        undefined
    }

}