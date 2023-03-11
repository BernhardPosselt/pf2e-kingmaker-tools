import {capitalize} from '../utils';

export interface Fortification {
    name: string;
    ac: number;
    hp: number;
    maximumArmies: number;
}

export const allFortifications: Fortification[] = [
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
    {level: 9, scouting: 18, standardDC: 26, ac: 28, highSave: 21, lowSave: 15, attack: 21, maximumTactics: 3},
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

export const armyStatisticsByLevel: Map<number, ArmyStatistic> = new Map<number, ArmyStatistic>();
allArmyStatistics.forEach(army => armyStatisticsByLevel.set(army.level, army));

export const allArmyActionNames = [
    'Advance',
    'Battle',
    'Disengage',
    'Guard',
    'Rally',
    'Retreat',
    'All-Out Assault',
    'Battlefield Medicine',
    'Counterattack',
    'Covering Fire',
    'Defensive Stance',
    'Dirty Fighting',
    'False Retreat',
    'Feint',
    'Outflank',
    'Overwhelming Bombardment',
    'Taunt',
    'Trampling Charge',
    'Trample',
    'Swift Recovery',
    'Accustomed to Panic',
    'Furious Charge',
    'Reactive Rally',
    'Revel in Battle',
    'Battlefield Adaptability',
] as const;

export type ArmyActionName = typeof allArmyActionNames[number];

export interface ArmyAction {
    name: ArmyActionName;
    trigger?: string;
    type?: 'attack' | 'morale' | 'maneuver';
    actions: '1' | '2' | '3' | 'r';
    requiresUnlock?: boolean;
    restrictedTypes?: ArmyType[];
    description?: string;
    requirements?: string;
    criticalSuccess?: string;
    success?: string;
    failure?: string;
    criticalFailure?: string;
    frequency?: string;
    effect?: string;
}

export const allArmyActions: ArmyAction[] = [{
    name: 'Advance',
    actions: '1',
    type: 'maneuver',
    description: 'Your army attempts to close the distance with a target enemy army it is not engaged with by attempting a Maneuver check.',
    criticalSuccess: 'The enemy army becomes engaged with your army, even if it previously had the distant condition (in which case it loses that condition and becomes engaged).',
    success: 'If the target army is distant, it loses that condition; otherwise, it becomes engaged.',
    failure: 'Your army’s attempt to advance fails.',
    criticalFailure: 'Your army’s attempt to advance fails, and it becomes disorganized, becoming mired 1 until the start of its next turn.',
}, {
    name: 'Battle',
    actions: '1',
    description: 'Your army attacks an enemy army with a Strike against the enemy army’s AC. You can do so with a melee Strike only if you are engaged with the target army. Otherwise, you must use a ranged Strike. An army can attempt a maximum of 5 ranged Strikes per war encounter (unless it has the Increased Ammunition tactic). As with any attack, multiple Strikes in a single round suffer a multiple attack penalty. A siege engine can use the Battle action to attack and damage a fortification.',
    type: 'attack',
    criticalSuccess: 'You deal 2 points of damage to the army.',
    success: 'You deal 1 point of damage to the army.',
}, {
    name: 'Disengage',
    actions: '2',
    description: 'Your army attempts to disengage from enemy armies to put some distance between itself and the enemy. Attempt a Maneuver check against each army your army is engaged with.',
    type: 'maneuver',
    criticalSuccess: 'Your army is no longer engaged with the target army. In addition, your army is automatically no longer engaged with any armies you haven’t yet rolled a Maneuver check against during this war action.',
    success: 'Your army breaks free and is no longer engaged with the target army.',
    failure: 'Your army remains engaged with the target army.',
    criticalFailure: 'Your army remains engaged with the target army and, for the remainder of this turn, your army cannot attempt to disengage from any army with which it is still engaged.',
}, {
    name: 'Guard',
    actions: '1',
    description: 'Your army spends a war action to adopt a defensive pose—raising shields, focusing on parrying attacks, or seeking cover. Attempt a Maneuver check against a target army.',
    type: 'maneuver',
    criticalSuccess: 'Your army gains a +2 item bonus to its AC until the start of your next turn; this bonus applies to all attacks against this army, not just from the targeted army.',
    success: 'Your army gains a +2 item bonus to its AC until the start of your next turn against attacks from the target army.',
    failure: 'Your army fails to guard against the target army.',
    criticalFailure: 'Your army fails spectacularly to guard against the target army and becomes mired 1.',
}, {
    name: 'Rally',
    actions: '2',
    description: 'Your army’s leaders attempt to bolster the soldiers’ morale and fight back the effects of fear and panic. Attempt a Morale check against a target enemy army of your choice.',
    type: 'morale',
    criticalSuccess: 'If your army is routed, it loses the routed condition. Reduce your army’s shaken condition by 2.',
    success: 'Reduce your army’s shaken condition by 1.',
    criticalFailure: 'Your attempt to rally backfires—increase your army’s shaken condition by 1.',
}, {
    name: 'Retreat',
    requirements: 'Your army is not engaged.',
    actions: '1',
    description: 'Your army tries to escape from the battlefield. If your army is already distant, it flees the battlefield, is no longer part of the war encounter, and becomes routed. Otherwise, your army gains the distant condition.',
}, {
    name: 'All-Out Assault',
    actions: '2',
    requiresUnlock: true,
    restrictedTypes: ['cavalry', 'infantry'],
    description: 'Your army attacks with frightening vigor. Attempt a melee Strike against an enemy army’s AC.',
    type: 'attack',
    criticalSuccess: 'Your army inflicts 3 points of damage to the target army. If your army’s next war action this turn is an attack war action against a different target army, you gain a +1 circumstance bonus to the Strike as your fury continues to the new target.',
    success: 'Your army deals 2 points of damage to the target army.',
    failure: 'Your army falters, but still deals 1 point of damage to the target army.',
    criticalFailure: 'Your army deals no damage to the target army and becomes outflanked until the start of its next turn.',
}, {
    name: 'Battlefield Medicine',
    actions: '3',
    requiresUnlock: true,
    restrictedTypes: ['skirmisher', 'infantry'],
    description: 'Your army attempts to patch up an allied army’s wounds during battle. Once you attempt this war action on an army, that army is temporarily immune to Battlefield Medicine for the remainder of the war encounter. Attempt a DC 25 Scouting check to successfully sort the army’s wounded and provide swift aid.',
    criticalSuccess: 'You restore 2 HP to the target army.',
    success: 'You restore 1 HP to the target army',
    criticalFailure: 'Your attempt to heal the army fails, and that army’s weary condition value increases by 1.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['skirmisher', 'infantry'],
    name: 'Counterattack',
    trigger: 'An army you are engaged with attempts a maneuver war action.',
    actions: 'r',
    description: 'Your army lashes out at the foe as they attempt to perform a maneuver. Attempt a melee Strike against the triggering army’s AC. Counterattack doesn’t count toward your multiple attack penalty, and your multiple attack penalty doesn’t apply to this Strike.',
    criticalSuccess: `You inflict 1 point of damage on the
army and increase its shaken condition value by 1.`,
    success: 'You inflict 1 point of damage on the army.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['skirmisher', 'infantry', 'cavalry'],
    name: 'Covering Fire',
    actions: '2',
    description: 'Your army’s ranged fire provides cover and protection for an allied army to maneuver. Attempt a ranged Strike against a target army’s AC.',
    type: 'attack',
    criticalSuccess: 'You inflict 2 points of damage to the target army, and it cannot take reactions triggered by maneuver war actions from any army until the start of your next turn.',
    success: 'You inflict 1 point of damage to the target army, and it can’t take reactions triggered by maneuver war actions from any army until the start of your next turn.',
    failure: 'Your attack fails to provide covering fire, but you inflict 1 point of damage to the target army.',
    criticalFailure: 'Your attempt fails.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['infantry'],
    name: 'Defensive Stance',
    actions: '2',
    description: 'Your army hunkers down behind its shields, presents pole arms in a wall of blades, or moves into position to protect a target allied army that is outflanked. Attempt a Maneuver check against an enemy army.',
    criticalSuccess: 'The target allied army is no longer outflanked by any army.',
    success: 'The target allied army is no longer outflanked by the target army.',
    criticalFailure: 'Your defensive stance fails, and your army is now outflanked by the target enemy army.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['skirmisher'],
    name: 'Dirty Fighting',
    actions: '1',
    description: 'Your army uses trickery, deception, and unfair tactics to attempt a devastating attack against an outflanked army. Attempt a melee Strike or a ranged Strike against the AC of a target outflanked army that is not distant.',
    type: 'attack',
    criticalSuccess: 'The target army becomes weary 2 until the start of your next turn.',
    success: 'The target army becomes weary 1 until the start of your next turn.',
    criticalFailure: 'Your attack deals no damage to the target army, which is emboldened by your failed attempt at dirty fighting. This reduces the target army’s weary value by 1.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['skirmisher', 'infantry'],
    name: 'False Retreat',
    actions: '1',
    trigger: 'Your army succeeds at a morale check.',
    description: 'Your army feigns defeat to trick an enemy army. Attempt a Morale check against a target army.',
    type: 'morale',
    criticalSuccess: 'The target army is caught off guard by your army’s deception. It becomes outflanked and is unable to take reactions until the start of your next turn.',
    success: 'The target army is caught off guard by your army’s deception and is outflanked until the start of its next turn.',
    criticalFailure: 'The enemy anticipated your tactic and moves to take advantage of the situation. Your army becomes outflanked until the start of your next turn.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['skirmisher', 'infantry'],
    name: 'Feint',
    actions: '1',
    description: 'Your army launches a probing attack meant to trick the enemy into thinking you are attacking from one quarter while your real thrust comes elsewhere.',
    type: 'attack',
    criticalSuccess: `The target army’s defenses are thrown
off; it is outflanked until the end of your turn.`,
    success: 'The target army is fooled, but only momentarily. It is outflanked against the next melee Strike your army attempts against it before the end of your current turn.',
    criticalFailure: 'The enemy anticipates your feint and presses the advantage. You are outflanked by the target army until the end of your next turn.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['skirmisher', 'cavalry'],
    name: 'Outflank',
    actions: '2',
    requirements: 'You aren\'t engaged',
    description: 'You send your army around an enemy’s flank to get a better attacking position and to push your enemy into disorder. Attempt a Maneuver check against the target army.',
    type: 'maneuver',
    criticalSuccess: 'The target army becomes outflanked until the start of your next turn. You can choose to become engaged with that army or not.',
    success: 'The target army is outflanked until the start of your next turn. You are now engaged with that army.',
    criticalFailure: 'You underestimate the target army’s position, and the blunder causes your army to become outflanked until the start of your next turn.',
}, {
    requiresUnlock: true,
    restrictedTypes: ['siege'],
    name: 'Overwhelming Bombardment',
    actions: '2',
    description: `Your siege engines focus all their fire on a fortification. This war action counts as using two ranged Strikes for
the purposes of depleting an army’s shots. Attempt a ranged Strike against the target fortification’s AC.`,
    type: 'attack',
    criticalSuccess: 'You deal 2 points of damage to the fortification. You also deal 1 point of damage to up to two armies of your choice that are within the fortification.',
    success: 'You deal 1 point of damage to the fortification, and an additional 1 point of damage either to the fortification or to an army within the fortification (your choice of which).',
    failure: 'You deal 1 damage to the fortification.',
    criticalFailure: 'You deal no damage, and your army becomes outflanked until the start of its next turn.',
}, {
    name: 'Taunt',
    requiresUnlock: true,
    actions: '1',
    description: `Your army attempts to frighten and cow an enemy army.
Attempt a Morale check against the target army.`,
    type: 'morale',
    criticalSuccess: 'The target army becomes shaken 2 until the start of your next turn.',
    success: 'The target army becomes shaken 1 until the start of your next turn.',
    criticalFailure: 'Your failed attempt bolsters the enemy’s spirits. This reduces the target army’s shaken value by 1',
}, {
    name: 'Trampling Charge',
    type: 'maneuver',
    actions: '3',
    requiresUnlock: true,
    description: 'The Riders trample an enemy army. They attempt a Maneuver check against a target non-engaged army’s Maneuver DC. Trampling Charge does not trigger Counterattack reactions.',
    criticalSuccess: 'Critical Success The target army takes 2 points of damage and increases their Shaken value by 1.',
    success: 'Success The target army takes 1 point of damage.',
    failure: 'Failure The target army takes 1 point of damage. The Tusker Riders are now engaged with the target army.',
    criticalFailure: 'Critical Failure The Tusker Riders are now engaged with the target army and are flat-footed until the start of their next turn.',
}, {
    name: 'Swift Recovery',
    requiresUnlock: true,
    actions: '2',
    frequency: 'Once Per Battle',
    effect: 'The army calls upon its magical connection to the First World to recover. This either restores 2 hit points or reduces the army’s Weary condition value by 2.',
}, {
    name: 'Trample',
    requiresUnlock: true,
    actions: '3',
    description: 'Nomen Scouts can attempt to trample an engaged enemy army by attempting a +20 melee Strike against the army’s AC. On a hit, they inflict 1 point of damage (2 points on a critical hit) and automatically move away from the army—they are no longer engaged with that army.',
}, {
    name: 'Accustomed to Panic',
    requiresUnlock: true,
    actions: 'r',
    trigger: 'The Sootscale Warriors’ shaken condition increases while they are not routed',
    effect: 'The Sootscales take comfort in their fear and channel the panic into energy. Rather than increase their shaken condition, they reduce their weary condition value by 1. Additionally, when the Sootscales use the Disengage action, the result is improved one degree.',
}, {
    name: 'Furious Charge',
    requiresUnlock: true,
    actions: '1',
    frequency: 'Oncer Per War Encounter',
    effect: 'The Tiger Lord Berserkers charge at an enemy army, taking an Advance action to attempt to engage. If the Tiger Lords manage to engage successfully, they can attempt a melee Strike against the army as a reaction.',
}, {
    name: 'Reactive Rally',
    requiresUnlock: true,
    actions: '3',
    requirements: 'The Tiger Lords are routed;',
    effect: 'Instead of taking a Retreat action, the Tiger Lord Berserkers can attempt to Rally.',
}, {
    name: 'Revel in Battle',
    requiresUnlock: true,
    actions: 'r',
    trigger: 'The Tiger Lord Berserkers score a critical hit with a melee Strike.',
    effect: 'The barbarians are infused with vigor and furious passion at their devastating blow. The army restores 1 hit point.',
}, {
    name: 'Battlefield Adaptability',
    requiresUnlock: true,
    actions: '1',
    description: 'The First World army has a wide range of creatures in its ranks, and it can shift its tactics to support those with different mobilities. It can take this action to achieve one of the following benefits: ignore ground-based difficult terrain, reduce Mired to 0, become concealed, or gain a +2 circumstance bonus on Maneuver checks to Advance and to Disengage. The effect lasts until the start of the First World Army’s next turn.',
}];

type ModifierType = 'circumstance' | 'item' | 'status' | 'untyped';

type ModifierSelector = 'ac'
    | 'morale'
    | 'maneuver'
    | 'attack'
    | 'ranged-attack'
    | 'melee-attack'
    | 'route-threshold'
    | 'shots'
    | 'hp'
    | 'initiative'
    | 'consumption'
    | 'scouting';

interface Brackets {
    start: number;
    end?: number;
    value: number;
}

interface ArmyModifier {
    target?: 'self' | 'attacker';
    value: number | string | Brackets[];
    type: ModifierType;
    label?: string;
    selector: ModifierSelector;
    toggle?: string;
    action?: ArmyActionName;
    targetTypes?: ArmyType[];
    enabled?: boolean;
}

export const allConditionNames = [
    'concealed',
    'defeated',
    'destroyed',
    'distant',
    'efficient',
    'engaged',
    'fortified',
    'lost',
    'mired',
    'outflanked',
    'pinned',
    'routed',
    'shaken',
    'weary',
] as const;

type ArmyConditionName = typeof allConditionNames[number];


export interface ConditionPredicate {
    name: ArmyConditionName,
    min: number;
}

export interface ArmyCondition {
    name: ArmyConditionName;
    description: string;
    value?: number;
    grantConditions?: ConditionPredicate[],
    modifiers?: ArmyModifier[];
}

export const allArmyConditions: ArmyCondition[] = [{
    name: 'concealed',
    description: 'A concealed army is tougher to target, and gains a +2 circumstance bonus on its Maneuver checks. Attacks against it take a –2 circumstance penalty. This condition lasts as long as the event granting the concealment persists.',
    modifiers: [{
        value: 2,
        type: 'circumstance',
        selector: 'maneuver',
    }, {
        target: 'attacker',
        value: -2,
        selector: 'attack',
        type: 'circumstance',
    }],
}, {
    name: 'defeated',
    description: 'When an army has zero Hit Points, it becomes defeated. A defeated army cannot take war actions. A defeated army can be restored to 1 Hit Point with the Recover Army activity (although the basic DC is increased by 5 for this check). Any effect that restores a defeated army to at least 1 Hit Point removes the defeated condition. A defeated army can only be moved one hex at a time with the Deploy Army activity. A defeated army can be Disbanded normally. It can’t be used for any other Army activity as long as it remains defeated. If a defeated army takes damage, it must succeed at a DC 16 flat check or be destroyed. If all armies on a side are defeated, those armies are destroyed.',
}, {
    name: 'destroyed',
    description: 'The army has been completely devastated, and it cannot be restored—it can only be replaced by a new army. Any gear the army had is ruined.',
}, {
    name: 'distant',
    description: 'An army that has the distant condition has managed to retreat a fair range away from enemy armies, and is potentially poised to make an escape from the field of battle. Armies can attempt ranged Strikes against distant armies, but they take a –5 penalty on that Strike.',
    modifiers: [{
        target: 'attacker',
        selector: 'ranged-attack',
        type: 'untyped',
        value: -5,
    }],
}, {
    name: 'efficient',
    description: `The army has performed an Army activity with such speed that it can be used to attempt a second Army activity immediately, but doing so causes it to lose the efficient condition. The second Army activity suffers a –5 penalty on its check, and
the result of this second Army activity check cannot grant the efficient condition. If the army doesn’t attempt a second Army activity, it instead loses the efficient condition and reduces the value of one condition of its choice by 1.`,
}, {
    name: 'engaged',
    description: 'An army that is in close combat with one or more enemy armies becomes engaged. An army must be engaged in order to attempt melee Strikes. If an army is engaged and attempts a maneuver war action that would cause it to disengage, it provokes reactions from any enemy armies they were engaged with.',
}, {
    name: 'fortified',
    description: `The army is in a defensive position as the result of a Garrison Army activity. While fortified, enemy armies cannot engage the army and the army cannot engage enemy armies. A fortified army gains a +4 item bonus to its AC and to Morale checks made
to rally. A fortified army that uses a maneuver war action immediately loses its fortified condition.`,
    modifiers: [{
        value: 4,
        type: 'item',
        selector: 'ac',
    }, {
        value: 4,
        type: 'item',
        selector: 'morale',
        action: 'Rally',
    }],
}, {
    name: 'lost',
    description: `When an army’s attempt to deploy to a new location fails, it can become lost. A lost army can take no Army activity other than Recover, and that only in an attempt to remove the lost condition. When an army recovers from the lost condition,
the GM decides what the army’s new location is (typically this is at an approximate midpoint between the army’s starting point and its intended destination).`,
}, {
    name: 'mired',
    description: 'The army’s movement is severely impaired. It may be bogged down in mud, snow, underbrush, rubble, or similar terrain, encumbered by carrying heavy burdens, or any other reason. Mired always has a value. A mired army takes a circumstance penalty on all maneuvers equal to its mired value and to Deploy Army checks. If an army ever becomes mired 4, it becomes pinned.',
    // TODO: circumstance penalty to deploy army checks equal to mired value
    value: 1,
    modifiers: [{
        value: '$value',
        type: 'circumstance',
        selector: 'maneuver',
    }],
    grantConditions: [{
        min: 4,
        name: 'pinned',
    }],
}, {
    name: 'outflanked',
    description: 'The army has enemies coming at it from many directions and must split its forces to deal with threats on every side. The army takes a –2 circumstance penalty to its AC.',
    modifiers: [{
        value: -2,
        type: 'circumstance',
        selector: 'ac',
    }],

}, {
    name: 'pinned',
    description: 'The army and cannot move freely. It has the outflanked condition and cannot use any maneuver war actions. A pinned army cannot be deployed.',
    grantConditions: [{
        name: 'outflanked',
        min: 0,
    }],
    modifiers: [],
}, {
    name: 'routed',
    description: 'The army retreats, whether due to magical compulsion or simply broken morale. On its turn, a routed army must use the Retreat war action. While routed, the army takes a –2 circumstance penalty to Morale checks. This condition ends automatically once a war encounter is resolved, but the routed army increases its shaken value by 1 in this case. If all armies on one side of a battle are routed simultaneously, the battle ends and the other army is victorious.',
    value: 1,
    modifiers: [{
        value: -2,
        type: 'circumstance',
        selector: 'morale',
    }],
}, {
    name: 'shaken',
    description: `The army’s morale has begun to falter, be it fear in the face of a powerful enemy, a supernatural effect such as a dragon’s frightful presence, or simply the result of ill fortune in the tide of battle. Shaken always has a numerical value.
The army’s Morale checks take a circumstance penalty equal to its shaken value, and whenever the army takes damage, it must succeed on a DC 11 flat check or its shaken value increases by 1. An army that becomes shaken 4 is automatically routed. An army reduces the value of this condition by 1 each Kingdom turn that passes during which it does not attempt an Army activity or engage in a war encounter.`,
    value: 1,
    grantConditions: [{
        name: 'routed',
        min: 4,
    }],
    modifiers: [{
        selector: 'morale',
        value: '$value',
        type: 'circumstance',
    }],
}, {
    name: 'weary',
    description: 'The army is exhausted. Weary always has a numerical value. A weary army takes a circumstance penalty equal to its weary value to its AC, to its Maneuver checks, and to its Army activity checks; it takes double this circumstance penalty on Deploy Army checks. An army reduces the value of this condition by 1 each Kingdom turn that passes during which it does not attempt an Army activity or engage in a war encounter.',
    value: 1,
    // TODO circumstance penalty equal to value army activities, double on deploy army
    modifiers: [{
        selector: 'ac',
        value: '$value',
        type: 'circumstance',
    }, {
        selector: 'maneuver',
        value: '$value',
        type: 'circumstance',
    }],
}];

// TODO: terrain

export interface ArmyGear {
    name: string;
    level: number;
    price: number;
    traits: string[];
    description: string;
    type?: string;
    quantity?: number;
    modifiers?: ArmyModifier[];
}

function createMagicWeapon(
    {
        bonus,
        type,
        level,
        price,
    }: {
        bonus: number,
        type: 'ranged' | 'melee',
        level: number,
        price: number
    }
): ArmyGear {
    return {
        name: `Magic Weapons +${bonus} (${capitalize(type)})`,
        traits: ['army', 'evocation', 'magical'],
        type: 'magic-weapons',
        level,
        price,
        description: `The army’s weapons are magic. If the army has melee and ranged weapons, choose which one is made magic when
this gear is purchased. You can buy this gear twice—once for melee weapons and once for ranged weapons. If you purchase a more powerful version, it replaces the previous version, and the RP cost of the more powerful version is reduced by the RP cost of the replaced weapons. These weapons increase the army’s Strike with that weapon by ${bonus}.`,
        modifiers: [{
            type: 'item',
            value: bonus,
            selector: type === 'ranged' ? 'ranged-attack' : 'melee-attack',
        }],
    };
}

function createMagicArmor(
    {
        bonus,
        level,
        price,
    }: {
        bonus: number,
        level: number,
        price: number
    }
): ArmyGear {
    return {
        name: `Magic Armor +${bonus}`,
        traits: ['army', 'evocation', 'magical'],
        type: 'magic-weapons',
        level,
        price,
        description: `Magic armor is magically enchanted to bolster the protection it affords to the soldiers. This armor increases the army’s AC by ${bonus}.`,
        modifiers: [{
            type: 'item',
            value: bonus,
            selector: 'ac',
        }],
    };
}

export const allGear: ArmyGear[] = [{
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
},
    createMagicArmor({bonus: 1, price: 25, level: 5}),
    createMagicArmor({bonus: 2, price: 50, level: 11}),
    createMagicArmor({bonus: 3, price: 75, level: 18}),
    createMagicWeapon({bonus: 1, type: 'ranged', price: 20, level: 2}),
    createMagicWeapon({bonus: 1, type: 'melee', price: 20, level: 2}),
    createMagicWeapon({bonus: 2, type: 'ranged', price: 40, level: 10}),
    createMagicWeapon({bonus: 2, type: 'melee', price: 40, level: 10}),
    createMagicWeapon({bonus: 3, type: 'ranged', price: 60, level: 16}),
    createMagicWeapon({bonus: 3, type: 'melee', price: 60, level: 16}),
];

export interface ArmyTactics {
    name: string;
    description?: string;
    level?: number;
    grantsActions?: ArmyActionName[];
    countsAgainstLimit?: boolean;
    unique?: boolean;
    traits?: string[];
    restrictedTypes?: ArmyType[];
    modifiers?: ArmyModifier[];
}

const allTactics: ArmyTactics[] = [{
    name: 'Ambush',
    level: 8,
    restrictedTypes: ['skirmisher'],
    description: 'Your skirmishers are experts at ambushing. On the first round of a war encounter, if your turn occurs before any enemy army turns, you can choose to start the encounter with your army already engaged with an enemy army whose initiative result is lower than yours. If you do so, your army gains a +2 status bonus on the first Attack war action they make against that army on the first round of the encounter.',
    modifiers: [{
        value: 2,
        type: 'status',
        enabled: false,
        selector: 'attack',
    }],
}, {
    name: 'Bloodied But Unbroken',
    level: 5,
    restrictedTypes: ['cavalry', 'infantry', 'skirmisher'],
}, {
    name: 'Cavalry Experts',
    level: 6,
    restrictedTypes: ['cavalry'],
    description: 'The army’s expert training with mounts increases its status bonus from its Overrun ability to +2',
    modifiers: [{
        type: 'status',
        selector: 'attack',
        value: 2,
        targetTypes: ['infantry', 'skirmisher'],
    }],
}, {
    name: 'Darkvision',
    level: 1,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army includes several spotters and scouts who have darkvision, and the rest of the soldiers have been trained to follow their lead so that the army itself functions as if it had darkvision.',
}, {
    name: 'Defensive Tactics',
    level: 3,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: `The army is especially good at enacting defensive tactics. The army gains a +1 status bonus on Maneuver checks
made to Guard. This bonus increases to +2 at 9th level, and +3 at 17th level. The army can use the Defensive Stance tactical war action.`,
    grantsActions: ['Defensive Stance'],
    modifiers: [{
        type: 'status',
        selector: 'maneuver',
        action: 'Guard',
        value: [{start: 0, end: 8, value: 1}, {start: 9, end: 16, value: 2}, {start: 17, value: 3}],
    }],
}, {
    name: 'Explosive Shot',
    level: 11,
    restrictedTypes: ['siege'],
    description: 'The army’s ranged attacks explode and spray fire, shrapnel, or other damaging material in every direction. Whenever the army critically hits a non-distant army with a ranged Strike, inflict 1 point of additional damage to another non-distant enemy army of your choice. You can use the Overwhelming Bombardment tactical war action with the army.',
    grantsActions: ['Overwhelming Bombardment'],
}, {
    name: 'Field Triage',
    level: 6,
    restrictedTypes: ['infantry', 'skirmisher'],
    description: 'The army’s soldiers are adept at using emergency methods to treat wounds. The army gains the Battlefield Medicine tactical war action.',
    grantsActions: ['Battlefield Medicine'],
}, {
    name: 'Flaming Shot',
    level: 9,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army attacks with projectiles treated with alchemical or magical oils that ignite as they are fired. When your army succeeds at a ranged Strike, the target army must attempt a Maneuver check against your army’s attack DC; if it fails, the Strike inflicts 1 additional point of damage.',
}, {
    name: 'Flexible Tactics',
    level: 5,
    restrictedTypes: ['infantry', 'skirmisher'],
    description: 'The army uses unconventional tactics. You can use the Dirty Fighting, False Retreat, and Feint tactical war actions, and the Counterattack tactical reaction with the army.',
    grantsActions: ['Dirty Fighting', 'False Retreat', 'Feint'],
}, {
    name: 'Focused Devotion',
    level: 3,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army is particularly loyal to your cause. The army gains a +1 status bonus on Morale checks made to Rally. This bonus increases to +2 at 9th level, and +3 at 17th level. The army can use the Taunt tactical war action.',
    modifiers: [{
        type: 'status',
        selector: 'morale',
        action: 'Rally',
        value: [{start: 0, end: 8, value: 1}, {start: 9, end: 16, value: 2}, {start: 17, value: 3}],
    }],
}, {
    name: 'Hold the Line',
    level: 1,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army has trained to maintain position even in the face of overwhelming opponents. The army gains a +1 status bonus on Morale checks made to resist rout, and its Rout Threshold is equal to 1/4 it’s total Hit Points (rounded up).',
    modifiers: [{
        label: 'Resist Route',
        type: 'status',
        selector: 'morale',
        value: 1,
    }, {
        type: 'untyped',
        value: '@hp*0.25|ceil',
        selector: 'route-threshold',
    }],
}, {
    name: 'Increased Ammunition',
    level: 5,
    restrictedTypes: ['cavalry', 'infantry', 'skirmisher', 'siege'],
    description: 'You increase the number of times your army can use ranged Strikes in each war encounter by 2. This tactic can be taken multiple times; each time you do so, increase the army’s maximum number of ranged Strikes by 2.',
    modifiers: [{
        type: 'untyped',
        value: '2',
        selector: 'shots',
    }],
}, {
    name: 'Keen Eyed',
    level: 1,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army includes several spotters and scouts who are particularly keen-eyed. The army gains a +2 status bonus on initiative checks.',
    modifiers: [{
        type: 'status',
        value: 2,
        selector: 'initiative',
    }],
}, {
    name: 'Keep Up The Pressure',
    level: 3,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The commander’s swift, decisive directions help the army attack more accurately. If an army attacks the same target a second time in a round, its multiple attack penalty is –4 rather than –5, and if they attack that same army a third time in a round, its multiple attack penalty is –8 rather than –10.',
}, {
    name: 'Live Off The Land',
    level: 1,
    restrictedTypes: ['cavalry', 'infantry', 'skirmisher'],
    description: 'The army is trained to be self-sufficient and sustains itself via hunting and gathering when they’re in the wild. If during a Kingdom turn’s Upkeep phase this army is located in a hex that doesn’t include a settlement, and if the army is not garrisoned, it reduces its Consumption by 1.',
    modifiers: [{
        type: 'untyped',
        value: -1,
        selector: 'consumption',
        toggle: 'Hex includes no settlement and not garrisoned',
    }],
}, {
    name: 'Low-Light Vision',
    level: 1,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army includes several spotters and scouts who have low-light vision, and the rest of the soldiers have been trained to follow their lead so that the army itself functions as if it had low-light vision.',
}, {
    name: 'Merciless',
    level: 5,
    restrictedTypes: ['cavalry', 'infantry'],
    description: 'This army is difficult to escape from. The army’s Mobility DC gains a +2 status bonus when other armies attempt Maneuver checks against it while attempting to Disengage. This army can use the All-Out Assault tactical war action.',
    grantsActions: ['All-Out Assault'],
    modifiers: [{
        target: 'attacker',
        type: 'status',
        value: 2,
        selector: 'maneuver',
        action: 'Disengage',
    }],
}, {
    name: 'Opening Salvo',
    level: 8,
    restrictedTypes: ['cavalry', 'siege', 'skirmisher'],
    description: 'Your army has trained to take the first shot at distant foes. On the first round of a war encounter, if your turn occurs before any enemy army turns, you can choose to start the encounter with your army distant from all enemy armies.',
}, {
    name: 'Reckless Flankers',
    level: 5,
    restrictedTypes: ['cavalry', 'skirmisher'],
    description: 'Your army is skilled at surrounding their foes and distracting them, at the cost of spreading out too much and being more vulnerable. When you use the Advance war action to successfully engage an army, you can choose to take a –2 circumstance penalty to your AC in order to gain a +1 circumstance bonus on attack rolls. If you do so, these modifiers remain in effect until you are no longer engaged. You can use the Outflank tactical war action.',
    modifiers: [{
        type: 'circumstance',
        value: 1,
        selector: 'attack',
        toggle: 'Reckless Flankers',
    }, {
        type: 'circumstance',
        value: -2,
        selector: 'ac',
        toggle: 'Reckless Flankers',
    }],
}, {
    name: 'Sharpshooter',
    level: 5,
    restrictedTypes: ['cavalry', 'infantry', 'skirmisher'],
    description: 'The commander drills the army in precision ranged attacks. You gain a +1 status bonus on attacks with ranged Strikes, but suffer a –2 status bonus on attacks with melee Strikes. At 9th level, the penalty to melee Strikes is reduced to –1, and at 15th level the penalty to melee Strikes is removed. The army can use the Covering Fire tactical war action.',
    grantsActions: ['Covering Fire'],
    modifiers: [{
        type: 'circumstance',
        value: 1,
        selector: 'ranged-attack',
    }, {
        type: 'status',
        value: [{start: 0, end: 8, value: -2}, {start: 9, end: 16, value: -1}, {start: 17, value: 0}],
        selector: 'melee-attack',
    }],
}, {
    name: 'Toughened Soldiers',
    level: 1,
    restrictedTypes: ['cavalry', 'infantry', 'siege', 'skirmisher'],
    description: 'The army is particularly hardy. Increase its maximum Hit Points by 1. You can take this tactic multiple times; each time you do, increase the army’s maximum Hit Points by 1.',
    modifiers: [{
        type: 'untyped',
        selector: 'hp',
        value: 1,
    }],
}, {
    name: 'Overrun',
    description: 'Cavalry armies gain a +1 status bonus on weapon attacks against infantry and skirmisher armies',
    modifiers: [{
        type: 'status',
        selector: 'attack',
        value: 1,
        targetTypes: ['infantry', 'skirmisher'],
    }],
}, {
    name: 'Engines of War',
    description: 'Siege engines cannot be outfitted with gear. They cannot attack engaged armies. They are more difficult to destroy due to their higher hit points than other basic armies. A siege engine can attack and damage fortifications with its ranged attacks as part of the Battle or Overwhelming Bombardment actions.',
}, {
    name: 'Brutal Assault',
    description: 'The Troll Marauders can use the All-Out Assault action. When they do, an army damaged by the assault must succeed at a DC 24 Morale check to avoid becoming shaken 1 (or shaken 2 on a critical failure) as a result of the brutality of this attack.',
    unique: true,
    grantsActions: ['All-Out Assault'],
}, {
    name: 'Frightening Foe',
    description: 'The Troll Marauders can use the Taunt tactical action. When they do, they gain a +2 status bonus on their Morale check if they used the Regeneration tactic this turn.',
    unique: true,
    grantsActions: ['Taunt'],
    modifiers: [{
        enabled: false,
        label: 'Regeneration used this turn',
        action: 'Taunt',
        value: 2,
        type: 'status',
        selector: 'morale',
    }],
}, {
    name: 'Regeneration',
    unique: true,
    description: 'At the beginning of its turn, the Troll Marauders regain 1 Hit Point. The Troll Marauders cannot be destroyed as usual unless they lose this tactic. The PCs can cause the trolls to lose the Regeneration tactic via prepared firepots (see page 296); while the trolls’ Regeneration tactic is lost, their RT increases to 3. Otherwise, an army that engages the Troll Marauders while they are defeated can take a three-round action to burn the trolls and destroy their army.',
}, {
    name: 'Tactical Training',
    unique: true,
    description: 'The Drelev Irregulars can use the All-Out Assault, Counterattack, and Dirty Fighting tactical actions.',
    grantsActions: ['All-Out Assault', 'Counterattack', 'Dirty Fighting'],
}, {
    name: 'Unpredictable Movement',
    unique: true,
    description: 'It’s difficult to do significant damage to the Drelev Irregulars with ranged attacks, as the mob moves about in a haphazard manner. All ranged attacks against the Drelev Irregulars suffer a –2 circumstance penalty as a result.',
    modifiers: [{
        target: 'attacker',
        selector: 'ranged-attack',
        type: 'circumstance',
        value: -2,
    }],
}, {
    name: 'Flight',
    unique: true,
    description: 'The Wyvern Flight ignores all ground-based difficult terrain and cannot become mired by effects that can be escaped by flight. When they use the Disengage action against armies that can’t fly, their check result is improved one degree. Armies that lack the ability to fly suffer a –2 circumstance penalty on Advance actions against a Wyvern Flight',
    modifiers: [{
        enabled: false,
        target: 'attacker',
        label: 'Can Fly',
        selector: 'maneuver',
        type: 'circumstance',
        action: 'Advance',
        value: -2,
    }],
}, {
    name: 'Wyvern Venom',
    unique: true,
    description: `An army that takes damage from a Wyvern Flight’s melee strike increases its weary condition value by 1. If this would cause an army to increase its weary condition above 4, it instead takes 1 point of damage. Each time an army regains Hit Points
during a battle, it can attempt a DC 11 flat check; on a success, it no longer suffers the ongoing effects of Wyvern Venom (but can still be affected by it later from a future attack, and does not reset its weary condition). The effects of Wyvern Venom also end as soon as an army escapes the battlefield or once the battle ends.`,
}, {
    name: 'Wyvern Tactics',
    unique: true,
    description: 'The Wyvern Flight can use the All-Out Assault and Counterattack tactical actions.',
    grantsActions: ['All-Out Assault', 'Counterattack'],
}, {
    name: 'Pitaxian Training',
    unique: true,
    description: 'The Pitaxian Raiders can use the Counterattack, Dirty Fighting, and Feint tactical actions.',
    grantsActions: ['Counterattack', 'Dirty Fighting', 'Feint'],
}, {
    name: 'Tusker Training',
    unique: true,
    description: 'The Tusker Riders can use the All-Out Assault, Covering Fire, and Taunt tactical actions.',
    grantsActions: ['All-Out Assault', 'Covering Fire', 'Taunt'],
}, {
    name: 'Trampling Charge',
    unique: true,
    description: 'The Riders trample an enemy army.',
    grantsActions: ['Trampling Charge'],
}, {
    name: 'Battlefield Adaptability',
    unique: true,
    description: 'The First World army has a wide range of creatures in its ranks, and it can shift its tactics to support those with different mobilities. It can take this action to achieve one of the following benefits: ignore ground-based difficult terrain, reduce Mired to 0, become concealed, or gain a +2 circumstance bonus on Maneuver checks to Advance and to Disengage. The effect lasts until the start of the First World Army’s next turn.',
    grantsActions: ['Battlefield Adaptability'],
    modifiers: [{
        type: 'circumstance',
        selector: 'maneuver',
        action: 'Disengage',
        value: 2,
        toggle: 'Battlefield Adaptability Bonus',
    }, {
        type: 'circumstance',
        selector: 'maneuver',
        action: 'Advance',
        value: 2,
        toggle: 'Battlefield Adaptability Bonus',
    }],
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
    description: 'Whenever a First World army scores a critical hit on another army, it increases that army’s Weary condition by 1 as their attacks cause some soldiers to become poisoned, fall asleep, shrink in size, or suffer other eerie side effects.',
}, {
    name: 'Swift Recovery',
    traits: ['primal'],
    unique: true,
    grantsActions: ['Swift Recovery'],
}, {
    name: 'Burning Weaponry',
    unique: true,
    description: 'Unlike basic siege engine armies, Greengripe Bombardiers can make melee Strikes while engaged. When a Greengripe Bombardier scores a critical success on a Strike while taking the Battle action against an army (but not against a fortification), the army struck must make a successful Maneuver check against the Greengripe Bombardiers’ Maneuver DC or take one additional point of damage from the goblins’ burning weaponry.',
}, {
    name: 'Explosive Defeat',
    unique: true,
    description: 'When the Greengripe Bombardiers are defeated, their alchemical siege engine explodes. Any army engaged with the Greengripe Bombardiers must succeed at a Maneuver check against the Greengripe Bombardier’s Maneuver DC or take 1 point of damage (2 points of damage on a critical failure). As long as the Greengripe Bombardiers aren’t destroyed, their siege engine gear is rebuilt automatically once the army loses the defeated condition.',
}, {
    name: 'Swamp Dwellers',
    unique: true,
    description: 'The lizardfolk defenders have the Live off the Land tactic for free (it does not count against the maximum number of tactics the army can possess), do not treat water or swamp on a battlefield as difficult terrain, and gain a +2 circumstance bonus to Scouting checks attempted in hexes that include swamps or water.',
    modifiers: [{
        value: 2,
        toggle: 'Hex includes Swamp or Water',
        type: 'circumstance',
        selector: 'scouting',
    }],
}, {
    name: 'Amphibious',
    unique: true,
    description: 'Frog Riders ignore difficult terrain caused by swamps or water and gain a +2 circumstance bonus on Maneuver checks while fighting on a battlefield that features either of these terrains.',
    modifiers: [{
        value: 2,
        toggle: 'Hex includes Swamp or Water',
        type: 'circumstance',
        selector: 'maneuver',
    }],
}, {
    name: 'Chorus of Croaks',
    unique: true,
    description: 'The terrifying croaking that constantly issues from this army of boggards causes all engaged enemy armies to suffer a –2 status penalty on Morale checks. The M’botuu Frog Riders can use the Taunt tactical action. If they know the Focused Devotion tactic, they treat critical failures made when attempting to Taunt as failures instead.',
}, {
    name: 'Swamp Charge',
    unique: true,
    description: 'If the battlefield includes swamp or water, the M’botuu Frog Riders can start the encounter engaged with a target enemy army. If they do so, they gain a +1 circumstance bonus on melee Strikes against that army on the first turn of the encounter.',
    modifiers: [{
        value: 1,
        toggle: 'Swamp Charge',
        type: 'circumstance',
        selector: 'melee-attack',
    }],
}, {
    name: 'Brave',
    unique: true,
    description: 'Nomen scouts are extraordinarily fearless and do not possess a Rout Threshold, and gain a +2 circumstance bonus on Morale checks made to avoid rout from other sources.',
    modifiers: [{
        enabled: false,
        label: 'Avoid Rout',
        value: 2,
        type: 'circumstance',
        selector: 'morale',
    }],
}, {
    name: 'Self-Sufficient',
    unique: true,
    description: 'Nomen scouts are adept at providing for themselves, and never count against the kingdom’s consumption. Furthermore, they can provide for other armies, and as long as they are not defeated during a Kingdom turn’s Upkeep phase, they reduce the kingdom’s consumption value by 1.',
    modifiers: [{
        value: -1,
        toggle: 'Not Defeated',
        type: 'untyped',
        selector: 'consumption',
    }],
}, {
    name: 'Trample',
    unique: true,
    description: 'Nomen Scouts can attempt to trample an engaged enemy army by attempting a +20 melee Strike against the army’s AC. On a hit, they inflict 1 point of damage (2 points on a critical hit) and automatically move away from the army—they are no longer engaged with that army.',
    grantsActions: ['Trample'],
}, {
    name: 'Accustomed to Panic',
    unique: true,
    grantsActions: ['Accustomed to Panic'],
}, {
    name: 'Furious Charge',
    unique: true,
    grantsActions: ['Furious Charge'],
}, {
    name: 'Reactive Rally',
    unique: true,
    grantsActions: ['Reactive Rally'],
}, {
    name: 'Revel in Battle',
    unique: true,
    grantsActions: ['Revel in Battle'],
}, {
    name: 'Warmongers',
    unique: true,
    description: 'The Tiger Lord Berserkers can use the following Tactical Actions without needing to meet additional requirements: All-Out Assault, Counterattack, and Taunt.',
    grantsActions: ['All-Out Assault', 'Counterattack', 'Taunt'],
}, {
    name: 'Hurl Nets',
    unique: true,
    description: 'If the Tok-Nikrat Scouts hit an army with a net Strike, the army takes no damage. Instead, it increases its mired condition value by 1 (or by 2 on a critical success). An army that is mired as a result of hurled nets can attempt to reduce the value of this mired condition (but not that caused by other sources) by attempting a Battle action against the Tok-Nikrat Scout’s Maneuver DC. On a success, the army reduces their mired condition by 1 (or by 2 on a critical success), but on a critical failure, they increase the mired condition caused by the hurled nets by 1. Tok-Nikrat Scouts can’t hurl nets at a target army that is distant.',
}, {
    name: 'Water Retreat',
    unique: true,
    description: 'If the battlefield features water terrain, Tok-Nikrat Scouts gain a +4 circumstance bonus on Maneuver checks made to Disengage.',
    modifiers: [{
        value: 4,
        toggle: 'Hex includes Water',
        type: 'circumstance',
        selector: 'maneuver',
        action: 'Disengage',
    }],
}, {
    name: 'Water Stride',
    unique: true,
    description: 'The Tok-Nikrat Scouts stride over watery surfaces, and they ignore difficult terrain caused by water or swamps. They gain a +4 circumstance bonus on initiative checks in war encounters when the battlefield features either terrain.',
    modifiers: [{
        value: 4,
        toggle: 'Hex includes Water or Swamp',
        type: 'circumstance',
        selector: 'initiative',
    }],
}];


export const allTacticsByName: Record<string, ArmyTactics> = Object.fromEntries((allTactics)
    .map((tactic) => [tactic.name, tactic]));


export interface MeleeAttack {
    name: string;
    plus?: string[];
}

export interface RangedAttack extends MeleeAttack {
    shots: number;
    currentShots: number;
}

export const allArmyTypes = ['skirmisher', 'infantry', 'cavalry', 'siege'] as const;
export const allAlignments = ['LG', 'NG', 'CG', 'LN', 'N', 'CN', 'LE', 'NE', 'CE'] as const;
export const allAlignmentLabels = [
    'Lawful Good', 'Neutral Good', 'Chaotic Good',
    'Lawful Neutral', 'Neutral', 'Chaotic Neutral',
    'Lawful Evil', 'Neutral Evil', 'Chaotic Evil',
];
export const allRarities = ['common', 'uncommon', 'rare', 'unique'] as const;
export const allLevels = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20] as const;
export type ArmyType = typeof allArmyTypes[number];

export type Alignment = typeof allAlignments[number];

export type Rarity = typeof allRarities[number];


export interface ArmyAdjustments {
    scouting: number;
    recruitmentDC: number;
    ac: number;
    melee: number;
    ranged: number;
    morale: number;
    maneuver: number;
}

export const allArmySaves = [
    'maneuver',
    'morale',
] as const;

export type ArmySave = typeof allArmySaves[number];

export interface Army {
    adjustments: ArmyAdjustments,
    alignment: Alignment;
    rarity: Rarity;
    highSave: ArmySave
    consumption: number;
    description: string;
    hp: number;
    currentHp: number;
    routeThreshold: number;
    level: number;
    name: string;
    melee?: MeleeAttack;
    ranged?: RangedAttack;
    type: ArmyType;
    gear: ArmyGear[];
    conditions: ArmyCondition[];
    tactics: ArmyTactics[];
}


const armies: Army[] = [{
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
    },
    rarity: 'common',
    alignment: 'N',
    name: 'Infantry',
    consumption: 1,
    description: 'This is a platoon of armored soldiers armed with melee weapons.',
    highSave: 'morale',
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    melee: {
        name: 'Weapons',
    },
    level: 1,
    type: 'infantry',
    tactics: [],
    gear: [],
    conditions: [],
}, {
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
    },
    rarity: 'common',
    alignment: 'N',
    name: 'Cavalry',
    level: 3,
    type: 'cavalry',
    consumption: 2,
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    melee: {
        name: 'Weapons',
    },
    highSave: 'maneuver',
    description: 'Cavalry consists of armored soldiers armed with melee weapons and mounted on horses.',
    tactics: [allTacticsByName['Overrun']],
    gear: [],
    conditions: [],
}, {
    rarity: 'common',
    alignment: 'N',
    name: 'Skirmishers',
    level: 5,
    type: 'skirmisher',
    consumption: 1,
    routeThreshold: 2,
    description: 'Skirmishers are lightly armored, but their ability to move quickly and to focus on individual tactics rather than working as a unit make them more resilient in other ways. A skirmisher army’s AC is two lower than normal for its level, but its Maneuver and Morale are two higher than normal for its level.',
    highSave: 'maneuver',
    adjustments: {
        ranged: 0,
        melee: 0,
        recruitmentDC: 0,
        scouting: 0,
        ac: -2,
        maneuver: 2,
        morale: 2,
    },
    hp: 4,
    currentHp: 4,
    melee: {
        name: 'Weapons',
    },
    tactics: [],
    gear: [],
    conditions: [],
}, {
    rarity: 'common',
    alignment: 'N',
    name: 'Siege Engines',
    level: 7,
    type: 'siege',
    consumption: 1,
    description: 'A siege engine army consists of several catapults, ballistae, trebuchets, or other mechanized engines of war.',
    highSave: 'morale',
    hp: 6,
    currentHp: 6,
    routeThreshold: 3,
    adjustments: {
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
        ranged: -3,
    },
    ranged: {
        currentShots: 5,
        name: 'Siege Engine',
        shots: 5,
    },
    tactics: [allTacticsByName['Engines of War']],
    gear: [],
    conditions: [],
}, {
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
    },
    rarity: 'common',
    name: 'Tatzlford Town Guard',
    alignment: 'NG',
    type: 'infantry',
    level: 7,
    consumption: 1,
    description: 'Tatzlford’s town guards have been organized into an impromptu army armed with longswords and led by Captain Coren Lawry.',
    highSave: 'morale',
    hp: 5,
    currentHp: 5,
    routeThreshold: 2,
    melee: {
        name: 'Longswords',
    },
    tactics: [allTacticsByName['Hold the Line'], allTacticsByName['Toughened Soldiers']],
    gear: [],
    conditions: [],
}, {
    rarity: 'common',
    name: 'Narlmarch Hunters',
    level: 6,
    alignment: 'NG',
    type: 'skirmisher',
    consumption: 1,
    description: 'This army is a band of hunters and trappers who have gathered into a ragtag group of archers led by Mayor Loy Rezbin.',
    highSave: 'maneuver',
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    adjustments: {
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
        melee: -2,
        ranged: 1,
    },
    melee: {
        name: 'Hatchets and Shortswords',
    },
    ranged: {
        currentShots: 7,
        name: 'Longbows',
        shots: 7,
    },
    tactics: [allTacticsByName['Efficient Ammunition'], allTacticsByName['Sharpshooter']],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    rarity: 'common',
    name: 'Drelev Irregulars',
    level: 7,
    alignment: 'CE',
    type: 'infantry',
    description: `The Drelev Irregulars are composed of equal parts Tiger Lords and mercenaries who were once bandits. Their fighting style is more akin to a mob than a disciplined force; while this allows the irregulars an advantage in mobility, it decentralizes their command
structure and lessens their morale.`,
    highSave: 'maneuver',
    adjustments: {
        ranged: 0,
        melee: 0,
        recruitmentDC: 0,
        ac: 0,
        scouting: 0,
        maneuver: 1,
        morale: -1,
    },
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    melee: {
        name: 'Swords and Axes',
    },
    tactics: [allTacticsByName['Tactical Training']],
    gear: [],
    conditions: [],
}, {
    rarity: 'common',
    consumption: 0,
    name: 'Troll Marauders',
    level: 8,
    alignment: 'NE',
    type: 'infantry',
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        scouting: -2,
        ac: 1,
    },
    description: 'There are only a few dozen trolls in this army, but their ferocity and regenerative capability make them a dangerous force nonetheless.',
    highSave: 'morale',
    hp: 5,
    currentHp: 5,
    routeThreshold: 1,
    melee: {
        name: 'Claws and Fangs',
    },
    tactics: [
        allTacticsByName['Brutal Assault'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Frightening Foe'],
        allTacticsByName['Regeneration'],
    ],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    rarity: 'common',
    name: 'Pitaxian Raiders',
    level: 12,
    alignment: 'CN',
    type: 'skirmisher',
    description: 'The Pitaxian Raiders consist of a mix of Pitax city guards and mercenaries eager for battle.',
    highSave: 'maneuver',
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    adjustments: {
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
        ranged: -2,
    },
    melee: {
        name: 'Swords',
    },
    ranged: {
        currentShots: 7,
        name: 'Longbows',
        shots: 7,
    },
    tactics: [allTacticsByName['Pitaxian Training']],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    rarity: 'common',
    name: 'Tusker Riders',
    level: 14,
    type: 'cavalry',
    alignment: 'CE',
    adjustments: {
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        scouting: 1,
        ac: -1,
        melee: 1,
        ranged: -1,
    },
    description: 'The hill giant warlord Kob Moleg sent these mammoth-mounted hill giants to King Irovetti as a gift.',
    highSave: 'morale',
    hp: 8,
    currentHp: 8,
    routeThreshold: 4,
    melee: {
        name: 'Morningstars and Tusks',
    },
    ranged: {
        currentShots: 5,
        name: 'Thrown Rock',
        shots: 5,
    },
    tactics: [
        allTacticsByName['Tusker Training'],
        allTacticsByName['Trampling Charge'],
        allTacticsByName['Darkvision'],
    ],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    rarity: 'common',
    name: 'Wyvern Flight',
    level: 12,
    alignment: 'CE',
    type: 'skirmisher',
    adjustments: {
        ranged: 0,
        maneuver: 0,
        recruitmentDC: 0,
        ac: 0,
        scouting: 1,
        morale: -2,
        melee: -8,
    },
    description: 'This flight of wyverns has been trained to obey orders in battle',
    highSave: 'maneuver',
    hp: 4,
    currentHp: 4,
    routeThreshold: 3,
    melee: {
        name: 'Fangs, Claws and Stingers',
        plus: ['Wyvern Venom'],
    },
    tactics: [
        allTacticsByName['Darkvision'],
        allTacticsByName['Flight'],
        allTacticsByName['Wyvern Venom'],
        allTacticsByName['Wyvern Tactics'],
    ],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    rarity: 'common',
    name: 'Pitax Horde',
    level: 10,
    alignment: 'CN',
    type: 'infantry',
    adjustments: {
        // 22 for scouting in gl2
        // 24 for maneuver in gl2
        ranged: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
        melee: 2,
    },
    description: 'The Pitax Horde consists of human warriors from seven different minor clans in Glenebon.',
    highSave: 'maneuver',
    hp: 5,
    currentHp: 5,
    routeThreshold: 2,
    melee: {
        name: 'Greater Magic Axes',
    },
    tactics: [
        allTacticsByName['Live off the Land'],
        allTacticsByName['Merciless'],
        allTacticsByName['Toughened Soldiers'],
    ],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        ac: 0,
        scouting: 0,
    },
    rarity: 'common',
    name: 'Pitax War Machines',
    level: 14,
    alignment: 'CN',
    type: 'siege',
    description: 'These siege weapons are positioned atop all of the city’s watchtowers and afford a controlling view of all approaches.',
    highSave: 'morale',
    hp: 8,
    currentHp: 8,
    routeThreshold: 4,
    ranged: {
        currentShots: 7,
        name: 'Siege Engine',
        shots: 7,
    },
    tactics: [
        allTacticsByName['Efficient Ammunition'],
        allTacticsByName['Explosive Shot'],
        allTacticsByName['Toughened Soldiers'],
        allTacticsByName['Toughened Soldiers'],
    ],
    gear: [],
    conditions: [],
}, {
    consumption: 0,
    name: 'First World Army',
    level: 16,
    rarity: 'rare',
    alignment: 'CE',
    type: 'skirmisher',
    adjustments: {
        maneuver: 0,
        recruitmentDC: 0,
        morale: 0,
        scouting: 0,
        ac: -1,
        ranged: 1,
        melee: 1,
    },
    description: 'This army is composed of an eclectic mix of fey, beasts, and plants—a supernatural mob of monsters with a wide range of options in battle.',
    highSave: 'maneuver',
    hp: 6,
    currentHp: 6,
    routeThreshold: 3,
    melee: {
        name: 'Weapons and Claws',
    },
    ranged: {
        currentShots: 5,
        name: 'Bows and Hurled Thorns',
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
    gear: [],
    conditions: [],
}, {
    alignment: 'N',
    name: 'Greengripe Bombardiers',
    level: 7,
    rarity: 'rare',
    type: 'siege',
    adjustments: {
        ranged: 0,
        maneuver: 0,
        morale: 0,
        scouting: -2,
        recruitmentDC: 5,
        ac: -2,
        melee: 1,
    },
    consumption: 2,
    description: 'Greengripe goblins have built a mobile platform outfitted with a catapult-like flinging arm that can throw flammable debris.',
    highSave: 'morale',
    hp: 6,
    currentHp: 6,
    routeThreshold: 3,
    melee: {
        name: 'Dogslicers and Torches',
    },
    ranged: {
        currentShots: 5,
        name: 'Burning Debris',
        shots: 5,
    },
    tactics: [
        allTacticsByName['Burning Weaponry'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Explosive Defeat'],
    ],
    gear: [],
    conditions: [],
}, {
    alignment: 'N',
    name: 'Lizardfolk Defenders',
    level: 5,
    rarity: 'uncommon',
    type: 'skirmisher',
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        morale: 0,
        scouting: 2,
        recruitmentDC: 2,
        ac: 1,
    },
    consumption: 1,
    description: 'These lizardfolk are from the settlement on the banks of Candlemere (encounter site KL3); they fight with flails and javelins.',
    highSave: 'morale',
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    melee: {
        name: 'Flails',
    },
    ranged: {
        currentShots: 5,
        name: 'Javelins',
        shots: 5,
    },
    tactics: [
        allTacticsByName['Swamp Dwellers'],
        allTacticsByName['Live Off The Land'],
    ],
    gear: [],
    conditions: [],
}, {
    name: 'M\'Botuu Frog Riders',
    alignment: 'N',
    level: 10,
    rarity: 'rare',
    type: 'cavalry',
    adjustments: {
        ranged: 0,
        maneuver: 0,
        ac: 0,
        scouting: 0,
        recruitmentDC: 5,
        morale: 2,
        melee: 2,
    },
    consumption: 2,
    description: 'These lance-armed boggards from M’botuu and ride giant frogs trained for warfare into battle.',
    highSave: 'maneuver',
    hp: 6,
    currentHp: 6,
    routeThreshold: 3,
    melee: {
        name: 'Lances',
    },
    ranged: {
        currentShots: 5,
        name: 'Javelins',
        shots: 5,
    },
    tactics: [
        allTacticsByName['Amphibious'],
        allTacticsByName['Chorus of Croaks'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Swamp Charge'],
    ],
    gear: [],
    conditions: [],
}, {
    name: 'Nomen Scouts',
    alignment: 'N',
    level: 8,
    rarity: 'uncommon',
    type: 'cavalry',
    adjustments: {
        ranged: 0,
        melee: 0,
        morale: 0,
        ac: 0,
        scouting: 2,
        maneuver: 1,
        recruitmentDC: 2,
    },
    consumption: -1,
    description: 'This band of Nomen centaurs fight with spears and longbows.',
    highSave: 'morale',
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    melee: {
        name: 'Spears',
    },
    ranged: {
        currentShots: 5,
        name: 'Longbows',
        shots: 5,
    },
    tactics: [
        allTacticsByName['Brave'],
        allTacticsByName['Darkvision'],
        allTacticsByName['Self-Sufficient'],
        allTacticsByName['Trample'],
    ],
    gear: [],
    conditions: [],
}, {
    name: 'Sootscale Warriors',
    alignment: 'N',
    level: 3,
    rarity: 'uncommon',
    type: 'infantry',
    adjustments: {
        ranged: 0,
        melee: 0,
        scouting: 0,
        recruitmentDC: 2,
        ac: 1,
        maneuver: 2,
        morale: -1,
    },
    consumption: 1,
    description: 'Sootscale kobolds fight with shortswords and crossbows, although they tend to do so warily and cautiously.',
    highSave: 'maneuver',
    hp: 4,
    currentHp: 4,
    routeThreshold: 3,
    melee: {
        name: 'Shortswords',
    },
    ranged: {
        currentShots: 7,
        name: 'Crossbows',
        shots: 7,
    },
    tactics: [
        allTacticsByName['Accustomed to Panic'],
        allTacticsByName['Darkvision'],
    ],
    gear: [],
    conditions: [],
}, {
    name: 'Tiger Lord Berserkers',
    alignment: 'N',
    level: 12,
    rarity: 'uncommon',
    type: 'infantry',
    adjustments: {
        ranged: 0,
        maneuver: 0,
        morale: 0,
        scouting: 1,
        recruitmentDC: 2,
        ac: -1,
        melee: 2,
    },
    consumption: 1,
    description: 'These Tiger Lord barbarians use rage in battle; they fight with greataxes.',
    highSave: 'morale',
    hp: 6,
    currentHp: 6,
    routeThreshold: 2,
    melee: {
        name: 'Greataxes',
    },
    tactics: [
        allTacticsByName['Furious Charge'],
        allTacticsByName['Reactive Rally'],
        allTacticsByName['Revel in Battle'],
        allTacticsByName['Warmongers'],
    ],
    gear: [],
    conditions: [],
}, {
    name: 'Tok-Nikrat Scouts',
    alignment: 'N',
    level: 10,
    rarity: 'rare',
    type: 'skirmisher',
    adjustments: {
        ranged: 0,
        melee: 0,
        maneuver: 0,
        scouting: 1,
        recruitmentDC: 5,
        ac: 1,
        morale: 2,
    },
    consumption: 1,
    description: 'Capable of striding across water, these bog striders from the settlement of Tok-Nikrat fight with nets and spears.',
    highSave: 'maneuver',
    hp: 4,
    currentHp: 4,
    routeThreshold: 2,
    melee: {
        name: 'Spears',
    },
    ranged: {
        currentShots: 5,
        name: 'Net',
        shots: 5,
    },
    tactics: [
        allTacticsByName['Darkvision'],
        allTacticsByName['Hurl Nets'],
        allTacticsByName['Water Retreat'],
        allTacticsByName['Water Stride'],
    ],
    gear: [],
    conditions: [],
}];

export const armiesByName = new Map<string, Army>();
armies.forEach(army => armiesByName.set(army.name, army));
