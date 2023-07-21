import {getBooleanSetting, getStringSetting} from './settings';
import {deCamelCase, parseSelect} from './utils';

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
    if (getCurrentlyPlayingWeather(game).length === 0 && effectName !== 'none') {
        const playlist = game.playlists?.getName(playlistName);
        if (playlist) {
            await playlist.playAll();
        } else {
            console.warn(`No playlist found with name ${playlistName}`);
        }
    }
}

const allWeatherNames = [
    'snow',
    'rain',
    'sunny',
    'leaves',
    'rainStorm',
    'fog',
    'blizzard',
    'none',
] as const;
type WeatherEffectName = typeof allWeatherNames[number];

export async function setWeather(game: Game, effectName: WeatherEffectName): Promise<void> {
    if (getBooleanSetting(game, 'enableWeather')) {
        // always persist the current one without checking for turned off weather
        await game.settings.set('pf2e-kingmaker-tools', 'currentWeatherFx', effectName);
        // fall back to sunny if weather is disabled
        const eff = getBooleanSetting(game, 'enableSheltered') ? 'none' : effectName;
        await applyWeatherSound(game, eff);
        console.info(`Setting weather to ${eff}`);
        if (eff !== 'sunny' && eff !== 'none' && allWeatherNames.includes(eff)) {
            await game.scenes?.current?.update({'weather': eff});
        } else {
            await game.scenes?.current?.update({'weather': ''});
        }
    }
}

export async function syncWeather(game: Game): Promise<void> {
    const weather = getStringSetting(game, 'currentWeatherFx') as WeatherEffectName;
    await setWeather(game, weather);
}

export async function toggleShelterd(game: Game): Promise<void> {
    const isEnabled = !getBooleanSetting(game, 'enableSheltered');
    await game.settings.set('pf2e-kingmaker-tools', 'enableSheltered', isEnabled);
    await syncWeather(game);
}

export async function toggleWeather(game: Game): Promise<void> {
    const isEnabled = !getBooleanSetting(game, 'enableWeather');
    await game.settings.set('pf2e-kingmaker-tools', 'enableWeather', isEnabled);
    await syncWeather(game);
}

export function setCurrentWeatherDialog(game: Game): void {
    const weather = getStringSetting(game, 'currentWeatherFx') as WeatherEffectName;
    const labels = allWeatherNames.map(name => {
        return {label: deCamelCase(name), value: name};
    });
    new Dialog({
        title: 'Set Weather',
        content: `
        <form class="simple-dialog-form">
            <div>
                <label for="km-weather">Weather</label>
                <select name="weather" id="km-weather">
                    ${labels.map((l) => {
            return `<option value="${l.value}" ${weather === l.value ? 'selected' : ''}>${l.label}</option>`;
        }).join('')}                
                </select>
            </div>
        </form>
        `,
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const weather = parseSelect($html, 'weather') as WeatherEffectName;
                    await setWeather(game, weather);
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 250,
    }).render(true);
}
