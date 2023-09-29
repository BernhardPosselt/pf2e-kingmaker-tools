import {Migration} from './migration';
import {setSetting} from '../settings';

export class Migration3 extends Migration {
    constructor() {
        super(3);
    }

    override async migrateOther(game: Game): Promise<void> {
        await setSetting(game, 'kingdomEventsTable', 'Random Kingdom Events');
    }
}