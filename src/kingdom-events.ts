import {findRollTableUuidWithFallback, rollRollTable} from './roll-tables';
import {getStringSetting} from './settings';

export async function rollKingdomEvent(game: Game): Promise<void> {
    const tableName = getStringSetting(game, 'kingdomEventsTable').trim();
    const rollTable = (await findRollTableUuidWithFallback(game, tableName, 'pf2e-kingmaker-tools.kingmaker-tools-rolltables'))!;

    const rollMode = getStringSetting(game, 'kingdomEventRollMode') as unknown as keyof CONFIG.Dice.RollModes;
    const rollOptions: Partial<RollTable.DrawOptions> = {rollMode};

    await rollRollTable(game, rollTable, rollOptions);
}
