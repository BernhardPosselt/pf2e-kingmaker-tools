package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.Menu
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Section
import at.posselt.pfrpg2e.app.forms.SectionsContext
import at.posselt.pfrpg2e.app.forms.Select
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
class KingdomSettingsDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            int("rpToXpConversionRate")
            int("rpToXpConversionLimit")
            int("resourceDicePerVillage")
            int("resourceDicePerTown")
            int("resourceDicePerCity")
            int("resourceDicePerMetropolis")
            int("xpPerClaimedHex")
            int("maximumFamePoints")
            boolean("expandMagicUse")
            boolean("includeCapitalItemModifier")
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
        }
    }
}

@JsPlainObject
external interface KingdomSettingsContext : HandlebarsRenderContext, SectionsContext {
    val isFormValid: Boolean
}


@JsExport
class KingdomSettingsApplication(
    private val game: Game,
    private val onSave: (settings: KingdomSettings) -> Unit,
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
                    ),
                ),
                Section(
                    legend = "Events",
                    formRows = listOf(
                        CheckboxInput(
                            name = "cultOfTheBloomEvents",
                            label = "Enable Cult of the Bloom Events",
                            value = settings.cultOfTheBloomEvents,
                            help = "If enabled, adds a Cult of the Bloom Events section in Event Phase",
                        ),
                        Select(
                            name = "kingdomCultTable",
                            label = "Cult Events Roll Table",
                            options = cultTableOptions,
                            value = settings.kingdomCultTable,
                            stacked = false,
                            required = false,
                            help = "If none selected, falls back to the Random Cult Events table in this module's Rolltables compendium",
                        ),
                        Select.fromEnum<RollMode>(
                            name = "kingdomEventRollMode",
                            label = "Kingdom Event Roll Mode",
                            value = fromCamelCase<RollMode>(settings.kingdomEventRollMode),
                            labelFunction = { it.label },
                            stacked = false,
                        ),
                        Select(
                            name = "kingdomEventsTable",
                            label = "Kingdom Events Roll Table",
                            options = kingdomEventsTableOptions,
                            value = settings.kingdomEventsTable,
                            stacked = false,
                            required = false,
                            help = "If none selected, falls back to the Random Kingdom Events table in this module's Rolltables compendium",
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
        undefined
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "save" -> {
                onSave(settings)
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
