export const allAbilities = [
    'culture',
    'economy',
    'loyalty',
    'stability',
] as const;

export type Ability = typeof allAbilities[number];
