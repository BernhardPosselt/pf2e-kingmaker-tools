import {ArmyAdjustments, armyStatisticsByLevel} from './data';
import {getArmyAdjustment} from './storage';
import {Kingdom} from '../kingdom/data/kingdom';
import {saveKingdom} from '../kingdom/storage';
import {getBooleanSetting} from '../settings';
import {sum} from '../utils';

export function getDefaultArmyAdjustment(): ArmyAdjustments {
    return {
        ac: 0,
        melee: 0,
        morale: 0,
        maneuver: 0,
        scouting: 0,
        recruitmentDC: 0,
        ranged: 0,
        highSave: 'maneuver',
        ammunition: 0,
    };
}

function isRanged(item: Item): boolean {
    return item.system?.weaponType?.value === 'ranged';
}

export async function addAttackModifiers(actor: Actor, item: Item, level: number, adjustments: ArmyAdjustments): Promise<void> {
    if (item.type === 'melee') { // true for both melee and ranged
        const calculated = calculateArmyAdjustments(actor, level, adjustments);
        const attackModifier = isRanged(item) ? calculated.ranged : calculated.melee;
        const calculatedUpdate = {system: {bonus: {value: attackModifier}}};
        await item.update(calculatedUpdate);
    }
}


export async function syncAttackModifiers(actor: Actor, level: number, adjustments: ArmyAdjustments): Promise<void> {
    const calculated = calculateArmyAdjustments(actor, level, adjustments);
    const updates = actor.items
        .filter(item => item.type === 'melee')
        .map(item => {
            const attackModifier = isRanged(item) ? calculated.ranged : calculated.melee;
            return {'_id': item.id, 'system.bonus.value': attackModifier};
        });
    console.log('onactorupdate', updates);
    await actor.updateEmbeddedDocuments('Item', updates);
}


export function addArmyStats(actor: Actor, update: Partial<Actor>, level: number, adjustments: ArmyAdjustments): void {
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
            },
            perception: {mod: calculated.scouting},
        },
    };
    foundry.utils.mergeObject(update, calculatedUpdate);
}

export function calculateArmyAdjustments(actor: Actor, level: number, adjustments: ArmyAdjustments): ArmyAdjustments {
    const data = armyStatisticsByLevel.get(level) ?? armyStatisticsByLevel.get(1)!;
    const maneuver = adjustments.highSave === 'maneuver' ? data.highSave : data.lowSave;
    const morale = adjustments.highSave === 'morale' ? data.highSave : data.lowSave;
    const increasedAmmunitionCount = actor.itemTypes.action
        .filter(i => i.name.startsWith('Increased Ammunition'))
        .length;
    return {
        ac: data.ac + adjustments.ac,
        recruitmentDC: data.standardDC + adjustments.recruitmentDC,
        melee: data.attack + adjustments.melee,
        ranged: data.attack + adjustments.ranged,
        maneuver: maneuver + adjustments.maneuver,
        scouting: data.scouting + adjustments.scouting,
        morale: morale + adjustments.morale,
        highSave: adjustments.highSave,
        ammunition: 5 + adjustments.ammunition + 2 * increasedAmmunitionCount,
    };
}


function isArmyActor(actor: Actor | null | undefined): boolean {
    return (actor?.type === 'npc' && getArmyAdjustment(actor) !== undefined) ||
        actor?.type === 'army';
}

export async function updateKingdomArmyConsumption(
    {
        actor = null,
        kingdomActor,
        game,
        forceUpdate = false,
    }: {
        actor?: Actor | null | undefined,
        kingdomActor: Actor | null | undefined,
        game: Game,
        forceUpdate?: boolean,
    },
): Promise<void> {
    const autoCalculationEnabled = getBooleanSetting(game, 'autoCalculateArmyConsumption');
    if (autoCalculationEnabled && kingdomActor && (forceUpdate || isArmyActor(actor))) {
        await saveKingdom(kingdomActor, {
            consumption: {
                armies: Math.max(calculateTotalArmyConsumption(game), 0),
            },
        } as Partial<Kingdom>);
    }
}


export function calculateTotalArmyConsumption(game: Game): number {
    // fucking foundry collection APIs :/
    const consumption: number[] = [];
    game?.scenes?.map(s => s.tokens)
        ?.forEach(tokenCollection => {
            tokenCollection.forEach(token => {
                const actor = token.actor;
                if (actor && !token.hidden) {
                    if (actor.type === 'army') {
                        consumption.push((actor as unknown as ArmyActor).system.consumption);
                    } else if (getArmyAdjustment(actor) !== undefined) {
                        consumption.push(actor.abilities.con.mod);
                    }
                }
            });
        });
    return sum(consumption);
}