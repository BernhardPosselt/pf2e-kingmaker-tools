import {getBooleanSetting, getStringSetting} from './settings';

export function dayHasChanged(game: Game, deltaInSeconds: number): boolean {
    const now = game.pf2e.worldClock.worldTime;
    const previous = now.minus({second: deltaInSeconds});
    return now.day !== previous.day || now.month !== previous.month || now.year !== previous.year;
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

export async function setWeather(game: Game, effectName: 'snowfall' | 'rain' | 'sunny'): Promise<void> {
    // always persist the current one without checking for turned off weather
    await game.settings.set('pf2e-kingmaker-tools', 'currentWeatherFx', effectName);
    // fall back to sunny if weather is disabled
    const eff = getBooleanSetting(game, 'enableWeather') ? effectName : 'sunny';
    await applyWeatherSound(game, eff);
    console.info(`Setting weather to ${eff}`);
    if (eff === 'rain') {
        await game.scenes?.current?.update({'weather': 'rain'});
    } else if (eff === 'snowfall') {
        await game.scenes?.current?.update({'weather': 'snow'});
    } else {
        await game.scenes?.current?.update({'weather': ''});
    }
}

export async function syncWeather(game: Game): Promise<void> {
    const weather = getStringSetting(game, 'currentWeatherFx') as 'sunny' | 'snowfall' | 'rain';
    await setWeather(game, weather);
}

export async function toggleWeather(game: Game): Promise<void> {
    const isEnabled = !getBooleanSetting(game, 'enableWeather');
    await game.settings.set('pf2e-kingmaker-tools', 'enableWeather', isEnabled);
    await syncWeather(game);
}
