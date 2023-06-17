export const allCampingActivities = [
    'Camouflage Campsite',
    'Cook Meal',
    'Discover Special Meal',
    'Hunt and Gather',
    'Learn from a Companion',
    'Organize Watch',
    'Relax',
    'Tell Campfire Story',
    'Blend Into the Night',
    'Bolster Confidence',
    'Camp Management',
    'Dawnflower\'s Blessing',
    'Enhance Campfire',
    'Enhance Weapons',
    'Intimidating Posture',
    'Maintain Armor',
    'Set Alarms',
    'Set Traps',
    'Undead Guardians',
    'Water Hazards',
    'Wilderness Survival',
    'Influence Amiri',
    'Discover Amiri',
    'Influence Ekundayo',
    'Discover Ekundayo',
    'Influence Jubilost',
    'Discover Jubilost',
    'Influence Linzi',
    'Discover Linzi',
    'Influence Nok-Nok',
    'Discover Nok-Nok',
    'Influence Tristian',
    'Discover Tristian',
    'Influence Valerie',
    'Discover Valerie',
    'Influence Harrim',
    'Discover Harrim',
    'Influence Jaethal',
    'Discover Jaethal',
    'Influence Kalikke',
    'Discover Kalikke',
    'Influence Kanerah',
    'Discover Kanerah',
    'Influence Octavia',
    'Discover Octavia',
    'Influence Regongar',
    'Discover Regongar',
] as const;

export type CampingActivityName = typeof allCampingActivities[number];


export interface CampingActivity {
    name: CampingActivityName;
    actorUuid: string | null;
}

export interface Camping {
    actorUuids: string[];
    prepareCamp: {
        actorUuid: string | null;
    };
    campingActivities: CampingActivity[];
}

export function getDefaultConfiguration(): Camping {
    return {
        actorUuids: [],
        campingActivities: allCampingActivities.map(a => {
            return {
                actorUuid: null,
                name: a,
            };
        }),
        prepareCamp: {
            actorUuid: null,
        },
    };
}

// TODO: fixme
export function getCampingActivities(): any[] {
    // TODO: Provide Aid, influencing
    return [{
        name: 'Camouflage Campsite',
        skills: [{
            skill: 'stealth',
            proficiency: 'trained',
            dc: 'zone',
        }],
    }, {
        name: 'Cook Meal',
    }, {
        name: 'Discover Special Meal',
        skills: [{
            skill: 'cooking',
            proficiency: 'trained',
            dc: 'meal',
        }],
    }, {
        name: 'Hunt and Gather',
        skills: [{
            proficiency: 'trained',
            skill: 'survival',
            dc: 'zone',
        }, {
            proficiency: 'trained',
            skill: 'hunting',
            dc: 'zone',
        }],
    }, {
        name: 'Learn from a Companion',
        skills: [{
            skill: 'perception',
            dc: 20,
        }],
    }, {
        name: 'Organize Watch',
        skills: [{
            skill: 'perception',
            proficiency: 'expert',
            dc: 20,
        }],
    }, {
        name: 'Relax',
    }, {
        name: 'Tell Campfire Story',
        skills: [{
            skill: 'performance',
            dc: 'actorLevel',
        }],
    }, {
        // increase night encounter dc by 2
        name: 'Blend Into the Night',
        locked: true,
    }, {
        name: 'Bolster Confidence',
        locked: true,
        // add effect
    }, {
        name: 'Camp Management',
        locked: true,
        skills: [{
            skill: 'survival',
            dc: 'zone',
        }],
    }, {
        name: 'Dawnflower\'s Blessing',
        locked: true,
        // gain min(constitution modifier, 1) * level hp after rest
    }, {
        name: 'Enhance Campfire',
        locked: true,
        // add effect
    }, {
        // add effect
        name: 'Enhance Weapons',
        locked: true,
    }, {
        name: 'Intimidating Posture',
        locked: true,
    }, {
        name: 'Maintain Armor',
        locked: true,
    }, {
        name: 'Set Alarms',
        locked: true,
    }, {
        name: 'Set Traps',
        locked: true,
    }, {
        name: 'Undead Guardians',
        locked: true,
    }, {
        name: 'Water Hazards',
        locked: true,
    }, {
        // add effect
        name: 'Wilderness Survival',
        locked: true,
    }];
}
