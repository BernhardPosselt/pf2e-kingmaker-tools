package at.posselt.pfrpg2e.kingdom.modifiers.evaluation

import at.posselt.pfrpg2e.data.kingdom.KingdomAbilityScores
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.Ruins
import at.posselt.pfrpg2e.data.kingdom.leaders.InvestedLeaders
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderActorTypes
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderKingdomSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderLevels
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkillRanks
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderSkills
import at.posselt.pfrpg2e.data.kingdom.leaders.Vacancies
import at.posselt.pfrpg2e.data.kingdom.settlements.Settlement
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createAbilityModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createAllProficiencyModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createInvestedBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createLeaderEventBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createLeadershipModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createRulerBonus
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createStructureBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.bonuses.createStructureEventBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createArmyConditionPenalties
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createFavoredLandPenalty
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createRuinModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createSecondaryTerritoryPenalty
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createUnrestModifier
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.createVacancyModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.noBridgePenalty

fun createAssuranceModifiers(
    kingdomSkillRanks: KingdomSkillRanks,
    kingdomLevel: Int,
    untrainedProficiencyMode: UntrainedProficiencyMode,
) = createAllProficiencyModifiers(
    ranks = kingdomSkillRanks,
    level = kingdomLevel,
    mode = untrainedProficiencyMode,
)

fun createAllModifiers(
    kingdomLevel: Int,
    globalBonuses: GlobalStructureBonuses,
    activeSettlement: MergedSettlement,
    investedLeaders: InvestedLeaders,
    abilityScores: KingdomAbilityScores,
    leaderLevels: LeaderLevels,
    leaderActorTypes: LeaderActorTypes,
    leaderSkills: LeaderSkills,
    leaderSkillRanks: LeaderSkillRanks,
    leaderKingdomSkills: LeaderKingdomSkills,
    kingdomSkillRanks: KingdomSkillRanks,
    allSettlements: List<Settlement>,
    ruins: Ruins,
    unrest: Int,
    vacancies: Vacancies,
    targetedArmy: ArmyConditionInfo?,
    untrainedProficiencyMode: UntrainedProficiencyMode,
    enableLeadershipBonuses: Boolean,
    featModifiers: List<Modifier>,
    featureModifiers: List<Modifier>,
    customModifiers: List<Modifier>,
): List<Modifier> =
    listOfNotNull(
        createRulerBonus(global = globalBonuses)
    ) + createStructureBonuses(
        mergedSettlement = activeSettlement,
    ) + if (enableLeadershipBonuses) {
        createLeadershipModifiers(
            leaderLevels = leaderLevels,
            leaderActorTypes = leaderActorTypes,
            leaderSkills = leaderSkills,
            leaderSkillRanks = leaderSkillRanks,
            leaderKingdomSkills = leaderKingdomSkills,
        )
    } else {
        createInvestedBonuses(
            kingdomLevel = kingdomLevel,
            investedLeaders = investedLeaders,
        )
    } + listOfNotNull(
        createLeaderEventBonus(kingdomLevel = kingdomLevel)
    ) + listOfNotNull(
        createStructureEventBonuses(currentSettlement = activeSettlement.settlement)
    ) + createAbilityModifiers(
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
    ) + listOfNotNull(
        createSecondaryTerritoryPenalty(currentSettlement = activeSettlement.settlement)
    ) + listOfNotNull(
        createUnrestModifier(unrest = unrest)
    ) + createVacancyModifiers(
        vacancies = vacancies
    ) + if (targetedArmy == null) {
        emptyList()
    } else {
        createArmyConditionPenalties(info = targetedArmy)
    } + featModifiers + featureModifiers + customModifiers