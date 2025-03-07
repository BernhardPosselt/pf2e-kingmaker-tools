export const allRuins = [
    'corruption',
    'crime',
    'decay',
    'strife',
] as const;

export type Ruin = typeof allRuins[number];
