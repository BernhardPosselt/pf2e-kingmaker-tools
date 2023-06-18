import {escapeHtml, parseCheckbox} from '../../utils';
import {CampingActivityData, CampingActivityName} from '../activities';

interface ManageActivitiesOptions {
    data: CampingActivityData[];
    lockedActivities: Set<CampingActivityName>;
    onSubmit: (lockedActivities: Set<CampingActivityName>) => Promise<void>
}

function tpl(data: CampingActivityData[], lockedActivities: Set<CampingActivityName>): string {
    return `
        <form>
            <table class="km-table">
                <tr>
                    <th>Name</th>
                    <th>Enabled</th>
                </tr>
                ${data.map(r => {
                    return `<tr>
                        <td>${escapeHtml(r.name)}</td>
                        <td><input type="checkbox" name="${escapeHtml(r.name)}" ${!lockedActivities.has(r.name) ? 'checked': ''}></td>
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
        content: tpl(data, options.lockedActivities),
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const lockedActivities = options.data
                        .map(r => {
                            const enabled = parseCheckbox($html, r.name);
                            return {enabled, name: r.name};
                        })
                        .filter(r => !r.enabled)
                        .map(r => r.name);
                    await options.onSubmit(new Set(lockedActivities));
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 420,
    }).render(true);
}
