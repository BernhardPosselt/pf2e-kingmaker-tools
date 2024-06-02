import {Camping} from './camping';
import {getDiffListeners} from './effect-syncing';

export async function saveCamping(game: Game, sheetActor: Actor, update: Partial<Camping>): Promise<void> {
    const current = getCamping(sheetActor);
    console.info('Saving', update);
    await sheetActor.setFlag('pf2e-kingmaker-tools', 'camping-sheet', update);
    console.log('diff', foundry.utils.diffObject(current, update));
    for (const l of getDiffListeners(game)) {
        await l.testFireChange(current, update);
    }
}

export function getCamping(sheetActor: Actor): Camping {
    const camping = sheetActor.getFlag('pf2e-kingmaker-tools', 'camping-sheet') as Camping;
    console.log('Reading', camping);
    return foundry.utils.deepClone(camping);
}

export function getCampingActor(game: Game): Actor | null {
    return game?.actors
            ?.find(a => a.name === 'Camping Sheet' && a instanceof Actor)
        ?? null;
}
