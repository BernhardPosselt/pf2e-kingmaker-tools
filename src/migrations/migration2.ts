import {FameType, getCultEventMilestones, Kingdom} from '../kingdom/data/kingdom';
import {Camping} from '../camping/camping';
import {Migration} from './migration';

export class Migration2 extends Migration {
    constructor() {
        super(2);
    }

    override migrateKingdom(kingdom: Kingdom): void {
        if (typeof kingdom.fame === 'number' && 'fameNext' in kingdom && 'fameType' in kingdom) {
            const current = kingdom as Kingdom & { fame: number, fameNext: number, fameType: FameType };
            kingdom.fame = {
                now: current.fame,
                next: current?.fameNext ?? 0,
                type: current?.fameType ?? 'famous',
            };
        }
        if (!kingdom.milestones.some(milestone => milestone.name.startsWith('Cult Event'))) {
            getCultEventMilestones().forEach(milestone => kingdom.milestones.push(milestone));
        }
        if (kingdom.supernaturalSolutions === undefined) {
            kingdom.supernaturalSolutions = 0;
        }
        if (kingdom.turnsWithoutCultEvent === undefined) {
            kingdom.turnsWithoutCultEvent = 0;
        }
        if (kingdom.creativeSolutions === undefined) {
            kingdom.creativeSolutions = 0;
        }
        if (kingdom.modifiers === undefined) {
            kingdom.modifiers = [];
        }
        if (kingdom.settlements === undefined) {
            kingdom.settlements = [];
            kingdom.activeSettlement = '';
        }
        kingdom.settlements.forEach(settlement => {
            settlement.level = parseInt(`${settlement.level}`, 10);
            settlement.lots = parseInt(`${settlement.lots}`, 10);
            settlement.waterBorders = parseInt(`${settlement.waterBorders}`, 10);
            console.log(JSON.stringify(settlement));
        });
        console.log(JSON.stringify(kingdom.settlements));
    }

    override migrateCamping(camping: Camping): void {
        if (camping.increaseWatchActorNumber === undefined) {
            camping.increaseWatchActorNumber = 0;
        }
        if (camping.actorUuidsNotKeepingWatch === undefined) {
            camping.actorUuidsNotKeepingWatch = [];
        }
        if (camping.ignoreSkillRequirements === undefined) {
            camping.ignoreSkillRequirements = false;
        }
    }
}