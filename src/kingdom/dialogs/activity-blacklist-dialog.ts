import {unslugify} from '../../utils';

function tpl(blacklistedActivities: string[], activities: string[]): string {
    const blacklisted = new Set(blacklistedActivities);
    return activities
        .sort((a, b) => unslugify(a).localeCompare(unslugify(b)))
        .map(activity => {
            return `<div>
            <label for="activity-${activity}">${unslugify(activity)}</label>
            <input id="activity-${activity}" name="${activity}" type="checkbox" ${blacklisted.has(activity) ? 'checked' : ''}>
        </div>`;
        }).join('\n');
}

export function activityBlacklistDialog(
    blacklistedActivities: string[],
    activities: string[],
    onOk: (blacklistedActivities: string[]) => void,
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
                        .map(checkbox => checkbox.name);
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
