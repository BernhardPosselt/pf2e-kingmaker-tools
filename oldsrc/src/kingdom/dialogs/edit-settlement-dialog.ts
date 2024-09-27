import {Settlement} from '../data/kingdom';
import {parseCheckbox, parseNumberInput, parseSelect} from '../../utils';

export function editSettlementDialog(autoLevel: boolean, name: string, data: Settlement, onOk: (data: Settlement) => void): void {
    const showLevelConfig = data.manualSettlementLevel === true || !autoLevel;
    new Dialog({
        title: `Edit Settlement: ${name}`,
        content: `
        <form class="simple-dialog-form">
            <div>
                <label>Type</label>
                <select name="type">
                    <option value="capital" ${data.type === 'capital' ? 'selected' : ''}>Capital</option>
                    <option value="settlement" ${data.type === 'settlement' ? 'selected' : ''}>Settlement</option>
                </select>
            </div>
            <div ${showLevelConfig ? '' : 'hidden'}>
                <label>Occupied Blocks</label>
                <input name="lots" type="number" value="${data.lots}">
            </div>
            <div ${showLevelConfig ? '' : 'hidden'}>
                <label>Level</label>
                <input name="level" type="number" value="${data.level}">
            </div>
            <div>
                <label>Water Borders</label>
                <input name="waterBorders" type="number" value="${data.waterBorders}">
            </div>
            <div>
                <label>Secondary Territory</label>
                <input name="secondaryTerritory" type="checkbox" ${data.secondaryTerritory ? 'checked' : ''}>
            </div>
            <div>
                <label>Manual Settlement Level (reopen dialog after saving)</label>
                <input name="manualSettlementLevel" type="checkbox" ${data.manualSettlementLevel === true ? 'checked' : ''}>
            </div>
        </form>
        `,
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    onOk({
                        level: parseNumberInput($html, 'level'),
                        secondaryTerritory: parseCheckbox($html, 'secondaryTerritory'),
                        type: parseSelect($html, 'type') as 'capital' | 'settlement',
                        lots: parseNumberInput($html, 'lots'),
                        waterBorders: parseNumberInput($html, 'waterBorders'),
                        sceneId: data.sceneId,
                        manualSettlementLevel: parseCheckbox($html, 'manualSettlementLevel'),
                    });
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 580,
    }).render(true);
}
