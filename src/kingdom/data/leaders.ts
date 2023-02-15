import {Ability} from './abilities';

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
