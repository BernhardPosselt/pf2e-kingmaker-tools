import {DegreeOfSuccess, degreeToProperty, determineDegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {Skill} from './data/skills';
import {postDegreeOfSuccessMessage, unslugify} from '../utils';
import {ActivityResults, getKingdomActivitiesById, KingdomActivity} from './data/activityData';
import {Modifier, modifierToLabel} from './modifiers';
import {getKingdom, saveKingdom} from './storage';
import {gainFame} from './kingdom-utils';

export interface RollMeta {
    formula: string;
    activity: string | undefined;
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
        activity: (meta.dataset.activity ?? undefined),
        skill: meta.dataset.skill as Skill,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
        formula: meta.dataset.formula as string,
        modifier: parseInt(meta.dataset.modifier ?? '0', 10),
    };
}

export interface ActivityResultMeta {
    activity: string;
    degree: StringDegreeOfSuccess;
}

export function parseUpgradeMeta(el: HTMLElement): ActivityResultMeta {
    const meta = el.querySelector('.km-upgrade-result') as HTMLElement;
    return {
        activity: meta.dataset.activity!,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
    };
}


export async function reRoll(actor: Actor, el: HTMLElement, type: 'fame' | 're-roll' | 'keep-higher' | 'keep-lower'): Promise<void> {
    const {total, formula, activity, skill, dc, modifier} = parseMeta(el);
    const label = activity ? unslugify(activity) : unslugify(skill);
    let reRollFormula = formula;
    const kingdom = getKingdom(actor);
    if (type === 'fame') {
        // deduct points from sheet
        await saveKingdom(actor, gainFame(kingdom, -1));
    } else if (type === 'keep-higher') {
        reRollFormula = `{${formula},${total}}kh`;
    } else if (type === 'keep-lower') {
        reRollFormula = `{${formula},${total}}kl`;
    }
    await rollCheck({
        formula: reRollFormula,
        label,
        activity: activity ? getKingdomActivitiesById(kingdom.homebrewActivities)[activity] : undefined,
        dc,
        skill,
        modifier,
        actor,
    });
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

export async function changeDegree(actor: Actor, el: HTMLElement, type: 'upgrade' | 'downgrade'): Promise<void> {
    const {activity, degree} = parseUpgradeMeta(el);
    const kingdom = getKingdom(actor);
    const kingdomActivity = getKingdomActivitiesById(kingdom.homebrewActivities)[activity];
    const newDegree = type === 'upgrade' ? upgradeDegree(degree) : downgradeDegree(degree);
    await postComplexDegreeOfSuccess(actor, getDegreeFromKey(newDegree), kingdomActivity);
}

export async function rollCheck(
    {
        formula,
        label,
        activity,
        dc,
        skill,
        modifier,
        actor,
    }: {
        formula: string,
        label: string,
        activity: KingdomActivity | undefined,
        dc: number,
        skill: Skill,
        modifier: number,
        actor: Actor,
    },
): Promise<DegreeOfSuccess> {
    const roll = await new Roll(formula).roll();
    const total = roll.total;
    const dieNumber = total - modifier;
    const degreeOfSuccess = determineDegreeOfSuccess(dieNumber, total, dc);
    const meta = `
        <div class="km-roll-meta" hidden 
            data-formula="${formula}" 
            ${activity === undefined ? '' : `data-activity="${activity.id}"`}
            data-degree="${degreeToProperty(degreeOfSuccess)}"
            data-skill="${skill}"
            data-dc="${dc}"
            data-total="${total}"
            data-modifier="${modifier}"
        ></div>`;
    await roll.toMessage({flavor: `Rolling Skill Check: ${label}, DC ${dc}${meta}`});
    await postDegreeOfSuccess(actor, activity, degreeOfSuccess);
    return degreeOfSuccess;
}

async function postDegreeOfSuccess(actor: Actor, activity: KingdomActivity | undefined, degreeOfSuccess: DegreeOfSuccess): Promise<void> {
    if (activity) {
        await postComplexDegreeOfSuccess(actor, degreeOfSuccess, activity);
    } else {
        await postSimpleDegreeOfSuccess(degreeOfSuccess);
    }
}

async function postSimpleDegreeOfSuccess(degreeOfSuccess: DegreeOfSuccess): Promise<void> {
    await postDegreeOfSuccessMessage(degreeOfSuccess, {
        critSuccess: `<b>Critical Success</b>${buildChatButtons([], 'criticalSuccess')}`,
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

function buildChatButtons(modifiers: Modifier[], resultKey: keyof ActivityResults, activity?: string): string {
    if (modifiers.length > 0 || resultKey === 'criticalSuccess') {
        return `
        <div class="km-chat-buttons">
            ${resultKey === 'criticalSuccess' ? '<button type="button" class="km-gain-fame-button">Gain 1 Fame</button>' : ''}
            ${modifiers.map((modifier, index) => {
            const label = modifierToLabel(modifier);
            return `<button class="km-apply-modifier-effect" 
                        data-activity="${activity}" 
                        data-degree="${resultKey}" 
                        data-index="${index}">Apply Effect: ${label}</button>`;
        }).join('')}    
        </div>`;
    } else {
        return '';
    }
}

async function postComplexDegreeOfSuccess(actor: Actor, degreeOfSuccess: DegreeOfSuccess, activity: KingdomActivity): Promise<void> {
    const resultKey = getResultKey(degreeOfSuccess);
    const results = activity[resultKey];
    if (results) {
        const kingdom = getKingdom(actor);
        const modifiers = results.modifiers;
        const message = results.msg;
        const buttons = modifiers === undefined
            ? buildChatButtons([], resultKey)
            : buildChatButtons(modifiers(kingdom), resultKey, activity.id);
        // div allows to upgrade/downgrade on right click
        const upgrade = `<div class="km-upgrade-result" data-activity="${activity.id}" data-degree="${resultKey}" hidden></div>`;
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

export async function addOngoingEvent(actor: Actor, uuid: string, label: string): Promise<void> {
    const kingdom = getKingdom(actor);
    const name = `@UUID[${uuid}]{${label}}`;
    await saveKingdom(actor, {
        ongoingEvents: [...kingdom.ongoingEvents, {name}],
    });
}
