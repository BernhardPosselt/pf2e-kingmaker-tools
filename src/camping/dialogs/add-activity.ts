import {parseCheckbox, parseNumberInput, parseSelect, parseTextArea, parseTextInput, unslugify} from '../../utils';
import {ActivityOutcome, CampingActivityData, CampingActivityName} from '../activities';
import {getEffectByUuid} from '../actor';
import {DcType, Proficiency} from '../data';

export interface AddActivityOptions {
    onSubmit: (activity: CampingActivityData) => Promise<void>;
    activities: CampingActivityData[];
}

export function addActivityDialog({onSubmit, activities}: AddActivityOptions): void {
    new Dialog({
        title: 'Add Camping Activity',
        content: `
        <div>
        <p>Hint: UUIDs can be found in the item's <b>Rules</b> tab</p>
        <form class="simple-dialog-form">
            <div>
                <label for="km-name">Name</label>
                <input type="text" name="name" id="km-name" placeholder="Unknown Activity">
            </div>
            <div>
                <label for="km-journal">Journal UUID</label>
                <input type="text" name="journal" id="km-journal">
            </div>
            <div>
                <label for="km-skills">Skills (<b>any</b> or skill name)</label>
                <input id="km-skills" type="text" placeholder="Survival,Perception" name="skills">
            </div>
            <div>
                <label for="km-dc">DC (number, <b>zone</b>, <b>actorLevel</b> or blank)</label>
                <input id="km-dc" type="text" placeholder="" name="dc">
            </div>
            <div>
                <label for="km-skill-requirement">Skill Requirement</label>
                <input id="km-skill-requirement" type="text" placeholder="Survival" name="skill-requirement">
            </div>
            <div>
                <label for="km-skill-proficiency">Skill Proficiency Requirement</label>
                <select name="skill-proficiency" id="km-skill-proficiency">
                    <option value="none">-</option>
                    <option value="trained">Trained</option>
                    <option value="expert">Expert</option>
                    <option value="master">Master</option>
                    <option value="legendary">Legendary</option>
                </select>
            </div>
            <div>
                <label for="km-day-encounter-dc">Day Encounter DC Modifier</label>
                <input type="number" name="day-encounter-dc" id="km-day-encounter-dc" placeholder="0">
            </div>
            <div>
                <label for="km-night-encounter-dc">Night Encounter DC Modifier</label>
                <input type="number" name="night-encounter-dc" id="km-night-encounter-dc" placeholder="0">
            </div>
            <div>
                <label for="km-secret">Secret Check</label>
                <input type="checkbox" name="secret" id="km-secret">
            </div>
            ${['critical-success', 'success', 'failure', 'critical-failure'].map(d => {
            return `
                  <div>
                    <label for="km-${d}-effect">${unslugify(d)}: Effect UUID</label>
                    <input type="text" name="${d}-effect" id="km-${d}-effect">
                  </div>
                  <div>
                    <label for="km-${d}-effect-target">${unslugify(d)}: Effect Target</label>
                    <select name="${d}-effect-target" id="km-${d}-effect-target">
                        <option value="all">All</option>
                        <option value="self">Self</option>
                    </select>
                  </div>
                  <div>
                    <label for="km-${d}-message">${unslugify(d)}: Message</label>
                    <textarea name="${d}-message" id="km-${d}-message" placeholder=""></textarea>
                  </div>
                  <div>
                    <label for="km-${d}-day-modifier">${unslugify(d)}: Day Encounter DC Modifier</label>
                    <input type="number" name="${d}-day-modifier" id="km-${d}-day-modifier" placeholder="0">
                  </div>
                  <div>
                    <label for="km-${d}-night-modifier">${unslugify(d)}: Night Encounter DC Modifier</label>
                    <input type="number" name="${d}-night-modifier" id="km-${d}-night-modifier" placeholder="0">
                  </div>
                  <div>
                    <label for="km-${d}-roll-encounter">${unslugify(d)}: Random Encounter</label>
                    <input type="checkbox" name="${d}-roll-encounter" id="km-${d}-roll-encounter">
                  </div>
                  `;
        }).join('')}
            <div>
                <label for="km-effect">Effect UUID</label>
                <input type="text" name="effect" id="km-effect">
            </div>
            <div>
                <label for="km-effect-target">Effect Target</label>
                <select name="effect-target" id="km-effect-target">
                    <option value="all">All</option>
                    <option value="self">Self</option>
                </select>
            </div>
        </form>
        </div>
        `,
        buttons: {
            add: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Add',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const journalUuid = parseTextInput($html, 'journal');
                    const effectUuid = parseTextInput($html, 'effect');
                    const journal = await fromUuid(journalUuid);
                    const effectItem = await getEffectByUuid(effectUuid);
                    const name = (parseTextInput($html, 'name') || 'Unknown Activity') as CampingActivityName;
                    if (journalUuid && journal === null) {
                        ui.notifications?.error(`Can not find journal with uuid ${journalUuid}`);
                    } else if (effectUuid && effectItem === null) {
                        ui.notifications?.error(`Can not find effect item with uuid ${effectUuid}`);
                    } else if (activities.find(r => r.name === name)) {
                        ui.notifications?.error(`Activity with name ${name} exists already`);
                    } else {
                        const skillRequirements = parseSkillList(parseTextInput($html, 'skill-requirement'));
                        const skillProficiency = parseProficiency(parseSelect($html, 'skill-proficiency'));
                        await onSubmit({
                            name,
                            journalUuid: journalUuid,
                            skills: parseSkills(parseTextInput($html, 'skills')),
                            skillRequirements: skillProficiency ? skillRequirements.map(s => {
                                return {skill: s, proficiency: skillProficiency};
                            }) : [],
                            modifyRandomEncounterDc: {
                                day: parseNumberInput($html, 'day-encounter-dc') || 0,
                                night: parseNumberInput($html, 'night-encounter-dc') || 0,
                            },
                            isSecret: parseCheckbox($html, 'secret'),
                            isLocked: false,
                            effectUuids: effectUuid ? [{
                                uuid: effectUuid,
                                targetAll: parseSelect($html, 'effect-target') === 'all',
                            }] : undefined,
                            dc: parseDC(parseTextInput($html, 'dc')),
                            isHomebrew: true,
                            criticalSuccess: await parseEffects($html, 'critical-success'),
                            success: await parseEffects($html, 'success'),
                            failure: await parseEffects($html, 'failure'),
                            criticalFailure: await parseEffects($html, 'critical-failure'),
                        });
                    }
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 510,
    }).render(true);
}

function parseSkillList(value: string): string[] {
    if (value.trim()) {
        return value.split(',')
            .map(s => s.toLowerCase()
                .replace('lore', '')
                .trim()
                .replace(' ', '-'));
    }
    return [];
}

function parseSkills(value: string): string[] | 'any' {
    if (value === 'any') {
        return 'any';
    } else {
        return parseSkillList(value);
    }
}


function parseProficiency(value: string): Proficiency | undefined {
    if (value === 'trained') {
        return 'trained';
    } else if (value === 'expert') {
        return 'expert';
    } else if (value === 'master') {
        return 'master';
    } else if (value === 'legendary') {
        return 'legendary';
    }
    return undefined;
}

function parseDC(value: string): DcType | undefined {
    if (value !== null && value !== undefined) {
        if (value === 'zone') {
            return 'zone';
        } else if (value === 'actorLevel') {
            return 'actorLevel';
        } else {
            const number = parseInt(value, 10);
            if (!isNaN(number)) {
                return number;
            }
        }
    }
    return undefined;
}

async function parseEffects($html: HTMLElement, name: 'critical-success' | 'success' | 'failure' | 'critical-failure'): Promise<ActivityOutcome | undefined> {
    const effect = parseTextInput($html, `${name}-effect`) || undefined;
    if (effect) {
        const item = await getEffectByUuid(effect);
        if (item === null) {
            ui.notifications?.error(`Can not find effect item with uuid ${effect}`);
        } else {
            return {
                message: parseTextArea($html, `${name}-message`) || '',
                checkRandomEncounter: parseCheckbox($html, `${name}-roll-encounter`),
                modifyRandomEncounterDc: {
                    day: parseNumberInput($html, `${name}-day-modifier`) || 0,
                    night: parseNumberInput($html, `${name}-night-modifier`) || 0,
                },
                effectUuids: [{
                    uuid: effect,
                    targetAll: parseSelect($html, `${name}-effect-target`) === 'all',
                }],
            };
        }
    }
    return undefined;
}
