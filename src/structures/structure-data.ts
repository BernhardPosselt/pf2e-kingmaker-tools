import {Structure} from './structures';

const structures: Structure[] = [
    {
        name: 'Academy',
        simpleKingdomSkillRules: [{
            value: 2,
            action: 'creative-solution',
        }],
        notes: 'While in a settlement with an Academy, you gain a +2 item bonus to Lore checks made to Recall Knowledge while Investigate, to all checks made while Researching, and to Decipher Writing.',
    },
    {
        name: 'Alchemy Laboratory',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'demolish',
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:trait:alchemical'],
        }],
        notes: 'Checks attempted to Identify Alchemy in any settlement with at least one alchemy laboratory gain a +1 item bonus.',
    },
    {
        name: 'Arcanist\'s Tower',
        kingdomSkillRules: [{
            value: 1,
            skill: 'magic',
            predicate: ['action:quell-unrest'],
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:trait:arcane'],
        }],
        notes: 'While in a settlement with an arcanist\'s tower, you gain a +1 item bonus to checks made to Borrow an Arcane Spell or Learn a Spell.',
    },
    {
        name: 'Arena',
        simpleKingdomSkillRules: [{
            value: 2,
            action: 'celebrate-holiday',
        }],
        kingdomSkillRules: [{
            value: 1,
            skill: 'warfare',
            predicate: ['action:quell-unrest'],
        }],
        notes: 'An arena lets you to retrain combat-themed feats more efficiently while in the settlement; doing so takes only 5 days rather than a week of downtime.',
    },
    {
        name: 'Bank',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'tap-treasury',
        }],
        enableCapitalInvestment: true,
    },
    {
        name: 'Barracks',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
    },
    {
        name: 'Castle',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
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
            predicate: ['item:trait:divine'],
        }],
        notes: 'While in a settlement with a cathedral, you gain a +3 item bonus to Lore and Religion checks made to Recall Knowledge while Investigating, and to all faith-themed checks made while Researching.',
    },
    {
        name: 'Construction Yard',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'build-structure',
        }, {
            value: 1,
            action: 'repair-reputation-decay',
        }],
    },
    {
        name: 'Dump',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'demolish',
        }],
    },
    {
        name: 'Embassy',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'send-diplomatic-envoy',
        }, {
            value: 1,
            action: 'request-foreign-aid',
        }],
    },
    {
        name: 'Festival Hall',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'celebrate-holiday',
        }],
    },
    {
        name: 'Foundry',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-work-site-mine',
        }],
        storage: {
            ore: 1,
        },
    },
    {
        name: 'Garrison',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'hire-adventurers',
        }],
        kingdomSkillRules: [{
            value: 3,
            skill: 'warfare',
            predicate: ['action:quell-unrest'],
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
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'provide-care',
        }],
    },
    {
        name: 'Hospital',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'clandestine-business',
        }],
        availableItemsRules: [{
            value: 1,
        }],
    },
    {
        name: 'Inn',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'hire-adventurers',
        }],
    },
    {
        name: 'Jail',
        kingdomSkillRules: [{
            value: 1,
            skill: 'intrigue',
            predicate: ['action:quell-unrest'],
        }],
    },
    {
        name: 'Keep',
        simpleKingdomSkillRules: [{
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
        kingdomSkillRules: [{
            value: 1,
            skill: 'scholarship',
            predicate: ['action:rest-and-relax'],
        }],
        notes: 'While in a settlement with a library, you gain a +1 item bonus to Lore checks made to Recall Knowledge while Investigating, as well as to Researching checks, and to Decipher Writing checks.',
    },
    {
        name: 'Lumberyard',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-work-site-lumber',
        }],
        storage: {
            lumber: 1,
        },
    },
    {
        name: 'Luxury Store',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:traits:luxury'],
        }],
    },
    {
        name: 'Magic Shop',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'supernatural-solution',
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:traits:magical'],
        }],
    },
    {
        name: 'Mansion',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'improve-lifestyle',
        }],
    },
    {
        name: 'Marketplace',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
        preventItemLevelPenalty: true,
    },
    {
        name: 'Menagerie',
        kingdomSkillRules: [{
            value: 2,
            skill: 'wilderness',
            predicate: ['action:rest-and-relax'],
        }],
        notes: 'A menagerie typically contains a selection of level 5 or lower animals. If your party captures a living creature of level 6 or higher and can transport the creature back to a settlement with a menagerie, you can add that creature to the menagerie as long as your kingdom level is at least 4 higher than the creature\'s level. Each time such a creature is added to a menagerie, gain 1 Fame or Infamy point (as appropriate) or reduce one Ruin of your choice by 1.\n' +
            'Only creatures with Intelligence modifiers of –4 or –5 are appropriate to place in a menagerie. A kingdom gains 1 Unrest at the start of a Kingdom turn for each sapient creature (anything with an Intelligence modifier of –3 or higher) on display in a menagerie.',
    },
    {
        name: 'Military Academy',
        simpleKingdomSkillRules: [{
            value: 2,
            action: 'train-army',
        }],
        kingdomSkillRules: [{
            value: 2,
            skill: 'warfare',
            predicate: ['action:pledge-of-fealty'],
        }],
    },
    {
        name: 'Mill',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'harvest-crops',
        }],
        consumptionReduction: 1,
    },
    {
        name: 'Mint',
        simpleKingdomSkillRules: [{
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
        kingdomSkillRules: [{
            value: 1,
            skill: 'arts',
            predicate: ['action:rest-and-relax'],
        }],
        notes: 'A magic item of level 6 or higher that has a particular import or bears significant historical or regional value (at the GM\'s discretion) can be donated to a museum. Each time such an item is donated, reduce Unrest by 1. If that item is later removed from display, increase Unrest by 1.',
    },
    {
        name: 'Noble Villa',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'improve-lifestyle',
        }],
        kingdomSkillRules: [{
            value: 1,
            skill: 'politics',
            predicate: ['action:quell-unrest'],
        }],
    },
    {
        name: 'Occult Shop',
        simpleKingdomSkillRules: [{
            value: 2,
            action: 'prognostication',
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:traits:occult'],
        }],
        notes: 'While in a settlement with an occult shop, you gain a +2 item bonus to all checks made to Research esoteric subjects or to Recall Knowledge about the same.',
    },
    {
        name: 'Opera House',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
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
        increaseLeadershipActivities: true,
        leadershipActivityMaxBonus: true,
    },
    {
        name: 'Park',
        kingdomSkillRules: [{
            value: 1,
            skill: 'wilderness',
            predicate: ['action:rest-and-relax'],
        }],
    },
    {
        name: 'Pier',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'go-fishing',
        }],
    },
    {
        name: 'Printing House',
        simpleKingdomSkillRules: [{
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
        kingdomSkillRules: [{
            value: 1,
            skill: 'folklore',
            predicate: ['action:quell-unrest'],
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:traits:primal'],
        }],
    },
    {
        name: 'Secure Warehouse',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'craft-luxuries',
        }],
        storage: {
            luxuries: 1,
        },
    },
    {
        name: 'Sewer System',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'clandestine-business',
        }],
        consumptionReduction: 1,
    },
    {
        name: 'Shrine',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'celebrate-holiday',
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:traits:divine'],
        }],
    },
    {
        name: 'Smithy',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'craft-luxuries',
        }],
        notes: 'While in a settlement with a specialized artisan, you gain a +1 item bonus to Craft checks made to craft specialized goods like jewelry.',
    },
    {
        name: 'Stable',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
    },
    {
        name: 'Stockyard',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'gather-lifestock',
        }],
        consumptionReduction: 1,
    },
    {
        name: 'Stonemason',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'establish-work-site-quarry',
        }],
        storage: {
            stone: 1,
        },
    },
    {
        name: 'Tannery',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'trade-commodities',
        }],
    },
    {
        name: 'Tavern, Luxury',
        simpleKingdomSkillRules: [{
            value: 2,
            action: 'hire-adventurers',
        }],
        kingdomSkillRules: [{
            value: 2,
            skill: 'trade',
            predicate: ['action:rest-and-relax'],
        }],
        notes: 'If attempt a Performance check to Earn Income in a settlement with a luxury tavern, you gain a +2 item bonus to the check. All checks made to Gather Information in a settlement with at least one luxury tavern gain a +2 item bonus.',
    },
    {
        name: 'Tavern, Popular',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'hire-adventurers',
        }],
        kingdomSkillRules: [{
            value: 1,
            skill: 'trade',
            predicate: ['action:rest-and-relax'],
        }],
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a popular tavern, you gain a +1 item bonus to the check. All checks made to Gather Information in a settlement with at least one popular tavern gain a +1 item bonus.',
    },
    {
        name: 'Tavern, World-Class',
        simpleKingdomSkillRules: [{
            value: 3,
            action: 'hire-adventurers',
        }, {
            value: 3,
            action: 'repair-reputation-strife',
        }],
        kingdomSkillRules: [{
            value: 3,
            skill: 'trade',
            predicate: ['action:rest-and-relax'],
        }],
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a world-class tavern, you gain a +3 item bonus to the check. All checks made to Gather Information in a settlement with a world-class tavern gain a +3 item bonus.',
    },
    {
        name: 'Temple',
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'celebrate-holiday',
        }, {
            value: 1,
            action: 'provide-care',
        }],
        availableItemsRules: [{
            value: 1,
            predicate: ['item:traits:divine'],
        }],
    },
    {
        name: 'Theater',
        simpleKingdomSkillRules: [{
            value: 2,
            action: 'celebrate-holiday',
        }],
        notes: 'While in a settlement with a theater, you gain a +2 item bonus to Performance checks made to Earn Income.',
    },
    {
        name: 'Thieves\' Guild',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
            value: 1,
            action: 'purchase-commodities',
        }],
        notes: 'When you build a trade shop, indicate the kind of shop it is, such as a bakery, carpenter, tailor, and so on. While in a settlement with a trade shop, you gain a +1 item bonus to all associated Crafting checks.',
    },
    {
        name: 'University',
        simpleKingdomSkillRules: [{
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
        simpleKingdomSkillRules: [{
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
