import {escapeHtml, parseCheckbox, parseNumberInput, parseSelect, parseTextInput, rollModeChoices} from '../../utils';
import {RestRollMode} from '../camping';
import {setSetting} from '../../settings';

export interface SettingData {
    gunsToClean: number;
    restRollMode: RestRollMode;
    increaseWatchActorNumber: number;
    huntAndGatherTargetActorUuid: string | null | undefined;
    actors: Actor[];
    actorsKeepingWatch: { uuid: string, name: string, watchEnabled: boolean }[];
    ignoreSkillRequirements: boolean;
    randomEncounterRollMode: RollMode,
    proxyRandomEncounterTable: string;
}

export interface CampingSettingOptions {
    onSubmit: (data: {
        gunsToClean: number,
        restRollMode: RestRollMode,
        increaseWatchActorNumber: number,
        actorUuidsNotKeepingWatch: string[],
        huntAndGatherTargetActorUuid: string | null,
        ignoreSkillRequirements: boolean,
    }) => Promise<void>;
    data: SettingData;
    game: Game,
}

export function campingSettingsDialog({onSubmit, data, game}: CampingSettingOptions): void {
    new Dialog({
        title: 'Camping Settings',
        content: `
        <form class="km-settings-form">
            <div>
                <div class="form-elements">
                    <label for="km-guns-to-clean">Guns To Clean</label>
                    <input type="number" name="guns-to-clean" id="km-guns-to-clean" value="${data.gunsToClean}">
                </div>
            </div>
            <div>
                <div class="form-elements">
                    <label for="km-increase-actor-watch-number">Increase Actors Keeping Watch</label>
                    <input type="number" name="increase-actor-watch-number" id="km-increase-actor-watch-number" value="${data.increaseWatchActorNumber}">
                </div>
            </div>
            ${data.actorsKeepingWatch.map(a => {
            return `
                <div>
                    <div class="form-elements">
                        <label for="km-actor-uuids-keeping-watch-${a.uuid}">Keep Watch: ${escapeHtml(a.name)}</label>
                        <input type="checkbox" name="actor-uuids-keeping-watch-${a.uuid}" id="km-actor-uuids-keeping-watch-${a.uuid}" ${a.watchEnabled ? 'checked' : ''}>
                    </div>
                </div>
                `;
        }).join('')}
            <div>
                <div class="form-elements">
                    <label for="km-rest-roll-mode">Rest Random Encounter Roll Mode</label>
                    <select name="rest-roll-mode" id="km-rest-roll-mode">
                        <option value="none" ${data.restRollMode === 'none' ? 'selected' : ''}>Don't Roll Encounter Check</option>
                        <option value="one" ${data.restRollMode === 'one' ? 'selected' : ''}>Roll 1 Encounter Check</option>
                        <option value="one-every-4-hours" ${data.restRollMode === 'one-every-4-hours' ? 'selected' : ''}>Roll 1 Encounter Check Every 4 Hours</option>
                    </select>
                </div>
            </div>
            <div>
                <div class="form-elements">
                    <label for="km-proxy-random-encounter-table">Proxy Random Encounter Table</label>
                    <input type="text" name="proxy-random-encounter-table" id="km-proxy-random-encounter-table" value="${data.proxyRandomEncounterTable}">
                </div>
                <div class="help">
                    <p>Name of the in world roll table that is rolled first to check what kind of encounter is rolled. Use the string "Creature" to roll on the region roll table in the proxy roll table or link another roll table of your choice. Leave blank to always roll on the region random encounter tables.</p>
                </div>
            </div>
            <div>
                <div class="form-elements">
                    <label for="km-random-encounter-roll-mode">Random Encounter Roll Mode</label>
                    <select name="random-encounter-roll-mode" id="km-random-encounter-roll-mode">
                        ${Object.entries(rollModeChoices)
            .map(([value, label]) => {
                    return `<option value="${value}" ${value === data.randomEncounterRollMode ? 'selected' : ''}>${label}</option>`;
                },
            ).join('')}
                    </select>
                </div>
            </div>
            <div>
                <div class="form-elements">
                    <label for="km-hunt-and-gather-actor">Add Ingredients from Hunt and Gather To</label>
                    <select name="hunt-and-gather-actor" id="km-hunt-and-gather-actor">
                        <option value="">Actor performing Hunt and Gather</option>
                        ${data.actors.map(a => {
            return `
                            <option value="${a.uuid}" ${a.uuid === data.huntAndGatherTargetActorUuid ? 'selected' : ''}>${a.name}</option>
                            `;
        }).join('')}
                    </select>
                </div>
            </div>
            <div>
                <div class="form-elements">
                    <label for="km-ignore-skill-requirements">Do not validate Activity Skill Proficiency</label>
                    <input type="checkbox" name="ignore-skill-requirements" id="km-ignore-skill-requirements" ${data.ignoreSkillRequirements ? 'checked' : ''}>
                </div>
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
                    const increaseWatchActorNumber = parseNumberInput($html, 'increase-actor-watch-number') || 0;
                    const restRollMode = parseSelect($html, 'rest-roll-mode') as RestRollMode;
                    const huntAndGatherTargetActorUuid = parseSelect($html, 'hunt-and-gather-actor') || null;
                    const ignoreSkillRequirements = parseCheckbox($html, 'ignore-skill-requirements') || false;
                    const actorUuidsNotKeepingWatch = data.actorsKeepingWatch
                        .filter(a => !parseCheckbox($html, `actor-uuids-keeping-watch-${a.uuid}`))
                        .map(a => a.uuid);
                    const randomEncounterRollMode: RollMode = (parseSelect($html, 'random-encounter-roll-mode') as RollMode) || 'publicroll';
                    const proxyRandomEncounterTable = parseTextInput($html, 'proxy-random-encounter-table');
                    await setSetting(game, 'proxyEncounterTable', proxyRandomEncounterTable);
                    await setSetting(game, 'randomEncounterRollMode', randomEncounterRollMode);
                    await onSubmit({
                        gunsToClean,
                        restRollMode,
                        increaseWatchActorNumber,
                        actorUuidsNotKeepingWatch,
                        huntAndGatherTargetActorUuid,
                        ignoreSkillRequirements,
                    });
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 550,
    }).render(true);
}
