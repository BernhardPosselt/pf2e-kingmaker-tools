import {parseNumberInput, parseSelect} from '../../utils';
import {RestRollMode} from '../camping';

export interface SettingData {
    gunsToClean: number;
    restRollMode: RestRollMode;
}
export interface CampingSettingOptions {
    onSubmit: (data: SettingData) => Promise<void>;
    data: SettingData;
}

export function campingSettingsDialog({onSubmit, data}: CampingSettingOptions): void {
    new Dialog({
        title: 'Camping Settings',
        content: `
        <form class="simple-dialog-form">
            <div>
                <label for="km-guns-to-clean">Guns To Clean</label>
                <input type="number" name="guns-to-clean" id="km-guns-to-clean" value="${data.gunsToClean}">
            </div>
            <div>
                <label for="km-rest-roll-mode">Rest Random Encounter Roll Mode</label>
                <select name="rest-roll-mode" id="km-rest-roll-mode">
                    <option value="none" ${data.restRollMode === 'none' ? 'selected' : ''}>Don't Roll Encounter Check</option>
                    <option value="one" ${data.restRollMode === 'one' ? 'selected' : ''}>Roll 1 Encounter Check</option>
                    <option value="one-every-4-hours" ${data.restRollMode === 'one-every-4-hours' ? 'selected' : ''}>Roll 1 Encounter Check Every 4 Hours</option>
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
                    const gunsToClean = parseNumberInput($html, 'guns-to-clean');
                    const restRollMode = parseSelect($html, 'rest-roll-mode') as RestRollMode;
                    await onSubmit({gunsToClean, restRollMode});
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 420,
    }).render(true);
}
