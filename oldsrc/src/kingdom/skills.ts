import {LeaderKingdomSkills, Leaders, LeaderSkills, Ruin, SkillRanks} from './data/kingdom';
import {capitalize} from '../utils';
import {Ability, AbilityScores} from './data/abilities';
import {allSkills, Skill, skillAbilities} from './data/skills';
import {KingdomPhase} from './data/activities';
import {
    createLeadershipModifier,
    calculateModifiers,
    createAbilityModifier,
    createInvestedModifier,
    createProficiencyModifier,
    createRuinModifier,
    createStructureModifiers,
    createUnrestModifier,
    createVacancyModifiers,
    Modifier,
    ModifierTotals,
    processModifiers,
    UntrainedProficiencyMode,
} from './modifiers';
import {SkillItemBonus, SkillItemBonuses} from './data/structures';
import {KingdomActivityById} from './data/activityData';
import {Leader} from "./data/leaders";


interface SkillStats {
    skill: Skill;
    skillLabel: string;
    ability: Ability;
    abilityLabel: string;
    rank: number;
    total: ModifierTotals;
}

export const allLeaderTypes = ['pc', 'regularNpc', 'highlyMotivatedNpc', 'nonPathfinderNpc'] as const;

export type LeadershipLeaderType = typeof allLeaderTypes[number];

export interface LeaderPerformingCheck {
    type: LeadershipLeaderType;
    level: number;
    position: Leader;
    skillRanks: Record<string, number>;
}

export function createSkillModifiers(
    {
        skill,
        ruin,
        unrest,
        skillRank,
        abilityScores,
        leaders,
        kingdomLevel,
        untrainedProficiencyMode,
        skillItemBonus,
        ability,
        activity,
        phase,
        additionalModifiers = [],
        overrides = {},
        activities,
        useLeadershipModifiers,
        leaderKingdomSkills,
        leaderSkills,
        currentLeader,
    }: {
        skill: Skill,
        ability: Ability,
        ruin: Ruin,
        unrest: number,
        skillRank: number,
        abilityScores: AbilityScores,
        kingdomLevel: number,
        leaders: Leaders,
        untrainedProficiencyMode: UntrainedProficiencyMode,
        skillItemBonus?: SkillItemBonus,
        activity?: string,
        phase?: KingdomPhase,
        additionalModifiers?: Modifier[],
        overrides?: Record<string, boolean>;
        activities: KingdomActivityById;
        useLeadershipModifiers: boolean;
        currentLeader?: LeaderPerformingCheck;
        leaderKingdomSkills: LeaderKingdomSkills;
        leaderSkills: LeaderSkills;
    },
): Modifier[] {
    const abilityModifier = createAbilityModifier(ability, abilityScores);
    const proficiencyModifier = createProficiencyModifier(skillRank, untrainedProficiencyMode, kingdomLevel);
    const vacancyModifiers = createVacancyModifiers(ability, leaders);
    // status bonus
    const investedModifier = createInvestedModifier(kingdomLevel, ability, leaders, useLeadershipModifiers);
    // leadership bonus
    const leadershipModifier = createLeadershipModifier(currentLeader, skill, leaderKingdomSkills, leaderSkills, useLeadershipModifiers);
    // item bonus
    const structureModifiers = skillItemBonus ? createStructureModifiers(skillItemBonus, activities) : [];
    // status penalty
    const unrestModifier = createUnrestModifier(unrest);
    // item penalty
    const ruinModifier = createRuinModifier(ability, ruin);
    const result = [
        abilityModifier,
        proficiencyModifier,
        ...vacancyModifiers,
        ...structureModifiers,
        ...additionalModifiers,
    ];
    if (ruinModifier) {
        result.push(ruinModifier);
    }
    if (unrestModifier) {
        result.push(unrestModifier);
    }
    if (investedModifier) {
        result.push(investedModifier);
    }
    if (leadershipModifier) {
        result.push(leadershipModifier);
    }
    return processModifiers({
        modifiers: result,
        skill,
        rank: skillRank,
        phase,
        activity,
        overrides,
        activities,
    });
}

export function calculateSkills(
    {
        ruin,
        unrest,
        skillRanks,
        abilityScores,
        leaders,
        kingdomLevel,
        untrainedProficiencyMode,
        skillItemBonuses,
        additionalModifiers,
        activities,
        useLeadershipModifiers,
        leaderKingdomSkills,
        leaderSkills,
        currentLeader,
    }: {
        ruin: Ruin,
        unrest: number,
        skillRanks: SkillRanks,
        abilityScores: AbilityScores,
        kingdomLevel: number,
        leaders: Leaders,
        untrainedProficiencyMode: UntrainedProficiencyMode,
        skillItemBonuses?: SkillItemBonuses,
        additionalModifiers?: Modifier[],
        activities: KingdomActivityById,
        currentLeader?: LeaderPerformingCheck;
        leaderKingdomSkills: LeaderKingdomSkills;
        leaderSkills: LeaderSkills;
        useLeadershipModifiers: boolean;
    },
): SkillStats[] {
    return allSkills.map(skill => {
        const ability = skillAbilities[skill];
        const modifiers = createSkillModifiers({
            ruin,
            unrest,
            skillRank: skillRanks[skill],
            abilityScores,
            leaders,
            kingdomLevel,
            untrainedProficiencyMode,
            ability,
            skillItemBonus: skillItemBonuses?.[skill],
            skill,
            additionalModifiers,
            activities,
            useLeadershipModifiers,
            leaderKingdomSkills,
            leaderSkills,
            currentLeader,
        });
        const total = calculateModifiers(modifiers);
        return {
            skill,
            rank: skillRanks[skill],
            ability,
            skillLabel: capitalize(skill),
            abilityLabel: capitalize(ability),
            total,
        };
    });
}
