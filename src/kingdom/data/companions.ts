const allActorTypes = [
    'pc',
    'npc',
    'companion',
] as const;

export type ActorTypes = typeof allActorTypes[number];

export const allCompanions = [
    'Amiri',
    'Ekundayo',
    'Harrim',
    'Jaethal',
    'Jubilost',
    'Kalikke',
    'Kanerah',
    'Linzi',
    'Nok-Nok',
    'Octavia',
    'Regongar',
    'Tristian',
    'Valerie',
];
