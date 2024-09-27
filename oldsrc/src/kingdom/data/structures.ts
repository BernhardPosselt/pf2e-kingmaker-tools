import {Skill} from './skills';
import {Ruin} from './ruin';


export interface ActivityBonusRule {
    value: number;
    activity: string;
}

export interface SkillBonusRule {
    value: number;
    skill: Skill;
    // e.g. 'quell-unrest'
    activity?: string;
}

export interface AvailableItemsRule {
    value: number;
    // e.g. 'alchemical' or 'magic'
    group?: ItemGroup;
    maximumStacks?: number;
}

export interface SettlementEventsRule {
    value: number;
}

export interface LeadershipActivityRule {
    value: number;
}

export interface CommodityStorage {
    ore: number;
    food: number;
    lumber: number;
    stone: number;
    luxuries: number;
}

interface ConstructionSkill {
    skill: Skill;
    proficiencyRank?: number;
}

interface Construction {
    skills: ConstructionSkill[];
    lumber?: number;
    luxuries?: number;
    ore?: number;
    stone?: number;
    rp: number;
    dc: number;
}

interface ReduceUnrestBy {
    value: string;
    moreThanOncePerTurn?: boolean;
    note?: string;
}

interface ReduceRuinBy {
    value: string;
    ruin: Ruin | 'any';
    moreThanOncePerTurn?: boolean;
}

interface GainRuin {
    value: string;
    ruin: Ruin | 'any';
    moreThanOncePerTurn?: boolean;
}

export interface Structure {
    name: string;
    stacksWith?: string;
    construction?: Construction;
    notes?: string;
    preventItemLevelPenalty?: boolean;
    enableCapitalInvestment?: boolean,
    skillBonusRules?: SkillBonusRule[];
    activityBonusRules?: ActivityBonusRule[];
    availableItemsRules?: AvailableItemsRule[];
    settlementEventRules?: SettlementEventsRule[];
    leadershipActivityRules?: LeadershipActivityRule[];
    storage?: Partial<CommodityStorage>;
    increaseLeadershipActivities?: boolean;
    isBridge?: boolean;
    consumptionReduction?: number;
    unlockActivities?: string[];
    traits?: BuildingTrait[];
    lots: number;
    affectsEvents?: boolean;
    affectsDowntime?: boolean;
    reducesUnrest?: boolean;
    reducesRuin?: boolean;
    level?: number;
    upgradeFrom?: string[];
    reduceUnrestBy?: ReduceUnrestBy;
    reduceRuinBy?: ReduceRuinBy;
    gainRuin?: GainRuin;
}

export const allBuildingTraits = ['edifice', 'yard', 'building', 'famous', 'infamous', 'residential', 'infrastructure'];

export type BuildingTrait = typeof allBuildingTraits[number];

export type ActivityBonuses = Partial<Record<string, number>>;

export interface SkillItemBonus {
    value: number;
    activities: ActivityBonuses;
}

export type SkillItemBonuses = Record<Skill, SkillItemBonus>;

export type ItemGroup = 'divine'
    | 'alchemical'
    | 'primal'
    | 'occult'
    | 'arcane'
    | 'luxury'
    | 'magical'
    | 'other';

export const magicalItemGroups: ItemGroup[] = ['magical', 'divine', 'occult', 'primal', 'arcane'];

export const mundaneItemGroups: ItemGroup[] = ['alchemical', 'luxury', 'other'];
export const itemGroups: ItemGroup[] = mundaneItemGroups.concat(magicalItemGroups);

export const recoverArmyIds = [
    'recover-army-damaged',
    'recover-army-defeated',
    'recover-army-lost',
    'recover-army-mired-pinned',
    'recover-army-shaken',
    'recover-army-weary',
];

function recoverArmyBonus(value: number): ActivityBonusRule[] {
    return recoverArmyIds.map(activity => ({value, activity}));
}

export type ItemLevelBonuses = {
    alchemical: number;
    magical: number;
    divine: number;
    occult: number;
    primal: number;
    arcane: number;
    luxuryMagical: number;
    luxuryDivine: number;
    luxuryPrimal: number;
    luxuryArcane: number;
    luxuryOccult: number;
    other: number;
};

const structures: Structure[] = [
    {
        name: 'Academy',
        activityBonusRules: [{
            value: 2,
            activity: 'creative-solution',
        }],
        notes: 'While in a settlement with an Academy, you gain a +2 item bonus to Lore checks made to Recall Knowledge while Investigate, to all checks made while Researching, and to Decipher Writing.',
        traits: ['building', 'edifice'],
        affectsDowntime: true,
        level: 10,
        upgradeFrom: ['Library', 'Library (V&K)'],
        construction: {
            skills: [{
                skill: 'scholarship',
                proficiencyRank: 2,
            }],
            dc: 27,
            rp: 52,
            lumber: 12,
            luxuries: 6,
            stone: 12,
        },
        lots: 2,
    },
    {
        name: 'Alchemy Laboratory',
        activityBonusRules: [{
            value: 1,
            activity: 'demolish',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'alchemical',
            maximumStacks: 3,
        }],
        notes: 'Checks attempted to Identify Alchemy in any settlement with at least one alchemy laboratory gain a +1 item bonus.',
        traits: ['building'],
        affectsDowntime: true,
        level: 3,
        construction: {
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            rp: 18,
            ore: 2,
            stone: 5,
            dc: 16,
        },
        lots: 1,
    },
    {
        name: 'Arcanist\'s Tower',
        skillBonusRules: [{
            value: 1,
            skill: 'magic',
            activity: 'quell-unrest',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'arcane',
            maximumStacks: 3,
        }],
        notes: 'While in a settlement with an arcanist\'s tower, you gain a +1 item bonus to checks made to Borrow an Arcane Spell or Learn a Spell.',
        traits: ['building'],
        affectsDowntime: true,
        level: 5,
        construction: {
            skills: [{
                skill: 'magic',
                proficiencyRank: 1,
            }],
            rp: 30,
            stone: 6,
            dc: 20,
        },
        lots: 1,
    },
    {
        name: 'Arena',
        activityBonusRules: [{
            value: 2,
            activity: 'celebrate-holiday',
        }],
        skillBonusRules: [{
            value: 1,
            skill: 'warfare',
            activity: 'quell-unrest',
        }],
        notes: 'An arena lets you to retrain combat-themed feats more efficiently while in the settlement; doing so takes only 5 days rather than a week of downtime.',
        traits: ['edifice', 'yard'],
        affectsDowntime: true,
        level: 9,
        construction: {
            skills: [{
                skill: 'warfare',
                proficiencyRank: 2,
            }],
            dc: 26,
            rp: 40,
            lumber: 6,
            stone: 12,
        },
        lots: 4,
    },
    {
        name: 'Bank',
        activityBonusRules: [{
            value: 1,
            activity: 'tap-treasury',
        }],
        enableCapitalInvestment: true,
        traits: ['building'],
        level: 5,
        construction: {
            skills: [{
                skill: 'trade',
                proficiencyRank: 1,
            }],
            dc: 20,
            rp: 28,
            ore: 4,
            stone: 6,
        },
        lots: 1,
    },
    {
        name: 'Barracks',
        activityBonusRules: [{
            value: 1,
            activity: 'garrison-army',
        }, ...recoverArmyBonus(1), {
            value: 1,
            activity: 'recruit-army',
        }],
        traits: ['building', 'residential'],
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
            moreThanOncePerTurn: true,
        },
        level: 3,
        construction: {
            skills: [{
                skill: 'defense',
            }],
            dc: 16,
            rp: 6,
            lumber: 2,
            stone: 1,
        },
        lots: 1,
    },
    {
        name: 'Brewery',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
        traits: ['building'],
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
            moreThanOncePerTurn: true,
            note: 'as long as you have fewer than 4 breweries in the settlement at that time',
        },
        level: 1,
        construction: {
            skills: [{
                skill: 'agriculture',
            }],
            dc: 15,
            rp: 6,
            lumber: 2,
        },
        lots: 1,
    },
    {
        name: 'Bridge',
        isBridge: true,
        traits: ['infrastructure'],
        lots: 0,
        affectsDowntime: false,
        reducesUnrest: false,
        level: 2,
        construction: {
            skills: [{
                skill: 'engineering',
            }],
            dc: 16,
            rp: 6,
            lumber: 1,
        },
    },
    {
        name: 'Bridge, Stone',
        isBridge: true,
        traits: ['infrastructure'],
        lots: 0,
        affectsDowntime: false,
        reducesUnrest: false,
        level: 2,
        construction: {
            skills: [{
                skill: 'engineering',
            }],
            dc: 16,
            rp: 6,
            stone: 1,
        },
    },
    {
        name: 'Castle',
        activityBonusRules: [{
            value: 2,
            activity: 'new-leadership',
        }, {
            value: 2,
            activity: 'pledge-of-fealty',
        }, {
            value: 2,
            activity: 'send-diplomatic-envoy',
        }, {
            value: 2,
            activity: 'garrison-army',
        }, {
            value: 2,
            activity: 'recruit-army',
        }, ...recoverArmyBonus(2)],
        increaseLeadershipActivities: true,
        traits: ['building', 'edifice', 'famous', 'infamous'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1d4',
        },
        level: 9,
        upgradeFrom: ['Town Hall', 'Town Hall (V&K)'],
        construction: {
            skills: [{
                skill: 'defense',
                proficiencyRank: 2,
            }, {
                skill: 'industry',
                proficiencyRank: 2,
            }, {
                skill: 'magic',
                proficiencyRank: 2,
            }, {
                skill: 'statecraft',
                proficiencyRank: 2,
            }],
            rp: 54,
            dc: 26,
            lumber: 12,
            stone: 12,
        },
        lots: 4,
    },
    {
        name: 'Cathedral',
        activityBonusRules: [{
            value: 3,
            activity: 'celebrate-holiday',
        }, {
            value: 3,
            activity: 'provide-care',
        }, {
            value: 3,
            activity: 'repair-reputation-corruption',
        }],
        availableItemsRules: [{
            value: 3,
            group: 'divine',
            maximumStacks: 3,
        }],
        notes: 'While in a settlement with a cathedral, you gain a +3 item bonus to Lore and Religion checks made to Recall Knowledge while Investigating, and to all faith-themed checks made while Researching.',
        traits: ['building', 'edifice', 'famous', 'infamous'],
        affectsDowntime: true,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '4',
        },
        level: 15,
        upgradeFrom: ['Temple'],
        construction: {
            skills: [{
                skill: 'folklore',
                proficiencyRank: 3,
            }],
            rp: 58,
            dc: 34,
            lumber: 20,
            stone: 20,
        },
        lots: 4,
    },
    {
        name: 'Cemetery',
        traits: ['yard'],
        affectsEvents: true,
        affectsDowntime: false,
        reducesUnrest: true,
        level: 1,
        construction: {
            skills: [{
                skill: 'folklore',
            }],
            dc: 15,
            rp: 4,
            stone: 1,
        },
        lots: 1,
    },
    {
        name: 'Construction Yard',
        activityBonusRules: [{
            value: 1,
            activity: 'build-structure',
        }, {
            value: 1,
            activity: 'repair-reputation-decay',
        }],
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 10,
        construction: {
            skills: [{
                skill: 'engineering',
            }],
            dc: 27,
            rp: 40,
            lumber: 10,
            stone: 10,
        },
        lots: 4,
    },
    {
        name: 'Dump',
        activityBonusRules: [{
            value: 1,
            activity: 'demolish',
        }],
        traits: ['yard'],
        affectsEvents: true,
        affectsDowntime: false,
        reducesUnrest: false,
        level: 2,
        construction: {
            skills: [{
                skill: 'industry',
            }],
            dc: 16,
            rp: 4,
        },
        lots: 1,
    },
    {
        name: 'Embassy',
        activityBonusRules: [{
            value: 1,
            activity: 'send-diplomatic-envoy',
        }, {
            value: 1,
            activity: 'request-foreign-aid',
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 8,
        construction: {
            skills: [{
                skill: 'politics',
            }],
            dc: 24,
            rp: 26,
            lumber: 10,
            stone: 4,
            luxuries: 6,
        },
        lots: 2,
    },
    {
        name: 'Festival Hall',
        activityBonusRules: [{
            value: 1,
            activity: 'celebrate-holiday',
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            skills: [{
                skill: 'arts',
            }],
            dc: 18,
            rp: 7,
            lumber: 3,
        },
        lots: 1,
    },
    {
        name: 'Foundry',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-work-site-mine',
        }],
        storage: {
            ore: 1,
        },
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 18,
            rp: 16,
            lumber: 5,
            stone: 3,
            ore: 2,
        },
        lots: 2,
    },
    {
        name: 'Garrison',
        activityBonusRules: [{
            value: 1,
            activity: 'outfit-army',
        }, {
            value: 1,
            activity: 'train-army',
        }],
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            moreThanOncePerTurn: true,
            value: '1',
        },
        level: 5,
        upgradeFrom: ['Barracks'],
        construction: {
            skills: [{
                skill: 'warfare',
                proficiencyRank: 1,
            }],
            dc: 20,
            rp: 28,
            lumber: 6,
            stone: 3,
        },
        lots: 2,
    },
    {
        name: 'General Store',
        preventItemLevelPenalty: true,
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 1,
        construction: {
            skills: [{
                skill: 'trade',
            }],
            dc: 15,
            rp: 8,
            lumber: 1,
        },
        lots: 1,
    },
    {
        name: 'Gladiatorial Arena',
        activityBonusRules: [{
            value: 3,
            activity: 'celebrate-holiday',
        }, {
            value: 3,
            activity: 'hire-adventurers',
        }],
        skillBonusRules: [{
            value: 3,
            skill: 'warfare',
            activity: 'quell-unrest',
        }],
        notes: 'A gladiatorial arena allows a PC in the settlement to retrain combat-themed feats (at the GM\'s discretion) more efficiently; doing so takes only 4 days rather than a week of downtime.',
        traits: ['edifice', 'famous', 'infamous', 'yard'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 15,
        construction: {
            rp: 58,
            lumber: 10,
            stone: 30,
            skills: [{
                skill: 'warfare',
                proficiencyRank: 3,
            }],
            dc: 34,
        },
        lots: 4,
    },
    {
        name: 'Houses',
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 1,
        upgradeFrom: ['Tenement'],
        construction: {
            rp: 3,
            lumber: 1,
            skills: [{
                skill: 'industry',
            }],
            dc: 15,
        },
        lots: 1,
    },
    {
        name: 'Granary',
        storage: {
            food: 1,
        },
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 1,
        construction: {
            rp: 12,
            lumber: 2,
            skills: [{
                skill: 'agriculture',
            }],
            dc: 15,
        },
        lots: 1,
    },
    {
        name: 'Guildhall',
        notes: 'While in a settlement with a guildhall, you gain a +1 item bonus to all related skill checks to Earn Income or to Repair.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 5,
        construction: {
            rp: 34,
            lumber: 8,
            skills: [{
                skill: 'trade',
                proficiencyRank: 2,
            }],
            dc: 20,
        },
        upgradeFrom: ['Trade Shop'],
        lots: 2,
    },
    {
        name: 'Herbalist',
        activityBonusRules: [{
            value: 1,
            activity: 'provide-care',
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 1,
        construction: {
            rp: 10,
            lumber: 1,
            skills: [{
                skill: 'wilderness',
            }],
            dc: 15,
        },
        lots: 1,
    },
    {
        name: 'Hospital',
        activityBonusRules: [{
            value: 1,
            activity: 'provide-care',
        }, {
            value: 1,
            activity: 'quell-unrest',
        }],
        notes: 'While in a settlement with a hospital, you gain a +2 item bonus to Medicine checks to Treat Disease and Treat Wounds.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 9,
        upgradeFrom: ['Herbalist'],
        construction: {
            rp: 30,
            lumber: 10,
            stone: 6,
            skills: [{
                skill: 'defense',
                proficiencyRank: 2,
            }],
            dc: 26,
        },
        lots: 2,
    },
    {
        name: 'Illicit Market',
        activityBonusRules: [{
            value: 1,
            activity: 'clandestine-business',
        }],
        availableItemsRules: [{
            value: 1,
            maximumStacks: 3,
        }],
        traits: ['building', 'infamous'],
        gainRuin: {
            value: '1',
            ruin: 'crime',
            moreThanOncePerTurn: true,
        },
        affectsDowntime: false,
        reducesUnrest: false,
        level: 6,
        construction: {
            rp: 50,
            lumber: 5,
            skills: [{
                skill: 'intrigue',
                proficiencyRank: 1,
            }],
            dc: 22,
        },
        lots: 1,
    },
    {
        name: 'Inn',
        activityBonusRules: [{
            value: 1,
            activity: 'hire-adventurers',
        }],
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 1,
        construction: {
            rp: 10,
            lumber: 2,
            skills: [{
                skill: 'trade',
            }],
            dc: 15,
        },
        lots: 1,
    },
    {
        name: 'Jail',
        skillBonusRules: [{
            value: 1,
            skill: 'intrigue',
            activity: 'quell-unrest',
        }],
        reduceRuinBy: {
            value: '1',
            ruin: 'crime',
        },
        traits: ['building'],
        affectsDowntime: false,
        reducesRuin: true,
        level: 2,
        construction: {
            rp: 14,
            lumber: 4,
            stone: 4,
            ore: 2,
            skills: [{
                skill: 'defense',
            }],
            dc: 16,
        },
        lots: 1,
    },
    {
        name: 'Keep',
        activityBonusRules: [{
            value: 1,
            activity: 'deploy-army',
        }, {
            value: 1,
            activity: 'garrison-army',
        }, {
            value: 1,
            activity: 'train-army',
        }],
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 3,
        construction: {
            rp: 32,
            lumber: 8,
            stone: 8,
            skills: [{
                skill: 'defense',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 2,
    },
    {
        name: 'Library',
        skillBonusRules: [{
            value: 1,
            skill: 'scholarship',
            activity: 'rest-and-relax',
        }],
        notes: 'While in a settlement with a library, you gain a +1 item bonus to Lore checks made to Recall Knowledge while Investigating, as well as to Researching checks, and to Decipher Writing checks.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 2,
        construction: {
            rp: 6,
            lumber: 4,
            stone: 2,
            skills: [{
                skill: 'scholarship',
                proficiencyRank: 1,
            }],
            dc: 16,
        },
        lots: 1,
    },
    {
        name: 'Lumberyard',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-work-site-lumber',
        }],
        storage: {
            lumber: 1,
        },
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 16,
            lumber: 5,
            ore: 1,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 2,
    },
    {
        name: 'Luxury Store',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'luxury',
            maximumStacks: 3,
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 6,
        upgradeFrom: ['General Store'],
        construction: {
            rp: 28,
            lumber: 10,
            luxuries: 6,
            skills: [{
                skill: 'trade',
                proficiencyRank: 2,
            }],
            dc: 22,
        },
        lots: 1,
    },
    {
        name: 'Magic Shop',
        activityBonusRules: [{
            value: 1,
            activity: 'supernatural-solution',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'magical',
            maximumStacks: 3,
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 8,
        upgradeFrom: ['Luxury Store'],
        construction: {
            rp: 44,
            lumber: 8,
            stone: 6,
            luxuries: 6,
            skills: [{
                skill: 'magic',
                proficiencyRank: 2,
            }],
            dc: 24,
        },
        lots: 1,
    },
    {
        name: 'Magical Streetlamps',
        traits: ['infrastructure'],
        lots: 0,
        affectsDowntime: false,
        reducesRuin: true,
        reduceRuinBy: {
            value: '1',
            ruin: 'crime',
        },
        level: 5,
        construction: {
            rp: 20,
            skills: [{
                skill: 'magic',
                proficiencyRank: 2,
            }],
            dc: 20,
        },
    },
    {
        name: 'Mansion',
        activityBonusRules: [{
            value: 1,
            activity: 'improve-lifestyle',
        }],
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 5,
        upgradeFrom: ['Houses'],
        construction: {
            rp: 10,
            lumber: 6,
            stone: 3,
            luxuries: 6,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 20,
        },
        lots: 1,
    },
    {
        name: 'Marketplace',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
        preventItemLevelPenalty: true,
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 4,
        upgradeFrom: ['General Store'],
        construction: {
            rp: 48,
            lumber: 4,
            skills: [{
                skill: 'trade',
                proficiencyRank: 1,
            }],
            dc: 19,
        },
        lots: 2,
    },
    {
        name: 'Menagerie',
        skillBonusRules: [{
            value: 2,
            skill: 'wilderness',
            activity: 'rest-and-relax',
        }],
        notes: 'A menagerie typically contains a selection of level 5 or lower animals. If your party captures a living creature of level 6 or higher and can transport the creature back to a settlement with a menagerie, you can add that creature to the menagerie as long as your kingdom level is at least 4 higher than the creature\'s level. Each time such a creature is added to a menagerie, gain 1 Fame or Infamy point (as appropriate) or reduce one Ruin of your choice by 1.\n' +
            'Only creatures with Intelligence modifiers of –4 or –5 are appropriate to place in a menagerie. A kingdom gains 1 Unrest at the start of a Kingdom turn for each sapient creature (anything with an Intelligence modifier of –3 or higher) on display in a menagerie.',
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: false,
        reducesRuin: true,
        level: 12,
        upgradeFrom: ['Park'],
        construction: {
            rp: 26,
            lumber: 14,
            stone: 10,
            ore: 10,
            skills: [{
                skill: 'wilderness',
                proficiencyRank: 2,
            }],
            dc: 30,
        },
        lots: 4,
    },
    {
        name: 'Military Academy',
        activityBonusRules: [{
            value: 2,
            activity: 'train-army',
        }],
        skillBonusRules: [{
            value: 2,
            skill: 'warfare',
            activity: 'pledge-of-fealty',
        }],
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 12,
        upgradeFrom: ['Academy'],
        construction: {
            rp: 36,
            lumber: 12,
            stone: 10,
            ore: 6,
            skills: [{
                skill: 'warfare',
                proficiencyRank: 2,
            }],
            dc: 30,
        },
        lots: 2,
    },
    {
        name: 'Mill',
        activityBonusRules: [{
            value: 1,
            activity: 'harvest-crops',
        }],
        consumptionReduction: 1,
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 2,
        construction: {
            rp: 6,
            lumber: 2,
            stone: 1,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 16,
        },
        lots: 1,
    },
    {
        name: 'Mint',
        activityBonusRules: [{
            value: 3,
            activity: 'capital-investment',
        }, {
            value: 3,
            activity: 'collect-taxes',
        }, {
            value: 3,
            activity: 'repair-reputation-crime',
        }],
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 15,
        construction: {
            rp: 30,
            lumber: 12,
            stone: 16,
            ore: 20,
            skills: [{
                skill: 'trade',
                proficiencyRank: 3,
            }],
            dc: 34,
        },
        lots: 1,
    },
    {
        name: 'Monument',
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        reduceRuinBy: {
            value: '1',
            ruin: 'any',
        },
        level: 3,
        construction: {
            rp: 6,
            stone: 1,
            skills: [{
                skill: 'arts',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Museum',
        skillBonusRules: [{
            value: 1,
            skill: 'arts',
            activity: 'rest-and-relax',
        }],
        notes: 'A magic item of level 6 or higher that has a particular import or bears significant historical or regional value (at the GM\'s discretion) can be donated to a museum. Each time such an item is donated, reduce Unrest by 1. If that item is later removed from display, increase Unrest by 1.',
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: true,
        level: 5,
        construction: {
            rp: 30,
            lumber: 6,
            stone: 2,
            skills: [{
                skill: 'exploration',
                proficiencyRank: 1,
            }],
            dc: 20,
        },
        lots: 2,
    },
    {
        name: 'Noble Villa',
        activityBonusRules: [{
            value: 1,
            activity: 'improve-lifestyle',
        }],
        skillBonusRules: [{
            value: 1,
            skill: 'politics',
            activity: 'quell-unrest',
        }],
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '2',
        },
        level: 9,
        upgradeFrom: ['Mansion'],
        construction: {
            rp: 24,
            lumber: 10,
            stone: 8,
            luxuries: 6,
            skills: [{
                skill: 'politics',
                proficiencyRank: 2,
            }],
            dc: 19,
        },
        lots: 2,
    },
    {
        name: 'Occult Shop',
        activityBonusRules: [{
            value: 2,
            activity: 'prognostication',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'occult',
            maximumStacks: 3,
        }],
        notes: 'While in a settlement with an occult shop, you gain a +2 item bonus to all checks made to Research esoteric subjects or to Recall Knowledge about the same.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 13,
        construction: {
            rp: 68,
            lumber: 12,
            stone: 6,
            luxuries: 12,
            skills: [{
                skill: 'magic',
                proficiencyRank: 3,
            }],
            dc: 32,
        },
        lots: 1,
    },
    {
        name: 'Opera House',
        activityBonusRules: [{
            value: 3,
            activity: 'celebrate-holiday',
        }, {
            value: 3,
            activity: 'create-a-masterpiece',
        }],
        notes: 'While in a settlement with an opera house, you gain a +3 item bonus to Performance checks made to Earn Income.',
        traits: ['building', 'edifice', 'famous', 'infamous'],
        affectsDowntime: true,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '2',
        },
        level: 15,
        upgradeFrom: ['Theater'],
        construction: {
            rp: 40,
            lumber: 20,
            stone: 16,
            luxuries: 18,
            skills: [{
                skill: 'arts',
                proficiencyRank: 3,
            }],
            dc: 34,
        },
        lots: 2,
    },
    {
        name: 'Orphanage',
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 2,
        upgradeFrom: ['Houses'],
        construction: {
            rp: 6,
            lumber: 2,
            skills: [{
                skill: 'industry',
            }],
            dc: 16,
        },
        lots: 1,
    },
    {
        name: 'Palace',
        activityBonusRules: [{
            value: 3,
            activity: 'new-leadership',
        }, {
            value: 3,
            activity: 'pledge-of-fealty',
        }, {
            value: 3,
            activity: 'send-diplomatic-envoy',
        }, {
            value: 3,
            activity: 'garrison-army',
        }, ...recoverArmyBonus(3), {
            value: 3,
            activity: 'recruit-army',
        }],
        leadershipActivityRules: [{
            value: 3,
        }],
        increaseLeadershipActivities: true,
        traits: ['building', 'edifice', 'famous', 'infamous'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '10',
        },
        level: 15,
        upgradeFrom: ['Castle', 'Castle (V&K)'],
        construction: {
            rp: 108,
            lumber: 20,
            stone: 20,
            ore: 15,
            luxuries: 12,
            skills: [{
                skill: 'defense',
                proficiencyRank: 3,
            }, {
                skill: 'industry',
                proficiencyRank: 3,
            }, {
                skill: 'magic',
                proficiencyRank: 3,
            }, {
                skill: 'statecraft',
                proficiencyRank: 3,
            }],
            dc: 34,
        },
        lots: 4,
    },
    {
        name: 'Park',
        skillBonusRules: [{
            value: 1,
            skill: 'wilderness',
            activity: 'rest-and-relax',
        }],
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 3,
        construction: {
            rp: 5,
            skills: [{
                skill: 'wilderness',
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Paved Streets',
        traits: ['infrastructure'],
        lots: 0,
        affectsDowntime: false,
        reducesUnrest: false,
        level: 4,
        construction: {
            rp: 12,
            stone: 6,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 19,
        },
    },
    {
        name: 'Pier',
        activityBonusRules: [{
            value: 1,
            activity: 'go-fishing',
        }],
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 16,
            lumber: 2,
            skills: [{
                skill: 'boating',
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Rubble',
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 0,
        lots: 1,
    },
    {
        name: 'Printing House',
        activityBonusRules: [{
            value: 2,
            activity: 'improve-lifestyle',
        }, {
            value: 2,
            activity: 'quell-unrest',
        }],
        notes: 'A PC in a settlement with a printing house gains a +2 item bonus to checks to Gather Information or to Research any topic in a library or similar structure.',
        unlockActivities: ['read-all-about-it'],
        traits: ['building', 'edifice'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 10,
        construction: {
            rp: 48,
            lumber: 14,
            luxuries: 12,
            skills: [{
                skill: 'industry',
                proficiencyRank: 3,
            }],
            dc: 27,
        },
        lots: 2,
    },
    {
        name: 'Sacred Grove',
        skillBonusRules: [{
            value: 1,
            skill: 'folklore',
            activity: 'quell-unrest',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'primal',
            maximumStacks: 3,
        }],
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 5,
        construction: {
            rp: 36,
            skills: [{
                skill: 'wilderness',
                proficiencyRank: 1,
            }],
            dc: 20,
        },
        lots: 1,
    },
    {
        name: 'Secure Warehouse',
        activityBonusRules: [{
            value: 1,
            activity: 'craft-luxuries',
        }],
        storage: {
            luxuries: 1,
        },
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 6,
        construction: {
            rp: 24,
            lumber: 6,
            stone: 6,
            ore: 4,
            skills: [{
                skill: 'industry',
                proficiencyRank: 2,
            }],
            dc: 22,
        },
        lots: 2,
    },
    {
        name: 'Sewer System',
        activityBonusRules: [{
            value: 1,
            activity: 'clandestine-business',
        }],
        consumptionReduction: 1,
        traits: ['infrastructure'],
        lots: 0,
        affectsEvents: true,
        affectsDowntime: false,
        reducesUnrest: false,
        level: 7,
        construction: {
            rp: 24,
            lumber: 8,
            stone: 8,
            skills: [{
                skill: 'engineering',
                proficiencyRank: 2,
            }],
            dc: 23,
        },
    },
    {
        name: 'Shrine',
        activityBonusRules: [{
            value: 1,
            activity: 'celebrate-holiday',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'divine',
            maximumStacks: 3,
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 1,
        construction: {
            rp: 8,
            lumber: 2,
            stone: 1,
            skills: [{
                skill: 'folklore',
                proficiencyRank: 1,
            }],
            dc: 15,
        },
        lots: 1,
    },
    {
        name: 'Smithy',
        activityBonusRules: [{
            value: 1,
            activity: 'trade-commodities',
        }, {
            value: 1,
            activity: 'outfit-army',
        }],
        notes: 'While in a settlement with a smithy, you gain a +1 item bonus to Craft checks made to work with metal.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 8,
            lumber: 2,
            stone: 1,
            ore: 1,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Specialized Artisan',
        activityBonusRules: [{
            value: 1,
            activity: 'craft-luxuries',
        }],
        notes: 'While in a settlement with a specialized artisan, you gain a +1 item bonus to Craft checks made to craft specialized goods like jewelry.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 4,
        construction: {
            rp: 10,
            lumber: 4,
            luxuries: 1,
            skills: [{
                skill: 'trade',
                proficiencyRank: 2,
            }],
            dc: 19,
        },
        lots: 1,
    },
    {
        name: 'Stable',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 10,
            lumber: 2,
            skills: [{
                skill: 'wilderness',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Stockyard',
        activityBonusRules: [{
            value: 1,
            activity: 'gather-livestock',
        }],
        consumptionReduction: 1,
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 20,
            lumber: 4,
            skills: [{
                skill: 'industry',
            }],
            dc: 18,
        },
        lots: 4,
    },
    {
        name: 'Stonemason',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-work-site-quarry',
        }],
        storage: {
            stone: 1,
        },
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 16,
            lumber: 2,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 2,
    },
    {
        name: 'Tannery',
        activityBonusRules: [{
            value: 1,
            activity: 'trade-commodities',
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 6,
            lumber: 2,
            skills: [{
                skill: 'industry',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Tavern, Dive',
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 1,
        construction: {
            rp: 12,
            lumber: 1,
            skills: [{
                skill: 'trade',
                proficiencyRank: 1,
            }],
            dc: 15,
        },
        lots: 1,
    },
    {
        name: 'Tavern, Luxury',
        activityBonusRules: [{
            value: 2,
            activity: 'hire-adventurers',
        }],
        skillBonusRules: [{
            value: 2,
            skill: 'trade',
            activity: 'rest-and-relax',
        }],
        notes: 'If attempt a Performance check to Earn Income in a settlement with a luxury tavern, you gain a +2 item bonus to the check. All checks made to Gather Information in a settlement with at least one luxury tavern gain a +2 item bonus.',
        traits: ['building', 'famous'],
        affectsDowntime: true,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1d4+1',
        },
        level: 9,
        upgradeFrom: ['Tavern, Popular', 'Tavern, Popular (V&K)'],
        construction: {
            rp: 48,
            lumber: 10,
            stone: 8,
            luxuries: 8,
            skills: [{
                skill: 'trade',
                proficiencyRank: 3,
            }],
            dc: 26,
        },
        lots: 2,
    },
    {
        name: 'Tavern, Popular',
        activityBonusRules: [{
            value: 1,
            activity: 'hire-adventurers',
        }],
        skillBonusRules: [{
            value: 1,
            skill: 'trade',
            activity: 'rest-and-relax',
        }],
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a popular tavern, you gain a +1 item bonus to the check. All checks made to Gather Information in a settlement with at least one popular tavern gain a +1 item bonus.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '2',
        },
        level: 3,
        upgradeFrom: ['Tavern, Dive', 'Tavern, Dive (V&K)'],
        construction: {
            rp: 24,
            lumber: 6,
            stone: 2,
            skills: [{
                skill: 'trade',
                proficiencyRank: 2,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Tavern, World-Class',
        activityBonusRules: [{
            value: 3,
            activity: 'hire-adventurers',
        }, {
            value: 3,
            activity: 'repair-reputation-strife',
        }],
        skillBonusRules: [{
            value: 3,
            skill: 'trade',
            activity: 'rest-and-relax',
        }],
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a world-class tavern, you gain a +3 item bonus to the check. All checks made to Gather Information in a settlement with a world-class tavern gain a +3 item bonus.',
        traits: ['building', 'edifice', 'famous'],
        affectsDowntime: true,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '2d4',
        },
        level: 15,
        upgradeFrom: ['Tavern, Luxury', 'Tavern, Luxury (V&K)'],
        construction: {
            rp: 64,
            lumber: 18,
            stone: 15,
            luxuries: 15,
            skills: [{
                skill: 'trade',
                proficiencyRank: 3,
            }],
            dc: 34,
        },
        lots: 2,
    },
    {
        name: 'Temple',
        activityBonusRules: [{
            value: 1,
            activity: 'celebrate-holiday',
        }, {
            value: 1,
            activity: 'provide-care',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'divine',
            maximumStacks: 3,
        }],
        traits: ['building', 'famous', 'infamous'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '2',
        },
        level: 7,
        upgradeFrom: ['Shrine'],
        construction: {
            rp: 32,
            lumber: 6,
            stone: 6,
            skills: [{
                skill: 'folklore',
                proficiencyRank: 1,
            }],
            dc: 23,
        },
        lots: 2,
    },
    {
        name: 'Tenement',
        traits: ['building', 'residential'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        gainRuin: {
            value: '1',
            ruin: 'any',
            moreThanOncePerTurn: true,
        },
        level: 0,
        construction: {
            rp: 1,
            lumber: 1,
            skills: [{
                skill: 'industry',
            }],
            dc: 14,
        },
        lots: 1,
    },
    {
        name: 'Theater',
        activityBonusRules: [{
            value: 2,
            activity: 'celebrate-holiday',
        }],
        notes: 'While in a settlement with a theater, you gain a +2 item bonus to Performance checks made to Earn Income.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 9,
        upgradeFrom: ['Festival Hall', 'Festival Hall (V&K)'],
        construction: {
            rp: 24,
            lumber: 8,
            stone: 3,
            skills: [{
                skill: 'arts',
                proficiencyRank: 2,
            }],
            dc: 26,
        },
        lots: 2,
    },
    {
        name: 'Thieves\' Guild',
        activityBonusRules: [{
            value: 1,
            activity: 'infiltration',
        }],
        notes: 'While in a settlement with a thieves\' guild, you gain a +1 item bonus to Create Forgeries.',
        traits: ['building', 'infamous'],
        affectsDowntime: true,
        reducesUnrest: false,
        gainRuin: {
            value: '1',
            ruin: 'crime',
            moreThanOncePerTurn: true,
        },
        level: 5,
        construction: {
            rp: 25,
            lumber: 4,
            skills: [{
                skill: 'intrigue',
                proficiencyRank: 1,
            }],
            dc: 20,
        },
        lots: 1,
    },
    {
        name: 'Town Hall',
        increaseLeadershipActivities: true,
        traits: ['building', 'edifice'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 2,
        construction: {
            rp: 22,
            lumber: 4,
            stone: 4,
            skills: [{
                skill: 'defense',
                proficiencyRank: 1,
            }, {
                skill: 'industry',
                proficiencyRank: 1,
            }, {
                skill: 'magic',
                proficiencyRank: 1,
            }, {
                skill: 'statecraft',
                proficiencyRank: 1,
            }],
            dc: 16,
        },
        lots: 2,
    },
    {
        name: 'Trade Shop',
        activityBonusRules: [{
            value: 1,
            activity: 'purchase-commodities',
        }],
        notes: 'When you build a trade shop, indicate the kind of shop it is, such as a bakery, carpenter, tailor, and so on. While in a settlement with a trade shop, you gain a +1 item bonus to all associated Crafting checks.',
        traits: ['building'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 3,
        construction: {
            rp: 10,
            lumber: 2,
            skills: [{
                skill: 'trade',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'University',
        activityBonusRules: [{
            value: 3,
            activity: 'creative-solution',
        }],
        notes: 'While in a settlement with a university, you gain a +3 item bonus to Lore checks made to Recall Knowledge while Investigating, to Research checks (Gamemastery Guide 154), and to Decipher Writing.',
        traits: ['building', 'edifice', 'famous'],
        affectsDowntime: true,
        reducesUnrest: false,
        level: 15,
        upgradeFrom: ['Academy'],
        construction: {
            rp: 78,
            lumber: 18,
            stone: 18,
            luxuries: 18,
            skills: [{
                skill: 'scholarship',
                proficiencyRank: 3,
            }],
            dc: 34,
        },
        lots: 4,
    },
    {
        name: 'Wall, Stone',
        traits: ['infrastructure'],
        lots: 0,
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
            moreThanOncePerTurn: true,
            note: 'as long as it\'s the first wall in a settlement',
        },
        level: 5,
        upgradeFrom: ['Wall, Wooden'],
        construction: {
            rp: 4,
            stone: 8,
            skills: [{
                skill: 'defense',
                proficiencyRank: 1,
            }],
            dc: 20,
        },
    },
    {
        name: 'Wall, Wooden',
        traits: ['infrastructure'],
        lots: 0,
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
            moreThanOncePerTurn: true,
            note: 'as long as it\'s the first wall in a settlement',
        },
        level: 1,
        construction: {
            rp: 2,
            lumber: 4,
            skills: [{
                skill: 'defense',
            }],
            dc: 15,
        },
    },
    {
        name: 'Watchtower',
        settlementEventRules: [{
            value: 1,
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 3,
        construction: {
            rp: 12,
            lumber: 4,
            skills: [{
                skill: 'defense',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Watchtower, Stone',
        stacksWith: 'Watchtower',
        settlementEventRules: [{
            value: 1,
        }],
        traits: ['building'],
        affectsDowntime: false,
        reducesUnrest: true,
        reduceUnrestBy: {
            value: '1',
        },
        level: 3,
        construction: {
            rp: 12,
            stone: 4,
            skills: [{
                skill: 'defense',
                proficiencyRank: 1,
            }],
            dc: 18,
        },
        lots: 1,
    },
    {
        name: 'Waterfront',
        activityBonusRules: [{
            value: 1,
            activity: 'go-fishing',
        }, {
            value: 1,
            activity: 'establish-trade-agreement',
        }],
        availableItemsRules: [{
            value: 1,
            maximumStacks: 1,
        }],
        traits: ['yard'],
        affectsDowntime: false,
        reducesUnrest: false,
        level: 8,
        upgradeFrom: ['Pier'],
        construction: {
            rp: 90,
            lumber: 10,
            skills: [{
                skill: 'boating',
                proficiencyRank: 2,
            }],
            dc: 24,
        },
        lots: 4,
    },
];

export const structuresByName = new Map<string, Structure>();
structures.forEach(s => structuresByName.set(s.name, s));

interface VKAdjustment {
    skillBonusRules?: SkillBonusRule[];
    activityBonusRules?: ActivityBonusRule[];
}

// add v&k adjustments
const vkAdjustments: Record<string, VKAdjustment> = {
    'Bank': {activityBonusRules: [{value: 1, activity: 'capital-investment'}, {value: 1, activity: 'collect-taxes'}]},
    'Castle': {
        activityBonusRules: [
            {
                value: 2,
                activity: 'manage-trade-agreements',
            }, {
                value: 2,
                activity: 'relocate-capital',
            }],
    },
    'Construction Yard': {
        activityBonusRules: [{
            value: 1,
            activity: 'build-roads',
        }, {
            value: 1,
            activity: 'irrigation',
        }],
    },
    'Festival Hall': {skillBonusRules: [{activity: 'quell-unrest', skill: 'arts', value: 1}]},
    'Garrison': {activityBonusRules: [{value: 1, activity: 'fortify-hex'}]},
    'Granary': {activityBonusRules: [{value: 1, activity: 'establish-farmland'}]},
    'Inn': {skillBonusRules: [{value: 1, activity: 'clear-hex', skill: 'exploration'}]},
    'Library': {activityBonusRules: [{value: 1, activity: 'creative-solution'}]},
    'Magic Shop': {activityBonusRules: [{value: 1, activity: 'prognostication'}]},
    'Monument': {activityBonusRules: [{value: 1, activity: 'create-a-masterpiece'}]},
    'Occult Shop': {activityBonusRules: [{value: 2, activity: 'supernatural-solution'}]},
    'Palace': {
        activityBonusRules: [{value: 3, activity: 'manage-trade-agreements'}, {
            value: 3,
            activity: 'relocate-capital',
        }],
    },
    'Smithy': {skillBonusRules: [{value: 1, activity: 'clear-hex', skill: 'engineering'}]},
    'Tavern, Dive': {skillBonusRules: [{value: 1, activity: 'clear-hex', skill: 'exploration'}]},
    'Tavern, Luxury': {activityBonusRules: [{value: 2, activity: 'reconnoiter-hex'}]},
    'Tavern, Popular': {activityBonusRules: [{value: 1, activity: 'reconnoiter-hex'}]},
    'Tavern, World-Class': {activityBonusRules: [{value: 3, activity: 'reconnoiter-hex'}]},
    'Town Hall': {activityBonusRules: [{value: 1, activity: 'manage-trade-agreements'}]},
};

Array.from(Object.entries(vkAdjustments))
    .forEach(([name, adjustment]) => {
        const existing = structuresByName.get(name);
        if (existing === undefined) {
            throw new Error(`VK Adjustment typo: ${name}`);
        }
        const copy = JSON.parse(JSON.stringify(existing)) as Structure;
        // hardcode granary proficiency requirement because it's too difficult otherwise
        if (name === 'Granary') {
            copy.construction!.skills![0]!.proficiencyRank = 1;
        }
        adjustment.skillBonusRules?.forEach(rule => {
            if (copy.skillBonusRules === undefined) {
                copy.skillBonusRules = [];
            }
            copy.skillBonusRules?.push(rule);
        });
        adjustment.activityBonusRules?.forEach(rule => {
            if (copy.activityBonusRules === undefined) {
                copy.activityBonusRules = [];
            }
            copy.activityBonusRules?.push(rule);
        });
        copy.name = `${copy.name} (V&K)`;
        structuresByName.set(copy.name, copy);
    });
