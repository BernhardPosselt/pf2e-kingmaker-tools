import {ArmyAdjustments} from './data';
import {getArmyAdjustment} from './storage';
import {isFirstGm} from '../utils';
import {addArmyStats, addAttackModifiers, syncAttackModifiers} from './utils';
import {CombatUpdate} from '../camping/combat-tracks';

function onUpdateArmyLevel<T>(
    {
        data,
        actor,
        game,
        callback,
    }: {
        data: Partial<Actor>;
        actor: Actor;
        game: Game;
        callback: (actor: Actor, data: Partial<Actor>, level: number, adjustments: ArmyAdjustments) => T;
    }): T | null {
    // we're updating level so calculate new statistics
    const level = data?.system?.details?.level?.value;
    const adjustments = getArmyAdjustment(actor);
    if (isFirstGm(game)
        && actor.type === 'npc'
        && adjustments !== undefined
        && level !== undefined
        && level >= 1 && level <= 20) {
        return callback(actor, data, level, adjustments);
    }
    return null;
}

export async function onPreUpdateArmy(game: Game, actor: Actor, data: Partial<Actor>): Promise<void> {
    onUpdateArmyLevel({
        data,
        actor,
        game,
        callback: (_, data, level, adjustments): void => {
            addArmyStats(data, level, adjustments);
        },
    });
}

export async function onCreateArmyItem(game: Game, item: Item, data: Partial<Item>): Promise<void> {
    const actor = item.actor!;
    const actorData = {system: {details: {level: {value: actor.system.details.level.value}}}} as Partial<Actor>;
    onUpdateArmyLevel({
        data: actorData,
        actor,
        game,
        callback: async (actor, _, level, adjustments): Promise<void> => {
            await addAttackModifiers(actor, item, data, level, adjustments);
        },
    });
}

export async function onPostUpdateArmy(game: Game, actor: Actor, data: Partial<Actor>): Promise<void> {
    await onUpdateArmyLevel({
        data,
        actor,
        game,
        callback: async (actor, _, level, adjustments): Promise<void> => {
            await syncAttackModifiers(actor, level, adjustments);
        },
    });
}

export async function updateAmmunition(game: Game, combat: StoredDocument<Combat>, update: CombatUpdate): Promise<void> {
    if (combat.round === 0 && update.round === 1 && isFirstGm(game)) {
        const actors = combat.combatants
            .map(a => a.actor)
            .filter(a => a !== null && a.type === 'npc') as Actor[];
        for (const actor of actors) {
            const adjustments = getArmyAdjustment(actor);
            if (adjustments !== undefined) {
                const ammunition = actor.itemTypes.equipment
                    .find(item => item.name === 'Ammunition');
                const increasedAmmunitionCount = actor.itemTypes.action
                    .filter(i => i.name.startsWith('Increased Ammunition'))
                    .length;
                if (ammunition) {
                    await ammunition.update({'system.quantity': 5 + increasedAmmunitionCount * 2});
                }
            }
        }
    }
}