import {Leaders, Ruin, SkillRanks} from './data/kingdom';
import {capitalize} from '../utils';
import {Ability, AbilityScores} from './data/abilities';
import {allSkills, Skill, skillAbilities} from './data/skills';
import {Activity, KingdomPhase} from './data/activities';
import {
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


interface SkillStats {
    skill: Skill;
    skillLabel: string;
    ability: Ability;
    abilityLabel: string;
    rank: number;
    total: ModifierTotals;
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
        alwaysAddLevel,
        skillItemBonus,
        ability,
        activity,
        phase,
        additionalModifiers = [],
        overrides = {},
    }: {
        skill: Skill,
        ability: Ability,
        ruin: Ruin,
        unrest: number,
        skillRank: number,
        abilityScores: AbilityScores,
        kingdomLevel: number,
        leaders: Leaders,
        alwaysAddLevel: boolean,
        skillItemBonus?: SkillItemBonus,
        activity?: Activity,
        phase?: KingdomPhase,
        additionalModifiers?: Modifier[],
        overrides?: Record<string, boolean>;
    },
): Modifier[] {
    const abilityModifier = createAbilityModifier(ability, abilityScores);
    const proficiencyModifier = createProficiencyModifier(skillRank, alwaysAddLevel, kingdomLevel);
    const vacancyModifiers = createVacancyModifiers(ability, leaders);
    // status bonus
    const investedModifier = createInvestedModifier(kingdomLevel, ability, leaders);
    // item bonus
    const structureModifiers = skillItemBonus ? createStructureModifiers(skillItemBonus) : [];
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
    return processModifiers({
        modifiers: result,
        skill,
        rank: skillRank,
        phase,
        activity,
        overrides,
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
        alwaysAddLevel,
        skillItemBonuses,
        additionalModifiers,
    }: {
        ruin: Ruin,
        unrest: number,
        skillRanks: SkillRanks,
        abilityScores: AbilityScores,
        kingdomLevel: number,
        leaders: Leaders,
        alwaysAddLevel: boolean,
        skillItemBonuses?: SkillItemBonuses,
        additionalModifiers?: Modifier[],
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
            alwaysAddLevel,
            ability,
            skillItemBonus: skillItemBonuses?.[skill],
            skill,
            additionalModifiers,
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
