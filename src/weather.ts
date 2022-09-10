import {getBooleanSetting, getStringSetting} from './settings';
import {FxFilterEffect, WeatherEffects} from './fxmaster';

export function dayHasChanged(game: Game, deltaInSeconds: number): boolean {
    const now = game.pf2e.worldClock.worldTime;
    const previous = now.minus({second: deltaInSeconds});
    return now.day !== previous.day || now.month !== previous.month || now.year !== previous.year;
}

interface FXMasterConfig {
    weather: WeatherEffects[];
    filter: FxFilterEffect[];
}

type FxType = 'sunny' | 'rain' | 'heavyRain' | 'fog' | 'heavyFog' | 'lightning' | 'snowstorm' | 'clouds';

function getFx(): Map<FxType, FXMasterConfig> {
    const fx = new Map<FxType, FXMasterConfig>();
    fx.set('sunny', {weather: [], filter: []});
    fx.set('rain', {weather: [{type: 'rain', options: {density: 0.5}}], filter: []});
    fx.set('heavyRain', {weather: [{type: 'rain', options: {density: 2}}], filter: []});
    fx.set('fog', {weather: [{type: 'fog', options: {density: 0.04}}], filter: []});
    fx.set('heavyFog', {weather: [{type: 'fog', options: {density: 0.15}}], filter: []});
    fx.set('lightning', {weather: [], filter: [{type: 'lightning', options: {frequency: 2000}}]});
    fx.set('snowstorm', {weather: [{type: 'snowstorm', options: {}}], filter: []});
    fx.set('clouds', {weather: [{type: 'clouds', options: {}}], filter: []});
    return fx;
}

function applyWeatherEffects(effectNames: FxType[]): void {
    const fxMaster = window.FXMASTER;
    if (fxMaster === undefined) {
        console.warn('FxMaster not enabled, ignoring weather!');
    } else {
        const fx = getFx();
        const changes = effectNames
            .map(name => fx.get(name)!)
            .reduce((prev, cur) => ({
                filter: [...prev.filter, ...cur.filter],
                weather: [...prev.weather, ...cur.weather],
            }), {filter: [], weather: []}) as FXMasterConfig;
        fxMaster.filters.setFilters(changes.filter);
        Hooks.call('fxmaster.updateParticleEffects', changes.weather);
    }
}

function getCurrentlyPlayingWeather(game: Game): StoredDocument<Playlist>[] {
    return game
            ?.playlists
            ?.filter(p => (p.name?.startsWith('weather.') ?? false) && p?.playing)
        ?? [];
}

async function applyWeatherSound(game: Game, effectName: string): Promise<void> {
    const playlistName = `weather.${effectName}`;

    // stop all others playlists
    for (const p of getCurrentlyPlayingWeather(game).filter(p => p.name !== playlistName)) {
        await p.stopAll();
    }

    // if playlist is not yet playing, start it
    if (getCurrentlyPlayingWeather(game).length === 0) {
        const playlist = game.playlists?.getName(playlistName);
        if (playlist) {
            await playlist.playAll();
        } else {
            console.warn(`No playlist found with name ${playlistName}`);
        }
    }
}

export async function setWeather(game: Game, effectName: string): Promise<void> {
    if (getBooleanSetting(game, 'enableWeather')) {
        console.info(`Setting weather to ${effectName}`);
        await game.settings.set('pf2e-kingmaker-tools', 'currentWeatherFx', effectName);
        await applyWeatherSound(game, effectName);
        if (effectName === 'storm') {
            applyWeatherEffects(['lightning', 'fog', 'heavyRain']);
        } else if (effectName === 'rain') {
            applyWeatherEffects(['rain']);
        } else if (effectName === 'heavyRain') {
            applyWeatherEffects(['heavyRain', 'fog']);
        } else if (effectName === 'snowfall') {
            applyWeatherEffects(['snowstorm']);
        } else if (effectName === 'snowstorm') {
            applyWeatherEffects(['snowstorm', 'heavyFog']);
        } else if (effectName === 'clouds') {
            applyWeatherEffects(['clouds']);
        } else if (effectName === 'heavyFog') {
            applyWeatherEffects(['heavyFog']);
        } else if (effectName === 'fog') {
            applyWeatherEffects(['fog']);
        } else {
            applyWeatherEffects(['sunny']);
        }
    } else {
        applyWeatherEffects(['sunny']);
        await applyWeatherSound(game, 'sunny');
        console.info('Weather is disabled, setting to sunny');
    }
}

/**
 * FxMaster sets effects as a flag on a scene, so we need to
 * manually sync it
 */
export async function syncWeather(game: Game): Promise<void> {
    const weather = getStringSetting(game, 'currentWeatherFx');
    await setWeather(game, weather);
}

export async function toggleWeather(game: Game): Promise<void> {
    const isEnabled = !getBooleanSetting(game, 'enableWeather');
    await game.settings.set('pf2e-kingmaker-tools', 'enableWeather', isEnabled);
    await syncWeather(game);
}
