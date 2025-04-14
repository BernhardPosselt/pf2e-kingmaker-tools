package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.Menu
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Section
import at.posselt.pfrpg2e.app.forms.SectionsContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.AutomateResources
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.UntrainedProficiencyMode
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsExport
class KingdomSettingsDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            int("eventDc")
            int("eventDcStep")
            int("cultEventDc")
            int("cultEventDcStep")
            int("rpToXpConversionRate")
            int("rpToXpConversionLimit")
            int("resourceDicePerVillage")
            int("resourceDicePerTown")
            int("resourceDicePerCity")
            int("resourceDicePerMetropolis")
            int("xpPerClaimedHex")
            int("maximumFamePoints")
            int("ruinThreshold") {
                min = 0
            }
            int("increaseScorePicksBy")
            boolean("expandMagicUse")
            boolean("includeCapitalItemModifier")
            boolean("automateStats")
            boolean("cultOfTheBloomEvents")
            boolean("autoCalculateSettlementLevel")
            boolean("vanceAndKerensharaXP")
            boolean("capitalInvestmentInCapital")
            boolean("reduceDCToBuildLumberStructures")
            boolean("kingdomSkillIncreaseEveryLevel")
            boolean("kingdomAllStructureItemBonusesStack")
            boolean("kingdomIgnoreSkillRequirements")
            boolean("autoCalculateArmyConsumption")
            boolean("enableLeadershipModifiers")
            string("recruitableArmiesFolderId", nullable = true)
            string("kingdomCultTable", nullable = true)
            string("kingdomEventsTable", nullable = true)
            string("kingdomEventRollMode") {
                choices = RollMode.entries
                    .map { it.toCamelCase() to it.label }
                    .toRecord()
            }
            string("automateResources") {
                choices = AutomateResources.entries
                    .map { it.toCamelCase() to it.toLabel() }
                    .toRecord()
            }
            string("proficiencyMode") {
                choices = UntrainedProficiencyMode.entries
                    .map { it.toCamelCase() to it.toLabel() }
                    .toRecord()
            }
            string("realmSceneId", nullable = true)
        }
    }
}

@JsPlainObject
external interface KingdomSettingsContext : ValidatedHandlebarsContext, SectionsContext

class KingdomSettingsApplication(
    private val game: Game,
    private val onSave: suspend (settings: KingdomSettings) -> Unit,
    val kingdomSettings: KingdomSettings,
) : FormApp<KingdomSettingsContext, KingdomSettings>(
    title = "Kingdom Settings",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = KingdomSettingsDataModel::class.js,
    id = "kmKingdomSettings",
    width = 600,
) {
    private var settings = deepClone(kingdomSettings)

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<KingdomSettingsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val cultTableOptions = game.tables.contents
            .mapNotNull { it.toOption(useUuid = true) }
            .sortedBy { it.label }
        val kingdomEventsTableOptions = game.tables.contents
            .mapNotNull { it.toOption(useUuid = true) }
            .sortedBy { it.label }
        val realmSceneOptions = game.scenes.contents
            .mapNotNull { it.id?.let { id -> SelectOption(it.name, id)}}
            .sortedBy { it.label }
        val folders = game.folders.contents
            .mapNotNull { it.id?.let { value -> SelectOption(it.name, value) } }
            .sortedBy { it.label }
        KingdomSettingsContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            sections = formContext(
                Section(
                    legend = "Automation",
                    formRows = listOf(
                        Select.range(
                            name = "maximumFamePoints",
                            label = "Maximum Fame Points",
                            value = settings.maximumFamePoints,
                            from = 1,
                            to = 10,
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "automateStats",
                            label = "Automate Kingdom Stats",
                            value = settings.automateStats,
                            help = "If enabled, automatically calculates skill proficiencies, ability scores and ruin thresholds from select choices"
                        ),
                        Select.fromEnum<AutomateResources>(
                            name = "automateResources",
                            label = "Automatically Calculate Kingdom Resources",
                            help = "Official Module parses the Stolen Lands data, Tile Based sums up worksites, size and farmlands from special Tiles or Drawings, Manual requires you to manage it manually in the Status tab",
                            value = fromCamelCase<AutomateResources>(settings.automateResources),
                            stacked = false,
                            labelFunction = {
                                when(it) {
                                    AutomateResources.KINGMAKER -> "Official Module"
                                    AutomateResources.TILE_BASED -> "Tile Based"
                                    AutomateResources.MANUAL -> "Manual"
                                }
                            }
                        ),
                        Select(
                            name = "realmSceneId",
                            label = "Realm Scene",
                            value = settings.realmSceneId,
                            disabled = this@KingdomSettingsApplication.settings.automateResources != AutomateResources.TILE_BASED.value,
                            options = realmSceneOptions,
                            help = "Automatically Calculate Kingdom Resources is set to tile based, use this scene to calculate it based off drawings/tiles",
                            required = false,
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "autoCalculateSettlementLevel",
                            label = "Automatically Calculate Settlement Level",
                            value = settings.autoCalculateSettlementLevel,
                            help = "Placing structures on the shipped settlement scene blocks automatically sets the settlement's level",
                        ),
                        CheckboxInput(
                            name = "autoCalculateArmyConsumption",
                            label = "Automatically Calculate Army Consumption",
                            value = settings.autoCalculateArmyConsumption,
                            help = "If enabled, gets all visible army tokens on all scenes and sums up their consumption",
                        ),
                        CheckboxInput(
                            name = "kingdomIgnoreSkillRequirements",
                            label = "Disable Activity Skill Proficiency Requirements",
                            value = settings.kingdomIgnoreSkillRequirements,
                            help = "If disabled, all activities can be performed regardless of skill proficiencies",
                        ),
                        Select(
                            name = "recruitableArmiesFolderId",
                            label = "Recruitable Armies Folder",
                            options = folders,
                            value = settings.recruitableArmiesFolderId,
                            help = "This folder will be used to look up armies for the Recruit Army activity and to calculate army consumption. When you first perform the Recruit Army activity, this folder will be created automatically and set if this setting is empty",
                            stacked = false,
                        )
                    ),
                ),
                Section(
                    legend = "Events",
                    formRows = listOf(
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "eventDc",
                            label = "Base Event DC",
                            value = settings.eventDc,
                            stacked = false,
                        ),
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "eventDcStep",
                            label = "Event DC Reduction per Check",
                            value = settings.eventDcStep,
                            stacked = false,
                        ),
                        Select(
                            name = "kingdomEventsTable",
                            label = "Kingdom Events Roll Table",
                            options = kingdomEventsTableOptions,
                            value = settings.kingdomEventsTable,
                            stacked = false,
                            required = false,
                            help = "If none selected, falls back to the Random Kingdom Events table in this module's Roll Tables compendium",
                        ),
                        CheckboxInput(
                            name = "cultOfTheBloomEvents",
                            label = "Enable Cult Events",
                            value = settings.cultOfTheBloomEvents,
                            help = "If enabled, adds a Cult Events section in Event Phase",
                        ),
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "cultEventDc",
                            label = "Cult Base Event DC",
                            value = settings.cultEventDc,
                            stacked = false,
                        ),
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "cultEventDcStep",
                            label = "Cult Event DC Reduction per Check",
                            value = settings.cultEventDcStep,
                            stacked = false,
                        ),
                        Select(
                            name = "kingdomCultTable",
                            label = "Cult Events Roll Table",
                            options = cultTableOptions,
                            value = settings.kingdomCultTable,
                            stacked = false,
                            required = false,
                            help = "If none selected, falls back to the Random Cult Events table in this module's Roll Tables compendium",
                        ),
                        Select.fromEnum<RollMode>(
                            name = "kingdomEventRollMode",
                            label = "Kingdom Event Roll Mode",
                            value = fromCamelCase<RollMode>(settings.kingdomEventRollMode),
                            labelFunction = { it.label },
                            stacked = false,
                        ),
                    ),
                ),
                Section(
                    legend = "Companion Guide",
                    formRows = listOf(
                        CheckboxInput(
                            name = "reduceDCToBuildLumberStructures",
                            label = "Reduce DC for building Structures made out of Lumber by 2",
                            value = settings.reduceDCToBuildLumberStructures,
                        ),
                        CheckboxInput(
                            name = "expandMagicUse",
                            label = "Expand Magic Use",
                            value = settings.expandMagicUse,
                            help = "When enabled, you can use Magic for the following activities: Celebrate Holiday, Craft Luxuries, Create a Masterpiece, and Rest and Relax",
                        ),
                    ),
                ),
                Section(
                    legend = "Vance & Kerenshara",
                    formRows = listOf(
                        CheckboxInput(
                            name = "kingdomAllStructureItemBonusesStack",
                            label = "All Structure Item Bonuses Stack",
                            value = settings.kingdomAllStructureItemBonusesStack,
                            help = "If enabled, stacks item bonuses from all structures in a settlement, regardless of building type"
                        ),
                        Select.range(
                            from = 0,
                            to = 2,
                            label = "Increase Ability Score Picks By",
                            value = settings.increaseScorePicksBy,
                            name = "increaseScorePicksBy",
                            help = "Whenever you gain an Ability Score increase, gain this value in addition to the default 2",
                            stacked = false,
                        ),
                        NumberInput(
                            name = "ruinThreshold",
                            label = "Starting Ruin Threshold",
                            value = settings.ruinThreshold,
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "capitalInvestmentInCapital",
                            label = "Enable Capital Investment in Capital without Bank",
                            value = settings.capitalInvestmentInCapital,
                        ),
                        CheckboxInput(
                            name = "includeCapitalItemModifier",
                            label = "Enable Capital Item Modifier Fallback",
                            value = settings.includeCapitalItemModifier,
                            help = "If enabled, falls back to the item bonus of the capital if it is higher than the currently selected settlement",
                        ),
                        CheckboxInput(
                            name = "vanceAndKerensharaXP",
                            label = "XP Rules",
                            value = settings.vanceAndKerensharaXP,
                            help = "Adds additional milestones, gain XP buttons and increases XP for claiming hexes and converting XP based on kingdom level",
                        ),
                        NumberInput(
                            name = "rpToXpConversionRate",
                            label = "RP to XP Conversion Rate",
                            value = settings.rpToXpConversionRate,
                            stacked = false,
                            readonly = settings.vanceAndKerensharaXP,
                        ),
                        NumberInput(
                            name = "rpToXpConversionLimit",
                            label = "RP to XP Conversion Limit",
                            value = settings.rpToXpConversionLimit,
                            stacked = false,
                            readonly = settings.vanceAndKerensharaXP,
                        ),
                        NumberInput(
                            name = "xpPerClaimedHex",
                            label = "XP per Claimed Hex",
                            value = settings.xpPerClaimedHex,
                            stacked = false,
                            readonly = settings.vanceAndKerensharaXP,
                        ),
                        CheckboxInput(
                            name = "enableLeadershipModifiers",
                            label = "Use Leadership Modifiers",
                            value = settings.enableLeadershipModifiers,
                            help = "Replaces Invested Status Bonus with Bonuses based on PC skill ranks",
                        ),
                        Menu(
                            label = "Leader Skills",
                            name = "Configure",
                            value = "configure-leader-skills",
                            disabled = !settings.enableLeadershipModifiers,
                            help = "If Leadership Modifiers are enabled, configure which skills and lores are used to calculate Leadership bonuses",
                        ),
                        Menu(
                            label = "Leader Kingdom Skills",
                            name = "Configure",
                            value = "configure-leader-kingdom-skills",
                            disabled = !settings.enableLeadershipModifiers,
                            help = "If Leadership Modifiers are enabled, configure which leaders and kingdom skills receive the full Leadership bonus",
                        ),
                        NumberInput(
                            name = "resourceDicePerVillage",
                            label = "Resource Dice each turn per Village",
                            value = settings.resourceDicePerVillage,
                            stacked = false,
                        ),
                        NumberInput(
                            name = "resourceDicePerTown",
                            label = "Resource Dice each turn per Town",
                            value = settings.resourceDicePerTown,
                            stacked = false,
                        ),
                        NumberInput(
                            name = "resourceDicePerCity",
                            label = "Resource Dice each turn per City",
                            value = settings.resourceDicePerCity,
                            stacked = false,
                        ),
                        NumberInput(
                            name = "resourceDicePerMetropolis",
                            label = "Resource Dice each turn per Metropolis",
                            value = settings.resourceDicePerMetropolis,
                            stacked = false,
                        ),
                        Select.fromEnum<UntrainedProficiencyMode>(
                            name = "proficiencyMode",
                            label = "Always Increase Untrained Skills By",
                            value = fromCamelCase<UntrainedProficiencyMode>(settings.proficiencyMode),
                            stacked = false,
                            labelFunction = {
                                when (it) {
                                    UntrainedProficiencyMode.NONE -> "Nothing"
                                    UntrainedProficiencyMode.HALF -> "Half Level"
                                    UntrainedProficiencyMode.FULL -> "Full Level"
                                }
                            },
                        ),
                        CheckboxInput(
                            name = "kingdomSkillIncreaseEveryLevel",
                            label = "Kingdom Skill Increase Every Level",
                            value = settings.kingdomSkillIncreaseEveryLevel,
                        ),
                    )
                ),
            ),
        )
    }

    override fun onParsedSubmit(value: KingdomSettings): Promise<Void> = buildPromise {
        settings = value.copy(
            leaderKingdomSkills = settings.leaderKingdomSkills,
            leaderSkills = settings.leaderSkills,
        )
        if(settings.automateResources != AutomateResources.TILE_BASED.value) {
            settings.realmSceneId = null
        }
        undefined
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> {
                buildPromise {
                    onSave(settings)
                }
                close()
            }

            "configure-leader-skills" -> {
                configureLeaderSkills(settings.leaderSkills) {
                    settings.leaderSkills = it
                }
            }

            "configure-leader-kingdom-skills" -> {
                configureLeaderKingdomSkills(settings.leaderKingdomSkills) {
                    settings.leaderKingdomSkills = it
                }
            }
        }
    }
}
