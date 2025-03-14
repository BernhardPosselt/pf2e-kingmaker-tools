package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.actor.openActor
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.RangeInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.data.kingdom.structures.StructureTrait
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.increasedSkills
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.sheet.contexts.NavEntryContext
import at.posselt.pfrpg2e.kingdom.sheet.contexts.createTabs
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
external interface CostContext {
    val value: Int
    val lacksFunds: Boolean
}

@JsPlainObject
external interface StructureContext {
    val lots: Int
    val name: String
    val uuid: String
    val img: String?
    val canBuild: Boolean
    val infrastructure: Boolean
    val residential: Boolean
    val rp: CostContext
    val lumber: CostContext
    val ore: CostContext
    val stone: CostContext
    val luxuries: CostContext
    val notes: String?
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
    val structures: Array<StructureContext>
    val repairActive: Boolean
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

private enum class Cost {
    UPGRADE,
    HALF,
    FREE,
    FULL;
}

private typealias StructureFilter = (structure: Structure) -> Boolean

class StructureBrowser(
    private val game: Game,
    private val actor: KingdomActor,
    private val kingdom: KingdomData,
    private val worldStructures: List<Structure>,
    private val kingdomRanks: KingdomSkillRanks,
    private val chosenFeats: List<ChosenFeat>,
) : FormApp<StructureBrowserContext, StructureBrowserData>(
    title = "Structure Browser",
    template = "applications/kingdom/structure-browser.hbs",
    debug = true,
    width = 1100,
    scrollable = arrayOf(".km-structures", ".km-structure-filters"),
    classes = arrayOf("km-structure-browser"),
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
                mainFilters = emptySet()
                activityFilters = emptySet()
                render()
            }

            "open-structure" -> buildPromise {
                event.preventDefault()
                event.stopPropagation()
                target.dataset["uuid"]?.let {
                    openActor(it)
                }
            }

            "build-structure" -> buildPromise {
                val structuresByUuid = worldStructures.associateBy { it.uuid }
                val structuresByName = worldStructures.associateBy { it.name }
                val uuid = target.dataset["uuid"]
                val structure = uuid?.let { structuresByUuid[it] }
                val rubble = structuresByName["Rubble"]
                val ore = target.dataset["ore"]?.toInt() ?: 0
                val lumber = target.dataset["lumber"]?.toInt() ?: 0
                val stone = target.dataset["stone"]?.toInt() ?: 0
                val luxuries = target.dataset["luxuries"]?.toInt() ?: 0
                val rp = target.dataset["rp"]?.toInt() ?: 0
                val repair = target.dataset["repair"] == "true"
                checkNotNull(structure) {
                    "Structure with $uuid was null"
                }
                checkNotNull(rubble) {
                    "Rubble was null"
                }
                val degreeMessages = buildDegreeMessages(
                    ore = ore,
                    lumber = lumber,
                    stone = stone,
                    luxuries = luxuries,
                    rp = rp,
                    structure = structure,
                    rubble = rubble,
                )
                kingdomCheckDialog(
                    game = game,
                    kingdom = kingdom,
                    kingdomActor = actor,
                    check = CheckType.BuildStructure(structure),
                    afterRoll = { close() },
                    degreeMessages = degreeMessages,
                    flags = if (repair) setOf("repair-structure") else emptySet()
                )
            }

            "change-nav" -> {
                event.preventDefault()
                event.stopPropagation()
                currentNav = target.dataset["link"]
                    ?.let { StructureBrowserNav.fromString(it) }
                    ?: StructureBrowserNav.BUILDABLE
                render()
            }
        }
    }

    private fun filterStructures(
        structures: List<Structure>,
        filters: List<(Structure) -> Boolean>,
        mode: Cost,
    ): List<Structure> {
        return when (mode) {
            Cost.UPGRADE -> {
                val settlementStructuresByName = structures.associateBy { it.name }
                structures
                    .flatMap { structure ->
                        structure.upgradeFrom.mapNotNull { name ->
                            settlementStructuresByName[name]?.let { upgradeFrom ->
                                structure.copy(
                                    construction = structure.construction
                                        .upgradeFrom(upgradeFrom.construction),
                                    notes = "Upgraded from ${upgradeFrom.name}"
                                )
                            }
                        }
                    }
            }

            Cost.HALF -> structures.map { it.copy(notes = null, construction = it.construction.halveCost()) }
            Cost.FULL -> structures.map { it.copy(notes = null) }
            Cost.FREE -> structures.map { it.copy(notes = null, construction = it.construction.free()) }
        }.filter { s -> filters.all { it(s) } && s.name != "Rubble"}
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<StructureBrowserContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val increaseSkills = chosenFeats.map { it.feat.increasedSkills() }
        val settlements = kingdom.getAllSettlements(game)
        val settlementStructures = settlements.current?.constructedStructures ?: emptyList()
        val activityStructureFilters: List<StructureFilter> =
            activityFilters.map { activityId -> { s -> s.bonuses.any { it.activity == activityId } } }
        val structuresUpgradedFrom = worldStructures.flatMap { it.upgradeFrom }.toSet()
        val filters = buildList<StructureFilter> {
            add { s -> s.lots >= minLots && s.lots <= maxLots && s.level <= maxLevel }
            search?.let { add { s -> it.lowercase() in s.name.lowercase() } }
            if (MainFilters.IGNORE_PROFICIENCY !in mainFilters) {
                add { canBuild(it, increaseSkills) }
            }
            if (MainFilters.IGNORE_BUILDING_COST !in mainFilters) {
                val now = kingdom.commodities.now
                add {
                    it.construction.hasFunds(
                        existingLumber = now.lumber,
                        existingLuxuries = now.luxuries,
                        existingOre = now.ore,
                        existingStone = now.stone,
                        existingRp = kingdom.resourcePoints.now,
                    )
                }
            }
            if (MainFilters.REDUCES_RUIN in mainFilters) {
                add { it.reducesRuin }
            }
            if (MainFilters.REDUCES_UNREST in mainFilters) {
                add { it.reducesUnrest }
            }
            if (MainFilters.REDUCES_CONSUMPTION in mainFilters) {
                add { it.consumptionReduction > 0 }
            }
            if (MainFilters.HOUSING in mainFilters) {
                add { StructureTrait.RESIDENTIAL in it.traits }
            }
            if (MainFilters.DOWNTIME in mainFilters) {
                add { it.affectsDowntime }
            }
            if (MainFilters.SHOPPING in mainFilters) {
                add { it.availableItemsRules.isNotEmpty() }
            }
            if (MainFilters.AFFECTS_EVENTS in mainFilters) {
                add { it.affectsEvents }
            }
            if (MainFilters.INCREASES_CAPACITY in mainFilters) {
                add { it.storage.isNotEmpty() }
            }
            if (MainFilters.CHEAPER_WHEN_UPGRADED in mainFilters) {
                add { it.name in structuresUpgradedFrom }
            }
            if (MainFilters.UPGRADEABLE in mainFilters) {
                add { it.upgradeFrom.isNotEmpty() }
            }
            addAll(activityStructureFilters)
        }
        val buildableStructures = filterStructures(worldStructures, filters, Cost.FULL)
        val freeStructures = filterStructures(
            settlements.current?.structuresInConstruction ?: emptyList(), filters, Cost.FREE
        )
        val upgradeableStructures = filterStructures(settlementStructures, filters, Cost.UPGRADE)
        val structures = when (currentNav) {
            StructureBrowserNav.BUILDABLE -> buildableStructures
            StructureBrowserNav.REPAIRABLE -> filterStructures(worldStructures, filters, Cost.HALF)
            StructureBrowserNav.UPGRADEABLE -> upgradeableStructures
            StructureBrowserNav.FREE -> freeStructures
        }
            .map {
                StructureContext(
                    lots = it.lots,
                    name = it.name,
                    notes = it.notes,
                    uuid = it.uuid,
                    img = it.img,
                    canBuild = canBuild(it, increaseSkills),
                    infrastructure = it.traits.contains(StructureTrait.INFRASTRUCTURE),
                    residential = it.traits.contains(StructureTrait.RESIDENTIAL),
                    rp = CostContext(
                        value = it.construction.rp,
                        lacksFunds = it.construction.rp >= kingdom.resourcePoints.now,
                    ),
                    lumber = CostContext(
                        value = it.construction.lumber,
                        lacksFunds = it.construction.lumber >= kingdom.commodities.now.lumber,
                    ),
                    ore = CostContext(
                        value = it.construction.ore,
                        lacksFunds = it.construction.ore >= kingdom.commodities.now.ore,
                    ),
                    stone = CostContext(
                        value = it.construction.stone,
                        lacksFunds = it.construction.stone >= kingdom.commodities.now.stone,
                    ),
                    luxuries = CostContext(
                        value = it.construction.luxuries,
                        lacksFunds = it.construction.luxuries >= kingdom.commodities.now.luxuries,
                    ),
                )
            }.toTypedArray()
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
        val buildable = buildableStructures.size
        val upgradeable = upgradeableStructures.size
        val free = freeStructures.size
        val mainFilters = MainFilters.entries.flatMapIndexed { index, it ->
            listOf(
                HiddenInput(
                    name = "mainFilters.$index.id",
                    value = it.value,
                ).toContext(),
                CheckboxInput(
                    name = "mainFilters.$index.enabled",
                    label = it.label,
                    value = it in mainFilters,
                ).toContext()
            )
        }.toTypedArray()
        val activityFilters = worldStructures
            .flatMap { it.bonuses.mapNotNull { b -> b.activity } }
            .distinct()
            .flatMapIndexed { index, it ->
                listOf(
                    HiddenInput(
                        name = "activityFilters.$index.id",
                        value = it,
                    ).toContext(),
                    CheckboxInput(
                        name = "activityFilters.$index.enabled",
                        label = it.unslugify(),
                        value = it in activityFilters,
                    ).toContext()
                )
            }
            .sortedBy { it.label }
            .toTypedArray()
        val nav = createTabs<StructureBrowserNav>("change-nav", currentNav)
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
            structures = structures,
            repairActive = currentNav == StructureBrowserNav.REPAIRABLE,
        )
    }

    private fun canBuild(
        structure: Structure,
        increaseSkills: List<Map<KingdomSkill, Set<KingdomSkill>>>
    ): Boolean = getValidActivitySkills(
        ranks = kingdomRanks,
        activityRanks = structure.construction.skills,
        ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
        expandMagicUse = kingdom.settings.expandMagicUse,
        activityId = "build-structure",
        increaseSkills = increaseSkills
    ).isNotEmpty()

    override fun onParsedSubmit(value: StructureBrowserData): Promise<Void> = buildPromise {
        kingdom.activeSettlement = value.activeSettlement
        maxLots = value.maxLots
        minLots = value.minLots
        maxLevel = value.maxLevel
        search = value.search
        mainFilters = value.mainFilters
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