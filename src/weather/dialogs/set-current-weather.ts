import {getStringSetting} from '../../settings';
import {deCamelCase, parseSelect} from '../../utils';
import {setWeather} from '../weather';
import {allWeatherNames, WeatherEffectName} from '../data';

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