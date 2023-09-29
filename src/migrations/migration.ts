import {Kingdom} from '../kingdom/data/kingdom';
import {Camping} from '../camping/camping';

export abstract class Migration {
    protected constructor(public readonly version: number) {
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    async migrateKingdom(game: Game, kingdom: Kingdom): Promise<void> {

    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    async migrateCamping(game: Game, camping: Camping): Promise<void> {

    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    async migrateOther(game: Game): Promise<void> {

    }
}