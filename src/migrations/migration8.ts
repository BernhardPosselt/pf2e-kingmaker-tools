import {Kingdom} from '../kingdom/data/kingdom';
import {Migration} from './migration';
import {isNullable} from '../utils';

export class Migration8 extends Migration {
    constructor() {
        super(8);
    }

    override async migrateKingdom(game: Game, kingdom: Kingdom): Promise<void> {
        if (isNullable(kingdom.realmSceneId)) {
            kingdom.realmSceneId = null;
        }
    }
}