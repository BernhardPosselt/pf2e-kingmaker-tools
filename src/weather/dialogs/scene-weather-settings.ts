import {parseCheckbox} from '../../utils';
import {getWeatherSettings, setWeatherSettings} from '../data';


export function sceneWeatherSettingsDialog(game: Game, scene: Scene): void {
    const {syncWeather, syncWeatherPlaylist} = getWeatherSettings(scene);
    new Dialog({
        title: 'Scene Weather Settings',
        content: `
        <form class="simple-dialog-form">
            <div>
                <label for="km-sync-weather">Sync Weather</label>
                <input type="checkbox" name="sync-weather" id="km-sync-weather" ${syncWeather ? 'checked' : ''}>
            </div>
            <div>
                <label for="km-sync-weather-playlist">Sync Weather Playlist</label>
                <input type="checkbox" name="sync-weather-playlist" id="km-sync-weather-playlist" ${syncWeatherPlaylist ? 'checked' : ''}>
            </div>
        </form>
        `,
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const syncWeather = parseCheckbox($html, 'sync-weather');
                    const syncWeatherPlaylist = parseCheckbox($html, 'sync-weather-playlist');
                    await setWeatherSettings(scene, {
                        syncWeather,
                        syncWeatherPlaylist,
                    });
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 250,
    }).render(true);
}