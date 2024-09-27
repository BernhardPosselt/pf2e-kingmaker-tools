import {distinctBy, groupBy} from '../../utils';

interface KingdomFeature {
    level: number;
    name: string;
    description: string;
}

function kingdomFeat(level: number): KingdomFeature {
    return {
        level,
        name: 'Kingdom Feat',
        description: 'At 2nd level, and then every 2 levels thereafter, the kingdom gains a Kingdom feat.',
    };
}

function skillIncrease(level: number): KingdomFeature {
    return {
        level,
        name: 'Skill Increase',
        description: 'your kingdom gains a skill increase. You can use this to increase your rank to trained in one skill in which your kingdom is untrained, or to increase your rank to expert in one skill in which your kingdom is trained. Starting at 7th level, you can use your skill increases to increase your kingdom’s proficiency to master in a skill in which your kingdom is already an expert. Beginning at 15th level, you can use them to increase your proficiency to legendary in a skill in which your kingdom is already a master.',
    };
}

function abilityBoosts(level: number): KingdomFeature {
    return {
        level,
        name: 'Ability Boosts',
        description: 'At 5th level and every 5 levels thereafter, you boost two different kingdom ability scores. You can use these ability boosts to increase your kingdom’s ability scores above 18. Boosting an ability score increases it by 2 if it starts out below 18, or by 1 if it’s already 18 or above.',
    };
}

function settlementConstruction(level: number, type: string): KingdomFeature {
    return {
        level,
        name: `Settlement Construction (${type})`,
        description: 'You can establish villages in your kingdom immediately. At 3rd level, you can expand villages into towns. At 9th level, you can expand towns into cities. expand towns into cities. And at 15th level, you can expand cities into metropolises. As villages grow into larger settlements, you not only gain more room to build, but the maximum item bonus you can gain from that settlement’s structures increases as well',
    };
}

function ruinResistance(level: number): KingdomFeature {
    return {
        level,
        name: 'Ruin Resistance',
        description: 'At 5th level and every 3 levels thereafter, your kingdom becomes more resistant to Ruin. Choose one of the four Ruin categories and increase its threshold by 2. When you do so, reset that Ruin’s penalty to 0. See page 38 for more information about Ruin.',
    };
}

function expansionExpert(level: number, hexes: number): KingdomFeature {
    return {
        level,
        name: `Expansion Expert (+${hexes})`,
        description: 'Your kingdom is better at expanding its territory. You gain a +2 circumstance bonus to skill checks made to Claim Hex and can attempt to Claim Hex up to twice during a Kingdom turn. At 9th level, you can attempt to Claim Hex up to three times during a Kingdom turn.',
    };
}

function experiencedLeadership(level: number, bonus: number): KingdomFeature {
    return {
        level,
        name: `Experienced Leadership (+${bonus})`,
        description: 'Invested leadership roles in your kingdom now grant a +2 status bonus to kingdom checks associated with their leadership role’s key ability. At 16th level, this increases to a +3 status bonus.',
    };
}


export const features: KingdomFeature[] = [
    // 1
    {
        level: 1,
        name: 'Charter',
        description: 'Your kingdom gains the benefits of your selected charter',
    },
    {
        level: 1,
        name: 'Government',
        description: 'Your kingdom gains the benefits of your selected government',
    },
    {
        level: 1,
        name: 'Government',
        description: 'Your kingdom gains the benefits of your selected heartland',
    },
    {
        level: 1,
        name: 'Initial Proficiencies',
        description: 'At 1st level, a kingdom receives the trained proficiency rank in two Kingdom skills gained from your initial choice of government and in up to four additional Kingdom skills determined by your invested leaders, giving you a proficiency bonus to checks using these skills equal to your kingdom level plus 2. Proficiencies cannot be changed, even if the kingdom’s government or leaders later change.',
    },
    {
        level: 1,
        name: 'Favored Land',
        description: 'Your heartland’s terrain becomes your kingdom’s favored land—the wilderness terrain that your people feel the strongest emotional ties to and to which your resource gatherers tend to flock. Once per Kingdom turn, during the Region Activities step of the Activity phase, you can attempt two Region activities simultaneously as long as both activities take place in the same hex and that hex contains the same terrain as your heartland. You take a –2 penalty to Kingdom skill checks made during these two activities.',
    },
    settlementConstruction(1, 'Village'),
    // 2
    kingdomFeat(2),
    // 3
    settlementConstruction(3, 'Town'),
    skillIncrease(3),
    // 4
    expansionExpert(4, 2),
    {
        level: 4,
        name: 'Fine Living',
        description: 'Your people celebrate by indulging you with feasts and finery. All PCs associated with the kingdom enjoy a Fine standard of living at no cost whenever they’re in the kingdom. Any PCs in hostile wilderness, a monster‑filled dungeon, or otherwise cut off from their citizens must provide their own sustenance as usual even if they are within the boundaries of their kingdom. You gain a +1 circumstance bonus to all checks made to Craft or Earn Income while in your kingdom.',
    },
    kingdomFeat(4),
    // 5
    abilityBoosts(5),
    ruinResistance(5),
    skillIncrease(5),
    // 6
    kingdomFeat(6),
    // 7
    skillIncrease(7),
    // 8
    experiencedLeadership(8, 2),
    kingdomFeat(8),
    ruinResistance(8),
    // 9
    expansionExpert(9, 3),
    settlementConstruction(9, 'City'),
    skillIncrease(9),
    // 10
    abilityBoosts(10),
    kingdomFeat(10),
    {
        level: 10,
        name: 'Life of Luxury',
        description: 'Your people lavish you with every creature comfort. This is identical to Fine Living, but all PC leaders enjoy an Extravagant standard of living at no cost whenever they’re in the kingdom. You gain a +2 circumstance bonus to all checks made to Craft or Earn Income while in your kingdom.',
    },
    // 11
    ruinResistance(11),
    skillIncrease(11),
    // 12
    {
        level: 12,
        name: 'Civic Planning',
        description: 'During the Civic Activities step of the Activities phase of a Kingdom turn, one settlement of the party’s choice can attempt two Civic activities rather than one. The second Civic activity occurs after all other settlements have taken their individual Civic activities.',
    },
    kingdomFeat(12),
    // 13
    skillIncrease(13),
    // 14
    kingdomFeat(14),
    ruinResistance(14),
    // 15
    abilityBoosts(15),
    settlementConstruction(15, 'Metropolis'),
    skillIncrease(15),
    // 16
    experiencedLeadership(16, 3),
    kingdomFeat(16),
    // 17
    ruinResistance(17),
    skillIncrease(17),
    // 18
    kingdomFeat(18),
    // 19
    skillIncrease(19),
    // 20
    abilityBoosts(20),
    {
        level: 20,
        name: 'Envy of the World',
        description: 'Your kingdom is one of the world’s prominent nations. The first time in a Kingdom turn when your kingdom would gain Unrest or Ruin, ignore that increase. You can ignore additional increases to Unrest or Ruin later in the same turn as well, but you must spend a Fame or Infamy point each time you do so. Your maximum Fame or Infamy point total increases by 1.',
    },
    kingdomFeat(20),
    ruinResistance(20),
];

export const featuresByLevel = groupBy(features, f => f.level);
export const uniqueFeatures = distinctBy(features, f => f.name);
