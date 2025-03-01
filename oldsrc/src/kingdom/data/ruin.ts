import {Ability} from './abilities';

export const allRuins = [
    'corruption',
    'crime',
    'decay',
    'strife',
] as const;

export type Ruin = typeof allRuins[number];

export const abilityRuins: Record<Ability, Ruin> = {
    culture: 'corruption',
    economy: 'crime',
    stability: 'decay',
    loyalty: 'strife',
};
