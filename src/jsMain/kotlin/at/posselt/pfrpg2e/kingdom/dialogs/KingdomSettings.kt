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
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
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
                    .map { it.toCamelCase() to t(it) }
                    .toRecord()
            }
            string("proficiencyMode") {
                choices = UntrainedProficiencyMode.entries
                    .map { it.toCamelCase() to t(it) }
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
    title = t("kingdom.kingdomSettings"),
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
                    legend = t("kingdom.automation"),
                    formRows = listOf(
                        Select.range(
                            name = "maximumFamePoints",
                            label = t("kingdom.maximumFamePoints"),
                            value = settings.maximumFamePoints,
                            from = 1,
                            to = 10,
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "automateStats",
                            label = t("kingdom.automateStats"),
                            value = settings.automateStats,
                            help = t("kingdom.automateStatsHelp")
                        ),
                        Select.fromEnum<AutomateResources>(
                            name = "automateResources",
                            label = t("kingdom.automateResources"),
                            help = t("kingdom.automateResourcesHelp"),
                            value = fromCamelCase<AutomateResources>(settings.automateResources),
                            stacked = false,
                        ),
                        Select(
                            name = "realmSceneId",
                            label = t("kingdom.realmSceneId"),
                            value = settings.realmSceneId,
                            disabled = settings.automateResources != AutomateResources.TILE_BASED.value,
                            options = realmSceneOptions,
                            help = t("kingdom.realmSceneIdHelp"),
                            required = false,
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "autoCalculateSettlementLevel",
                            label = t("kingdom.autoCalculateSettlementLevel"),
                            value = settings.autoCalculateSettlementLevel,
                            help = t("kingdom.autoCalculateSettlementLevelHelp"),
                        ),
                        CheckboxInput(
                            name = "autoCalculateArmyConsumption",
                            label = t("kingdom.autoCalculateArmyConsumption"),
                            value = settings.autoCalculateArmyConsumption,
                            help = t("kingdom.autoCalculateArmyConsumptionHelp"),
                        ),
                        CheckboxInput(
                            name = "kingdomIgnoreSkillRequirements",
                            label = t("kingdom.kingdomIgnoreSkillRequirements"),
                            value = settings.kingdomIgnoreSkillRequirements,
                            help = t("kingdom.kingdomIgnoreSkillRequirementsHelp"),
                        ),
                        Select(
                            name = "recruitableArmiesFolderId",
                            label = t("kingdom.recruitableArmiesFolderId"),
                            options = folders,
                            value = settings.recruitableArmiesFolderId,
                            help = t("kingdom.recruitableArmiesFolderIdHelp"),
                            stacked = false,
                        )
                    ),
                ),
                Section(
                    legend = t("kingdom.events"),
                    formRows = listOf(
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "eventDc",
                            label = t("kingdom.eventDc"),
                            value = settings.eventDc,
                            stacked = false,
                        ),
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "eventDcStep",
                            label = t("kingdom.eventDcStep"),
                            value = settings.eventDcStep,
                            stacked = false,
                        ),
                        Select(
                            name = "kingdomEventsTable",
                            label = t("kingdom.kingdomEventsTable"),
                            options = kingdomEventsTableOptions,
                            value = settings.kingdomEventsTable,
                            stacked = false,
                            required = false,
                            help = t("kingdom.kingdomEventsTableHelp"),
                        ),
                        CheckboxInput(
                            name = "cultOfTheBloomEvents",
                            label = t("kingdom.cultOfTheBloomEvents"),
                            value = settings.cultOfTheBloomEvents,
                            help = t("kingdom.cultOfTheBloomEventsHelp"),
                        ),
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "cultEventDc",
                            label = t("kingdom.cultEventDc"),
                            value = settings.cultEventDc,
                            stacked = false,
                        ),
                        Select.range(
                            from = 0,
                            to = 20,
                            name = "cultEventDcStep",
                            label = t("kingdom.cultEventDcStep"),
                            value = settings.cultEventDcStep,
                            stacked = false,
                        ),
                        Select(
                            name = "kingdomCultTable",
                            label = t("kingdom.kingdomCultTable"),
                            options = cultTableOptions,
                            value = settings.kingdomCultTable,
                            stacked = false,
                            required = false,
                            help = t("kingdom.kingdomCultTableHelp"),
                        ),
                        Select.fromEnum<RollMode>(
                            name = "kingdomEventRollMode",
                            label = t("kingdom.kingdomEventRollMode"),
                            value = fromCamelCase<RollMode>(settings.kingdomEventRollMode),
                            labelFunction = { it.label },
                            stacked = false,
                        ),
                    ),
                ),
                Section(
                    legend = t("kingdom.companionGuide"),
                    formRows = listOf(
                        CheckboxInput(
                            name = "reduceDCToBuildLumberStructures",
                            label = t("kingdom.reduceDCToBuildLumberStructures"),
                            value = settings.reduceDCToBuildLumberStructures,
                        ),
                        CheckboxInput(
                            name = "expandMagicUse",
                            label = t("kingdom.expandMagicUse"),
                            value = settings.expandMagicUse,
                            help = t("kingdom.expandMagicUseHelp"),
                        ),
                    ),
                ),
                Section(
                    legend = "Vance & Kerenshara ${t("kingdom.homebrew")}",
                    formRows = listOf(
                        CheckboxInput(
                            name = "kingdomAllStructureItemBonusesStack",
                            label = t("kingdom.kingdomAllStructureItemBonusesStack"),
                            value = settings.kingdomAllStructureItemBonusesStack,
                            help = t("kingdom.kingdomAllStructureItemBonusesStackHelp")
                        ),
                        Select.range(
                            from = 0,
                            to = 2,
                            label = t("kingdom.increaseScorePicksBy"),
                            value = settings.increaseScorePicksBy,
                            name = "increaseScorePicksBy",
                            help = t("kingdom.increaseScorePicksByHelp"),
                            stacked = false,
                        ),
                        NumberInput(
                            name = "ruinThreshold",
                            label = t("kingdom.ruinThreshold"),
                            value = settings.ruinThreshold,
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "capitalInvestmentInCapital",
                            label = t("kingdom.capitalInvestmentInCapital"),
                            value = settings.capitalInvestmentInCapital,
                        ),
                        CheckboxInput(
                            name = "includeCapitalItemModifier",
                            label = t("kingdom.includeCapitalItemModifier"),
                            value = settings.includeCapitalItemModifier,
                            help = t("kingdom.includeCapitalItemModifierHelp"),
                        ),
                        CheckboxInput(
                            name = "vanceAndKerensharaXP",
                            label = t("kingdom.xpRules"),
                            value = settings.vanceAndKerensharaXP,
                            help = t("kingdom.xpRulesHelp"),
                        ),
                        NumberInput(
                            name = "rpToXpConversionRate",
                            label = t("kingdom.rpToXpConversionRate"),
                            value = settings.rpToXpConversionRate,
                            stacked = false,
                            readonly = settings.vanceAndKerensharaXP,
                        ),
                        NumberInput(
                            name = "rpToXpConversionLimit",
                            label = t("kingdom.rpToXpConversionLimit"),
                            value = settings.rpToXpConversionLimit,
                            stacked = false,
                            readonly = settings.vanceAndKerensharaXP,
                        ),
                        NumberInput(
                            name = "xpPerClaimedHex",
                            label = t("kingdom.xpPerClaimedHex"),
                            value = settings.xpPerClaimedHex,
                            stacked = false,
                            readonly = settings.vanceAndKerensharaXP,
                        ),
                        CheckboxInput(
                            name = "enableLeadershipModifiers",
                            label = t("kingdom.enableLeadershipModifiers"),
                            value = settings.enableLeadershipModifiers,
                            help = t("kingdom.enableLeadershipModifiersHelp"),
                        ),
                        Menu(
                            label =t("kingdom.configureLeaderSkills") ,
                            name = t("kingdom.configure"),
                            value = "configure-leader-skills",
                            disabled = !settings.enableLeadershipModifiers,
                            help = t("kingdom.configureLeaderSkillsHelp"),
                        ),
                        Menu(
                            label =t("kingdom.configureLeaderKingdomSkills") ,
                            name = t("kingdom.configure"),
                            value = "configure-leader-kingdom-skills",
                            disabled = !settings.enableLeadershipModifiers,
                            help = t("kingdom.configureLeaderKingdomSkillsHelp"),
                        ),
                        NumberInput(
                            name = "resourceDicePerVillage",
                            label = t("kingdom.resourceDicePerVillage"),
                            value = settings.resourceDicePerVillage,
                            stacked = false,
                        ),
                        NumberInput(
                            name = "resourceDicePerTown",
                            label = t("kingdom.resourceDicePerTown"),
                            value = settings.resourceDicePerTown,
                            stacked = false,
                        ),
                        NumberInput(
                            name = "resourceDicePerCity",
                            label = t("kingdom.resourceDicePerCity"),
                            value = settings.resourceDicePerCity,
                            stacked = false,
                        ),
                        NumberInput(
                            name = "resourceDicePerMetropolis",
                            label = t("kingdom.resourceDicePerMetropolis"),
                            value = settings.resourceDicePerMetropolis,
                            stacked = false,
                        ),
                        Select.fromEnum<UntrainedProficiencyMode>(
                            name = "proficiencyMode",
                            value = fromCamelCase<UntrainedProficiencyMode>(settings.proficiencyMode),
                            stacked = false,
                        ),
                        CheckboxInput(
                            name = "kingdomSkillIncreaseEveryLevel",
                            label = t("kingdom.kingdomSkillIncreaseEveryLevel"),
                            value = settings.kingdomSkillIncreaseEveryLevel,
                        ),
                    )
                ),
            ),
        )
    }

    override fun onParsedSubmit(value: KingdomSettings): Promise<Void> = buildPromise {
        settings = KingdomSettings.copy(
            value,
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
