import {Kingdom} from './data/kingdom';
import {groupBy} from '../utils';

export function getKingdom(sheetActor: Actor): Kingdom {
    const kingdom = sheetActor.getFlag('pf2e-kingmaker-tools', 'kingdom-sheet') as Kingdom;
    console.log(kingdom);
    return kingdom;
}

function makeRuinPenaltiesPositive(kingdom: Partial<Kingdom>): void {
    const ruin = kingdom.ruin;
    if (ruin !== undefined) {
        ruin.corruption.penalty = Math.abs(ruin.corruption.penalty);
        ruin.crime.penalty = Math.abs(ruin.crime.penalty);
        ruin.decay.penalty = Math.abs(ruin.decay.penalty);
        ruin.strife.penalty = Math.abs(ruin.strife.penalty);
    }
}

async function giveMilestoneXP(kingdom: Partial<Kingdom>, existingKingdom: Kingdom): Promise<void> {
    const milestones = kingdom.milestones;
    if (milestones !== undefined) {
        const existingMilestones = existingKingdom.milestones;
        const existingMap = groupBy(existingMilestones, m => m.name);
        const newMap = groupBy(milestones, m => m.name);
        for (const key of existingMap.keys()) {
            const existingVal = existingMap.get(key)?.[0];
            const newVal = newMap.get(key)?.[0];
            if (newVal !== undefined &&
                existingVal !== undefined &&
                newVal.completed !== existingVal.completed) {
                const xp = newVal.completed ? newVal.xp : -newVal.xp;
                const result = newVal.completed ? 'Gained' : 'Lost';
                await ChatMessage.create({content: `${result} ${Math.abs(xp)} Kingdom XP`});
                kingdom.xp = existingKingdom.xp + xp;
            }
        }
    }
}

export async function saveKingdom(sheetActor: Actor, kingdom: Partial<Kingdom>): Promise<void> {
    const currentKingdom = getKingdom(sheetActor);
    makeRuinPenaltiesPositive(kingdom);
    await giveMilestoneXP(kingdom, currentKingdom);
    console.info('Saving', kingdom);
    await sheetActor.setFlag('pf2e-kingmaker-tools', 'kingdom-sheet', kingdom);
}
