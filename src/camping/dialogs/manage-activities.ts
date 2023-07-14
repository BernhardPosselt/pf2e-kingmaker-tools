import {escapeHtml, parseCheckbox} from '../../utils';
import {CampingActivityData, CampingActivityName} from '../activities';

interface ManageActivitiesOptions {
    data: CampingActivityData[];
    lockedActivities: Set<CampingActivityName>;
    onSubmit: (lockedActivities: Set<CampingActivityName>, deletedActivities: Set<CampingActivityName>) => Promise<void>;
    isGM: boolean;
}

function tpl(data: CampingActivityData[], lockedActivities: Set<CampingActivityName>, isGM: boolean): string {
    return `
        <form class="camping-dialog">
            <table class="km-table">
                <tr>
                    <th>Name</th>
                    <th>Enabled</th>
                    <th>Delete</th>
                </tr>
                ${data.map((r, i) => {
        return `<tr>
                        <td>${escapeHtml(r.name)}</td>
                        <td><input type="checkbox" name="lock-${i}" ${!lockedActivities.has(r.name) ? 'checked' : ''}></td>
                        <td><input type="checkbox" name="delete-${i}" ${!isGM || !r.isHomebrew ? 'disabled' : ''}></td>                      
                    </tr>`;
    }).join('\n')}
            </table>
        </form>
        `;
}

export async function manageActivitiesDialog(options: ManageActivitiesOptions): Promise<void> {
    const data = [...options.data];
    data.sort((a, b) => a.name.localeCompare(b.name));
    new Dialog({
        title: 'Manage Activities',
        content: tpl(data, options.lockedActivities, options.isGM),
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const lockedActivities = data
                        .map((r, i) => {
                            const enabled = parseCheckbox($html, `lock-${i}`);
                            return {enabled, name: r.name};
                        })
                        .filter(r => !r.enabled)
                        .map(r => r.name);
                    const deletedActivities = data
                        .map((r, i) => {
                            const enabled = parseCheckbox($html, `delete-${i}`);
                            return {enabled, name: r.name};
                        })
                        .filter(r => r.enabled)
                        .map(r => r.name);
                    await options.onSubmit(new Set(lockedActivities), new Set(deletedActivities));
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 420,
        height: 600,
    }).render(true);
}
