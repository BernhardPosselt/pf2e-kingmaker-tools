import {Kingdom} from '../kingdom/data/kingdom';
import {Migration} from './migration';
import {isNullable} from '../utils';

export class Migration7 extends Migration {
    constructor() {
        super(7);
    }

    override async migrateKingdom(game: Game, kingdom: Kingdom): Promise<void> {
        if (isNullable(kingdom.notes)) {
            kingdom.notes = {
                public: '',
                gm: '',
            };
        }
    }
}