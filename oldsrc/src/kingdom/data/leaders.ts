import {Ability} from './abilities';
import {getLevelData, Leaders} from './kingdom';

export const allLeaders = [
    'ruler',
    'counselor',
    'general',
    'emissary',
    'magister',
    'treasurer',
    'viceroy',
    'warden',
] as const;

export type Leader = typeof allLeaders[number];

export const leaderAbilities: Record<Leader, Ability> = {
    ruler: 'loyalty',
    counselor: 'culture',
    general: 'stability',
    emissary: 'loyalty',
    magister: 'culture',
    treasurer: 'economy',
    viceroy: 'economy',
    warden: 'stability',
};

export const abilityLeaders: Record<Ability, [Leader, Leader]> = {
    loyalty: ['ruler', 'emissary'],
    culture: ['counselor', 'magister'],
    stability: ['general', 'warden'],
    economy: ['treasurer', 'viceroy'],
};

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
