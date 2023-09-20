import {getStringSetting} from '../settings';
import {findRollTableUuidWithFallback, findWorldTableUuid, rollRollTable} from '../roll-tables';
import {DegreeOfSuccess, determineDegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {regions, toOfficialKingmakerRollTableName} from './regions';
import {CampingActivityData} from './activities';
import {Camping, getCampingActivityData} from './camping';
import {getWorldTime, isDayOrNight} from '../time/calculation';
import {isKingmakerInstalled} from '../utils';


async function succeededEncounterFlatCheck(region: string, dc: EncounterDc): Promise<boolean> {
    const flavor = `Rolling Random Encounter for terrain ${region} with Flat DC ${dc.total}`;
    const dieRoll = await new Roll('1d20').evaluate();
    await dieRoll.toMessage({flavor}, {rollMode: 'gmroll'});
    const degreeOfSuccess = determineDegreeOfSuccess(dieRoll.total, dieRoll.total, dc.total);
    return degreeOfSuccess === DegreeOfSuccess.CRITICAL_SUCCESS || degreeOfSuccess === DegreeOfSuccess.SUCCESS;
}

async function findCreatureRollTable(game: Game, region: string): Promise<string> {
    const key = isKingmakerInstalled(game) ? toOfficialKingmakerRollTableName(region, regions.get(region)!) : region;
    return (await findRollTableUuidWithFallback(game, key, 'pf2e-kingmaker-tools.kingmaker-tools-random-encounters'))!;
}

export async function rollRandomEncounter(game: Game, region: string, dc: EncounterDc, forgoFlatCheck = false): Promise<boolean> {
    const creatureRollTable = await findCreatureRollTable(game, region);
    const worldEncounterTable = getStringSetting(game, 'proxyEncounterTable')?.trim();
    const encounterTypeRollTable = findWorldTableUuid(game, worldEncounterTable)!;
    const rollMode = getStringSetting(game, 'randomEncounterRollMode') as unknown as keyof CONFIG.Dice.RollModes;
    const rollOptions: Partial<RollTable.DrawOptions> = {rollMode};

    if (forgoFlatCheck || await succeededEncounterFlatCheck(region, dc)) {
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
        return true;
    }
    return false;
}


export interface ActivityModifier {
    day: number,
    night: number
}

export function getActivityModifier(
    campingActivityData: CampingActivityData,
    result: StringDegreeOfSuccess | null,
): ActivityModifier {
    if (campingActivityData.modifyRandomEncounterDc) {
        return {
            day: campingActivityData.modifyRandomEncounterDc.day,
            night: campingActivityData.modifyRandomEncounterDc.night,
        };
    } else if (result !== null) {
        const mods = campingActivityData[result]?.modifyRandomEncounterDc;
        return {
            day: mods?.day ?? 0,
            night: mods?.night ?? 0,
        };
    } else {
        return {day: 0, night: 0};
    }
}

export interface EncounterDc {
    modifier: number;
    activityModifier: number;
    dc: number;
    total: number;
}

export function getEncounterDC(data: Camping, game: Game): EncounterDc {
    const currentRegionData = regions.get(data.currentRegion);
    const modifier = data.encounterModifier;
    const activityModifier = getCampingActivityData(data)
        .map(campingActivityData => {
            const campingActivity = data.campingActivities
                .find(ca => ca.activity === campingActivityData.name);
            const result = campingActivity?.result ?? null;
            const actorUuid = campingActivity?.actorUuid ?? null;
            if (actorUuid !== null && (result !== null || campingActivityData.skills.length === 0)) {
                const mods = getActivityModifier(campingActivityData, result);
                return mods[isDayOrNight(getWorldTime(game))];
            } else {
                return 0;
            }
        }).reduce((a, b) => a + b, 0);
    const dc = currentRegionData?.encounterDC ?? 0;
    return {
        modifier,
        activityModifier,
        dc,
        total: modifier + dc + activityModifier,
    };
}
