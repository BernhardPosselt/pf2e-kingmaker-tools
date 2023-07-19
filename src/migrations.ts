import {getKingdom, saveKingdom} from './kingdom/storage';
import {getNumberSetting, setSetting} from './settings';
import {getCamping, saveCamping} from './camping/storage';
import {isFirstGm} from './utils';
import {Migration} from './migrations/migration';
import {Migration2} from './migrations/migration2';


const migrations: Migration[] = [
    new Migration2(),
];

export async function migrate(game: Game, kingdomActor: Actor | undefined | null, campingActor: Actor | null | undefined): Promise<void> {
    const currentVersion = getNumberSetting(game, 'schemaVersion');
    const latestMigration = Math.max(1, ...migrations.map(m => m.version));
    if (isFirstGm(game) && currentVersion < latestMigration) {
        ui.notifications?.info('Running Kingmaker Tools Migrations, please do not close the window');
        const migrationsToRun = migrations.filter(m => m.version > currentVersion);

        if (kingdomActor) {
            const kingdom = getKingdom(kingdomActor);
            migrationsToRun.forEach(m => m.migrateKingdom(kingdom));
            await saveKingdom(kingdomActor, kingdom);
        }

        if (campingActor) {
            const camping = getCamping(campingActor);
            migrationsToRun.forEach(m => m.migrateCamping(camping));
            await saveCamping(game, campingActor, camping);
        }

        await setSetting(game, 'schemaVersion', latestMigration);
        ui.notifications?.info('Successfully migrated Kingmaker tools');
    }
}