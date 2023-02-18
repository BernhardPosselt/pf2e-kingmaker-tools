import {Activity} from '../data/activities';
import {findHelp} from '../data/activityData';
import {capitalize} from '../../utils';
import {rankToLabel} from '../modifiers';

export async function showHelpDialog(help: string): Promise<void> {
    const data = findHelp(help as Activity);
    const traits = (data.fortune ? [capitalize(data.phase), 'Downtime', 'Fortune'] : [capitalize(data.phase), 'Downtime'])
        .join(', ');
    const skills = Object.entries(data.skills)
        .map(([skill, rank]) => {
            if (rank === 0) {
                return capitalize(skill);
            } else {
                return `${capitalize(skill)}: ${rankToLabel(rank)}`;
            }
        })
        .join(', ');

    await new Dialog({
        title: data.title,
        content: `
        <h2>${data.title}</h2>
        <p><b>Skills</b>: ${skills}</p>
        <p><b>Traits</b>: ${traits}</p>
        ${data.companion ? '<p><b>Companion</b>: ${data.companion}</p>' : ''}
        ${data.requirement ? `<p><b>Requirements</b>: ${data.requirement}</p>`: ''}
        <hr>
        <div>${await TextEditor.enrichHTML(data.description)}</div>
        <hr>
        ${data.criticalSuccess ? `<p><b>Critical Success</b>: ${await TextEditor.enrichHTML(data.criticalSuccess.msg)}</p>`: ''}
        ${data.success ? `<p><b>Success</b>: ${await TextEditor.enrichHTML(data.success.msg)}</p>`: ''}
        ${data.failure ? `<p><b>Failure</b>: ${await TextEditor.enrichHTML(data.failure.msg)}</p>`: ''}
        ${data.criticalFailure ? `<p><b>Critical Failure</b>: ${await TextEditor.enrichHTML(data.criticalFailure.msg)}</p>`: ''}
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
        width: 500,
    }).render(true);
}
