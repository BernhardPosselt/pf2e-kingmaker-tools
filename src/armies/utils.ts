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
    const calculated = calculateArmyAdjustments(actor, level, adjustments);
    const calculatedUpdate = {
        system: {
            saves: {
                reflex: {value: calculated.maneuver},
                will: {value: calculated.morale},
                fortitude: {value: calculated.recruitmentDC},
            },
            attributes: {
                ac: {value: calculated.ac},
                perception: {value: calculated.scouting},
            },
        },
    };
    foundry.utils.mergeObject(update, calculatedUpdate);
    // TODO: update ranged or melee attack modifiers
    console.log(update, calculated.melee, calculated.ranged);
}

export function calculateArmyAdjustments(actor: Actor, level: number, adjustments: ArmyAdjustments): ArmyAdjustments {
    const data = armyStatisticsByLevel.get(level) ?? armyStatisticsByLevel.get(1)!;
    const highSave = actor.system.saves.will > actor.system.saves.reflex ? 'morale' : 'maneuver';
    const maneuver = highSave === 'maneuver' ? data.highSave : data.lowSave;
    const morale = highSave === 'morale' ? data.highSave : data.lowSave;
    return {
        ac: data.ac + adjustments.ac,
        recruitmentDC: data.standardDC + adjustments.recruitmentDC,
        melee: data.attack + adjustments.melee,
        ranged: data.attack + adjustments.ranged,
        maneuver: maneuver + adjustments.maneuver,
        scouting: data.scouting + adjustments.scouting,
        morale: morale + adjustments.morale,
    };
}