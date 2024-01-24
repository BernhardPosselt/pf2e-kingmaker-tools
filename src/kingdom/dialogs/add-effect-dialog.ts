import {allKingdomPhases, KingdomPhase} from '../data/activities';
import {allModifierTypes, Modifier, ModifierType} from '../modifiers';
import {capitalize, createLabels, parseCheckbox, parseNumberInput, parseSelect, parseTextInput} from '../../utils';
import {allSkills, Skill} from '../data/skills';
import {Ability, allAbilities} from '../data/abilities';

const types = allModifierTypes.flatMap(type => {
    if (type === 'vacancy') {
        return [{
            label: capitalize(type) + ' Penalty',
            value: `${type}-penalty`,
        }];
    } else {
        return [{
            label: capitalize(type) + ' Bonus',
            value: `${type}-bonus`,
        }, {
            label: capitalize(type) + ' Penalty',
            value: `${type}-penalty`,
        }];
    }
});
const phases = createLabels(allKingdomPhases);
const skills = createLabels(allSkills);
const abilities = createLabels(allAbilities);

function tpl(allActivities: string[]): string {
    const activities = createLabels(allActivities, true);
    return `
        <form class="simple-dialog-form">
            <div>
                <label for="km-effect-name">Name</label>
                <input type="text" name="name" id="km-effect-name">
            </div>
            <div>
                <label for="km-effect-type"></label>
                <select name="type" id="km-effect-type">
                    ${types.map(t => `<option value="${t.value}">${t.label}</option>`)}
                </select>
            </div>
            <div>
                <label for="km-effect-value">Value</label>
                <input type="number" name="value" id="km-effect-value">
            </div>
            <div>
                <label for="km-effect-phase">Phase</label>
                <select name="phase" id="km-effect-phase">
                    <option value="-">-</option>
                    ${phases.map(t => `<option value="${t.value}">${t.label}</option>`)}
                </select>
            </div>
            <div>
                <label for="km-effect-activity">Activity</label>
                <select name="activity" id="km-effect-activity">
                    <option value="-">-</option>
                    ${activities.map(t => `<option value="${t.value}">${t.label}</option>`)}
                </select>
            </div>
            <div>
                <label for="km-effect-ability">Ability</label>
                <select name="ability" id="km-effect-ability">
                    <option value="-">-</option>
                    ${abilities.map(t => `<option value="${t.value}">${t.label}</option>`)}
                </select>
            </div>
            <div>
                <label for="km-effect-skill">Skill</label>
                <select name="skill" id="km-effect-skill">
                    <option value="-">-</option>
                    ${skills.map(t => `<option value="${t.value}">${t.label}</option>`)}
                </select>
            </div>
            <div>
                <label for="km-effect-enabled">Enabled</label>
                <input type="checkbox" name="enabled" id="km-effect-enabled" checked>
            </div>
            <div>
                <label for="km-effect-turns">Turns (0 for indefinite)</label>
                <input type="number" name="turns" id="km-effect-turns" value="1">
            </div>
            <div>
                <label for="km-effect-consumable">Consume after Use</label>
                <input type="checkbox" name="consumable" id="km-effect-consumable">
            </div>
        </form>
        `;
}

function parseModifierType(typeSlug: string, value: number): { type: ModifierType, value: number } {
    const [type, bonusOrPenalty] = typeSlug.split('-');
    if (bonusOrPenalty === 'penalty') {
        return {type: type as ModifierType, value: -Math.abs(value)};
    } else {
        return {type: type as ModifierType, value: Math.abs(value)};
    }
}

function parseOptionalSelect<T extends string>($html: HTMLElement, name: string): T[] | undefined {
    const value = parseSelect($html, name);
    if (value === '-') {
        return undefined;
    } else {
        return [value as T];
    }
}

export function addEffectDialog(
    activities: string[],
    onOk: (modifier: Modifier) => void,
): void {
    new Dialog({
        title: 'Add Effect',
        content: tpl(activities),
        buttons: {
            add: {
                icon: '<i class="fa-solid fa-plus"></i>',
                label: 'Add',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const turns = parseNumberInput($html, 'turns');
                    const {type, value} = parseModifierType(
                        parseSelect($html, 'type'),
                        parseNumberInput($html, 'value'),
                    );
                    const phases = parseOptionalSelect($html, 'phase') as KingdomPhase[] | undefined;
                    const activities = parseOptionalSelect($html, 'activity');
                    const skills = parseOptionalSelect($html, 'skill') as Skill[] | undefined;
                    const abilities = parseOptionalSelect($html, 'ability') as Ability[] | undefined;
                    onOk({
                        name: parseTextInput($html, 'name'),
                        type,
                        value,
                        enabled: parseCheckbox($html, 'enabled'),
                        consumeId: parseCheckbox($html, 'consumable') ? crypto.randomUUID() : undefined,
                        phases,
                        activities,
                        skills,
                        abilities,
                        turns: turns === 0 ? undefined : turns,
                    });
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 400,
    }).render(true);
}
