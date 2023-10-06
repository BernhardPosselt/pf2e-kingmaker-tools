import {ArmyAdjustments, armyStatisticsByLevel} from './data';

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

function isRanged(item: Item): boolean {
    return item.system?.weaponType?.value === 'ranged';
}

export async function addAttackModifiers(actor: Actor, item: Item, update: Partial<Item>, level: number, adjustments: ArmyAdjustments): Promise<void> {
    if (item.type === 'melee') { // true for both melee and ranged
        const calculated = calculateArmyAdjustments(actor, level, adjustments);
        const attackModifier = isRanged(item) ? calculated.ranged : calculated.melee;
        const calculatedUpdate = {system: {bonus: {value: attackModifier}}};
        await item.update(calculatedUpdate);
        console.log('oncreate', update);
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
                perception: {value: calculated.scouting},
            },
        },
    };
    foundry.utils.mergeObject(update, calculatedUpdate);
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