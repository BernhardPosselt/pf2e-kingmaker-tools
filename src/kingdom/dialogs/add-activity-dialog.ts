import {ActivityResult, KingdomActivity, SkillRanks} from '../data/activityData';
import {
    capitalize,
    escapeHtml,
    isBlank,
    parseCheckbox,
    parseNumberInput,
    parseNumberSelect,
    parseSelect,
    parseTextArea,
    parseTextInput,
    range,
} from '../../utils';
import {KingdomPhase} from '../data/activities';
import {allSkillRanks, allSkills} from '../data/skills';


function parseOptionalTextArea(html: HTMLElement, selector: string): string | undefined {
    const value = parseTextArea(html, selector);
    return isBlank(value) ? undefined : value;
}

function parseDc(html: HTMLElement, selector: string): 'control' | 'custom' | 'none' | number {
    const value = parseSelect(html, selector);
    if (value === 'control' || value === 'custom' || value === 'none') {
        return value;
    } else {
        return parseInt(value, 10) || 0;
    }
}


function parseOutcome(html: HTMLElement, selector: string): ActivityResult | undefined {
    const value = parseTextArea(html, selector);
    if (isBlank(value)) {
        return undefined;
    } else {
        return {
            msg: value,
        };
    }
}

function parseSkills(html: HTMLElement): SkillRanks {
    const skill = parseSelect(html, 'skill');
    const rank = parseNumberSelect(html, 'rank');
    return {
        [skill]: rank,
    };
}

export function addActivityDialog(
    onOk: (activity: KingdomActivity) => Promise<void>,
    activity?: KingdomActivity,
): void {
    const data = activity ?? {
        special: '',
        requirement: '',
        description: '',
        dcAdjustment: 0,
        fortune: false,
        failure: {msg: ''},
        success: {msg: ''},
        criticalFailure: {msg: ''},
        criticalSuccess: {msg: ''},
        dc: 'control',
        title: '',
        hint: '',
        id: '',
        skills: {agriculture: 0},
        phase: 'leadership',
        oncePerRound: false,
    };
    new Dialog({
        title: 'Add Kingdom Activity',
        content: `
        <p>Use the same id as an existing activity to override it. The ID is the name in lowercase separated by hyphens, e.g. "Do Something" becomes "do-something".</p>
        <form class="simple-dialog-form">
            <div>
                <label for="km-add-kingdom-activity-id">Id (lowercase English letters and hyphens)</label>
                <input type="text" name="id" id="km-add-kingdom-activity-id" value="${escapeHtml(data.id)}" ${activity ? 'disabled' : ''}>
            </div>
            <div>
                <label for="km-add-kingdom-activity-title">Title</label>
                <input type="text" name="title" id="km-add-kingdom-activity-title"  value="${escapeHtml(data.title)}">
            </div>
            <div>
                <label for="km-add-kingdom-activity-description">Description</label>
                <textarea name="description" id="km-add-kingdom-activity-description">${escapeHtml(data.description)}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-hint">Hint</label>
                <textarea name="hint" id="km-add-kingdom-activity-hint">${escapeHtml(data.hint ?? '')}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-special">Special</label>
                <textarea name="special" id="km-add-kingdom-activity-special">${escapeHtml(data.special ?? '')}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-requirement">Requirement</label>
                <textarea name="requirement" id="km-add-kingdom-activity-requirement">${escapeHtml(data.requirement ?? '')}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-phase">Phase</label>
                <select id="km-add-kingdom-activity-phase" name="phase">
                ${
            ['army', 'leadership', 'region']
                .map(p => `<option value="${p}" ${data.phase === p ? 'selected' : ''}>${capitalize(p)}</option>`)
                .join('')
        }
                </select>
            </div>
            <div>
                <label for="km-add-kingdom-activity-skill">Skill</label>
                <select id="km-add-kingdom-activity-skill" name="skill">
                ${
            allSkills
                .map(p => `<option value="${p}" ${Object.keys(data.skills)[0] === p ? 'selected' : ''}>${capitalize(p)}</option>`)
                .join('')
        }
                </select>
            </div>
            <div>
                <label for="km-add-kingdom-activity-rank">Proficiency</label>
                <select id="km-add-kingdom-activity-rank" name="rank">
                ${
            allSkillRanks
                .map((p, index) => `<option value="${index}" ${Object.values(data.skills)[0] === index ? 'selected' : ''}>${capitalize(p)}</option>`)
                .join('')
        }
                </select>
            </div>
            <div>
                <label for="km-add-kingdom-activity-dc">DC</label>
                <select id="km-add-kingdom-activity-dc" name="dc">
                    <option value="none" ${data.dc === 'none' ? 'selected' : ''}>None</option>
                    <option value="control" ${data.dc === 'control' ? 'selected' : ''}>Control</option>
                ${
            range(14, 51)
                .map((p) => `<option value="${p}" ${data.dc === p ? 'selected' : ''}>${p}</option>`)
                .join('')
        }
                </select>
            </div>
            <div>
                <label for="km-add-kingdom-activity-dc-adjustment">DC Adjustment</label>
                <input type="number" name="dcAdjustment" id="km-add-kingdom-activity-dc-adjustment" value="${data.dcAdjustment ?? 0}">
            </div>
            <div>
                <label for="km-add-kingdom-activity-once-per-round">Once Per Round</label>
                <input type="checkbox" name="oncePerRound" id="km-add-kingdom-activity-once-per-round" ${data.oncePerRound ? 'checked' : ''}>
            </div>
            <div>
                <label for="km-add-kingdom-activity-fortune">Fortune</label>
                <input type="checkbox" name="fortune" id="km-add-kingdom-activity-fortune" ${data.fortune ? 'checked' : ''}>
            </div>
            <div>
                <label for="km-add-kingdom-activity-critical-success">Critical Success</label>
                <textarea name="criticalSuccess" id="km-add-kingdom-activity-critical-success">${escapeHtml(data.criticalSuccess?.msg ?? '')}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-success">Success</label>
                <textarea name="success" id="km-add-kingdom-activity-success">${escapeHtml(data.success?.msg ?? '')}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-failure">Failure</label>
                <textarea name="failure" id="km-add-kingdom-activity-failure">${escapeHtml(data.failure?.msg ?? '')}</textarea>
            </div>
            <div>
                <label for="km-add-kingdom-activity-critical-failure">Critical Failure</label>
                <textarea name="criticalFailure" id="km-add-kingdom-activity-critical-failure">${escapeHtml(data.criticalFailure?.msg ?? '')}</textarea>
            </div>
        </form>
        `,
        buttons: {
            add: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const dcAdjustment = parseNumberInput($html, 'dcAdjustment');
                    const id = parseTextInput($html, 'id');
                    if (!/^[a-z\\-]+$/.test(id)) {
                        ui.notifications?.error('ID must only contain lower case English letters and hyphens!');
                    } else {
                        await onOk({
                            id,
                            title: parseTextInput($html, 'title'),
                            description: parseTextArea($html, 'description'),
                            enabled: true,
                            phase: parseSelect($html, 'phase') as KingdomPhase,
                            skills: parseSkills($html),
                            hint: parseOptionalTextArea($html, 'hint'),
                            requirement: parseOptionalTextArea($html, 'requirement'),
                            special: parseOptionalTextArea($html, 'requirement'),
                            dc: parseDc($html, 'dc'),
                            dcAdjustment,
                            oncePerRound: parseCheckbox($html, 'oncePerRound'),
                            fortune: parseCheckbox($html, 'fortune'),
                            criticalSuccess: parseOutcome($html, 'criticalSuccess'),
                            success: parseOutcome($html, 'success'),
                            failure: parseOutcome($html, 'failure'),
                            criticalFailure: parseOutcome($html, 'criticalFailure'),
                        });
                    }
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 550,
    }).render(true);

}