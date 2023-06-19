import {getStringSetting} from '../settings';
import {findRollTableUuidWithFallback, findWorldTableUuid, rollRollTable} from '../roll-tables';
import {DegreeOfSuccess, determineDegreeOfSuccess} from '../degree-of-success';
import {regions} from './regions';


async function rollHasEncounterFlatCheck(region: string, modifier: number): Promise<boolean> {
    const data = regions.get(region)!;
    const dc = data.encounterDC + modifier;
    const flavor = `Rolling Random Encounter for terrain ${region} with Flat DC ${dc}`;
    const dieRoll = await new Roll('1d20').evaluate();
    await dieRoll.toMessage({flavor}, {rollMode: 'gmroll'});
    const degreeOfSuccess = determineDegreeOfSuccess(dieRoll.total, dieRoll.total, dc);
    return degreeOfSuccess === DegreeOfSuccess.CRITICAL_SUCCESS || degreeOfSuccess === DegreeOfSuccess.SUCCESS;
}

export async function rollRandomEncounter(game: Game, region: string, modifier: number, forgoFlatCheck = false): Promise<void> {
    const creatureRollTable = (await findRollTableUuidWithFallback(game, region, 'pf2e-kingmaker-tools.kingmaker-tools-random-encounters'))!;
    const worldEncounterTable = getStringSetting(game, 'proxyEncounterTable')?.trim();
    const encounterTypeRollTable = findWorldTableUuid(game, worldEncounterTable)!;
    const rollMode = getStringSetting(game, 'randomEncounterRollMode') as unknown as keyof CONFIG.Dice.RollModes;
    const rollOptions: Partial<RollTable.DrawOptions> = {rollMode};

    if (forgoFlatCheck || await rollHasEncounterFlatCheck(region, modifier)) {
        if (encounterTypeRollTable === undefined ||
            encounterTypeRollTable === null ||
            encounterTypeRollTable.trim() === '') {
            console.log('Rolling on creature table ' + creatureRollTable);
            await rollRollTable(game, creatureRollTable, rollOptions);
        } else {
            const typeResult = await rollRollTable(game, encounterTypeRollTable, rollOptions);
            /* eslint-disable @typescript-eslint/no-explicit-any */
            const tableResult = typeResult.draw.results[0] as any;
            if (tableResult?.text?.trim() === 'Creature') {
                console.log('Rolling on creature table ' + creatureRollTable);
                await rollRollTable(game, creatureRollTable, rollOptions);
            }
        }
    }
}


