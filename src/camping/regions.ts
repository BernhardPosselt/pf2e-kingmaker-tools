import {getStringSetting} from '../settings';

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

interface RegionInfo {
    zoneDC: number,
    zoneLevel: number
}

export function getRegionInfo(game: Game): RegionInfo {
    /* eslint-disable @typescript-eslint/no-explicit-any */
    const region = getStringSetting(game, 'currentRegion') || 'Rostland';
    const zone = regions.get(region);
    const zoneDC = zone?.zoneDC ?? 14;
    const zoneLevel = zone?.level ?? 0;
    return {zoneDC, zoneLevel};
}
