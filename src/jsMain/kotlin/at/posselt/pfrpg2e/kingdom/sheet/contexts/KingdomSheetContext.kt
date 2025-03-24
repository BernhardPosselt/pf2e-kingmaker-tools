package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import kotlinx.js.JsPlainObject

@Suppress("unused")
@JsPlainObject
external interface KingdomSheetContext : ValidatedHandlebarsContext {
    val kingdomNameInput: FormElementContext
    val settlementInput: FormElementContext
    val xpInput: FormElementContext
    val xpThresholdInput: FormElementContext
    val levelInput: FormElementContext
    val fameContext: FameContext
    val atWarInput: FormElementContext
    val sizeInput: FormElementContext
    val size: Int
    val kingdomSize: String
    val unrestInput: FormElementContext
    val commoditiesContext: CommoditiesContext
    val resourcePointsContext: ResourceContext
    val resourceDiceContext: ResourceContext
    val worksitesContext: Array<WorkSiteContext>
    val ruinContext: Array<RuinContext>
    val controlDc: Int
    val unrestPenalty: Int
    val anarchyAt: Int
    val consumptionContext: ConsumptionContext
    val supernaturalSolutionsInput: FormElementContext
    val creativeSolutionsInput: FormElementContext
    val notesContext: NotesContext
    val leadersContext: Array<LeaderValuesContext>
    val charter: CharterContext
    val heartland: HeartlandContext
    val government: GovernmentContext
    val abilityBoosts: AbilityBoostContext
    val featuresByLevel: Array<FeatureByLevelContext>
    val mainNav: Array<NavEntryContext>
    val kingdomSectionNav: Array<NavEntryContext>
    val hideCreation: Boolean
    val hideBonus: Boolean
    val currentNavEntry: String
    val settings: KingdomSettings
    val bonusFeat: AddBonusFeatContext
    val bonusFeats: Array<BonusFeatContext>
    val groups: Array<GroupContext>
    val skillRanks: SkillRanksContext
    val abilityScores: Array<AbilityScoreContext>
    val milestones: Array<MilestoneContext>
    val ongoingEvent: FormElementContext
    val isGM: Boolean
    val actor: KingdomActor
    val modifiers: Array<ModifierContext>
    val initialProficiencies: Array<FormElementContext>
    val enableLeadershipModifiers: Boolean
    val settlements: Array<SettlementsContext>
    val canAddCurrentSceneAsSettlement: Boolean
    val turnSectionNav: Array<NavEntryContext>
    val vkXp: Boolean
    val activities: ActivitiesContext
    val canLevelUp: Boolean
    val ongoingEvents: Array<String>
    val eventDC: Int
    val cultEventDC: Int
    val cultOfTheBloomEvents: Boolean
    val civicPlanning: Boolean
    val heartlandLabel: String?
    val leadershipActivities: Int
    val ongoingEventButtonDisabled: Boolean
    val collectTaxesReduceUnrestDisabled: Boolean
    val consumption: Int
    val automateStats: Boolean
    val resourceDiceIncome: String
    val skillChecks: Array<SkillChecksContext>
    val automateResources: Boolean
    val useLeadershipModifiers: Boolean
    val activeSettlementType: String
    val actorUuid: String
}