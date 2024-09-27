export const allAbilities = [
    'culture',
    'economy',
    'loyalty',
    'stability',
] as const;

export type Ability = typeof allAbilities[number];

export function calculateAbilityModifier(score: number): number {
    return Math.floor((score - 10) / 2);
}

export type AbilityScores = Record<Ability, number>;
