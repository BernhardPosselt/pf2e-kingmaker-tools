import {Activity} from '../data/activities';
import {unslugifyActivity} from '../../utils';

function tpl(blacklistedActivities: Activity[], activities: Activity[]): string {
    const blacklisted = new Set(blacklistedActivities);
    return activities
        .sort((a, b) => unslugifyActivity(a).localeCompare(unslugifyActivity(b)))
        .map(activity => {
        return `<div>
            <label for="activity-${activity}">${unslugifyActivity(activity)}</label>
            <input id="activity-${activity}" name="${activity}" type="checkbox" ${blacklisted.has(activity) ? 'checked' : ''}>
        </div>`;
    }).join('\n');
}

export function activityBlacklistDialog(
    blacklistedActivities: Activity[],
    activities: Activity[],
    onOk: (blacklistedActivities: Activity[]) => void
): void {
    new Dialog({
        title: 'Activity Blacklist',
        content: `
        <form class="simple-dialog-form">
            ${tpl(blacklistedActivities, activities)}
        </form>
        `,
        buttons: {
            blacklist: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Blacklist',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const checkboxes = Array.from($html.querySelectorAll('input'));
                    const blacklisted = checkboxes
                        .filter(checkbox => checkbox.checked)
                        .map(checkbox => checkbox.name) as Activity[];
                    onOk(blacklisted);
                },
            },
        },
        default: 'blacklist',
    }, {
        jQuery: false,
        width: 250,
    }).render(true);
}
