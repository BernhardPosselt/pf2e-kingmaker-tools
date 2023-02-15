import {getLevelData, Leaders, Ruin, SkillRanks} from './data/kingdom';
import {capitalize} from '../utils';
import {Ability, calculateAbilityModifier} from './data/abilities';
import {allSkills, Skill} from './data/skills';
import {abilityRuins} from './data/ruin';
import {AbilityScores, Activity, KingdomPhase, skillAbilities} from './data/activities';
import {calculateUnrestPenalty} from './data/unrest';
import {isInvested} from './data/leaders';
import {calculateModifiers, disableModifiers, Modifier, ModifierTotals} from './modifiers';


interface SkillStats {
    skill: Skill;
    skillLabel: string;
    ability: Ability;
    abilityLabel: string;
    rank: number;
    total: ModifierTotals;
}

function createVacancyModifier(value: number, rulerVacant: boolean, phase?: KingdomPhase): Modifier {
    return {
        name: 'Vacancy',
        value: -(rulerVacant ? value + 1 : value),
        phases: phase ? [phase] : undefined,
        type: 'vacancy',
        enabled: true,
    };
}

export function createVacancyModifiers(
    ability: Ability,
    leaders: Leaders,
): Modifier[] {
    const modifiers = [];
    const rulerVacant = leaders.ruler.vacant;
    if (leaders.counselor.vacant && ability === 'culture') {
        modifiers.push(createVacancyModifier(1, rulerVacant));
    }
    if (leaders.general.vacant) {
        modifiers.push(createVacancyModifier(4, rulerVacant, 'warfare'));
    }
    if (leaders.emissary.vacant && ability === 'loyalty') {
        modifiers.push(createVacancyModifier(1, rulerVacant));
    }
    if (leaders.magister.vacant) {
        modifiers.push(createVacancyModifier(4, rulerVacant, 'warfare'));
    }
    if (leaders.treasurer.vacant && ability === 'economy') {
        modifiers.push(createVacancyModifier(1, rulerVacant));
    }
    if (leaders.viceroy.vacant && ability === 'stability') {
        modifiers.push(createVacancyModifier(1, rulerVacant));
    }
    if (leaders.warden.vacant) {
        modifiers.push(createVacancyModifier(4, rulerVacant, 'region'));
    }
    if (rulerVacant) {
        modifiers.push(createVacancyModifier(0, rulerVacant));
    }
    return modifiers;
}

function createInvestedModifier(
    kingdomLevel: number,
    ability: Ability,
    leaders: Leaders,
): Modifier | undefined {
    if (isInvested(ability, leaders)) {
        return {
            value: getLevelData(kingdomLevel).investedLeadershipBonus,
            enabled: true,
            name: 'Invested Leadership Role',
            type: 'status',
        };
    }
}

function createAbilityModifier(ability: Ability, abilityScores: AbilityScores): Modifier {
    return {
        type: 'ability',
        name: capitalize(ability),
        enabled: true,
        value: calculateAbilityModifier(abilityScores[ability]),
    };
}

function rankToLabel(rank: number): string {
    if (rank === 0) {
        return 'Untrained';
    } else if (rank === 1) {
        return 'Trained';
    } else if (rank === 2) {
        return 'Expert';
    } else if (rank === 3) {
        return 'Master';
    } else {
        return 'Legendary';
    }
}

function createProficiencyModifier(rank: number, alwaysAddLevel: boolean, kingdomLevel: number): Modifier {
    const value = rank > 0 ? (kingdomLevel + rank * 2) : (alwaysAddLevel ? kingdomLevel : 0);
    const name = rank > 0 ? rankToLabel(rank) : 'Kingdom Level';
    return {
        value,
        enabled: true,
        name,
        type: 'proficiency',
    };
}

function createUnrestModifier(unrest: number): Modifier | undefined {
    const penalty = calculateUnrestPenalty(unrest);
    if (penalty > 0) {
        return {
            type: 'status',
            name: 'Unrest',
            enabled: true,
            value: -penalty,
        };
    }
}

function createRuinModifier(ability: Ability, ruin: Ruin): Modifier | undefined {
    const ruinAbility = abilityRuins[ability];
    const itemPenalty = ruin[ruinAbility].penalty;
    if (itemPenalty > 0) {
        return {
            value: -itemPenalty,
            enabled: true,
            name: 'Ruin',
            type: 'item',
        };
    }
}

function createStructureModifiers(): Modifier[] {
    // TODO
    return [];
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
        ability,
        activity,
        phase,
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
        activity?: Activity,
        phase?: boolean,
    }
): Modifier[] {
    const abilityModifier = createAbilityModifier(ability, abilityScores);
    const proficiencyModifier = createProficiencyModifier(skillRank, alwaysAddLevel, kingdomLevel);
    const vacancyModifiers = createVacancyModifiers(ability, leaders);
    // status bonus
    const investedModifier = createInvestedModifier(kingdomLevel, ability, leaders);
    // item bonus
    const structureModifiers = createStructureModifiers();
    // TODO: circumstance bonus
    // status penalty
    const unrestModifier = createUnrestModifier(unrest);
    // item penalty
    const ruinModifier = createRuinModifier(ability, ruin);
    // TODO: circumstance penalty
    const result = [
        abilityModifier,
        proficiencyModifier,
        ...vacancyModifiers,
        ...structureModifiers,
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
    return result;
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
    }: {
        ruin: Ruin,
        unrest: number,
        skillRanks: SkillRanks,
        abilityScores: AbilityScores,
        kingdomLevel: number,
        leaders: Leaders,
        alwaysAddLevel: boolean,
    }
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
            skill,
            ability,
        });
        const enabledModifiers = disableModifiers(modifiers);
        const total = calculateModifiers(enabledModifiers);
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
