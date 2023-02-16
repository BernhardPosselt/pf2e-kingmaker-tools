import {Activity} from './activities';
import {Skill} from './skills';


export interface ActivityBonusRule {
    value: number;
    activity: Activity;
}

export interface SkillBonusRule {
    value: number;
    skill: Skill;
    // e.g. 'quell-unrest'
    activity?: Activity;
}

export interface AvailableItemsRule {
    value: number;
    // e.g. 'alchemical' or 'magic'
    group?: ItemGroup;
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

export interface Structure {
    name: string;
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
    consumptionReduction?: number;
    unlockActivities?: Activity[];
}

export type ActivityBonuses = Partial<Record<Activity, number>>;

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

export type ItemLevelBonuses = Record<ItemGroup, number>;

const structures: Structure[] = [
    {
        name: 'Academy',
        activityBonusRules: [{
            value: 2,
            activity: 'creative-solution',
        }],
        notes: 'While in a settlement with an Academy, you gain a +2 item bonus to Lore checks made to Recall Knowledge while Investigate, to all checks made while Researching, and to Decipher Writing.',
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
        }],
        notes: 'Checks attempted to Identify Alchemy in any settlement with at least one alchemy laboratory gain a +1 item bonus.',
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
        }],
        notes: 'While in a settlement with an arcanist\'s tower, you gain a +1 item bonus to checks made to Borrow an Arcane Spell or Learn a Spell.',
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
    },
    {
        name: 'Bank',
        activityBonusRules: [{
            value: 1,
            activity: 'tap-treasury',
        }],
        enableCapitalInvestment: true,
    },
    {
        name: 'Barracks',
        activityBonusRules: [{
            value: 1,
            activity: 'garrison-army',
        }, {
            value: 1,
            activity: 'recover-army',
        }, {
            value: 1,
            activity: 'recruit-army',
        }],
    },
    {
        name: 'Brewery',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
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
        }, {
            value: 2,
            activity: 'recover-army',
        }],
        increaseLeadershipActivities: true,
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
        }],
        notes: 'While in a settlement with a cathedral, you gain a +3 item bonus to Lore and Religion checks made to Recall Knowledge while Investigating, and to all faith-themed checks made while Researching.',
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
    },
    {
        name: 'Dump',
        activityBonusRules: [{
            value: 1,
            activity: 'demolish',
        }],
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
    },
    {
        name: 'Festival Hall',
        activityBonusRules: [{
            value: 1,
            activity: 'celebrate-holiday',
        }],
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
    },
    {
        name: 'General Store',
        preventItemLevelPenalty: true,
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
    },
    {
        name: 'Granary',
        storage: {
            food: 1,
        },
    },
    {
        name: 'Guildhall',
        notes: 'While in a settlement with a guildhall, you gain a +1 item bonus to all related skill checks to Earn Income or to Repair.',
    },
    {
        name: 'Herbalist',
        activityBonusRules: [{
            value: 1,
            activity: 'provide-care',
        }],
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
    },
    {
        name: 'Illicit Market',
        activityBonusRules: [{
            value: 1,
            activity: 'clandestine-business',
        }],
        availableItemsRules: [{
            value: 1,
        }],
    },
    {
        name: 'Inn',
        activityBonusRules: [{
            value: 1,
            activity: 'hire-adventurers',
        }],
    },
    {
        name: 'Jail',
        skillBonusRules: [{
            value: 1,
            skill: 'intrigue',
            activity: 'quell-unrest',
        }],
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
    },
    {
        name: 'Library',
        skillBonusRules: [{
            value: 1,
            skill: 'scholarship',
            activity: 'rest-and-relax',
        }],
        notes: 'While in a settlement with a library, you gain a +1 item bonus to Lore checks made to Recall Knowledge while Investigating, as well as to Researching checks, and to Decipher Writing checks.',
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
        }],
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
        }],
    },
    {
        name: 'Mansion',
        activityBonusRules: [{
            value: 1,
            activity: 'improve-lifestyle',
        }],
    },
    {
        name: 'Marketplace',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
        preventItemLevelPenalty: true,
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
    },
    {
        name: 'Mill',
        activityBonusRules: [{
            value: 1,
            activity: 'harvest-crops',
        }],
        consumptionReduction: 1,
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
    },
    {
        name: 'Museum',
        skillBonusRules: [{
            value: 1,
            skill: 'arts',
            activity: 'rest-and-relax',
        }],
        notes: 'A magic item of level 6 or higher that has a particular import or bears significant historical or regional value (at the GM\'s discretion) can be donated to a museum. Each time such an item is donated, reduce Unrest by 1. If that item is later removed from display, increase Unrest by 1.',
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
        }],
        notes: 'While in a settlement with an occult shop, you gain a +2 item bonus to all checks made to Research esoteric subjects or to Recall Knowledge about the same.',
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
        }, {
            value: 3,
            activity: 'recover-army',
        }, {
            value: 3,
            activity: 'recruit-army',
        }],
        leadershipActivityRules: [{
            value: 3,
        }],
        increaseLeadershipActivities: true,
    },
    {
        name: 'Park',
        skillBonusRules: [{
            value: 1,
            skill: 'wilderness',
            activity: 'rest-and-relax',
        }],
    },
    {
        name: 'Pier',
        activityBonusRules: [{
            value: 1,
            activity: 'go-fishing',
        }],
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
        }],
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
    },
    {
        name: 'Sewer System',
        activityBonusRules: [{
            value: 1,
            activity: 'clandestine-business',
        }],
        consumptionReduction: 1,
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
        }],
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
    },
    {
        name: 'Specialized Artisan',
        activityBonusRules: [{
            value: 1,
            activity: 'craft-luxuries',
        }],
        notes: 'While in a settlement with a specialized artisan, you gain a +1 item bonus to Craft checks made to craft specialized goods like jewelry.',
    },
    {
        name: 'Stable',
        activityBonusRules: [{
            value: 1,
            activity: 'establish-trade-agreement',
        }],
    },
    {
        name: 'Stockyard',
        activityBonusRules: [{
            value: 1,
            activity: 'gather-livestock',
        }],
        consumptionReduction: 1,
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
    },
    {
        name: 'Tannery',
        activityBonusRules: [{
            value: 1,
            activity: 'trade-commodities',
        }],
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
        }],
    },
    {
        name: 'Theater',
        activityBonusRules: [{
            value: 2,
            activity: 'celebrate-holiday',
        }],
        notes: 'While in a settlement with a theater, you gain a +2 item bonus to Performance checks made to Earn Income.',
    },
    {
        name: 'Thieves\' Guild',
        activityBonusRules: [{
            value: 1,
            activity: 'infiltration',
        }],
        notes: 'While in a settlement with a thieves\' guild, you gain a +1 item bonus to Create Forgeries.',
    },
    {
        name: 'Town Hall',
        increaseLeadershipActivities: true,
    },
    {
        name: 'Trade Shop',
        activityBonusRules: [{
            value: 1,
            activity: 'purchase-commodities',
        }],
        notes: 'When you build a trade shop, indicate the kind of shop it is, such as a bakery, carpenter, tailor, and so on. While in a settlement with a trade shop, you gain a +1 item bonus to all associated Crafting checks.',
    },
    {
        name: 'University',
        activityBonusRules: [{
            value: 3,
            activity: 'creative-solution',
        }],
        notes: 'While in a settlement with a university, you gain a +3 item bonus to Lore checks made to Recall Knowledge while Investigating, to Research checks (Gamemastery Guide 154), and to Decipher Writing.',
    },
    {
        name: 'Watchtower',
        settlementEventRules: [{
            value: 1,
        }],
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
        }],
    },
];

export const structuresByName = new Map<string, Structure>();
structures.forEach(s => structuresByName.set(s.name, s));
