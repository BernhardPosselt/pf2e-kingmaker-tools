package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.RuinValues
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActors
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderKingdomSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createAbilityModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createAllProficiencyModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createInvestedBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createLeaderEventBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createRepairBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createRulerBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createStructureEventBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createSupernaturalSolutionModifier
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createAnarchyPenalty
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createArmyConditionPenalties
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createFavoredLandPenalty
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createRuinModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createSecondaryTerritoryPenalty
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createUnrestModifier
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createVacancyModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.noBridgePenalty


fun createAllModifiers(
    kingdomLevel: Int,
    globalBonuses: GlobalStructureBonuses,
    currentSettlement: MergedSettlement?,
    abilityScores: KingdomAbilityScores,
    leaderActors: LeaderActors,
    leaderSkills: LeaderSkills,
    leaderKingdomSkills: LeaderKingdomSkills,
    kingdomSkillRanks: KingdomSkillRanks,
    allSettlements: List<Settlement>,
    ruins: RuinValues,
    unrest: Int,
    vacancies: Vacancies,
    targetedArmy: ArmyConditionInfo?,
    untrainedProficiencyMode: UntrainedProficiencyMode,
    enableLeadershipBonuses: Boolean,
    featModifiers: List<Modifier>,
    featureModifiers: List<Modifier>,
    eventModifiers: List<Modifier>,
): List<Modifier> =
    listOfNotNull(
        createRulerBonus(global = globalBonuses)
    ) + if (currentSettlement == null) {
        emptyList()
    } else {
        createStructureBonuses(
            mergedSettlement = currentSettlement,
        )
    } + if (enableLeadershipBonuses) {
        createLeadershipModifiers(
            leaderActors = leaderActors,
            leaderSkills = leaderSkills,
            leaderKingdomSkills = leaderKingdomSkills,
        )
    } else {
        createInvestedBonuses(
            kingdomLevel = kingdomLevel,
            leaderActors = leaderActors,
        )
    } + listOfNotNull(
        createLeaderEventBonus(kingdomLevel = kingdomLevel)
    ) + if (currentSettlement == null) {
        emptyList()
    } else {
        listOfNotNull(
            createStructureEventBonuses(currentSettlement = currentSettlement.settlement)
        )
    } + createAbilityModifiers(
        abilities = abilityScores
    ) + createAllProficiencyModifiers(
        ranks = kingdomSkillRanks,
        level = kingdomLevel,
        mode = untrainedProficiencyMode,
    ) + listOf(
        createFavoredLandPenalty()
    ) + listOfNotNull(
        noBridgePenalty(settlements = allSettlements)
    ) + createRuinModifiers(
        values = ruins,
    ) + if (currentSettlement == null) {
        emptyList()
    } else {
        listOfNotNull(
            createSecondaryTerritoryPenalty(currentSettlement = currentSettlement.settlement)
        )
    } + listOfNotNull(
        createUnrestModifier(unrest = unrest)
    ) + createVacancyModifiers(
        vacancies = vacancies
    ) + if (targetedArmy == null) {
        emptyList()
    } else {
        createArmyConditionPenalties(info = targetedArmy)
    } + listOf(createSupernaturalSolutionModifier()) + featModifiers + featureModifiers + createAnarchyPenalty() + createRepairBonus() + eventModifiers