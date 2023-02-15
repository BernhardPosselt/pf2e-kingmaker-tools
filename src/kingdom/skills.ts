import {getLevelData, Leaders, Ruin, SkillRanks} from './data/kingdom';
import {capitalize} from '../utils';
import {Ability} from './data/abilities';
import {allSkills, Skill} from './data/skills';
import {abilityLeaders} from './data/leaders';
import {abilityRuins} from './data/ruin';
import {AbilityScores, skillAbilities} from './data/activities';

export function calculateAbilityModifier(score: number): number {
    return Math.floor((score - 10) / 2);
}

export function calculateUnrestPenalty(unrest: number): number {
    if (unrest < 1) {
        return 0;
    } else if (unrest < 5) {
        return 1;
    } else if (unrest < 10) {
        return 2;
    } else if (unrest < 15) {
        return 3;
    } else {
        return 4;
    }
}

interface SkillStats {
    skill: Skill;
    skillLabel: string;
    ability: Ability;
    abilityLabel: string;
    rank: number;
    abilityBonus: number;
    proficiencyBonus: number;
    statusBonus: number;
    circumstanceBonus: number;
    itemBonus: number;
    statusPenalty: number;
    circumstancePenalty: number;
    itemPenalty: number;
    vacancyPenalty: number;
    value: number;

}

function calculateVacancy(
    ability: Ability,
    leaders: Leaders,
    isRegionActivity: boolean,
    isWarfareActivity: boolean,
): number {
    const rulerVacancyPenalty = leaders.ruler.vacant ? 1 : 0;
    if (leaders.counselor.vacant && ability === 'culture') {
        return 1 + rulerVacancyPenalty;
    } else if (leaders.general.vacant && isWarfareActivity) {
        return 4 + rulerVacancyPenalty;
    } else if (leaders.emissary.vacant && ability === 'loyalty') {
        return 1 + rulerVacancyPenalty;
    } else if (leaders.magister.vacant && isWarfareActivity) {
        return 4 + rulerVacancyPenalty;
    } else if (leaders.treasurer.vacant && ability === 'economy') {
        return 1 + rulerVacancyPenalty;
    } else if (leaders.viceroy.vacant && ability === 'stability') {
        return 1 + rulerVacancyPenalty;
    } else if (leaders.warden.vacant && isRegionActivity) {
        return 4 + rulerVacancyPenalty;
    } else {
        return rulerVacancyPenalty;
    }
}

export function isInvested(ability: Ability, leaders: Leaders): boolean {
    const relevantLeaders = abilityLeaders[ability];
    return leaders[relevantLeaders[0]].invested || leaders[relevantLeaders[1]].invested;
}

export function calculateInvestedBonus(
    kingdomLevel: number,
    ability: Ability,
    leaders: Leaders,
): number {
    const levelData = getLevelData(kingdomLevel);
    return isInvested(ability, leaders) ? levelData.investedLeadershipBonus : 0;
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
    const isRegionActivity = false; // TODO
    const isWarfareActivity = false; // TODO
    return allSkills.map(skill => {
        const circumstanceBonus = 0;
        const itemBonus = 0;
        const circumstancePenalty = 0;

        const ability = skillAbilities[skill];
        const investedBonus = calculateInvestedBonus(kingdomLevel, ability, leaders);
        const vacancyPenalty = calculateVacancy(ability, leaders, isRegionActivity, isWarfareActivity);
        const statusBonus = investedBonus;
        const rank = skillRanks[skill];
        const ruinAbility = abilityRuins[ability];
        const abilityBonus = calculateAbilityModifier(abilityScores[ability]);
        const proficiencyBonus = rank > 0 ? (kingdomLevel + rank * 2) : (alwaysAddLevel ? kingdomLevel : 0);
        const itemPenalty = ruin[ruinAbility].penalty;
        const statusPenalty = calculateUnrestPenalty(unrest);
        const value = (abilityBonus + proficiencyBonus + statusBonus + circumstanceBonus + itemBonus) -
            (statusPenalty + circumstancePenalty + itemPenalty + vacancyPenalty);
        return {
            skill,
            skillLabel: capitalize(skill),
            ability,
            abilityLabel: capitalize(ability),
            rank,
            abilityBonus,
            statusBonus,
            proficiencyBonus,
            circumstanceBonus,
            itemBonus,
            statusPenalty,
            circumstancePenalty,
            itemPenalty,
            vacancyPenalty,
            value,
        };
    });
}
