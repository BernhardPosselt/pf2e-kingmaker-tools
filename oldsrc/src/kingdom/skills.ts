import {Kingdom} from './data/kingdom';
import {capitalize} from '../utils';
import {Ability} from './data/abilities';
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
        kingdom,
        skill,
        skillItemBonus,
        ability,
        activity,
        phase,
        additionalModifiers = [],
        overrides = {},
        activities,
        currentLeader,
        flags,
    }: {
        skill: Skill,
        ability: Ability,
        skillItemBonus?: SkillItemBonus,
        activity?: string,
        phase?: KingdomPhase,
        additionalModifiers?: Modifier[],
        overrides?: Record<string, boolean>;
        activities: KingdomActivityById;
        currentLeader?: LeaderPerformingCheck;
        kingdom: Kingdom;
        flags: string[];
    },
): Modifier[] {
    const abilityScores = kingdom.abilityScores;
    const leaders = kingdom.leaders;
    const kingdomLevel = kingdom.level;
    const ruin = kingdom.ruin;
    const unrest = kingdom.unrest;
    const skillRank = kingdom.skillRanks[skill];
    const untrainedProficiencyMode = kingdom.settings.proficiencyMode;
    const abilityModifier = createAbilityModifier(ability, abilityScores);
    const proficiencyModifier = createProficiencyModifier(skillRank, untrainedProficiencyMode, kingdomLevel);
    const leaderSkills = kingdom.settings.leaderSkills;
    const leaderKingdomSkills = kingdom.settings.leaderKingdomSkills;
    const useLeadershipModifiers = kingdom.settings.enableLeadershipModifiers;
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
        kingdom,
        flags,
    });
}

export function calculateSkills(
    {
        skillItemBonuses,
        additionalModifiers,
        activities,
        currentLeader,
        kingdom,
        flags,
    }: {
        skillItemBonuses?: SkillItemBonuses,
        additionalModifiers?: Modifier[],
        activities: KingdomActivityById,
        currentLeader?: LeaderPerformingCheck;
        kingdom: Kingdom;
        flags: string[];
    },
): SkillStats[] {
    return allSkills.map(skill => {
        const ability = skillAbilities[skill];
        const modifiers = createSkillModifiers({
            kingdom,
            ability,
            skillItemBonus: skillItemBonuses?.[skill],
            skill,
            additionalModifiers,
            activities,
            currentLeader,
            flags,
        });
        const total = calculateModifiers(modifiers);
        return {
            skill,
            rank: kingdom.skillRanks[skill],
            ability,
            skillLabel: capitalize(skill),
            abilityLabel: capitalize(ability),
            total,
        };
    });
}
