import {Structure} from './structures';

const structures: Structure[] = [
    {
        name: 'Academy',
        actionBonusRules: [{
            value: 2,
            action: 'creative-solution',
        }],
        notes: 'While in a settlement with an Academy, you gain a +2 item bonus to Lore checks made to Recall Knowledge while Investigate, to all checks made while Researching, and to Decipher Writing.',
    },
    {
        name: 'Alchemy Laboratory',
        actionBonusRules: [{
            value: 1,
            action: 'demolish',
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
            action: 'quell-unrest',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'arcane',
        }],
        notes: 'While in a settlement with an arcanist\'s tower, you gain a +1 item bonus to checks made to Borrow an Arcane Spell or Learn a Spell.',
    },
    {
        name: 'Arena',
        actionBonusRules: [{
            value: 2,
            action: 'celebrate-holiday',
        }],
        skillBonusRules: [{
            value: 1,
            skill: 'warfare',
            action: 'quell-unrest',
        }],
        notes: 'An arena lets you to retrain combat-themed feats more efficiently while in the settlement; doing so takes only 5 days rather than a week of downtime.',
    },
    {
        name: 'Bank',
        actionBonusRules: [{
            value: 1,
            action: 'tap-treasury',
        }],
        enableCapitalInvestment: true,
    },
    {
        name: 'Barracks',
        actionBonusRules: [{
            value: 1,
            action: 'garrison-army',
        }, {
            value: 1,
            action: 'recover-army',
        }, {
            value: 1,
            action: 'recruit-army',
        }],
    },
    {
        name: 'Brewery',
        actionBonusRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
    },
    {
        name: 'Castle',
        actionBonusRules: [{
            value: 2,
            action: 'new-leadership',
        }, {
            value: 2,
            action: 'pledge-of-fealty',
        }, {
            value: 2,
            action: 'send-diplomatic-envoy',
        }, {
            value: 2,
            action: 'garrison-army',
        }, {
            value: 2,
            action: 'recruit-army',
        }, {
            value: 2,
            action: 'recover-army',
        }],
        increaseLeadershipActivities: true,
    },
    {
        name: 'Cathedral',
        actionBonusRules: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'provide-care',
        }, {
            value: 3,
            action: 'repair-reputation-corruption',
        }],
        availableItemsRules: [{
            value: 3,
            group: 'divine',
        }],
        notes: 'While in a settlement with a cathedral, you gain a +3 item bonus to Lore and Religion checks made to Recall Knowledge while Investigating, and to all faith-themed checks made while Researching.',
    },
    {
        name: 'Construction Yard',
        actionBonusRules: [{
            value: 1,
            action: 'build-structure',
        }, {
            value: 1,
            action: 'repair-reputation-decay',
        }],
    },
    {
        name: 'Dump',
        actionBonusRules: [{
            value: 1,
            action: 'demolish',
        }],
    },
    {
        name: 'Embassy',
        actionBonusRules: [{
            value: 1,
            action: 'send-diplomatic-envoy',
        }, {
            value: 1,
            action: 'request-foreign-aid',
        }],
    },
    {
        name: 'Festival Hall',
        actionBonusRules: [{
            value: 1,
            action: 'celebrate-holiday',
        }],
    },
    {
        name: 'Foundry',
        actionBonusRules: [{
            value: 1,
            action: 'establish-work-site-mine',
        }],
        storage: {
            ore: 1,
        },
    },
    {
        name: 'Garrison',
        actionBonusRules: [{
            value: 1,
            action: 'outfit-army',
        }, {
            value: 1,
            action: 'train-army',
        }],
    },
    {
        name: 'General Store',
        preventItemLevelPenalty: true,
    },
    {
        name: 'Gladiatorial Arena',
        actionBonusRules: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'hire-adventurers',
        }],
        skillBonusRules: [{
            value: 3,
            skill: 'warfare',
            action: 'quell-unrest',
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
        actionBonusRules: [{
            value: 1,
            action: 'provide-care',
        }],
    },
    {
        name: 'Hospital',
        actionBonusRules: [{
            value: 1,
            action: 'provide-care',
        }, {
            value: 1,
            action: 'quell-unrest',
        }],
        notes: 'While in a settlement with a hospital, you gain a +2 item bonus to Medicine checks to Treat Disease and Treat Wounds.',
    },
    {
        name: 'Illicit Market',
        actionBonusRules: [{
            value: 1,
            action: 'clandestine-business',
        }],
        availableItemsRules: [{
            value: 1,
        }],
    },
    {
        name: 'Inn',
        actionBonusRules: [{
            value: 1,
            action: 'hire-adventurers',
        }],
    },
    {
        name: 'Jail',
        skillBonusRules: [{
            value: 1,
            skill: 'intrigue',
            action: 'quell-unrest',
        }],
    },
    {
        name: 'Keep',
        actionBonusRules: [{
            value: 1,
            action: 'deploy-army',
        }, {
            value: 1,
            action: 'garrison-army',
        }, {
            value: 1,
            action: 'train-army',
        }],
    },
    {
        name: 'Library',
        skillBonusRules: [{
            value: 1,
            skill: 'scholarship',
            action: 'rest-and-relax',
        }],
        notes: 'While in a settlement with a library, you gain a +1 item bonus to Lore checks made to Recall Knowledge while Investigating, as well as to Researching checks, and to Decipher Writing checks.',
    },
    {
        name: 'Lumberyard',
        actionBonusRules: [{
            value: 1,
            action: 'establish-work-site-lumber',
        }],
        storage: {
            lumber: 1,
        },
    },
    {
        name: 'Luxury Store',
        actionBonusRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'luxury',
        }],
    },
    {
        name: 'Magic Shop',
        actionBonusRules: [{
            value: 1,
            action: 'supernatural-solution',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'magical',
        }],
    },
    {
        name: 'Mansion',
        actionBonusRules: [{
            value: 1,
            action: 'improve-lifestyle',
        }],
    },
    {
        name: 'Marketplace',
        actionBonusRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
        preventItemLevelPenalty: true,
    },
    {
        name: 'Menagerie',
        skillBonusRules: [{
            value: 2,
            skill: 'wilderness',
            action: 'rest-and-relax',
        }],
        notes: 'A menagerie typically contains a selection of level 5 or lower animals. If your party captures a living creature of level 6 or higher and can transport the creature back to a settlement with a menagerie, you can add that creature to the menagerie as long as your kingdom level is at least 4 higher than the creature\'s level. Each time such a creature is added to a menagerie, gain 1 Fame or Infamy point (as appropriate) or reduce one Ruin of your choice by 1.\n' +
            'Only creatures with Intelligence modifiers of –4 or –5 are appropriate to place in a menagerie. A kingdom gains 1 Unrest at the start of a Kingdom turn for each sapient creature (anything with an Intelligence modifier of –3 or higher) on display in a menagerie.',
    },
    {
        name: 'Military Academy',
        actionBonusRules: [{
            value: 2,
            action: 'train-army',
        }],
        skillBonusRules: [{
            value: 2,
            skill: 'warfare',
            action: 'pledge-of-fealty',
        }],
    },
    {
        name: 'Mill',
        actionBonusRules: [{
            value: 1,
            action: 'harvest-crops',
        }],
        consumptionReduction: 1,
    },
    {
        name: 'Mint',
        actionBonusRules: [{
            value: 3,
            action: 'capital-investment',
        }, {
            value: 3,
            action: 'collect-taxes',
        }, {
            value: 3,
            action: 'repair-reputation-crime',
        }],
    },
    {
        name: 'Museum',
        skillBonusRules: [{
            value: 1,
            skill: 'arts',
            action: 'rest-and-relax',
        }],
        notes: 'A magic item of level 6 or higher that has a particular import or bears significant historical or regional value (at the GM\'s discretion) can be donated to a museum. Each time such an item is donated, reduce Unrest by 1. If that item is later removed from display, increase Unrest by 1.',
    },
    {
        name: 'Noble Villa',
        actionBonusRules: [{
            value: 1,
            action: 'improve-lifestyle',
        }],
        skillBonusRules: [{
            value: 1,
            skill: 'politics',
            action: 'quell-unrest',
        }],
    },
    {
        name: 'Occult Shop',
        actionBonusRules: [{
            value: 2,
            action: 'prognostication',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'occult',
        }],
        notes: 'While in a settlement with an occult shop, you gain a +2 item bonus to all checks made to Research esoteric subjects or to Recall Knowledge about the same.',
    },
    {
        name: 'Opera House',
        actionBonusRules: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'create-a-masterpiece',
        }],
        notes: 'While in a settlement with an opera house, you gain a +3 item bonus to Performance checks made to Earn Income.',
    },
    {
        name: 'Palace',
        actionBonusRules: [{
            value: 3,
            action: 'new-leadership',
        }, {
            value: 3,
            action: 'pledge-of-fealty',
        }, {
            value: 3,
            action: 'send-diplomatic-envoy',
        }, {
            value: 3,
            action: 'garrison-army',
        }, {
            value: 3,
            action: 'recover-army',
        }, {
            value: 3,
            action: 'recruit-army',
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
            action: 'rest-and-relax',
        }],
    },
    {
        name: 'Pier',
        actionBonusRules: [{
            value: 1,
            action: 'go-fishing',
        }],
    },
    {
        name: 'Printing House',
        actionBonusRules: [{
            value: 2,
            action: 'improve-lifestyle',
        }, {
            value: 2,
            action: 'quell-unrest',
        }],
        notes: 'A PC in a settlement with a printing house gains a +2 item bonus to checks to Gather Information or to Research any topic in a library or similar structure.',
    },
    {
        name: 'Sacred Grove',
        skillBonusRules: [{
            value: 1,
            skill: 'folklore',
            action: 'quell-unrest',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'primal',
        }],
    },
    {
        name: 'Secure Warehouse',
        actionBonusRules: [{
            value: 1,
            action: 'craft-luxuries',
        }],
        storage: {
            luxuries: 1,
        },
    },
    {
        name: 'Sewer System',
        actionBonusRules: [{
            value: 1,
            action: 'clandestine-business',
        }],
        consumptionReduction: 1,
    },
    {
        name: 'Shrine',
        actionBonusRules: [{
            value: 1,
            action: 'celebrate-holiday',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'divine',
        }],
    },
    {
        name: 'Smithy',
        actionBonusRules: [{
            value: 1,
            action: 'trade-commodities',
        }, {
            value: 1,
            action: 'outfit-army',
        }],
        notes: 'While in a settlement with a smithy, you gain a +1 item bonus to Craft checks made to work with metal.',
    },
    {
        name: 'Specialized Artisan',
        actionBonusRules: [{
            value: 1,
            action: 'craft-luxuries',
        }],
        notes: 'While in a settlement with a specialized artisan, you gain a +1 item bonus to Craft checks made to craft specialized goods like jewelry.',
    },
    {
        name: 'Stable',
        actionBonusRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
    },
    {
        name: 'Stockyard',
        actionBonusRules: [{
            value: 1,
            action: 'gather-lifestock',
        }],
        consumptionReduction: 1,
    },
    {
        name: 'Stonemason',
        actionBonusRules: [{
            value: 1,
            action: 'establish-work-site-quarry',
        }],
        storage: {
            stone: 1,
        },
    },
    {
        name: 'Tannery',
        actionBonusRules: [{
            value: 1,
            action: 'trade-commodities',
        }],
    },
    {
        name: 'Tavern, Luxury',
        actionBonusRules: [{
            value: 2,
            action: 'hire-adventurers',
        }],
        skillBonusRules: [{
            value: 2,
            skill: 'trade',
            action: 'rest-and-relax',
        }],
        notes: 'If attempt a Performance check to Earn Income in a settlement with a luxury tavern, you gain a +2 item bonus to the check. All checks made to Gather Information in a settlement with at least one luxury tavern gain a +2 item bonus.',
    },
    {
        name: 'Tavern, Popular',
        actionBonusRules: [{
            value: 1,
            action: 'hire-adventurers',
        }],
        skillBonusRules: [{
            value: 1,
            skill: 'trade',
            action: 'rest-and-relax',
        }],
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a popular tavern, you gain a +1 item bonus to the check. All checks made to Gather Information in a settlement with at least one popular tavern gain a +1 item bonus.',
    },
    {
        name: 'Tavern, World-Class',
        actionBonusRules: [{
            value: 3,
            action: 'hire-adventurers',
        }, {
            value: 3,
            action: 'repair-reputation-strife',
        }],
        skillBonusRules: [{
            value: 3,
            skill: 'trade',
            action: 'rest-and-relax',
        }],
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a world-class tavern, you gain a +3 item bonus to the check. All checks made to Gather Information in a settlement with a world-class tavern gain a +3 item bonus.',
    },
    {
        name: 'Temple',
        actionBonusRules: [{
            value: 1,
            action: 'celebrate-holiday',
        }, {
            value: 1,
            action: 'provide-care',
        }],
        availableItemsRules: [{
            value: 1,
            group: 'divine',
        }],
    },
    {
        name: 'Theater',
        actionBonusRules: [{
            value: 2,
            action: 'celebrate-holiday',
        }],
        notes: 'While in a settlement with a theater, you gain a +2 item bonus to Performance checks made to Earn Income.',
    },
    {
        name: 'Thieves\' Guild',
        actionBonusRules: [{
            value: 1,
            action: 'infiltration',
        }],
        notes: 'While in a settlement with a thieves\' guild, you gain a +1 item bonus to Create Forgeries.',
    },
    {
        name: 'Town Hall',
        increaseLeadershipActivities: true,
    },
    {
        name: 'Trade Shop',
        actionBonusRules: [{
            value: 1,
            action: 'purchase-commodities',
        }],
        notes: 'When you build a trade shop, indicate the kind of shop it is, such as a bakery, carpenter, tailor, and so on. While in a settlement with a trade shop, you gain a +1 item bonus to all associated Crafting checks.',
    },
    {
        name: 'University',
        actionBonusRules: [{
            value: 3,
            action: 'creative-solution',
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
        actionBonusRules: [{
            value: 1,
            action: 'go-fishing',
        }, {
            value: 1,
            action: 'establish-trade-agreement',
        }],
        availableItemsRules: [{
            value: 1,
        }],
    },
];

export const structuresByName = new Map<string, Structure>();
structures.forEach(s => structuresByName.set(s.name, s));
