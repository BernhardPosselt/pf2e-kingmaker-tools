import {Camping} from './camping';

export async function saveCamping(sheetActor: Actor, camping: Partial<Camping>): Promise<void> {
    console.info('Saving', camping);
    await sheetActor.setFlag('pf2e-kingmaker-tools', 'camping-sheet', camping);
}
export function getCamping(sheetActor: Actor): Camping {
    const camping = sheetActor.getFlag('pf2e-kingmaker-tools', 'camping-sheet') as Camping;
    console.log('Reading', camping);
    return camping;
}
