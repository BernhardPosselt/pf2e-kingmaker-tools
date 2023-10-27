import {MonthNumbers} from 'luxon';

interface ZoneData {
    zoneDC: number;
    encounterDC: number;
    level: number;
}

export const regions = new Map<string, ZoneData>();
regions.set('Brevoy', {zoneDC: 14, encounterDC: 12, level: 0});
regions.set('Rostland Hinterlands', {zoneDC: 15, encounterDC: 12, level: 1});
regions.set('Greenbelt', {zoneDC: 16, encounterDC: 14, level: 2});
regions.set('Tuskwater', {zoneDC: 18, encounterDC: 12, level: 3});
regions.set('Kamelands', {zoneDC: 19, encounterDC: 12, level: 4});
regions.set('Narlmarches', {zoneDC: 20, encounterDC: 14, level: 5});
regions.set('Sellen Hills', {zoneDC: 20, encounterDC: 12, level: 6});
regions.set('Dunsward', {zoneDC: 18, encounterDC: 12, level: 7});
regions.set('Nomen Heights', {zoneDC: 24, encounterDC: 12, level: 8});
regions.set('Tors of Levenies', {zoneDC: 28, encounterDC: 16, level: 9});
regions.set('Hooktongue', {zoneDC: 32, encounterDC: 14, level: 10});
regions.set('Drelev', {zoneDC: 28, encounterDC: 12, level: 11});
regions.set('Tiger Lords', {zoneDC: 28, encounterDC: 12, level: 12});
regions.set('Rushlight', {zoneDC: 26, encounterDC: 12, level: 13});
regions.set('Glenebon Lowlands', {zoneDC: 30, encounterDC: 12, level: 14});
regions.set('Pitax', {zoneDC: 29, encounterDC: 12, level: 15});
regions.set('Glenebon Uplands', {zoneDC: 35, encounterDC: 12, level: 16});
regions.set('Numeria', {zoneDC: 36, encounterDC: 12, level: 17});
regions.set('Thousand Voices', {zoneDC: 43, encounterDC: 14, level: 18});
regions.set('Branthlend Mountains', {zoneDC: 41, encounterDC: 16, level: 19});

export function toOfficialKingmakerRollTableName(regionName: string, data: ZoneData): string {
    const zoneId = `${data.level}`.padStart(2, '0');
    return `Zone ${zoneId}: ${regionName}`;
}

interface RegionInfo {
    zoneDC: number,
    zoneLevel: number
}

export function getRegionInfo(game: Game, region: string): RegionInfo {
    /* eslint-disable @typescript-eslint/no-explicit-any */
    const zone = regions.get(region);
    const zoneDC = zone?.zoneDC ?? 14;
    const zoneLevel = zone?.level ?? 0;
    return {zoneDC, zoneLevel};
}

export type GolarionMonth = 'Abadius'
    | 'Calistril'
    | 'Pharast'
    | 'Gozran'
    | 'Desnus'
    | 'Sarenith'
    | 'Erastus'
    | 'Arodus'
    | 'Rova'
    | 'Lamashan'
    | 'Neth'
    | 'Kuthona';

export interface WeatherData {
    precipitationDC: number;
    coldDC?: number;
    season: 'spring' | 'summer' | 'fall' | 'winter';
}

export const golarionMonths = new Map<MonthNumbers, GolarionMonth>();
golarionMonths.set(1, 'Abadius');
golarionMonths.set(2, 'Calistril');
golarionMonths.set(3, 'Pharast');
golarionMonths.set(4, 'Gozran');
golarionMonths.set(5, 'Desnus');
golarionMonths.set(6, 'Sarenith');
golarionMonths.set(7, 'Erastus');
golarionMonths.set(8, 'Arodus');
golarionMonths.set(9, 'Rova');
golarionMonths.set(10, 'Lamashan');
golarionMonths.set(11, 'Neth');
golarionMonths.set(12, 'Kuthona');

const weatherData = new Map<GolarionMonth, WeatherData>();
weatherData.set('Abadius', {coldDC: 16, precipitationDC: 8, season: 'winter'});
weatherData.set('Calistril', {coldDC: 18, precipitationDC: 8, season: 'winter'});
weatherData.set('Pharast', {precipitationDC: 15, season: 'spring'});
weatherData.set('Gozran', {precipitationDC: 15, season: 'spring'});
weatherData.set('Desnus', {precipitationDC: 15, season: 'spring'});
weatherData.set('Sarenith', {precipitationDC: 20, season: 'summer'});
weatherData.set('Erastus', {precipitationDC: 20, season: 'summer'});
weatherData.set('Arodus', {precipitationDC: 20, season: 'summer'});
weatherData.set('Rova', {precipitationDC: 15, season: 'fall'});
weatherData.set('Lamashan', {precipitationDC: 15, season: 'fall'});
weatherData.set('Neth', {precipitationDC: 15, season: 'fall'});
weatherData.set('Kuthona', {coldDC: 18, precipitationDC: 8, season: 'winter'});

export function getSeason(month: GolarionMonth): WeatherData {
    return weatherData.get(month)!;
}

export const eventLevels = new Map<string, number>();
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
