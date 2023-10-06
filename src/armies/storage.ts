import {ArmyAdjustments} from './data';
import {addArmyStats, syncAttackModifiers} from './utils';

export async function saveArmyAdjustment(actor: Actor, update: Partial<ArmyAdjustments>): Promise<void> {
    console.info('Saving', update);
    await actor.setFlag('pf2e-kingmaker-tools', 'army-adjustment', update);
    const adjustments = getArmyAdjustment(actor)!;
    const level = actor.system.details.level.value;
    const syncedActorUpdates = {};
    addArmyStats(actor, syncedActorUpdates, level, adjustments);
    await actor.update(syncedActorUpdates);
    await syncAttackModifiers(actor, level, adjustments);
}

export function getArmyAdjustment(sheetActor: Actor): ArmyAdjustments | undefined {
    return sheetActor.getFlag('pf2e-kingmaker-tools', 'army-adjustment') as ArmyAdjustments | undefined;
}