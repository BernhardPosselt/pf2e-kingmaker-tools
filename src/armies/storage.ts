import {ArmyAdjustments} from './data';

export async function saveArmyAdjustment(sheetActor: Actor, update: Partial<ArmyAdjustments>): Promise<void> {
    console.info('Saving', update);
    await sheetActor.setFlag('pf2e-kingmaker-tools', 'army-adjustment', update);
    // TODO: also update calculated data
}

export function getArmyAdjustment(sheetActor: Actor): ArmyAdjustments | undefined {
    return sheetActor.getFlag('pf2e-kingmaker-tools', 'army-adjustment') as ArmyAdjustments | undefined;
}