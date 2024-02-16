import {DegreeOfSuccess, degreeToProperty, determineDegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {Skill} from './data/skills';
import {decodeJson, isNotBlank, postDegreeOfSuccessMessage, unslugify} from '../utils';
import {ActivityResults, getKingdomActivitiesById, KingdomActivity} from './data/activityData';
import {Modifier, modifierToLabel} from './modifiers';
import {getKingdom, saveKingdom} from './storage';
import {gainFame} from './kingdom-utils';
import {hasFeat} from './data/kingdom';
import {ModifierBreakdown, ModifierBreakdowns} from './dialogs/check-dialog';

export interface RollMeta {
    formula: string;
    activity: string | undefined;
    degree: StringDegreeOfSuccess;
    skill: Skill;
    dc: number;
    total: number;
    modifier: number;
    rollOptions: string[];
    creativeSolutionModifier: number;
    supernaturalSolutionModifier: number;
    modifierBreakdown?: string;
    rollType: RollType;
}

export function parseMeta(el: HTMLElement): RollMeta {
    const meta = el.querySelector('.km-roll-meta') as HTMLElement;
    const rollOptions = meta.dataset.rollOptions;
    return {
        total: parseInt(meta.dataset.total ?? '0', 10),
        dc: parseInt(meta.dataset.dc ?? '0', 10),
        activity: (meta.dataset.activity ?? undefined),
        skill: meta.dataset.skill as Skill,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
        formula: meta.dataset.formula as string,
        modifier: parseInt(meta.dataset.modifier ?? '0', 10),
        rollType: meta.dataset.rollType as RollType,
        rollOptions: isNotBlank(rollOptions) ? JSON.parse(atob(rollOptions)) : [],
        creativeSolutionModifier: parseInt(meta.dataset.creativeSolutionModifier ?? '0', 10),
        supernaturalSolutionModifier: parseInt(meta.dataset.supernaturalSolutionModifier ?? '0', 10),
        modifierBreakdown: isNotBlank(meta.dataset.modifierBreakdown) ? meta.dataset.modifierBreakdown : undefined,
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

export function cooperativeLeadership(
    hasCooperativeLeadership: boolean,
    kingdomLevel: number,
    skillRank: number,
    rollOptions: string[],
): (degree: DegreeOfSuccess) => DegreeOfSuccess {
    return (degree) => {
        if (hasCooperativeLeadership && rollOptions.includes('focused-attention')) {
            if (kingdomLevel >= 11) {
                if (degree === DegreeOfSuccess.CRITICAL_FAILURE) {
                    degree = DegreeOfSuccess.FAILURE;
                } else if (degree === DegreeOfSuccess.FAILURE && skillRank >= 2) {
                    degree = DegreeOfSuccess.SUCCESS;
                }
            }
        }
        return degree;
    };
}

export async function reRoll(
    actor: Actor,
    el: HTMLElement,
    type: 'fame' | 're-roll' | 'keep-higher' | 'keep-lower' | 'creative-solution' | 'supernatural-solution',
): Promise<void> {
    const {
        total,
        formula,
        activity,
        skill,
        dc,
        modifier,
        rollOptions,
        creativeSolutionModifier,
        supernaturalSolutionModifier,
        modifierBreakdown,
        rollType,
    } = parseMeta(el);
    const label = activity ? unslugify(activity) : unslugify(skill);
    let reRollFormula = formula;
    const kingdom = getKingdom(actor);
    let overrideRollType: RollType | undefined;
    if (type === 'fame') {
        // deduct points from sheet
        await saveKingdom(actor, gainFame(kingdom, -1));
    } else if (type === 'creative-solution') {
        reRollFormula = `1d20+${creativeSolutionModifier}`;
        overrideRollType = 'creative-solution';
        await saveKingdom(actor, {
            creativeSolutions: kingdom.creativeSolutions - 1,
        });
    } else if (type === 'supernatural-solution') {
        overrideRollType = 'supernatural-solution';
        reRollFormula = `1d20+${supernaturalSolutionModifier}`;
        await saveKingdom(actor, {
            supernaturalSolutions: kingdom.supernaturalSolutions - 1,
        });
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
        modifierBreakdown,
        actor,
        adjustDegreeOfSuccess: cooperativeLeadership(
            hasFeat(kingdom, 'Cooperative Leadership'),
            kingdom.level,
            kingdom.skillRanks[skill],
            rollOptions,
        ),
        rollOptions,
        rollType: overrideRollType ?? rollType,
        supernaturalSolutionModifier,
        creativeSolutionModifier,
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
    const degreeEnum = getDegreeFromKey(newDegree);
    await postComplexDegreeOfSuccess(actor, kingdomActivity, degreeEnum, degreeEnum);
}

interface RollCheckOptions {
    formula: string,
    label: string,
    activity: KingdomActivity | undefined,
    dc: number,
    skill: Skill,
    modifier: number,
    actor: Actor,
    adjustDegreeOfSuccess?: (degree: DegreeOfSuccess) => DegreeOfSuccess;
    rollOptions: string[];
    creativeSolutionModifier: number;
    supernaturalSolutionModifier: number;
    modifierBreakdown?: string;
    rollType: RollType;
}

type RollType = 'creative-solution' | 'supernatural-solution' | 'selected';

function getModifiersByType(parsedBreakdown: ModifierBreakdowns, type: RollType): ModifierBreakdown[] {
    if (type === 'supernatural-solution') {
        return parsedBreakdown.supernaturalSolution;
    } else if (type === 'creative-solution') {
        return parsedBreakdown.creativeSolution;
    } else {
        return parsedBreakdown.selected;
    }
}

function createModifierPills(
    modifierBreakdown: string | undefined,
    type: RollType,
): string {
    if (isNotBlank(modifierBreakdown)) {
        const parsedBreakdown = decodeJson(modifierBreakdown) as ModifierBreakdowns;
        const modifiers = getModifiersByType(parsedBreakdown, type);
        console.log(parsedBreakdown, modifiers);
        const mods = modifiers.map(m => {
            const value = m.value;
            return `<span class="km-modifier-pill">${unslugify(m.name)} ${value > 0 ? `+${value}` : value}</span>`;
        }).join('');
        return `<div class="km-modifier-breakdown">${mods}</div>`;
    }
    return '';
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
        adjustDegreeOfSuccess = (degree): DegreeOfSuccess => degree,
        rollOptions,
        modifierBreakdown,
        supernaturalSolutionModifier,
        creativeSolutionModifier,
        rollType,
    }: RollCheckOptions,
): Promise<DegreeOfSuccess> {
    const roll = await new Roll(formula).roll();
    const total = roll.total;
    const dieNumber = total - modifier;
    const previousDegree = determineDegreeOfSuccess(dieNumber, total, dc);
    const degreeOfSuccess = adjustDegreeOfSuccess(previousDegree);
    const meta = `
        <div class="km-roll-meta" hidden 
            data-formula="${formula}" 
            ${activity === undefined ? '' : `data-activity="${activity.id}"`}
            data-degree="${degreeToProperty(degreeOfSuccess)}"
            data-skill="${skill}"
            data-roll-options="${rollOptions ? btoa(JSON.stringify(rollOptions)) : ''}"
            data-dc="${dc}"
            data-total="${total}"
            data-modifier-breakdown="${modifierBreakdown ?? ''}"
            data-modifier="${modifier}"
            data-roll-type="${rollType}"
            data-creative-solution-modifier="${creativeSolutionModifier}"
            data-supernatural-solution-modifier="${supernaturalSolutionModifier}"
        ></div>`;
    const modifierPills = createModifierPills(modifierBreakdown, rollType);
    await roll.toMessage({flavor: `<span class="km-skill-check-header">Skill Check: ${label}, DC ${dc}</span>${meta}<hr>${modifierPills}`});
    await postDegreeOfSuccess(
        actor,
        activity,
        previousDegree,
        degreeOfSuccess === previousDegree ? undefined : degreeOfSuccess,
    );
    return degreeOfSuccess;
}

async function postDegreeOfSuccess(
    actor: Actor,
    activity: KingdomActivity | undefined,
    degreeOfSuccess: DegreeOfSuccess,
    upgradedDegreeOfSuccess?: DegreeOfSuccess,
): Promise<void> {
    if (activity) {
        await postComplexDegreeOfSuccess(actor, activity, degreeOfSuccess, upgradedDegreeOfSuccess);
    } else {
        await postSimpleDegreeOfSuccess(degreeOfSuccess, upgradedDegreeOfSuccess);
    }
}

async function postSimpleDegreeOfSuccess(degreeOfSuccess: DegreeOfSuccess, upgradedDegreeOfSuccess?: DegreeOfSuccess): Promise<void> {
    await postDegreeOfSuccessMessage({
        degreeOfSuccess,
        upgradedDegreeOfSuccess,
        messageConfig: {
            critSuccess: `${buildChatButtons([], 'criticalSuccess')}`,
        },
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

async function postComplexDegreeOfSuccess(
    actor: Actor,
    activity: KingdomActivity,
    degreeOfSuccess: DegreeOfSuccess,
    upgradedDegreeOfSuccess?: DegreeOfSuccess,
): Promise<void> {
    const resultKey = getResultKey(upgradedDegreeOfSuccess ?? degreeOfSuccess);
    const results = activity[resultKey];
    if (results) {
        const kingdom = getKingdom(actor);
        const modifiers = results.modifiers;
        const message = results.msg;
        const buttons = modifiers === undefined
            ? buildChatButtons([], resultKey)
            : buildChatButtons(modifiers(kingdom), resultKey, activity.id);
        // div allows to upgrade/downgrade on right click
        const upgrade = `<div class="km-upgrade-result" data-activity="${activity.id}" data-degree="${resultKey}"></div>`;
        const msg = message;
        const tail = buttons + upgrade;
        const description = `<h3>${activity.title}</h3>${activity.description.trimEnd()}<hr>`;
        await postDegreeOfSuccessMessage({
            degreeOfSuccess,
            upgradedDegreeOfSuccess,
            beforeHeader: description,
            messageConfig: {
                critSuccess: `${msg}${tail}`,
                success: `${msg}${tail}`,
                failure: `${msg}${tail}`,
                critFailure: `${msg}${tail}`,
            },
        });
    } else {
        await postSimpleDegreeOfSuccess(degreeOfSuccess, upgradedDegreeOfSuccess);
    }
}

export async function addOngoingEvent(actor: Actor, uuid: string, label: string): Promise<void> {
    const kingdom = getKingdom(actor);
    const name = `@UUID[${uuid}]{${label}}`;
    await saveKingdom(actor, {
        ongoingEvents: [...kingdom.ongoingEvents, {name}],
    });
}
