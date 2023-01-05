type Action = 'establish-trade-agreement'
    | 'provide-care'
    | 'hire-adventurers'
    | 'celebrate-holiday'
    | 'demolish'
    | 'quell-unrest'
    | 'rest-and-relax'
    | 'harvest-crops'
    | 'buy-divine-items'
    | 'buy-alchemy-items'
    | 'buy-primal-items'
    | 'buy-occult-items'
    | 'buy-arcane-items'
    | 'buy-luxury-items'
    | 'buy-magic-items'
    | 'buy-items'
    | 'garrison-army'
    | 'recover-army'
    | 'recruit-army'
    | 'deploy-army'
    | 'train-army'
    | 'outfit-army'
    | 'establish-work-site-mine'
    | 'establish-work-site-lumber'
    | 'establish-work-site-quarry'
    | 'go-fishing'
    | 'trade-commodities'
    | 'gather-lifestock'
    | 'purchase-commodities'
    | 'resolve-settlement-events'
    | 'improve-lifestyle'
    | 'craft-luxuries'
    | 'tap-treasury'
    | 'infiltration'
    | 'clandestine-business'
    | 'send-diplomatic-envoy'
    | 'request-foreign-aid'
    | 'supernatural-solution'
    | 'new-leadership'
    | 'pledge-of-fealty'
    | 'creative-solution'
    | 'build-structure'
    | 'repair-reputation-decay'
    | 'repair-reputation-corruption'
    | 'repair-reputation-crime'
    | 'repair-reputation-strife'
    | 'prognostication'
    | 'capital-investment'
    | 'collect-taxes'
    | 'create-masterpiece'
    ;

type Skill = 'intrigue'
    | 'scholarship'
    | 'wilderness'
    | 'trade'
    | 'magic'
    | 'arts'
    | 'folklore'
    | 'boating'
    | 'warfare'
    | 'politics'
    ;

interface StructureItemBonus {
    value: number;
    skill?: Skill;
    action: Action;
    includeCapitalBonuses?: boolean;
    stacksWithOtherBuyBonuses?: boolean;
}

interface Structure {
    name: string;
    notes?: string;
    preventBuyPenalty?: boolean;
    enableCapitalInvestment?: boolean,
    itemBonuses: StructureItemBonus[];
}

const structures: Structure[] = [
    {
        name: 'Brewery',
        itemBonuses: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
    }, {
        name: 'Cemetary',
        notes: 'If you have at least one cemetery in a settlement, reduce Unrest gained from any dangerous settlement events in that particular settlement by 1 (to a maximum of 4 for four cemeteries). The presence of a cemetery provides additional effects during certain kingdom events.',
        itemBonuses: [],
    }, {
        name: 'General Store',
        preventBuyPenalty: true,
        itemBonuses: [],
    }, {
        name: 'Herbalist',
        itemBonuses: [{
            value: 1,
            action: 'provide-care',
        }],
    }, {
        name: 'Inn',
        itemBonuses: [{
            value: 1,
            action: 'hire-adventurers',
        }],
    }, {
        name: 'Shrine',
        itemBonuses: [{
            value: 1,
            action: 'buy-divine-items',
            includeCapitalBonuses: false,
        }, {
            value: 1,
            action: 'celebrate-holiday',
        }],
    }, {
        name: 'Dump',
        notes: 'Certain events have a more dangerous impact on settlements that don\'t include a dump',
        itemBonuses: [{
            value: 1,
            action: 'demolish',
        }],
    }, {
        name: 'Jail',
        itemBonuses: [{
            value: 1,
            action: 'quell-unrest',
            skill: 'intrigue',
        }],
    }, {
        name: 'Library',
        notes: 'While in a settlement with a library, you gain a +1 item bonus to Lore checks made to Recall Knowledge while Investigating, as well as to Researching checks, and to Decipher Writing checks.',
        itemBonuses: [{
            value: 1,
            action: 'rest-and-relax',
            skill: 'scholarship',
        }],
    }, {
        name: 'Mill',
        itemBonuses: [{
            value: 1,
            action: 'harvest-crops',
        }],
    }, {
        name: 'Alchemy Laboratory',
        notes: 'Checks attempted to Identify Alchemy in any settlement with at least one alchemy laboratory gain a +1 item bonus.',
        itemBonuses: [{
            value: 1,
            action: 'demolish',
        }, {
            value: 1,
            action: 'buy-alchemy-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Barracks',
        itemBonuses: [{
            value: 1,
            action: 'garrison-army',
        }, {
            value: 1,
            action: 'recover-army',
        }, {
            value: 1,
            action: 'recruit-army',
        }],
    }, {
        name: 'Festival Hall',
        itemBonuses: [{
            value: 1,
            action: 'celebrate-holiday',
        }],
    }, {
        name: 'Foundry',
        itemBonuses: [{
            value: 1,
            action: 'establish-work-site-mine',
        }],
    }, {
        name: 'Keep',
        itemBonuses: [{
            value: 1,
            action: 'deploy-army',
        }, {
            value: 1,
            action: 'garrison-army',
        }, {
            value: 1,
            action: 'train-army',
        }],
    }, {
        name: 'Lumberyard',
        itemBonuses: [{
            value: 1,
            action: 'establish-work-site-lumber',
        }],
    }, {
        name: 'Park',
        itemBonuses: [{
            value: 1,
            action: 'rest-and-relax',
            skill: 'wilderness',
        }],
    }, {
        name: 'Pier',
        itemBonuses: [{
            value: 1,
            action: 'go-fishing',
        }],
    }, {
        name: 'Smithy',
        notes: 'While in a settlement with a smithy, you gain a +1 item bonus to Craft checks made to work with metal.',
        itemBonuses: [{
            value: 1,
            action: 'trade-commodities',
        }, {
            value: 1,
            action: 'outfit-army',
        }],
    }, {
        name: 'Stable',
        itemBonuses: [{
            value: 1,
            action: 'establish-trade-agreement',
        }],
    }, {
        name: 'Stockyard',
        itemBonuses: [{
            value: 1,
            action: 'gather-lifestock',
        }],
    }, {
        name: 'Stonemason',
        itemBonuses: [{
            value: 1,
            action: 'establish-work-site-quarry',
        }],
    }, {
        name: 'Tannery',
        itemBonuses: [{
            value: 1,
            action: 'trade-commodities',
        }],
    }, {
        name: 'Tavern, Popular',
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a popular tavern, you gain a +1 item bonus to the check. All checks made to Gather Information in a settlement with at least one popular tavern gain a +1 item bonus.',
        itemBonuses: [{
            value: 1,
            action: 'hire-adventurers',
        }, {
            value: 1,
            action: 'rest-and-relax',
            skill: 'trade',
        }],
    }, {
        name: 'Trade Shop',
        notes: 'When you build a trade shop, indicate the kind of shop it is, such as a bakery, carpenter, tailor, and so on. While in a settlement with a trade shop, you gain a +1 item bonus to all associated Crafting checks.',
        itemBonuses: [{
            value: 1,
            action: 'purchase-commodities',
        }],
    }, {
        name: 'Watchtower',
        itemBonuses: [{
            value: 1,
            action: 'resolve-settlement-events',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Marketplace',
        itemBonuses: [{
            value: 1,
            action: 'improve-lifestyle',
        }],
    }, {
        name: 'Specialized Artisan',
        notes: 'While in a settlement with a specialized artisan, you gain a +1 item bonus to Craft checks made to craft specialized goods like jewelry.',
        itemBonuses: [{
            value: 1,
            action: 'craft-luxuries',
        }],
    }, {
        name: 'Paved Streets',
        notes: 'It takes a character only 5 minutes to move from one lot to an adjacent lot in an Urban Grid when moving on paved streets',
        itemBonuses: [],
    }, {
        name: 'Arcanist\'s Tower',
        notes: 'While in a settlement with an arcanist\'s tower, you gain a +1 item bonus to checks made to Borrow an Arcane Spell or Learn a Spell.',
        itemBonuses: [{
            value: 1,
            action: 'quell-unrest',
            skill: 'magic',
        }, {
            value: 1,
            action: 'buy-arcane-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Bank',
        enableCapitalInvestment: true,
        itemBonuses: [{
            value: 1,
            action: 'tap-treasury',
        }],
    }, {
        name: 'Garrison',
        itemBonuses: [{
            value: 1,
            action: 'outfit-army',
        }, {
            value: 1,
            action: 'train-army',
        }],
    }, {
        name: 'Mansion',
        itemBonuses: [{
            value: 1,
            action: 'improve-lifestyle',
        }],
    }, {
        name: 'Museum',
        notes: 'A magic item of level 6 or higher that has a particular import or bears significant historical or regional value (at the GM\'s discretion) can be donated to a museum. Each time such an item is donated, reduce Unrest by 1. If that item is later removed from display, increase Unrest by 1.',
        itemBonuses: [{
            value: 1,
            action: 'rest-and-relax',
            skill: 'arts',
        }],
    }, {
        name: 'Sacred Grove',
        itemBonuses: [{
            value: 1,
            action: 'quell-unrest',
            skill: 'folklore',
        }, {
            value: 1,
            action: 'buy-primal-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Thieves\' Guild',
        notes: 'While in a settlement with a thieves\' guild, you gain a +1 item bonus to Create Forgeries.',
        itemBonuses: [{
            value: 1,
            action: 'infiltration',
        }],
    }, {
        name: 'Illicit Market',
        itemBonuses: [{
            value: 1,
            action: 'clandestine-business',
        }, {
            value: 1,
            action: 'buy-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Luxury Store',
        itemBonuses: [{
            value: 1,
            action: 'establish-trade-agreement',
        }, {
            value: 1,
            action: 'buy-luxury-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Secure Warehouse',
        itemBonuses: [{
            value: 1,
            action: 'craft-luxuries',
        }],
    }, {
        name: 'Sewer System',
        itemBonuses: [{
            value: 1,
            action: 'clandestine-business',
        }],
    }, {
        name: 'Temple',
        itemBonuses: [{
            value: 1,
            action: 'celebrate-holiday',
        }, {
            value: 1,
            action: 'provide-care',
        }, {
            value: 1,
            action: 'buy-divine-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Embassy',
        itemBonuses: [{
            value: 1,
            action: 'send-diplomatic-envoy',
        }, {
            value: 1,
            action: 'request-foreign-aid',
        }],
    }, {
        name: 'Magic Shop',
        itemBonuses: [{
            value: 1,
            action: 'supernatural-solution',
        }, {
            value: 1,
            action: 'buy-magic-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Waterfront',
        itemBonuses: [{
            value: 1,
            action: 'go-fishing',
        }, {
            value: 1,
            action: 'establish-trade-agreement',
        }, {
            value: 1,
            action: 'rest-and-relax',
            skill: 'boating',
        }, {
            value: 1,
            action: 'buy-items',
            includeCapitalBonuses: false,
            stacksWithOtherBuyBonuses: true,
        }],
    }, {
        name: 'Arena',
        notes: 'An arena lets you to retrain combat-themed feats more efficiently while in the settlement; doing so takes only 5 days rather than a week of downtime.',
        itemBonuses: [{
            value: 2,
            action: 'celebrate-holiday',
        }, {
            value: 2,
            action: 'quell-unrest',
            skill: 'warfare',
        }],
    }, {
        name: 'Castle',
        itemBonuses: [{
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
            action: 'recover-army',
        }, {
            value: 2,
            action: 'recruit-army',
        }],
    }, {
        name: 'Hospital',
        notes: 'While in a settlement with a hospital, you gain a +2 item bonus to Medicine checks to Treat Wounds and Treat Wounds.',
        itemBonuses: [{
            value: 1,
            action: 'provide-care',
        }, {
            value: 1,
            action: 'quell-unrest',
        }],
    }, {
        name: 'Noble Villa',
        itemBonuses: [{
            value: 1,
            action: 'improve-lifestyle',
        }, {
            value: 1,
            action: 'quell-unrest',
            skill: 'politics',
        }],
    }, {
        name: 'Tavern, Luxury',
        notes: 'If attempt a Performance check to Earn Income in a settlement with a luxury tavern, you gain a +2 item bonus to the check. All checks made to Gather Information in a settlement with at least one luxury tavern gain a +2 item bonus.',
        itemBonuses: [{
            value: 2,
            action: 'hire-adventurers',
        }, {
            value: 2,
            action: 'rest-and-relax',
            skill: 'trade',
        }],
    }, {
        name: 'Theater',
        notes: 'While in a settlement with a theater, you gain a +2 item bonus to Performance checks made to Earn Income.',
        itemBonuses: [{
            value: 2,
            action: 'celebrate-holiday',
        }],
    }, {
        name: 'Academy',
        notes: 'While in a settlement with an Academy, you gain a +2 item bonus to Lore checks made to Recall Knowledge while Investigate, to all checks made while Researching, and to Decipher Writing.',
        itemBonuses: [{
            value: 2,
            action: 'creative-solution',
        }],
    }, {
        name: 'Construction Yard',
        itemBonuses: [{
            value: 1,
            action: 'build-structure',
        }, {
            value: 1,
            action: 'repair-reputation-decay',
        }],
    }, {
        name: 'Printing House',
        notes: 'A PC in a settlement with a printing house gains a +2 item bonus to checks to Gather Information or to Research any topic in a library or similar structure.',
        itemBonuses: [{
            value: 2,
            action: 'improve-lifestyle',
        }, {
            value: 2,
            action: 'quell-unrest',
        }],
    }, {
        name: 'Menagerie',
        notes: 'A menagerie typically contains a selection of level 5 or lower animals. If your party captures a living creature of level 6 or higher and can transport the creature back to a settlement with a menagerie, you can add that creature to the menagerie as long as your kingdom level is at least 4 higher than the creature\'s level. Each time such a creature is added to a menagerie, gain 1 Fame or Infamy point (as appropriate) or reduce one Ruin of your choice by 1.\n' +
            'Only creatures with Intelligence modifiers of –4 or –5 are appropriate to place in a menagerie. A kingdom gains 1 Unrest at the start of a Kingdom turn for each sapient creature (anything with an Intelligence modifier of –3 or higher) on display in a menagerie.',
        itemBonuses: [{
            value: 2,
            action: 'rest-and-relax',
            skill: 'wilderness',
        }],
    }, {
        name: 'Military Academy',
        itemBonuses: [{
            value: 2,
            action: 'pledge-of-fealty',
            skill: 'warfare',
        }, {
            value: 2,
            action: 'train-army',
        }],
    }, {
        name: 'Occult Shop',
        notes: 'Apply the item bonus to buy occult items (+1) manually to other stores that function in this way for more specific categories of magic items. While in a settlement with an occult shop, you gain a +2 item bonus to all checks made to Research esoteric subjects or to Recall Knowledge about the same.',
        itemBonuses: [{
            value: 2,
            action: 'prognostication',
        }, {
            value: 1,
            action: 'buy-occult-items',
        }],
    }, {
        name: 'Cathedral',
        notes: 'While in a settlement with a cathedral, you gain a +3 item bonus to Lore and Religion checks made to Recall Knowledge while Investigating, and to all faith-themed checks made while Researching',
        itemBonuses: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'provide-care',
        }, {
            value: 3,
            action: 'repair-reputation-corruption',
        }, {
            value: 3,
            action: 'buy-divine-items',
            includeCapitalBonuses: false,
        }],
    }, {
        name: 'Gladiatorial Arena',
        notes: 'A gladiatorial arena allows a PC in the settlement to retrain combat-themed feats (at the GM’s discretion) more efficiently; doing so takes only 4 days rather than a week of downtime.',
        itemBonuses: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'hire-adventurers',
        }, {
            value: 3,
            action: 'quell-unrest',
            skill: 'warfare',
        }],
    }, {
        name: 'Mint',
        itemBonuses: [{
            value: 3,
            action: 'capital-investment',
        }, {
            value: 3,
            action: 'collect-taxes',
        }, {
            value: 3,
            action: 'repair-reputation-crime',
        }],
    }, {
        name: 'Opera House',
        notes: 'While in a settlement with an opera house, you gain a +3 item bonus to Performance checks made to Earn Income.',
        itemBonuses: [{
            value: 3,
            action: 'celebrate-holiday',
        }, {
            value: 3,
            action: 'create-masterpiece',
        }],
    }, {
        name: 'Palace',
        notes: 'If you Relocate your Capital, a palace left behind in that capital instead functions as a noble villa that takes up 4 lots. (If you represent this by placing two noble villas in these lots, make sure to note that they constitute a single building and aren\'t two separate structures.)',
        itemBonuses: [{
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
    }, {
        name: 'Tavern, World-Class',
        notes: 'If you attempt a Performance check to Earn Income in a settlement with a world-class tavern, you gain a +3 item bonus to the check. All checks made to Gather Information in a settlement with a world-class tavern gain a +3 item bonus.',
        itemBonuses: [{
            value: 3,
            action: 'hire-adventurers',
        }, {
            value: 3,
            action: 'rest-and-relax',
            skill: 'trade',
        }, {
            value: 3,
            action: 'repair-reputation-strife',
        }],
    }, {
        name: 'University',
        notes: 'While in a settlement with a university, you gain a +3 item bonus to Lore checks made to Recall Knowledge while Investigating, to Research checks (Gamemastery Guide 154), and to Decipher Writing.',
        itemBonuses: [{
            value: 3,
            action: 'creative-solution',
        }],
    },
];
const structuresByName = new Map<string, Structure>();
structures.forEach(s => structuresByName.set(s.name, s));

export interface StructureResult {
    allowCapitalInvestment: boolean;
    itemBonuses: StructureItemBonus[];
    notes: string[];
}
export function evaluate(maxItemBonus: number, actorNames: string[]): StructureResult {
    const structureOccurrences = actorNames
        .reduce((map, name) => map.set(name, (map.get(name) ?? 0) + 1), new Map());
    const multipliedResults = Array.from(structureOccurrences.entries())
        .filter(([name]) => structuresByName.has(name))
        .map(([name, occurrences]) => {
            const structure = structuresByName.get(name)!;
            const result: Structure = {
                ...structure,
                itemBonuses: structure.itemBonuses.map(bonus => {
                    return {
                        ...bonus,
                        itemBonuses: Math.min(bonus.value * occurrences, maxItemBonus),
                    };
                }),
            };
            return result;
        });
    const hasBuyPenalty = multipliedResults.some(structure => structure.preventBuyPenalty === true);
    const allowCapitalInvestment = multipliedResults.some(structure => structure.enableCapitalInvestment === true);
    const notes = Array.from(new Set(multipliedResults.flatMap(result => result.notes ?? [])));
    // TODO: add buy bonuses that stack with other results to the maxItemBonus
    // TODO: filter out item bonuses that are more specific but have a lower value
    // TODO: go from: bonuses without action requirement -> bonuses with action requirement -> bonuses with action and skill requirement
    const itemBonuses = multipliedResults.flatMap(result => result.itemBonuses);
    return {
        allowCapitalInvestment,
        notes,
        itemBonuses,
    };
}
