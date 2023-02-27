interface Fortification {
    name: string;
    ac: number;
    hp: number;
    maximumArmies: number;
}

const allFortifications: Fortification[] = [
    {name: 'Castle', ac: 30, hp: 8, maximumArmies: 6},
    {name: 'Keep', ac: 25, hp: 5, maximumArmies: 4},
    {name: 'Tower', ac: 20, hp: 2, maximumArmies: 1},
    {name: 'Trench', ac: 15, hp: 1, maximumArmies: 1},
    {name: 'Wall, stone', ac: 20, hp: 3, maximumArmies: 2},
    {name: 'Wall, wooden', ac: 15, hp: 2, maximumArmies: 2},
];

interface ArmyStatistic {
    level: number;
    scouting: number;
    standardDC: number;
    ac: number;
    highSave: number;
    lowSave: number;
    attack: number;
    maximumTactics: number;
}

const allArmyStatistics: ArmyStatistic[] = [
    {level: 1, scouting: 7, standardDC: 15, ac: 16, highSave: 10, lowSave: 4, attack: 9, maximumTactics: 1},
    {level: 2, scouting: 8, standardDC: 16, ac: 18, highSave: 11, lowSave: 5, attack: 11, maximumTactics: 1},
    {level: 3, scouting: 9, standardDC: 18, ac: 19, highSave: 12, lowSave: 6, attack: 12, maximumTactics: 1},
    {level: 4, scouting: 11, standardDC: 19, ac: 21, highSave: 14, lowSave: 8, attack: 14, maximumTactics: 2},
    {level: 5, scouting: 12, standardDC: 20, ac: 22, highSave: 15, lowSave: 9, attack: 15, maximumTactics: 2},
    {level: 6, scouting: 14, standardDC: 22, ac: 24, highSave: 17, lowSave: 11, attack: 17, maximumTactics: 2},
    {level: 7, scouting: 15, standardDC: 23, ac: 25, highSave: 18, lowSave: 12, attack: 18, maximumTactics: 2},
    {level: 8, scouting: 16, standardDC: 24, ac: 27, highSave: 19, lowSave: 13, attack: 20, maximumTactics: 3},
    {level: 9, scouting: 18, standardDC: 26, ac: 28,highSave: 21, lowSave: 15, attack: 21, maximumTactics: 3},
    {level: 10, scouting: 19, standardDC: 27, ac: 30, highSave: 22, lowSave: 16, attack: 23, maximumTactics: 3},
    {level: 11, scouting: 21, standardDC: 28, ac: 31, highSave: 24, lowSave: 18, attack: 24, maximumTactics: 3},
    {level: 12, scouting: 22, standardDC: 30, ac: 33, highSave: 25, lowSave: 19, attack: 26, maximumTactics: 4},
    {level: 13, scouting: 23, standardDC: 31, ac: 34, highSave: 26, lowSave: 20, attack: 27, maximumTactics: 4},
    {level: 14, scouting: 25, standardDC: 32, ac: 36, highSave: 28, lowSave: 22, attack: 29, maximumTactics: 4},
    {level: 15, scouting: 26, standardDC: 34, ac: 37, highSave: 29, lowSave: 23, attack: 30, maximumTactics: 4},
    {level: 16, scouting: 28, standardDC: 35, ac: 39, highSave: 30, lowSave: 25, attack: 32, maximumTactics: 5},
    {level: 17, scouting: 29, standardDC: 36, ac: 40, highSave: 32, lowSave: 26, attack: 33, maximumTactics: 5},
    {level: 18, scouting: 30, standardDC: 38, ac: 42, highSave: 33, lowSave: 27, attack: 35, maximumTactics: 5},
    {level: 19, scouting: 32, standardDC: 39, ac: 43, highSave: 35, lowSave: 29, attack: 36, maximumTactics: 5},
    {level: 20, scouting: 33, standardDC: 40, ac: 45, highSave: 36, lowSave: 30, attack: 38, maximumTactics: 6},
];

interface ArmyAction {
    name: string;
    type: 'attack' | 'morale' | 'maneuver';
    actions: '1' | '2' | '3' | 'r';
    restrictedTypes?: ArmyType[];
    description: string;
    prerequisites?: string;
}

const allArmyActions: ArmyAction[] = [
    // TODO:
];

interface ArmyCondition {
    name: string;
    // todo: modifiers?
}

const allArmyConditions: ArmyCondition[] = [
    // todo
];

interface ArmyGear {
    name: string;
    level: number;
    price: number;
    traits: string[];
    description: string;
    type?: string;
    quantity?: number;
    // todo: modifiers?
}

const allGear: ArmyGear[] = [{
    name: 'Additional Weapon',
    level: 1,
    price: 10,
    traits: ['army'],
    description: 'Most armies have only one weapon—a melee or a ranged weapon. This gear outfits an army with an additional weapon of the other type. The army gains a melee or ranged Strike (as appropriate) at the basic modifier for their level.',
}, {
    name: 'Healing Potion',
    price: 15,
    level: 1,
    quantity: 1,
    traits: ['army', 'consumable', 'healing', 'magical', 'necromancy', 'potion'],
    description: 'An army equipped with healing potions (these rules are the same if you instead supply the army with alchemical healing elixirs) can use a single dose as part of any Maneuver action. When an army uses a dose of healing potions, it regains 1 HP. An army can be outfitted with up to 3 doses of healing potions at a time; unlike ranged Strike shots, healing potion doses do not automatically replenish after a war encounter—new doses must be purchased.',
}, {
    name: 'Magic Armor +1',
    type: 'magic-armor',
    level: 5,
    traits: ['abjuration', 'army', 'magical'],
    description: 'Magic armor is magically enchanted to bolster the protection it affords to the soldiers. This armor increases the army’s AC by 1',
    price: 5,
}, {
    name: 'Magic Armor +2',
    type: 'magic-armor',
    level: 11,
    traits: ['abjuration', 'army', 'magical'],
    description: 'Magic armor is magically enchanted to bolster the protection it affords to the soldiers. This armor increases the army’s AC by 2',
    price: 50,
}, {
    name: 'Magic Armor +3',
    type: 'magic-armor',
    level: 18,
    traits: ['abjuration', 'army', 'magical'],
    description: 'Magic armor is magically enchanted to bolster the protection it affords to the soldiers. This armor increases the army’s AC by 3',
    price: 75,
}, {
    name: 'Magic Weapons',
    traits: ['army', 'evocation', 'magical'],
    type: 'magic-weapons',
    level: 2,
    price: 20,
    description: `The army’s weapons are magic. If the army has melee and ranged weapons, choose which one is made magic when
this gear is purchased. You can buy this gear twice—once for melee weapons and once for ranged weapons. If you purchase a more powerful version, it replaces the previous version, and the RP cost of the more powerful version is reduced by the RP cost of the replaced weapons. These weapons increase the army’s Strike with that weapon by 1.`,
}, {
    name: 'Magic Weapons +2',
    traits: ['army', 'evocation', 'magical'],
    type: 'magic-weapons',
    level: 10,
    price: 40,
    description: `The army’s weapons are magic. If the army has melee and ranged weapons, choose which one is made magic when
this gear is purchased. You can buy this gear twice—once for melee weapons and once for ranged weapons. If you purchase a more powerful version, it replaces the previous version, and the RP cost of the more powerful version is reduced by the RP cost of the replaced weapons. These weapons increase the army’s Strike with that weapon by 2.`,
}, {
    name: 'Magic Weapons +3',
    traits: ['army', 'evocation', 'magical'],
    type: 'magic-weapons',
    level: 16,
    price: 60,
    description: `The army’s weapons are magic. If the army has melee and ranged weapons, choose which one is made magic when
this gear is purchased. You can buy this gear twice—once for melee weapons and once for ranged weapons. If you purchase a more powerful version, it replaces the previous version, and the RP cost of the more powerful version is reduced by the RP cost of the replaced weapons. These weapons increase the army’s Strike with that weapon by 3.`,
}];

interface ArmyTactics {
    name: string;
    description: string;
    countsAgainstLimit?: boolean;
    unique?: boolean;
    traits?: string[];
    restrictedTypes?: ArmyType[];
    // todo: modifiers
}

const allTactics: ArmyTactics[] = [{
    name: 'Overrun',
    description: 'Cavalry armies gain a +1 status bonus on weapon attacks against infantry and skirmisher armies, but they suffer a –1 status penalty on Maneuver and Morale saves against area attacks and mental attacks.',
}, {
    name: 'Engines of War',
    description: 'Siege engines cannot be outfitted with gear. They cannot attack engaged armies. They are more difficult to destroy due to their higher hit points than other basic armies. A siege engine can attack and damage fortifications with its ranged attacks as part of the Battle or Overwhelming Bombardment actions.',
}, {
    name: 'Brutal Assault',
    description: 'The Troll Marauders can use the All-Out Assault action. When they do, an army damaged by the assault must succeed at a DC 24 Morale check to avoid becoming shaken 1 (or shaken 2 on a critical failure) as a result of the brutality of this attack.',
    unique: true,
}, {
    name: 'Frightening Foe',
    description: 'The Troll Marauders can use the Taunt tactical action. When they do, they gain a +2 status bonus on their Morale check if they used the Regeneration tactic this turn.',
    unique: true,
}, {
    name: 'Regeneration',
    unique: true,
    description: 'At the beginning of its turn, the Troll Marauders regain 1 Hit Point. The Troll Marauders cannot be destroyed as usual unless they lose this tactic. The PCs can cause the trolls to lose the Regeneration tactic via prepared firepots (see page 296); while the trolls’ Regeneration tactic is lost, their RT increases to 3. Otherwise, an army that engages the Troll Marauders while they are defeated can take a three-round action to burn the trolls and destroy their army.',
}, {
    name: 'Tactical Training',
    unique: true,
    description: 'The Drelev Irregulars can use the All-Out Assault, Counterattack, and Dirty Fighting tactical actions.',
}, {
    name: 'Unpredictable Movement',
    unique: true,
    description: 'It’s difficult to do significant damage to the Drelev Irregulars with ranged attacks, as the mob moves about in a haphazard manner. All ranged attacks against the Drelev Irregulars suffer a –2 circumstance penalty as a result.',
}, {
    name: 'Flight',
    unique: true,
    description: 'The Wyvern Flight ignores all ground-based difficult terrain and cannot become mired by effects that can be escaped by flight. When they use the Disengage action against armies that can’t fly, their check result is improved one degree. Armies that lack the ability to fly suffer a –2 circumstance penalty on Advance actions against a Wyvern Flight',
}, {
    name: 'Wyvern Venom',
    unique: true,
    description: `An army that takes damage from a Wyvern Flight’s melee strike increases its weary condition value by 1. If this would cause an army to increase its weary condition above 4, it instead takes 1 point of damage. Each time an army regains Hit Points
during a battle, it can attempt a DC 11 flat check; on a success, it no longer suffers the ongoing effects of Wyvern Venom (but can still be affected by it later from a future attack, and does not reset its weary condition). The effects of Wyvern Venom also end as soon as an
army escapes the battlefield or once the battle ends.`,
}, {
    name: 'Wyvern Tactics',
    unique: true,
    description: 'The Wyvern Flight can use the All-Out Assault and Counterattack tactical actions.',
}, {
    name: 'Pitaxian Training',
    unique: true,
    description: 'The Pitaxian Raiders can use the Counterattack, Dirty Fighting, and Feint tactical actions.',
}, {
    name: 'Tusker Training',
    unique: true,
    description: 'The Tusker Riders can use the All-Out Assault, Covering Fire, and Taunt tactical actions.',
}, {
    name: 'Trampling Charge',
    unique: true,
    description: `The Riders trample an enemy army. They attempt a Maneuver check against a target non-engaged army’s Maneuver DC. Trampling
Charge does not trigger Counterattack reactions. 
Critical Success The target army takes 2 points of damage and increases their Shaken value by 1.
Success The target army takes 1 point of damage.
Failure The target army takes 1 point of damage. The Tusker Riders are now engaged with the target army.
Critical Failure The Tusker Riders are now engaged with the target army and are flat-footed until the start of their next turn.`,
}, {
    name: 'Battlefield Adaptability',
    unique: true,
    description: `The First World army has a wide range of creatures in its ranks, and it can shift its tactics to support those with different mobilities. It can take this action to achieve one of the following benefits: ignore ground-based difficult terrain, reduce
Mired to 0, become concealed, or gain a +2 circumstance bonus on Maneuver checks to Advance and to Disengage. The effect lasts until the start of the First World Army’s next turn.`,
}, {
    name: 'Primal Magic',
    traits: ['primal'],
    unique: true,
    description: `Requirement The First World army is not engaged; Effect The First World army uses its primal magic against another army on the battlefield and must attempt a Morale check against the target’s Morale DC. If they succeed, the First World
army applies one of the following effects, determined randomly, to the target army. 
1–2 Entangling Vines Coils of whipping vines grow out of the ground to entangle the army. The army increases its Mired condition by 1 (2 on a critical hit).
3–4 Horrific Visions The soldiers’ minds are assaulted with horrific illusions. The army increases its Shaken condition by 1 (2 on a critical hit).
5–6 Primal Storm A churning storm cloud above the army strikes the soldiers dozens of times with bolts of lightning, inflicting 1 point of damage to the army (2 points of damage on a critical hit). Until the end of the targeted army’s next turn, it functions as if in high wind
and rain (see Battlefield Terrain Features on page 578). 
7–8 Sensory Assault The soldiers become overwhelmed with strange lights or tricked by illusions. Until the end of their next turn, the army takes a –2 circumstance penalty on all attacks and Maneuver checks.
9–10 Overwhelming Magic Roll twice and apply both results, but the First World army suffers a –4 circumstance penalty on its Morale check against the target army’s Morale DC.`,
}, {
    name: 'Supernatural Attacks',
    unique: true,
    traits: ['primal'],
    description: `Whenever a First World army scores a critical hit on another army, it increases that army’s Weary condition by 1 as their
attacks cause some soldiers to become poisoned, fall asleep, shrink in size, or suffer other eerie side effects.`,
}, {
    name: 'Swift Recovery',
    traits: ['primal'],
    unique: true,
    description: `Frequency once per battle; Effect The army calls upon its magical connection to the First World to recover. This either
restores 2 hit points or reduces the army’s Weary condition value by 2.`,
}, {
    name: 'Burning Weaponry',
    unique: true,
    description: 'Unlike basic siege engine armies, Greengripe Bombardiers can make melee Strikes while engaged. When a Greengripe Bombardier scores a critical success on a Strike while taking the Battle action against an army (but not against a fortification), the army struck must make a successful Maneuver check against the Greengripe Bombardiers’ Maneuver DC or take one additional point of damage from the goblins’ burning weaponry.',
}, {
    name: 'Explosive Defeat',
    unique: true,
    description: `When the Greengripe Bombardiers are defeated, their alchemical siege engine explodes. Any army engaged with the Greengripe
Bombardiers must succeed at a Maneuver check against the Greengripe Bombardier’s Maneuver DC or take 1 point of damage (2 points of damage on a critical failure). As long as the Greengripe Bombardiers aren’t destroyed, their siege engine gear is rebuilt automatically once the army loses the defeated condition.`,
}, {
    name: 'Swamp Dwellers',
    unique: true,
    description: 'The lizardfolk defenders have the Live off the Land tactic for free (it does not count against the maximum number of tactics the army can possess), do not treat water or swamp on a battlefield as difficult terrain, and gain a +2 circumstance bonus to Scouting checks attempted in hexes that include swamps or water.',
}, {
    name: 'Amphibious',
    unique: true,
    description: 'Frog Riders ignore difficult terrain caused by swamps or water and gain a +2 circumstance bonus on Maneuver checks while fighting on a battlefield that features either of these terrains.',
}, {
    name: 'Chorus of Croaks',
    unique: true,
    description: 'The terrifying croaking that constantly issues from this army of boggards causes all engaged enemy armies to suffer a –2 status penalty on Morale checks. The M’botuu Frog Riders can use the Taunt tactical action. If they know the Focused Devotion tactic, they treat critical failures made when attempting to Taunt as failures instead.',
}, {
    name: 'Swamp Charge',
    unique: true,
    description: 'If the battlefield includes swamp or water, the M’botuu Frog Riders can start the encounter engaged with a target enemy army. If they do so, they gain a +1 circumstance bonus on melee Strikes against that army on the first turn of the encounter.',
}, {
    name: 'Brave',
    unique: true,
    description: 'Nomen scouts are extraordinarily fearless and do not possess a Rout Threshold, and gain a +2 circumstance bonus on Morale checks made to avoid rout from other sources.',
}, {
    name: 'Self-Sufficient',
    unique: true,
    description: 'Nomen scouts are adept at providing for themselves, and never count against the kingdom’s consumption. Furthermore, they can provide for other armies, and as long as they are not defeated during a Kingdom turn’s Upkeep phase, they reduce the kingdom’s consumption value by 1.',
}, {
    name: 'Trample',
    unique: true,
    description: 'Nomen Scouts can attempt to trample an engaged enemy army by attempting a +20 melee Strike against the army’s AC. On a hit, they inflict 1 point of damage (2 points on a critical hit) and automatically move away from the army—they are no longer engaged with that army.',
}, {
    name: 'Accustomed to Panic',
    unique: true,
    description: `Trigger The Sootscale Warriors’ shaken condition increases while they are not routed. Effect The Sootscales take comfort
in their fear and channel the panic into energy. Rather than increase their shaken condition, they reduce their weary condition value by 1. Additionally, when the Sootscales use the Disengage action, the result is improved one degree.`,
}, {
    name: 'Furious Charge',
    unique: true,
    description: 'Frequency once per war encounter; Effect The Tiger Lord Berserkers charge at an enemy army, taking an Advance action to attempt to engage. If the Tiger Lords manage to engage successfully, they can attempt a melee Strike against the army as a reaction.',
}, {
    name: 'Reactive Rally',
    unique: true,
    description: 'Requirement The Tiger Lords are routed; Effect Instead of taking a Retreat action, the Tiger Lord Berserkers can attempt to Rally.',
}, {
    name: 'Revel in Battle',
    unique: true,
    description: 'Trigger The Tiger Lord Berserkers score a critical hit with a melee Strike. Effect The barbarians are infused with vigor and furious passion at their devastating blow. The army restores 1 hit point.',
}, {
    name: 'Warmongers',
    unique: true,
    description: 'The Tiger Lord Berserkers can use the following Tactical Actions without needing to meet additional requirements: All-Out Assault, Counterattack, and Taunt.',
}, {
    name: 'Hurl Nets',
    unique: true,
    description: 'If the Tok-Nikrat Scouts hit an army with a net Strike, the army takes no damage. Instead, it increases its mired condition value by 1 (or by 2 on a critical success). An army that is mired as a result of hurled nets can attempt to reduce the value of this mired condition (but not that caused by other sources) by attempting a Battle action against the Tok-Nikrat Scout’s Maneuver DC. On a success, the army reduces their mired condition by 1 (or by 2 on a critical success), but on a critical failure, they increase the mired condition caused by the hurled nets by 1. Tok-Nikrat Scouts can’t hurl nets at a target army that is distant.',
}, {
    name: 'Water Retreat',
    unique: true,
    description: 'If the battlefield features water terrain, Tok-Nikrat Scouts gain a +4 circumstance bonus on Maneuver checks made to Disengage.',
}, {
    name: 'Water Stride',
    unique: true,
    description: 'The Tok-Nikrat Scouts stride over watery surfaces, and they ignore difficult terrain caused by water or swamps. They gain a +4 circumstance bonus on initiative checks in war encounters when the battlefield features either terrain.',
}];


export const allTacticsByName: Record<string, ArmyTactics> = Object.fromEntries((allTactics)
    .map((tactic) => [tactic.name, tactic]));




interface MeleeAttack {
    name: string;
    modifier: number;
    plus?: string[];
}

interface RangedAttack extends MeleeAttack {
    shots: number;
}

type ArmyType = 'skirmisher' | 'infantry' | 'cavalry' | 'siege'

interface Army {
    adjustments?: {
        scouting?: number;
        recruitmentDC?: number;
        ac?: number;
        melee?: number;
        ranged?: number;
        morale?: number;
        maneuver?: number;
    },
    alignment?: 'LG' | 'NG' | 'CG' | 'LN' | 'N' | 'CN' | 'LE' | 'NE' | 'CE';
    rarity?: 'common' | 'uncommon' | 'rare' | 'unique';
    scouting: number;
    maneuver: number;
    morale: number;
    recruitmentDC?: number;
    consumption?: number;
    description: string;
    ac: number;
    hp: number;
    routeThreshold?: number;
    level: number;
    name: string;
    melee?: MeleeAttack;
    ranged?: RangedAttack;
    type: ArmyType;
    gear?: ArmyGear[];
    conditions?: ArmyCondition[];
    tactics?: ArmyTactics[];
    specialAbilities?: any;
}




const armies: Army[] = [{
    name: 'Infantry',
    recruitmentDC: 15,
    consumption: 1,
    description: 'This is a platoon of armored soldiers armed with melee weapons.',
    ac: 16,
    maneuver: 4,
    morale: 10,
    hp: 4,
    melee: {
        name: 'Weapons',
        modifier: 9,
    },
    level: 1,
    scouting: 7,
    type: 'infantry',
}, {
    name: 'Cavalry',
    level: 3,
    scouting: 9,
    type: 'cavalry',
    consumption: 2,
    recruitmentDC: 18,
    ac: 19,
    hp: 4,
    melee: {
        name: 'Weapons',
        modifier: 12,
    },
    morale: 6,
    maneuver: 12,
    description: 'Cavalry consists of armored soldiers armed with melee weapons and mounted on horses.',
    tactics: [allTacticsByName['Overrun']],
}, {
    name: 'Skirmishers',
    level: 5,
    type: 'skirmisher',
    scouting: 12,
    recruitmentDC: 20,
    consumption: 1,
    description: 'Skirmishers are lightly armored, but their ability to move quickly and to focus on individual tactics rather than working as a unit make them more resilient in other ways. A skirmisher army’s AC is two lower than normal for its level, but its Maneuver and Morale are two higher than normal for its level.',
    ac: 20,
    maneuver: 17,
    morale: 11,
    hp: 4,
    melee: {
        modifier: 15,
        name: 'Weapons',
    },
}, {
    name: 'Siege Engines',
    level: 7,
    type: 'siege',
    scouting: 15,
    recruitmentDC: 23,
    consumption: 1,
    description: 'A siege engine army consists of several catapults, ballistae, trebuchets, or other mechanized engines of war.',
    ac: 25,
    maneuver: 12,
    morale: 18,
    hp: 6,
    ranged: {
        name: 'Siege Engine',
        modifier: 15,
        shots: 5,
    },
    tactics: [allTacticsByName['Engines of War']],
}, {
    name: 'Tatzlford Town Guard',
    alignment: 'NG',
    type: 'infantry',
    level: 7,
    scouting: 15,
    consumption: 1,
    description: 'Tatzlford’s town guards have been organized into an impromptu army armed with longswords and led by Captain Coren Lawry.',
    ac: 25,
    maneuver: 12,
    morale: 18,
    hp: 5,
    routeThreshold: 2,
    melee: {
        modifier: 18,
        name: 'Longswords',
    },
    tactics: [allTacticsByName['Hold the Line'], allTacticsByName['Toughened Soldiers']],
}, {
    name: 'Narlmarch Hunters',
    level: 6,
    alignment: 'NG',
    type: 'skirmisher',
    scouting: 14,
    consumption: 1,
    description: 'This army is a band of hunters and trappers who have gathered into a ragtag group of archers led by Mayor Loy Rezbin.',
    ac: 24,
    maneuver: 17,
    morale: 11,
    hp: 4,
    melee: {
        name: 'Hatchets and Shortswords',
        modifier: 15,
    },
    ranged: {
        name: 'Longbows',
        modifier: 18,
        shots: 7,
    },
    tactics: [allTacticsByName['Efficient Ammunition'], allTacticsByName['Sharpshooter']],
}, {
    name: 'Drelev Irregulars',
    level: 7,
    alignment: 'CE',
    type: 'infantry',
    scouting: 15,
    description: `The Drelev Irregulars are composed of equal parts Tiger Lords and mercenaries who were once bandits. Their fighting style is more akin to a mob than a disciplined force; while this allows the irregulars an advantage in mobility, it decentralizes their command
structure and lessens their morale.`,
    ac: 25,
    maneuver: 19,
    morale: 11,
    hp: 4,
    melee: {
        name: 'Swords and Axes',
        modifier: 18,
    },
    tactics: [allTacticsByName['Tactical Training']],
}, {
    name: 'Troll Marauders',
    level: 8,
    alignment: 'NE',
    type: 'infantry',
    scouting: 14,
    description: 'There are only a few dozen trolls in this army, but their ferocity and regenerative capability make them a dangerous force nonetheless.',
    ac: 28,
    maneuver: 13,
    morale: 19,
    hp: 5,
    routeThreshold: 1,
    melee: {
        name: 'Claws and Fangs',
        modifier: 20,
    },
    tactics: [
        allTacticsByName['Brutal Assault'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Frightening Foe'],
        allTacticsByName['Regeneration'],
    ],
}, {
    name: 'Pitaxian Raiders',
    level: 12,
    alignment: 'CN',
    type: 'skirmisher',
    scouting: 22,
    description: 'The Pitaxian Raiders consist of a mix of Pitax city guards and mercenaries eager for battle.',
    ac: 33,
    maneuver: 25,
    morale: 19,
    hp: 4,
    melee: {
        name: 'Swords',
        modifier: 26,
    },
    ranged: {
        name: 'Longbows',
        modifier: 24,
        shots: 7,
    },
    tactics: [allTacticsByName['Pitaxian Training']],
}, {
    name: 'Tusker Riders',
    level: 14,
    type: 'cavalry',
    alignment: 'CE',
    scouting: 24,
    description: 'The hill giant warlord Kob Moleg sent these mammoth-mounted hill giants to King Irovetti as a gift.',
    ac: 35,
    maneuver: 22,
    morale: 28,
    hp: 8,
    melee: {
        name: 'Morningstars and Tusks',
        modifier: 30,
    },
    ranged: {
        name: 'Thrown Rock',
        modifier: 28,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Tusker Training'],
        allTacticsByName['Trampling Charge'],
        allTacticsByName['Darkvision'],
    ],
}, {
    name: 'Wyvern Flight',
    level: 12,
    alignment: 'CE',
    type: 'skirmisher',
    scouting: 23,
    description: 'This flight of wyverns has been trained to obey orders in battle',
    ac: 33,
    maneuver: 25,
    morale: 17,
    hp: 4,
    routeThreshold: 3,
    melee: {
        name: 'Fangs, Claws and Stingers',
        modifier: 18,
        plus: ['Wyvern Venom'],
    },
    tactics: [
        allTacticsByName['Darkvision'],
        allTacticsByName['Flight'],
        allTacticsByName['Wyvern Venom'],
        allTacticsByName['Wyvern Tactics'],
    ],
}, {
    name: 'Pitax Horde',
    level: 10,
    alignment: 'CN',
    type: 'infantry',
    scouting: 19, // TODO: 22 in encounter site gl2,
    description: 'The Pitax Horde consists of human warriors from seven different minor clans in Glenebon.',
    ac: 30,
    maneuver: 22, // TODO: 24 in encounter site gl2
    morale: 16,
    hp: 5,
    routeThreshold: 2,
    melee: {
        name: 'Greater Magic Axes',
        modifier: 25,
    },
    tactics: [
        allTacticsByName['Live off the Land'],
        allTacticsByName['Merciless'],
        allTacticsByName['Toughened Soldiers'],
    ],
}, {
    name: 'Pitax War Machines',
    level: 14,
    alignment: 'CN',
    type: 'siege',
    scouting: 25,
    description: 'These siege weapons are positioned atop all of the city’s watchtowers and afford a controlling view of all approaches.',
    ac: 36,
    maneuver: 22,
    morale: 28,
    hp: 8,
    ranged: {
        name: 'Siege Engine',
        modifier: 29,
        shots: 7,
    },
    tactics: [
        allTacticsByName['Efficient Ammunition'],
        allTacticsByName['Explosive Shot'],
        allTacticsByName['Toughened Soldiers'],
        allTacticsByName['Toughened Soldiers'],
    ],
}, {
    name: 'First World Army',
    level: 16,
    rarity: 'rare',
    alignment: 'CE',
    type: 'skirmisher',
    scouting: 28,
    description: 'This army is composed of an eclectic mix of fey, beasts, and plants—a supernatural mob of monsters with a wide range of options in battle.',
    ac: 38,
    maneuver: 30,
    morale: 25,
    hp: 6,
    melee: {
        name: 'Weapons and Claws',
        modifier: 33,
    },
    ranged: {
        name: 'Bows and Hurled Thorns',
        modifier: 33,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Battlefield Adaptability'],
        allTacticsByName['Primal Magic'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Supernatural Attacks'],
        allTacticsByName['Swift Recovery'],
        {
            name: 'Tactical Training',
            unique: true,
            description: 'The First World Army can use the following tactical actions: counterattack, dirty fighting, feint, and taunt.',
        },
    ],
}, {
    name: 'Greengripe Bombardiers',
    level: 7,
    rarity: 'rare',
    type: 'siege',
    adjustments: {
        scouting: -2,
        recruitmentDC: 5,
        ac: -2,
        melee: 1,
    },
    scouting: 13,
    recruitmentDC: 28,
    consumption: 2,
    description: 'Greengripe goblins have built a mobile platform outfitted with a catapult-like flinging arm that can throw flammable debris.',
    ac: 23,
    maneuver: 12,
    morale: 18,
    hp: 6,
    melee: {
        name: 'Dogslicers and Torches',
        modifier: 19,
    },
    ranged: {
        name: 'Burning Debris',
        modifier: 18,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Burning Weaponry'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Explosive Defeat'],
    ],
}, {
    name: 'Lizardfolk Defenders',
    level: 5,
    rarity: 'uncommon',
    type: 'skirmisher',
    adjustments: {
        scouting: 2,
        recruitmentDC: 2,
        ac: 1,
    },
    scouting: 14,
    recruitmentDC: 22,
    consumption: 1,
    description: 'These lizardfolk are from the settlement on the banks of Candlemere (encounter site KL3); they fight with flails and javelins.',
    ac: 23,
    maneuver: 9,
    morale: 15,
    hp: 4,
    melee: {
        name: 'Flails',
        modifier: 12,
    },
    ranged: {
        name: 'Javelins',
        modifier: 12,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Swamp Dwellers'],
    ],
}, {
    name: 'M\'Botuu Frog Riders',
    level: 10,
    rarity: 'rare',
    type: 'cavalry',
    adjustments: {
        recruitmentDC: 5,
        morale: 2,
        melee: 2,
    },
    scouting: 19,
    recruitmentDC: 32,
    consumption: 2,
    description: 'These lance-armed boggards from M’botuu and ride giant frogs trained for warfare into battle.',
    ac: 27,
    maneuver: 22,
    morale: 18,
    hp: 6,
    melee: {
        name: 'Lances',
        modifier: 25,
    },
    ranged: {
        name: 'Javelins',
        modifier: 23,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Amphibious'],
        allTacticsByName['Chorus of Croaks'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Swamp Charge'],
    ],
}, {
    name: 'Nomen Scouts',
    level: 8,
    rarity: 'uncommon',
    type: 'cavalry',
    adjustments: {
        scouting: 2,
        maneuver: 1,
        recruitmentDC: 2,
    },
    scouting: 18,
    recruitmentDC: 26,
    consumption: -1,
    description: 'This band of Nomen centaurs fight with spears and longbows.',
    ac: 27,
    maneuver: 14,
    morale: 19,
    hp: 4,
    melee: {
        name: 'Spears',
        modifier: 20,
    },
    ranged: {
        name: 'Longbows',
        modifier: 20,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Brave'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Self-Sufficient'],
        allTacticsByName['Trample'],
    ],
}, {
    name: 'Sootscale Warriors',
    level: 3,
    rarity: 'uncommon',
    type: 'infantry',
    adjustments: {
        recruitmentDC: 2,
        ac: 1,
        maneuver: 2,
        morale: -1,
    },
    scouting: 9,
    recruitmentDC: 20,
    consumption: 1,
    description: 'Sootscale kobolds fight with shortswords and crossbows, although they tend to do so warily and cautiously.',
    ac: 20,
    maneuver: 14,
    morale: 5,
    hp: 4,
    routeThreshold: 3,
    melee: {
        name: 'Shortswords',
        modifier: 12,
    },
    ranged: {
        name: 'Crossbows',
        modifier: 12,
        shots: 7,
    },
    tactics: [
        allTacticsByName['Accustomed to Panic'],
        allTacticsByName['Darkvision'],
    ],
}, {
    name: 'Tiger Lord Berserkers',
    level: 12,
    rarity: 'uncommon',
    type: 'infantry',
    adjustments: {
        scouting: 1,
        recruitmentDC: 2,
        ac: -1,
        melee: 2,
    },
    scouting: 23,
    recruitmentDC: 32,
    consumption: 1,
    description: 'These Tiger Lord barbarians use rage in battle; they fight with greataxes.',
    ac: 32,
    maneuver: 19,
    morale: 25,
    hp: 6,
    routeThreshold: 2,
    melee: {
        name: 'Greataxes',
        modifier: 28,
    },
    tactics: [
        allTacticsByName['Furious Charge'],
        allTacticsByName['Reactive Rally'],
        allTacticsByName['Revel in Battle'],
        allTacticsByName['Warmongers'],
    ],
}, {
    name: 'Tok-Nikrat Scouts',
    level: 10,
    rarity: 'rare',
    type: 'skirmisher',
    adjustments: {
        scouting: 1,
        recruitmentDC: 5,
        ac: 1,
        morale: 2,
    },
    scouting: 20,
    recruitmentDC: 32,
    consumption: 1,
    description: 'Capable of striding across water, these bog striders from the settlement of Tok-Nikrat fight with nets and spears.',
    ac: 31,
    maneuver: 22,
    morale: 18,
    hp: 4,
    melee: {
        name: 'Spears',
        modifier: 23,
    },
    ranged: {
        name: 'Net',
        modifier: 23,
        shots: 5,
    },
    tactics: [
        allTacticsByName['Darkvision'],
        allTacticsByName['Hurl Nets'],
        allTacticsByName['Water Retreat'],
        allTacticsByName['Water Stride'],
    ],
}];
