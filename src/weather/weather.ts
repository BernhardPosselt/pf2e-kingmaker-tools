import {getBooleanSetting, getStringSetting} from '../settings';
import {allWeatherNames, getWeatherSettings, WeatherEffectName} from './data';

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

async function applyWeatherSound(game: Game, effectName: WeatherEffectName): Promise<void> {
    const playlistName = `weather.${effectName}`;

    // stop all others playlists
    for (const p of getCurrentlyPlayingWeather(game).filter(p => p.name !== playlistName)) {
        await p.stopAll();
    }

    // if playlist is not yet playing, start it
    if (getCurrentlyPlayingWeather(game).length === 0 && effectName !== '') {
        const playlist = game.playlists?.getName(playlistName);
        if (playlist) {
            await playlist.playAll();
        } else {
            console.warn(`No playlist found with name ${playlistName}`);
        }
    }
}

async function updateSceneWeather(scene: Scene | null | undefined, eff: WeatherEffectName): Promise<void> {
    if (scene && getWeatherSettings(scene).syncWeather) {
        console.info(`Setting scene ${scene.name} weather to ${eff}`);
        if (eff !== 'sunny' && eff !== '' && allWeatherNames.includes(eff)) {
            await scene.update({'weather': eff});
        } else {
            await scene?.update({'weather': ''});
        }
    }
}

export async function setWeather(game: Game, effectName: WeatherEffectName): Promise<void> {
    if (getBooleanSetting(game, 'enableWeather')) {
        // always persist the current one without checking for turned off weather
        await game.settings.set('pf2e-kingmaker-tools', 'currentWeatherFx', effectName);
        // fall back to sunny if weather is disabled
        const eff = getBooleanSetting(game, 'enableSheltered') ? '' : effectName;
        const currentScene = game.scenes?.current;
        const activeScene = game.scenes?.active;
        await updateSceneWeather(currentScene, eff);
        await updateSceneWeather(activeScene, eff);
        if (activeScene) {
            if (getWeatherSettings(activeScene).syncWeatherPlaylist) {
                const weatherEffect = activeScene.weather
                    || (getBooleanSetting(game, 'enableSheltered') ? '' : 'sunny');
                console.info(`Setting weather playlist to to ${weatherEffect}`);
                await applyWeatherSound(game, weatherEffect);
            } else {
                console.info('Disabling weather playlist since syncing was turned off');
                await applyWeatherSound(game, '');
            }
        }
    }
}

export async function syncWeather(game: Game): Promise<void> {
    const weather = getStringSetting(game, 'currentWeatherFx') as WeatherEffectName;
    await setWeather(game, weather);
}

export async function toggleSheltered(game: Game): Promise<void> {
    const isEnabled = !getBooleanSetting(game, 'enableSheltered');
    await game.settings.set('pf2e-kingmaker-tools', 'enableSheltered', isEnabled);
    await syncWeather(game);
}

export async function toggleWeather(game: Game): Promise<void> {
    const isEnabled = !getBooleanSetting(game, 'enableWeather');
    await game.settings.set('pf2e-kingmaker-tools', 'enableWeather', isEnabled);
    await syncWeather(game);
}

