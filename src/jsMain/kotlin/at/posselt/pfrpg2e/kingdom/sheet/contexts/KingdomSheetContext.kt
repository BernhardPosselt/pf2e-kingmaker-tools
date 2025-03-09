package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.kingdom.KingdomSettings
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface KingdomSheetContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val kingdomNameInput: FormElementContext
    val settlementInput: FormElementContext
    val xpInput: FormElementContext
    val xpThresholdInput: FormElementContext
    val levelInput: FormElementContext
    val fameContext: FameContext
    val atWarInput: FormElementContext
    val sizeInput: FormElementContext
    val unrestInput: FormElementContext
    val commoditiesContext: CommoditiesContext
    val resourcePointsContext: ResourceContext
    val resourceDiceContext: ResourceContext
    val worksitesContext: WorkSitesContext
    val ruinContext: RuinsContext
    val controlDc: Int
    val unrestPenalty: Int
    val anarchyAt: Int
    val consumptionContext: ConsumptionContext
    val supernaturalSolutionsInput: FormElementContext
    val creativeSolutionsInput: FormElementContext
    val notesContext: NotesContext
    val leadersContext: LeadersContext
    val charter: CharterContext
    val heartland: HeartlandContext
    val government: GovernmentContext
    val abilityBoosts: AbilityBoostContext
    val featuresByLevel: Array<FeatureByLevelContext>
    val kingdomSectionNav: Array<NavEntryContext>
    val hideCreation: Boolean
    val hideBonus: Boolean
    val currentNavEntry: String
    val settings: KingdomSettings
    val bonusFeat: AddBonusFeatContext
    val bonusFeats: Array<BonusFeatContext>
    val groups: Array<GroupContext>
    val skillRanks: SkillRanksContext
    val abilityScores: AbilityScoresContext
}