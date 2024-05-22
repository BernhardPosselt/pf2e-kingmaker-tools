import {getNumberSetting, getRollMode, RollMode} from '../settings';
import {buildUuids, rollRollTable} from '../roll-tables';
import {setWeather} from './weather';
import {eventLevels, getSeason, GolarionMonth, golarionMonths} from '../camping/regions';
import {MonthNumbers} from 'luxon';
import {postChatMessage} from '../utils';


async function rollOnWeatherEventTable(
    game: Game,
    averagePartyLevel: number,
    weatherHazardRange: number,
    rollMode: RollMode,
    rollTwice: boolean,
): Promise<void> {
    const uuids = await buildUuids(game);
    const {table, draw} = await rollRollTable(game, uuids['Weather Events'], {rollMode, displayChat: false});
    const {results} = draw;
    /* eslint-disable @typescript-eslint/no-explicit-any */
    const tableResult = results[0] as any; // FIXME: remove cast once v10 TS types are available
    const event = tableResult.text;
    const eventLevel = Array.from(eventLevels.entries())
        .filter(([name]) => event.startsWith(name))
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        .map(([_, level]) => level)[0] ?? 0;
    if (eventLevel > (averagePartyLevel + weatherHazardRange)) {
        console.info(`Re-rolling event, level ${event.level} is more than ${weatherHazardRange} levels higher than party level ${averagePartyLevel}`);
        await rollOnWeatherEventTable(game, averagePartyLevel, weatherHazardRange, rollMode, rollTwice);
    } else {
        await table.toMessage(results, {roll: draw.roll, messageOptions: {rollMode}});
        if (rollTwice) {
            await postChatMessage('Choose a second weather event!', rollMode);
        }
    }
}

async function rollWeatherEvent(
    game: Game,
    averagePartyLevel: number,
    weatherHazardRange: number,
    rollMode: RollMode,
): Promise<void> {
    const {isSuccess, total} = await rollCheck(17, 'Rolling for weather event with DC 17', rollMode);
    if (total === 20) {
        await rollOnWeatherEventTable(game, averagePartyLevel, weatherHazardRange, rollMode, true);
    } else if (isSuccess) {
        await rollOnWeatherEventTable(game, averagePartyLevel, weatherHazardRange, rollMode, false);
    }
}


async function rollCheck(dc: number, flavor: string, rollMode: RollMode): Promise<{
    isSuccess: boolean,
    total: number
}> {
    const roll = await new Roll('1d20').evaluate({async: true});
    const isSuccess = roll.total >= dc;
    await roll.toMessage({flavor}, {rollMode});
    return {isSuccess, total: roll.total};
}


export async function rollKingmakerWeather(game: Game): Promise<void> {
    const rollMode = getRollMode(game, 'weatherRollMode');
    const levels = (game?.actors?.find(a => a.type === 'party') as PartyActor | undefined)
        ?.members
        ?.filter(a => a.type === 'character')
        ?.map(a => a.level) ?? [];
    const averagePartyLevel = Math.max(...levels, 1);
    const weatherHazardRange = getNumberSetting(game, 'weatherHazardRange');
    await rollWeather(game, averagePartyLevel, weatherHazardRange, rollMode);
}

async function rollWeather(game: Game, averagePartyLevel: number, weatherHazardRange: number, rollMode: RollMode): Promise<void> {
    const month = golarionMonths.get(game.pf2e.worldClock.worldTime.month as MonthNumbers);
    const {precipitationDC, coldDC} = getSeason(month as GolarionMonth);

    const hasPrecipitation = (await rollCheck(
        precipitationDC,
        `Checking for precipitation in ${month} on a DC of ${precipitationDC}`,
        rollMode,
    )).isSuccess;

    let message;
    if (coldDC !== undefined) {
        const isCold = (await rollCheck(
            coldDC,
            `Checking for mild cold in ${month} on a DC of ${coldDC}`,
            rollMode,
        )).isSuccess;
        if (isCold && hasPrecipitation) {
            await setWeather(game, 'snow');
            message = 'Weather: Cold & Snowing';
        } else if (isCold) {
            await setWeather(game, 'sunny');
            message = 'Weather: Cold';
        } else if (hasPrecipitation) {
            await setWeather(game, 'rain');
            message = 'Weather: Rainy';
        } else {
            await setWeather(game, 'sunny');
            message = 'Weather: Sunny';
        }
    } else {
        if (hasPrecipitation) {
            await setWeather(game, 'rain');
            message = 'Weather: Rainy';
        } else {
            await setWeather(game, 'sunny');
            message = 'Weather: Sunny';
        }
    }
    await rollWeatherEvent(game, averagePartyLevel, weatherHazardRange, rollMode);
    await postChatMessage(message, rollMode);
}
