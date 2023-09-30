import {ArmyAdjustments} from './data';
import {updateCalculatedArmyStats} from './utils';

export async function saveArmyAdjustment(actor: Actor, update: Partial<ArmyAdjustments>): Promise<void> {
    console.info('Saving', update);
    await actor.setFlag('pf2e-kingmaker-tools', 'army-adjustment', update);
    const syncedActorUpdates = {};
    updateCalculatedArmyStats(actor, syncedActorUpdates, actor.system.details.level.value, getArmyAdjustment(actor)!);
    await actor.update(syncedActorUpdates);
    // TODO: update melee and ranged weapons
}

export function getArmyAdjustment(sheetActor: Actor): ArmyAdjustments | undefined {
    return sheetActor.getFlag('pf2e-kingmaker-tools', 'army-adjustment') as ArmyAdjustments | undefined;
}