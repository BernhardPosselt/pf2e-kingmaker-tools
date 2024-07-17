import {getKingdom, saveKingdom} from './kingdom/storage';
import {getNumberSetting, setSetting} from './settings';
import {getCamping, saveCamping} from './camping/storage';
import {isFirstGm} from './utils';
import {Migration} from './migrations/migration';
import {Migration2} from './migrations/migration2';
import {Migration3} from './migrations/migration3';
import {Migration4} from './migrations/migration4';
import {Migration5} from './migrations/migration5';
import {Migration6} from './migrations/migration6';
import {Migration7} from './migrations/migration7';
import {Migration8} from './migrations/migration8';
import {Migration9} from './migrations/migration9';


const migrations: Migration[] = [
    new Migration2(),
    new Migration3(),
    new Migration4(),
    new Migration5(),
    new Migration6(),
    new Migration7(),
    new Migration8(),
    new Migration9(),
];

interface BackupParams {
    game: Game;
    kingdomActor: Actor | undefined | null;
    campingActor: Actor | null | undefined;
    currentVersion: number;
}

async function createBackups(params: BackupParams): Promise<void> {
    const backup = {
        version: params.currentVersion,
        camping: params.campingActor ? getCamping(params.campingActor) : null,
        kingdom: params.kingdomActor ? getKingdom(params.kingdomActor) : null,
    };
    await setSetting(params.game, 'latestMigrationBackup', JSON.stringify(backup));
}

export async function migrate(game: Game, kingdomActor: Actor | undefined | null, campingActor: Actor | null | undefined): Promise<void> {
    const currentVersion = getNumberSetting(game, 'schemaVersion');
    const latestMigration = Math.max(1, ...migrations.map(m => m.version));
    if (isFirstGm(game) && currentVersion < latestMigration) {
        ui.notifications?.info('Running Kingmaker Tools Migrations, please do not close the window');
        await createBackups({game, kingdomActor, campingActor, currentVersion});

        const migrationsToRun = migrations.filter(m => m.version > currentVersion);

        if (kingdomActor) {
            const kingdom = getKingdom(kingdomActor);
            for (const migration of migrationsToRun) {
                await migration.migrateKingdom(game, kingdom);
            }
            await saveKingdom(kingdomActor, kingdom);
        }

        if (campingActor) {
            const camping = getCamping(campingActor);
            for (const migration of migrationsToRun) {
                await migration.migrateCamping(game, camping);
            }
            await saveCamping(game, campingActor, camping);
        }

        for (const migration of migrationsToRun) {
            await migration.migrateOther(game);
        }

        await setSetting(game, 'schemaVersion', latestMigration);
        ui.notifications?.info('Successfully migrated Kingmaker tools');
    }
}