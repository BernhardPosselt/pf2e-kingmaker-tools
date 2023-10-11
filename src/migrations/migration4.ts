import {Migration} from './migration';
import {Kingdom} from '../kingdom/data/kingdom';

export class Migration4 extends Migration {
    constructor() {
        super(4);
    }

    override async migrateKingdom(game: Game, kingdom: Kingdom): Promise<void> {
        kingdom.activityBlacklist.push('take-charge');
        kingdom.activityBlacklist.push('reconnoiter-hex');
    }
}