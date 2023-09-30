import {ArmyAdjustments, armyStatisticsByLevel} from './data';
import {getArmyAdjustment} from './storage';
import {isFirstGm} from '../utils';

export function getDefaultArmyAdjustment(): ArmyAdjustments {
    return {
        ac: 0,
        melee: 0,
        morale: 0,
        maneuver: 0,
        scouting: 0,
        recruitmentDC: 0,
        ranged: 0,
    };
}

export function onPreUpdateArmy(game: Game, actor: Actor, data: Partial<Actor>): void {
    // we're updating level so calculate new statistics
    const level = data?.system?.details?.level?.value;
    const adjustments = getArmyAdjustment(actor);
    if (isFirstGm(game)
        && actor.type === 'npc'
        && adjustments !== undefined
        && level !== undefined
        && level >= 1 && level <= 20) {
        updateCalculatedArmyStats(actor, data, level, adjustments);
    }
}


export function updateCalculatedArmyStats(actor: Actor, update: Partial<Actor>, level: number, adjustments: ArmyAdjustments): void {
    const data = armyStatisticsByLevel.get(level)!;
    const highSave = actor.system.saves.will > actor.system.saves.reflex ? 'morale' : 'maneuver';
    const maneuver = highSave === 'maneuver' ? data.highSave : data.lowSave;
    const morale = highSave === 'morale' ? data.highSave : data.lowSave;
    const updates = {
        ac: data.ac + adjustments.ac,
        system: {
            saves: {
                reflex: {value: maneuver + adjustments.maneuver},
                will: {value: morale + adjustments.morale},
                fortitude: {value: data.standardDC + adjustments.recruitmentDC},
            },
            attributes: {
                perception: {
                    value: data.scouting + adjustments.scouting,
                },
            },
        },
    };
    // TODO: update ranged or melee attack modifiers
    const meleeModifier = data.attack + adjustments.melee;
    const rangedModifier = data.attack + adjustments.ranged;
    console.log(updates, meleeModifier, rangedModifier);
}
