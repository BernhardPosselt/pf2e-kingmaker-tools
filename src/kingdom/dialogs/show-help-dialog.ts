import {Activity} from '../data/activities';
import {findHelp} from '../data/help';

export function showHelpDialog(help: string): void {
    const data = findHelp(help as Activity);
    new Dialog({
        title: data.title,
        content: `
        <h2>${data.title}</h2>
        ${data.requirement ? `<b>Requirements</b>: ${data.requirement}<hr>`: ''}
        <div>${data.description}</div>
        <hr>
        <ul>
            ${data.criticalSuccess ? `<li><b>Critical Success</b>: ${data.criticalSuccess}</li>`: ''}
            ${data.success ? `<li><b>Success</b>: ${data.success}</li>`: ''}
            ${data.failure ? `<li><b>Failure</b>: ${data.failure}</li>`: ''}
            ${data.criticalFailure ? `<li><b>Critical Failure</b>: ${data.criticalFailure}</li>`: ''}
        </ul>
        ${data.special ? `<hr><b>Special</b>: ${data.special}`: ''}
        `,
        buttons: {
            close: {
                icon: '<i class="fa-solid close"></i>',
                label: 'Close',
            },
        },
        default: 'close',
    }, {
        jQuery: false,
        width: 400,
    }).render(true);
}
