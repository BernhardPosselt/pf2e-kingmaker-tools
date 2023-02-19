import {Activity} from './data/activities';
import {DegreeOfSuccess, degreeToProperty, determineDegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {Skill} from './data/skills';
import {capitalize, postDegreeOfSuccessMessage} from '../utils';
import {activityData, ActivityResults} from './data/activityData';
import {Modifier, modifierToLabel} from './modifiers';
import {getKingdom, getKingdomSheetActorOrThrow, saveKingdom} from './storage';

export interface RollMeta {
    formula: string;
    activity: Activity | undefined;
    degree: StringDegreeOfSuccess;
    skill: Skill;
    dc: number;
    total: number;
    modifier: number;
}

export function parseMeta(el: HTMLElement): RollMeta {
    const meta = el.querySelector('.km-roll-meta') as HTMLElement;
    return {
        total: parseInt(meta.dataset.total ?? '0', 10),
        dc: parseInt(meta.dataset.dc ?? '0', 10),
        activity: (meta.dataset.activity ?? undefined) as Activity | undefined,
        skill: meta.dataset.skill as Skill,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
        formula: meta.dataset.formula as string,
        modifier: parseInt(meta.dataset.modifier ?? '0', 10),
    };
}

export interface ActivityResultMeta {
    activity: Activity;
    degree: StringDegreeOfSuccess;
}

export function parseUpgradeMeta(el: HTMLElement): ActivityResultMeta {
    const meta = el.querySelector('.km-upgrade-result') as HTMLElement;
    return {
        activity: meta.dataset.activity as Activity,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
    };
}


export async function reRoll(el: HTMLElement, type: 'fame' | 're-roll' | 'keep-higher' | 'keep-lower'): Promise<void> {
    const {total, formula, activity, skill, dc, modifier} = parseMeta(el);
    const label = activity ? capitalize(activity) : capitalize(skill);
    let reRollFormula = formula;
    if (type === 'fame') {
        // deduct points from sheet
        const actor = getKingdomSheetActorOrThrow();
        const kingdom = getKingdom(actor);
        await saveKingdom(actor, {
            fame: kingdom.fame - 1,
        });
    } else if (type === 'keep-higher') {
        reRollFormula = `{${formula},${total}}kh`;
    } else if (type === 'keep-lower') {
        reRollFormula = `{${formula},${total}}kl`;
    }
    await rollCheck(reRollFormula, label, activity, dc, skill, modifier);
}

function upgradeDegree(degree: StringDegreeOfSuccess): StringDegreeOfSuccess {
    if (degree === 'success') {
        return 'criticalSuccess';
    } else if (degree === 'failure') {
        return 'success';
    } else if (degree === 'criticalFailure') {
        return 'failure';
    } else {
        return degree;
    }
}

function downgradeDegree(degree: StringDegreeOfSuccess): StringDegreeOfSuccess {
    if (degree === 'criticalSuccess') {
        return 'success';
    } else if (degree === 'success') {
        return 'failure';
    } else if (degree === 'failure') {
        return 'criticalFailure';
    } else {
        return degree;
    }
}

export async function upgradeDowngrade(el: HTMLElement, type: 'upgrade' | 'downgrade'): Promise<void> {
    const {activity, degree} = parseUpgradeMeta(el);
    const newDegree = type === 'upgrade' ? upgradeDegree(degree) : downgradeDegree(degree);
    await postComplexDegreeOfSuccess(getDegreeFromKey(newDegree), activity);
}

export async function rollCheck(
    formula: string,
    type: string,
    activity: Activity | undefined,
    dc: number,
    skill: Skill,
    modifier: number,
): Promise<void> {
    const roll = await new Roll(formula).roll();
    const total = roll.total;
    const dieNumber = total - modifier;
    const degreeOfSuccess = determineDegreeOfSuccess(dieNumber, total, dc);
    const meta = `
        <div class="km-roll-meta" hidden 
            data-formula="${formula}" 
            ${activity === undefined ? '' : `data-activity="${activity}"`}
            data-degree="${degreeToProperty(degreeOfSuccess)}"
            data-skill="${skill}"
            data-dc="${dc}"
            data-total="${total}"
            data-modifier="${modifier}"
        ></div>`;
    await roll.toMessage({flavor: `Rolling Skill Check: ${type}, DC ${dc}${meta}`});
    await postDegreeOfSuccess(activity, degreeOfSuccess);
}

async function postDegreeOfSuccess(activity: Activity | undefined, degreeOfSuccess: DegreeOfSuccess): Promise<void> {
    if (activity) {
        await postComplexDegreeOfSuccess(degreeOfSuccess, activity);
    } else {
        await postSimpleDegreeOfSuccess(degreeOfSuccess);
    }
}

async function postSimpleDegreeOfSuccess(degreeOfSuccess: DegreeOfSuccess): Promise<void> {
    await postDegreeOfSuccessMessage(degreeOfSuccess, {
        critSuccess: '<b>Critical Success</b>',
        success: '<b>Success</b>',
        failure: '<b>Failure</b>',
        critFailure: '<b>Critical Failure</b>',
    });
}

function getResultKey(degreeOfSuccess: DegreeOfSuccess): keyof ActivityResults {
    if (degreeOfSuccess === DegreeOfSuccess.CRITICAL_SUCCESS) {
        return 'criticalSuccess';
    } else if (degreeOfSuccess === DegreeOfSuccess.SUCCESS) {
        return 'success';
    } else if (degreeOfSuccess === DegreeOfSuccess.FAILURE) {
        return 'failure';
    } else {
        return 'criticalFailure';
    }
}

function getDegreeFromKey(degreeOfSuccess: keyof ActivityResults): DegreeOfSuccess {
    if (degreeOfSuccess === 'criticalSuccess') {
        return DegreeOfSuccess.CRITICAL_SUCCESS;
    } else if (degreeOfSuccess === 'success') {
        return DegreeOfSuccess.SUCCESS;
    } else if (degreeOfSuccess === 'failure') {
        return DegreeOfSuccess.FAILURE;
    } else {
        return DegreeOfSuccess.CRITICAL_FAILURE;
    }
}

function buildModifierButtons(modifiers: Modifier[], activity: Activity, resultKey: keyof ActivityResults): string {
    return `
        <div class="km-chat-buttons">
            ${modifiers.map((modifier, index) => {
        const label = modifierToLabel(modifier);
        return `<button class="km-apply-modifier-effect" 
                        data-activity="${activity}" 
                        data-degree="${resultKey}" 
                        data-index="${index}">Apply Effect: ${label}</button>`;
    })}    
        </div>`;
}

async function postComplexDegreeOfSuccess(degreeOfSuccess: DegreeOfSuccess, activity: Activity): Promise<void> {
    const resultKey = getResultKey(degreeOfSuccess);
    const results = activityData[activity][resultKey];
    if (results) {
        const actor = getKingdomSheetActorOrThrow();
        const kingdom = getKingdom(actor);
        const modifiers = results.modifiers;
        const message = results.msg;
        const buttons = modifiers === undefined ? '' : buildModifierButtons(modifiers(kingdom), activity, resultKey);
        // div allows to upgrade/downgrade on right click
        const upgrade = `<div class="km-upgrade-result" data-activity="${activity}" data-degree="${resultKey}" hidden></div>`;
        const msg = message + buttons + upgrade;
        await postDegreeOfSuccessMessage(degreeOfSuccess, {
            critSuccess: `<b>Critical Success</b>: ${msg}`,
            success: `<b>Success</b>: ${msg}`,
            failure: `<b>Failure</b>: ${msg}`,
            critFailure: `<b>Critical Failure</b>: ${msg}`,
        });
    } else {
        await postSimpleDegreeOfSuccess(degreeOfSuccess);
    }
}
