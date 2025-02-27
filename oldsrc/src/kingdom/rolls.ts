import {DegreeOfSuccess, degreeToProperty, determineDegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {Skill} from './data/skills';
import {decodeJson, encodeJson, isNotBlank, postChatMessage, postDegreeOfSuccessMessage, unslugify} from '../utils';
import {ActivityResults, ChatModifier, getKingdomActivitiesById, KingdomActivity} from './data/activityData';
import {filterPredicates, modifierToLabel} from './modifiers';
import {getKingdom, saveKingdom} from './storage';
import {gainFame} from './kingdom-utils';
import {getFlags, getUpgradeResults, Kingdom} from './data/kingdom';
import {AdditionalChatMessages, ModifierBreakdown, ModifierBreakdowns} from './dialogs/check-dialog';
import {UpgradeResult} from "./data/feats";

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
    additionalChatMessages: AdditionalChatMessages;
    modifierBreakdown?: string;
    rollType: RollType;
    rollMode: RollMode;
}

export function parseMeta(el: HTMLElement): RollMeta {
    const meta = el.querySelector('.km-roll-meta') as HTMLElement;
    const rollOptions = meta.dataset.rollOptions;
    const additionalChatMessages = meta.dataset.additionalChatMessages;
    return {
        total: parseInt(meta.dataset.total ?? '0', 10),
        dc: parseInt(meta.dataset.dc ?? '0', 10),
        activity: (meta.dataset.activity ?? undefined),
        skill: meta.dataset.skill as Skill,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
        formula: meta.dataset.formula as string,
        modifier: parseInt(meta.dataset.modifier ?? '0', 10),
        rollType: meta.dataset.rollType as RollType,
        rollOptions: isNotBlank(rollOptions) ? decodeJson(rollOptions) as string[] : [],
        additionalChatMessages: isNotBlank(additionalChatMessages) ? decodeJson(additionalChatMessages) as AdditionalChatMessages : [],
        creativeSolutionModifier: parseInt(meta.dataset.creativeSolutionModifier ?? '0', 10),
        supernaturalSolutionModifier: parseInt(meta.dataset.supernaturalSolutionModifier ?? '0', 10),
        modifierBreakdown: isNotBlank(meta.dataset.modifierBreakdown) ? meta.dataset.modifierBreakdown : undefined,
        rollMode: meta.dataset.rollMode as RollMode,
    };
}

export interface ActivityResultMeta {
    activity: string;
    rollMode: RollMode;
    degree: StringDegreeOfSuccess;
    additionalChatMessages: AdditionalChatMessages;
    skill: Skill;
    rollOptions: string[];
}

export function parseUpgradeMeta(el: HTMLElement): ActivityResultMeta {
    const meta = el.querySelector('.km-upgrade-result') as HTMLElement;
    const additionalChatMessages = meta.dataset.additionalChatMessages;
    const rollOptions = meta.dataset.rollOptions;
    return {
        activity: meta.dataset.activity!,
        rollMode: meta.dataset.rollMode as RollMode,
        degree: meta.dataset.degree as StringDegreeOfSuccess,
        skill: (meta.dataset.skill ?? 'athletics') as Skill,
        rollOptions: rollOptions ? decodeJson(rollOptions) as string[] : [],
        additionalChatMessages: isNotBlank(additionalChatMessages) ? decodeJson(additionalChatMessages) as AdditionalChatMessages : [],
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
        rollMode,
        additionalChatMessages,
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

    const upgrades = getUpgradeResults(kingdom);
    const flags = getFlags(kingdom);
    await rollCheck({
        formula: reRollFormula,
        label,
        activity: activity ? getKingdomActivitiesById(kingdom.homebrewActivities)[activity] : undefined,
        dc,
        skill,
        modifier,
        modifierBreakdown,
        actor,
        upgrades,
        rollOptions,
        flags,
        kingdom,
        rollType: overrideRollType ?? rollType,
        supernaturalSolutionModifier,
        creativeSolutionModifier,
        rollMode,
        additionalChatMessages,
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
    const {activity, degree, rollMode, rollOptions, skill, additionalChatMessages} = parseUpgradeMeta(el);
    const kingdom = getKingdom(actor);
    const kingdomActivity = getKingdomActivitiesById(kingdom.homebrewActivities)[activity];
    const newDegree = type === 'upgrade' ? upgradeDegree(degree) : downgradeDegree(degree);
    const degreeEnum = getDegreeFromKey(newDegree);
    const oldDegree = getDegreeFromKey(degree);
    await postComplexDegreeOfSuccess(actor, kingdomActivity, rollMode, additionalChatMessages, oldDegree, rollOptions, skill, degreeEnum);
    await postAdditionalChatMessages(additionalChatMessages, degreeEnum, rollMode);
}

interface RollCheckOptions {
    formula: string,
    label: string,
    activity: KingdomActivity | undefined,
    dc: number,
    skill: Skill,
    modifier: number,
    actor: Actor,
    rollOptions: string[];
    creativeSolutionModifier: number;
    supernaturalSolutionModifier: number;
    modifierBreakdown?: string;
    rollType: RollType;
    rollMode: RollMode;
    additionalChatMessages: AdditionalChatMessages;
    upgrades: UpgradeResult[];
    kingdom: Kingdom;
    flags: string[];
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
            return `<span class="km-modifier-pill">${unslugify(m.name)} ${value >= 0 ? `+${value}` : value}</span>`;
        }).join('');
        return `<div class="km-modifier-breakdown">${mods}</div>`;
    }
    return '';
}

async function postAdditionalChatMessages(
    additionalChatMessages: Partial<Record<DegreeOfSuccess, string>>[],
    degreeOfSuccess: DegreeOfSuccess,
    rollMode: RollMode,
): Promise<void> {
    const messages = additionalChatMessages
        .map(message => message[degreeOfSuccess])
        .filter(message => isNotBlank(message)) as string[];
    await Promise.all(messages.map(async (message) => {
        await postChatMessage(message.trimEnd(), rollMode);
    }));
}

function adjustDegreeOfSuccess(
    upgrades: UpgradeResult[],
    current: DegreeOfSuccess,
    skill: Skill,
    kingdom: Kingdom,
    rollOptions: string[],
    flags: string[],
): DegreeOfSuccess {
    const relevantUpgrades = filterPredicates(
        kingdom,
        upgrades,
        flags,
        rollOptions,
        skill,
        (u) => u.predicate,
    )
    console.log('bluuuuuuuuuuuuuuuuuuuuuub');
    console.log(relevantUpgrades)
    if (current === DegreeOfSuccess.CRITICAL_FAILURE
        && relevantUpgrades.some(a => a.upgrade === "criticalFailure")) {
        return DegreeOfSuccess.FAILURE;
    } else if (current === DegreeOfSuccess.FAILURE
        && relevantUpgrades.some(a => a.upgrade === "failure")) {
        return DegreeOfSuccess.SUCCESS;
    } else if (current === DegreeOfSuccess.SUCCESS
        && relevantUpgrades.some(a => a.upgrade === "success")) {
        return DegreeOfSuccess.CRITICAL_SUCCESS;
    } else {
        return current;
    }
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
        upgrades,
        rollOptions,
        modifierBreakdown,
        supernaturalSolutionModifier,
        creativeSolutionModifier,
        rollType,
        rollMode,
        additionalChatMessages,
        kingdom,
        flags,
    }: RollCheckOptions,
): Promise<DegreeOfSuccess> {
    const roll = await new Roll(formula).roll();
    const total = roll.total;
    const dieNumber = total - modifier;
    const previousDegree = determineDegreeOfSuccess(dieNumber, total, dc);
    const degreeOfSuccess = adjustDegreeOfSuccess(upgrades, previousDegree, skill, kingdom, rollOptions, flags);
    const meta = `
        <div class="km-roll-meta" hidden 
            data-formula="${formula}" 
            ${activity === undefined ? '' : `data-activity="${activity.id}"`}
            data-degree="${degreeToProperty(degreeOfSuccess)}"
            data-skill="${skill}"
            data-roll-options="${rollOptions ? encodeJson(rollOptions) : ''}"
            data-dc="${dc}"
            data-total="${total}"
            data-modifier-breakdown="${modifierBreakdown ?? ''}"
            data-modifier="${modifier}"
            data-roll-type="${rollType}"
            data-roll-mode="${rollMode}"
            data-additional-chat-messages="${encodeJson(additionalChatMessages)}"
            data-creative-solution-modifier="${creativeSolutionModifier}"
            data-supernatural-solution-modifier="${supernaturalSolutionModifier}"
        ></div>`;
    const modifierPills = createModifierPills(modifierBreakdown, rollType);
    await roll.toMessage({
        flavor: `<span class="km-skill-check-header">Skill Check: ${label}, DC ${dc}</span>${meta}<hr>${modifierPills}`,
    }, {rollMode});
    await postDegreeOfSuccess(
        actor,
        rollMode,
        activity,
        additionalChatMessages,
        previousDegree,
        rollOptions,
        skill,
        degreeOfSuccess === previousDegree ? undefined : degreeOfSuccess,
    );
    await postAdditionalChatMessages(additionalChatMessages, degreeOfSuccess, rollMode);
    return degreeOfSuccess;
}

async function postDegreeOfSuccess(
    actor: Actor,
    rollMode: RollMode,
    activity: KingdomActivity | undefined,
    additionalChatMessages: AdditionalChatMessages,
    degreeOfSuccess: DegreeOfSuccess,
    rollOptions: string[],
    skill: Skill,
    upgradedDegreeOfSuccess?: DegreeOfSuccess,
): Promise<void> {
    if (activity) {
        await postComplexDegreeOfSuccess(actor, activity, rollMode, additionalChatMessages, degreeOfSuccess, rollOptions, skill, upgradedDegreeOfSuccess);
    } else {
        await postSimpleDegreeOfSuccess(rollMode, degreeOfSuccess, upgradedDegreeOfSuccess);
    }
}

async function postSimpleDegreeOfSuccess(
    rollMode: RollMode,
    degreeOfSuccess: DegreeOfSuccess,
    upgradedDegreeOfSuccess?: DegreeOfSuccess,
): Promise<void> {
    await postDegreeOfSuccessMessage({
        degreeOfSuccess,
        upgradedDegreeOfSuccess,
        messageConfig: {
            critSuccess: `${buildChatButtons([], 'criticalSuccess')}`,
            rollMode,
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

function buildChatButtons(modifiers: ChatModifier[], resultKey: keyof ActivityResults): string {
    if (modifiers.length > 0 || resultKey === 'criticalSuccess') {
        return `
        <div class="km-chat-buttons">
            ${resultKey === 'criticalSuccess' ? '<button type="button" class="km-gain-fame-button">Gain 1 Fame</button>' : ''}
            ${modifiers.map((modifier, index) => {
            const copy = {...modifier};
            delete copy['renderPredicate'];
            const label = modifierToLabel(copy);
            const data = encodeJson(copy);
            return `<button class="km-apply-modifier-effect" 
                        data-data="${data}">Apply Effect: ${label}</button>`;
        }).join('')}    
        </div>`;
    } else {
        return '';
    }
}

async function postComplexDegreeOfSuccess(
    actor: Actor,
    activity: KingdomActivity,
    rollMode: RollMode,
    additionalChatMessages: AdditionalChatMessages,
    degreeOfSuccess: DegreeOfSuccess,
    rollOptions: string[],
    skill: Skill,
    upgradedDegreeOfSuccess?: DegreeOfSuccess,
): Promise<void> {
    const resultKey = getResultKey(upgradedDegreeOfSuccess ?? degreeOfSuccess);
    const results = activity[resultKey];
    if (results) {
        const kingdom = getKingdom(actor);
        const modifiers = results.modifiers;
        const message = results.msg;
        const flags = getFlags(kingdom);
        const buttons = modifiers === undefined
            ? buildChatButtons([], resultKey)
            : buildChatButtons(filterPredicates(kingdom, modifiers, flags, rollOptions, skill, (m) => m.renderPredicate), resultKey);
        // div allows to upgrade/downgrade on right click
        const upgrade = `<div class="km-upgrade-result"
                data-roll-mode="${rollMode}" 
                data-activity="${activity.id}" 
                data-degree="${resultKey}"
                data-roll-options="${encodeJson(rollOptions)}"
                data-skill="${skill}"
                data-additional-chat-messages="${encodeJson(additionalChatMessages)}"></div>`;
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
                rollMode,
            },
        });
    } else {
        await postSimpleDegreeOfSuccess(rollMode, degreeOfSuccess, upgradedDegreeOfSuccess);
    }
}

export async function addOngoingEvent(actor: Actor, uuid: string, label: string): Promise<void> {
    const kingdom = getKingdom(actor);
    const name = `@UUID[${uuid}]{${label}}`;
    await saveKingdom(actor, {
        ongoingEvents: [...kingdom.ongoingEvents, {name}],
    });
}
