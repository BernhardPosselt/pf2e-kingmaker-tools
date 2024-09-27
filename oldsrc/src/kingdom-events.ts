import {findRollTableUuidWithFallback, rollRollTable} from './roll-tables';
import {getStringSetting} from './settings';

async function rollEvent(game: Game, tableName: string): Promise<void> {
    const rollTable = (await findRollTableUuidWithFallback(game, tableName, 'pf2e-kingmaker-tools.kingmaker-tools-rolltables'))!;

    const rollMode = getStringSetting(game, 'kingdomEventRollMode') as unknown as keyof CONFIG.Dice.RollModes;
    const rollOptions: Partial<RollTable.DrawOptions> = {rollMode};

    await rollRollTable(game, rollTable, rollOptions);
}

export async function rollKingdomEvent(game: Game): Promise<void> {
    const tableName = getStringSetting(game, 'kingdomEventsTable').trim();
    await rollEvent(game, tableName);
}

export async function rollCultEvent(game: Game): Promise<void> {
    const tableName = getStringSetting(game, 'kingdomCultTable').trim();
    await rollEvent(game, tableName);
}
