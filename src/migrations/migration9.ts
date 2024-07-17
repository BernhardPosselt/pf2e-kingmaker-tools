import {Kingdom} from '../kingdom/data/kingdom';
import {Migration} from './migration';
import {recoverArmyIds} from '../kingdom/data/structures';

export class Migration9 extends Migration {
    constructor() {
        super(9);
    }

    override async migrateKingdom(game: Game, kingdom: Kingdom): Promise<void> {
        kingdom.modifiers.forEach(m => {
            const hasRecoverArmy = m.activities?.includes('recover-army');
            if (m.activities && hasRecoverArmy) {
                m.activities = [...m.activities.filter(a => a !== 'recover-army'), ...recoverArmyIds];
            }
        });
    }
}