import {RollMode} from "./settings";
import {buildUuids, rollRollTable} from "./roll-tables";
import {setWeather} from "./weather";

const eventLevels = new Map<string, number>();
eventLevels.set('Fog', 0);
eventLevels.set('Heavy downpour', 0);
eventLevels.set('Cold snap', 1);
eventLevels.set('Windstorm', 1);
eventLevels.set('Hailstorm, severe', 2);
eventLevels.set('Blizzard', 6);
eventLevels.set('Supernatural storm', 6);
eventLevels.set('Flash flood', 7);
eventLevels.set('Wildfire', 4);
eventLevels.set('Subsidence', 5);
eventLevels.set('Thunderstorm', 7);
eventLevels.set('Tornado', 12);

async function rollOnWeatherEventTable(game: Game, averagePartyLevel: number, rollMode: RollMode, rollTwice: boolean) {
    const uuids = await buildUuids(game);
    const {table, draw} = await rollRollTable(game, uuids['Weather Events'], {rollMode, displayChat: false});
    const {results} = draw;
    const tableResult = results[0] as any; // FIXME: remove cast once v10 TS types are available
    const event = tableResult.text;
    const eventLevel = Array.from(eventLevels.entries())
        .filter(([name]) => event.startsWith(name))
        .map(([_, level]) => level)[0] ?? 0;
    if ((averagePartyLevel + 4) <= eventLevel) {
        console.info(`Re-rolling event, level ${event.level} is 4 higher than party level ${averagePartyLevel}`)
        await rollOnWeatherEventTable(game, averagePartyLevel, rollMode, rollTwice);
    } else {
        await table.toMessage(results, {roll: draw.roll, messageOptions: {rollMode}});
        if (rollTwice) {
            await postMessage('Choose a second weather event!');
        }
    }
}

async function rollWeatherEvent(game: Game, averagePartyLevel: number, rollMode: RollMode) {
    const {isSuccess, total} = await rollCheck(17, `Rolling for weather event with DC 17`, rollMode);
    if (total === 20) {
        await rollOnWeatherEventTable(game, averagePartyLevel, rollMode, true);
    } else if (isSuccess) {
        await rollOnWeatherEventTable(game, averagePartyLevel, rollMode, false);
    }
}

function getSeason(month: string) {
    if (['Kuthona', 'Abadius', 'Calistril'].includes(month)) {
        const coldDC = month === 'Abadius' ? 16 : 18
        return {season: 'winter', precipitationDC: 8, coldDC};
    } else if (['Pharast', 'Gozran', 'Desnus'].includes(month)) {
        return {season: 'spring', precipitationDC: 15};
    } else if (['Sarenith', 'Erastus', 'Arodus'].includes(month)) {
        return {season: 'summer', precipitationDC: 20};
    } else {
        return {season: 'fall', precipitationDC: 15};
    }
}

async function rollCheck(dc: number, flavor: string, rollMode: RollMode) {
    const roll = await new Roll('1d20').evaluate({async: true});
    const isSuccess = roll.total >= dc;
    await roll.toMessage({flavor}, {rollMode});
    return {isSuccess, total: roll.total};
}

async function postMessage(message: string): Promise<void> {
    await ChatMessage.create({content: message, blind: true});
}

export async function rollWeather(game: Game, averagePartyLevel: number, rollMode: RollMode): Promise<void> {
    const month = game.pf2e.worldClock.month;
    const {precipitationDC, coldDC} = getSeason(month);

    const hasPrecipitation = (await rollCheck(
        precipitationDC,
        `Checking for precipitation on a DC of ${precipitationDC}`,
        rollMode
    )).isSuccess;

    let message;
    if (coldDC !== undefined) {
        const isCold = (await rollCheck(
            coldDC,
            `Checking for mild cold on a DC of ${coldDC}`,
            rollMode
        )).isSuccess;
        if (isCold && hasPrecipitation) {
            await setWeather(game, 'snowfall');
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
    await rollWeatherEvent(game, averagePartyLevel, rollMode);
    await postMessage(message);
}
