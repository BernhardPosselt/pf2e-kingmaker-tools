import {getBooleanSetting, getStringSetting} from '../settings';
import {allWeatherNames, getWeatherSettings, WeatherEffectName} from './data';
import {isFirstGm} from '../utils';

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

function getSceneWeather(game: Game, scene: Scene | null | undefined): WeatherEffectName | null {
    if (scene && getWeatherSettings(scene).syncWeather) {
        const effectName = getBooleanSetting(game, 'enableSheltered')
            ? ''
            : getStringSetting(game, 'currentWeatherFx') as WeatherEffectName;
        if (effectName === 'sunny') {
            return '';
        } else if (allWeatherNames.includes(effectName)) {
            return effectName;
        }
    }
    return null;
}

function getWeatherPlaylist(game: Game, activeScene: Scene | undefined | null): WeatherEffectName {
    if (activeScene && getWeatherSettings(activeScene).syncWeatherPlaylist) {
        return activeScene.weather || (getBooleanSetting(game, 'enableSheltered') ? '' : 'sunny');
    } else {
        return '';
    }
}

async function syncSceneWeather(game: Game, scene: Scene | null | undefined): Promise<void> {
    const effectName = await getSceneWeather(game, scene);
    if (scene && effectName !== null) {
        console.info(`Setting scene ${scene.name} weather to '${effectName}'`);
        await scene?.update({'weather': effectName});
    }
}

async function syncScenePlaylist(game: Game, scene: Scene | null | undefined): Promise<void> {
    const weatherEffect = getWeatherPlaylist(game, scene);
    console.info(`Setting weather playlist to to ${weatherEffect}`);
    await applyWeatherSound(game, weatherEffect);
}

async function syncWeather(game: Game): Promise<void> {
    if (isFirstGm(game) && getBooleanSetting(game, 'enableWeather')) {
        const currentScene = game.scenes?.current;
        const activeScene = game.scenes?.active;
        if (activeScene && currentScene && activeScene.id === currentScene.id) {
            await syncSceneWeather(game, activeScene);
        } else {
            await syncSceneWeather(game, currentScene);
            await syncSceneWeather(game, activeScene);
        }
        await syncScenePlaylist(game, activeScene);
    }
}

export async function setWeather(game: Game, effectName: WeatherEffectName): Promise<void> {
    await game.settings.set('pf2e-kingmaker-tools', 'currentWeatherFx', effectName);
    await syncWeather(game);
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

export async function onPreUpdateScene(game: Game, scene: Scene, update: Partial<Scene>): Promise<void> {
    if (isFirstGm(game) && getBooleanSetting(game, 'enableWeather') && update?.active) {
        // make sure to set the weather effect when viewing a scene and then activating it
        const weather = getSceneWeather(game, scene);
        if (weather !== null) {
            update.weather = weather;
        }
    }
}

export async function onUpdateScene(game: Game, scene: Scene, update: Partial<Scene>): Promise<void> {
    if (isFirstGm(game) && getBooleanSetting(game, 'enableWeather') && update?.active) {
        // activating a scene plays its weather playlist
        // setting the correct scene weather is done pre update or on render
        await syncScenePlaylist(game, scene);
    }
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export async function onRenderScene(game: Game): Promise<void> {
    // disabled because this produces foundry race conditions
    // const current = game.scenes?.current;
    // if (isFirstGm(game) && getBooleanSetting(game, 'enableWeather') && current) {
    //     // when changing scenes, also ensure that the weather syncs without activating the scene
    //     const current = game.scenes?.current;
    //     await syncSceneWeather(game, current);
    // }
}

