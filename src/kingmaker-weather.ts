import {RollMode} from "./settings";

const weatherEvents = [
    {roll: [1, 2, 3], name: 'Fog', level: 0},
    {roll: [4, 5, 6, 7], name: 'Heavy downpour', level: 0},
    {roll: [8, 9], name: 'Cold snap', level: 1},
    {roll: [10, 11, 12], name: 'Windstorm', level: 1},
    {roll: [13], name: 'Hailstorm, severe', level: 2},
    {roll: [14], name: 'Blizzard', level: 6},
    {roll: [15], name: 'Supernatural storm', level: 6},
    {roll: [16], name: 'Flash flood', level: 7},
    {roll: [17], name: 'Wildfire', level: 4},
    {roll: [18], name: 'Subsidence', level: 5},
    {roll: [19], name: 'Thunderstorm', level: 7},
    {roll: [20], name: 'Tornado', level: 12},
];

async function rollOnWeatherEventTable(averagePartyLevel: number, rollMode: RollMode, rollTwice: boolean) {
    const roll = await new Roll('1d20').evaluate({async: true});
    const total = roll.total;
    const event = weatherEvents.find(event => event.roll.includes(total))!!;
    if ((averagePartyLevel + 4) <= event.level) {
        console.info(`Rerolling event, level ${event.level} is 4 higher than party level ${averagePartyLevel}`)
        await rollOnWeatherEventTable(averagePartyLevel, rollMode, rollTwice);
    } else {
        await roll.toMessage({flavor: 'Rolling on weather event table'}, {rollMode});
        let message = event.name;
        if (rollTwice) {
            message += ', choose a second event';
        }
        await postMessage(message);
    }
}

async function rollWeatherEvent(averagePartyLevel: number, rollMode: RollMode) {
    const {isSuccess, total} = await rollCheck(17, `Rolling for weather event with DC 17`, rollMode);
    if (total === 20) {
        await rollOnWeatherEventTable(averagePartyLevel, rollMode, true);
    } else if (isSuccess) {
        await rollOnWeatherEventTable(averagePartyLevel, rollMode, false);
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
            message = 'Weather: Cold & Snowing';
        } else if (isCold) {
            message = 'Weather: Cold';
        } else if (hasPrecipitation) {
            message = 'Weather: Rainy';
        } else {
            message = 'Weather: Normal';
        }
    } else {
        if (hasPrecipitation) {
            message = 'Weather: Rainy';
        } else {
            message = 'Weather: Normal';
        }
    }
    await rollWeatherEvent(averagePartyLevel, rollMode);
    await postMessage(message);
}
