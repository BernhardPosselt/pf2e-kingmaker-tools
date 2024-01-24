import {Kingdom} from '../kingdom/data/kingdom';
import {Migration} from './migration';
import {isNullable} from '../utils';

export class Migration6 extends Migration {
    constructor() {
        super(6);
    }

    override async migrateKingdom(game: Game, kingdom: Kingdom): Promise<void> {
        if (isNullable(kingdom.homebrewActivities)) {
            kingdom.homebrewActivities = [];
        }
    }
}