import {unslugify} from '../utils';
import {Commodities, getSizeData, Kingdom} from './data/kingdom';
import {getCapacity} from './kingdom';
import {getGameOrThrow, getKingdom, getKingdomSheetActorOrThrow, saveKingdom} from './storage';
import {Ruin} from './data/ruin';

interface ResourceValues {
    value: number;
    message: string;
    missingMessage?: string;
}

export function calculateNewValue(
    {
        currentValue,
        newValue,
        type,
        turn,
        mode,
        limit,
    }: {
        currentValue: number,
        newValue: number,
        type: RolledResources,
        turn: ResourceTurn,
        mode: ResourceMode,
        limit?: number,
    }
): ResourceValues {
    const value = mode === 'gain' ? currentValue + newValue : currentValue - newValue;
    const missing = value < 0 ? Math.abs(value) : 0;
    const limitedValue = limit === undefined ? value : Math.min(limit, value);
    const label = type === 'rolled-resource-dice' ? 'Resource Points' : unslugify(type);
    const turnLabel = turn === 'now' ? 'this turn' : 'next turn';
    const message = `${mode === 'gain' ? 'Gaining' : 'Losing'} ${Math.abs(newValue)} ${label} ${turnLabel}`;
    const missingMessage = missing > 0 && turn === 'now' ? `Missing ${missing} ${label}` : '';
    return {
        value: turn === 'now' ? Math.max(0, limitedValue) : limitedValue,
        message,
        missingMessage,
    };
}

export function getCurrentValue(kingdom: Kingdom, type: RolledResources, turn: ResourceTurn): number {
    if (type === 'food' || type === 'luxuries' || type === 'ore' || type === 'lumber' || type === 'stone') {
        return kingdom.commodities[turn][type];
    } else if (type === 'unrest') {
        return kingdom.unrest;
    } else if (type === 'resource-dice') {
        return kingdom.resourceDice[turn];
    } else if (type === 'resource-points' || type === 'rolled-resource-dice') {
        return kingdom.resourcePoints[turn];
    } else if (type === 'crime' || type === 'decay' || type === 'strife' || type === 'corruption') {
        return kingdom.ruin[type].value;
    } else if (type === 'fame') {
        if (turn === 'now') {
            return kingdom.fame;
        } else {
            return kingdom.fameNext;
        }
    } else if (type === 'size') {
        return kingdom.size;
    } else {
        throw Error(`Unhandled type ${type}`);
    }
}

export function getLimit(game: Game, kingdom: Kingdom, type: RolledResources, turn: ResourceTurn): number | undefined {
    if (turn === 'now' && (type === 'food' || type === 'luxuries' || type === 'lumber' || type === 'ore' || type === 'stone')) {
        const commodityCapacity = getSizeData(kingdom.size).commodityCapacity;
        const capacity = getCapacity(game, commodityCapacity);
        return capacity[type];
    } else if (type === 'fame') {
        return 3;
    } else {
        return undefined;
    }
}


export async function evaluateValue(kingdom: Kingdom, resources: RolledResources, value: string): Promise<number> {
    if (resources === 'rolled-resource-dice') {
        const num = value.includes('d') ? `(${value})` : value;
        const dice = getSizeData(kingdom.size).resourceDieSize;
        const roll = await new Roll(`${num}${dice}`).roll();
        await roll.toMessage({flavor: 'Rolling Resource Dice'});
        return roll.total;
    } else if (value.includes('d')) {
        const roll = await new Roll(value).roll();
        await roll.toMessage({flavor: `Rolling ${unslugify(resources)}`});
        return roll.total;
    } else {
        return parseInt(value, 10);
    }
}

export function createUpdate(kingdom: Kingdom, type: RolledResources, turn: ResourceTurn, value: number): Partial<Kingdom> {
    if (type === 'rolled-resource-dice' || type === 'resource-points') {
        return {
            resourcePoints: {
                ...kingdom.resourcePoints,
                [turn]: value,
            },
        };
    } else if (type === 'fame') {
        if (turn === 'now') {
            return {fame: value};
        } else {
            return {fameNext: value};
        }
    } else if (type === 'size') {
        return {size: value};
    } else if (type === 'resource-dice') {
        return {
            resourceDice: {
                ...kingdom.resourceDice,
                [turn]: value,
            },
        };
    } else if (type === 'unrest') {
        return {
            unrest: value,
        };
    } else if (type === 'strife' || type === 'crime' || type === 'decay' || type === 'corruption') {
        return {
            ruin: {
                ...kingdom.ruin,
                [type]: {
                    ...kingdom.ruin[type],
                    value: value,
                },
            },
        };
    } else if (type === 'lumber' || type === 'ore' || type === 'stone' || type === 'food' || type === 'luxuries') {
        return {
            commodities: {
                ...kingdom.commodities,
                [turn]: {
                    ...kingdom.commodities[turn],
                    [type]: value,
                },
            },
        };
    } else {
        throw Error(`Unhandled type ${type}`);
    }
}


export type ResourceMode = 'gain' | 'lose';
export type RolledResources = 'resource-dice'
    | 'crime'
    | 'decay'
    | 'corruption'
    | 'strife'
    | 'resource-points'
    | 'food'
    | 'luxuries'
    | 'unrest'
    | 'ore'
    | 'lumber'
    | 'fame'
    | 'stone'
    | 'size'
    | 'rolled-resource-dice'; // TODO: add ruin, commodities
export type ResourceTurn = 'now' | 'next';

interface ResourceButton {
    type: RolledResources,
    mode: ResourceMode,
    turn: ResourceTurn,
    value: string,
}

export function parseResourceButton(element: HTMLButtonElement): ResourceButton {
    const type = element.dataset.type! as RolledResources;
    const mode = element.dataset.mode! as 'gain' | 'lose';
    const turn = element.dataset.turn! as 'now' | 'next';
    const value = element.dataset.value!;
    return {
        type,
        mode,
        turn,
        value,
    };
}

interface CreateResourceButton {
    type: RolledResources,
    mode?: ResourceMode,
    turn?: ResourceTurn,
    value: string,
    hints?: string;
}

export function gainFame(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'fame'});
}

export function loseFame(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'fame', mode: 'lose'});
}

export function gainCommodities(type: keyof Commodities, value: number | string): string {
    return createResourceButton({value: `${value}`, type});
}

export function loseCommodities(type: keyof Commodities, value: number | string): string {
    return createResourceButton({value: `${value}`, type, mode: 'lose'});
}

export function gainRuin(type: Ruin, value: number | string): string {
    return createResourceButton({value: `${value}`, type});
}

export function loseRuin(type: Ruin, value: number | string): string {
    return createResourceButton({value: `${value}`, type, mode: 'lose'});
}

export function gainRolledRD(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'rolled-resource-dice'});
}

export function loseRolledRD(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'rolled-resource-dice', mode: 'lose'});
}

export function gainRP(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'resource-points'});
}

export function loseRP(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'resource-points', mode: 'lose'});
}

export function gainUnrest(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'unrest'});
}

export function loseUnrest(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'unrest', mode: 'lose'});
}

export function gainSize(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'size'});
}

export function loseSize(value: number | string): string {
    return createResourceButton({value: `${value}`, type: 'size', mode: 'lose'});
}

export function createResourceButton({turn = 'now', value, mode = 'gain', type, hints}: CreateResourceButton): string {
    const turnLabel = turn === 'now' ? '' : ' Next Turn';
    const label = `${mode === 'gain' ? 'Gain' : 'Lose'} ${value} ${unslugify(type)}${turnLabel}`;
    return `<button type="button" class="km-gain-lose" 
        data-type="${type}"
        data-mode="${mode}"
        data-turn="${turn}"
        ${value !== undefined ? `data-value="${value}"` : ''}
        >${label}${hints !== undefined ? `(${hints})` : ''}</button>`;
}

export async function updateResources(target: HTMLButtonElement): Promise<void> {
    const {type, mode, turn, value: parsedValue} = parseResourceButton(target);
    const game = getGameOrThrow();
    const actor = getKingdomSheetActorOrThrow();
    const kingdom = getKingdom(actor);

    const evaluatedValue = await evaluateValue(kingdom, type, parsedValue);
    const currentValue = getCurrentValue(kingdom, type, turn);
    const limit = getLimit(game, kingdom, type, turn);
    const {missingMessage, message, value} = calculateNewValue({
        mode,
        limit,
        turn,
        newValue: evaluatedValue,
        type,
        currentValue,
    });

    await ChatMessage.create({'content': `${message}`});
    if (missingMessage) {
        await ChatMessage.create({'content': `${missingMessage}`});
    }

    const update = createUpdate(kingdom, type, turn, value);
    await saveKingdom(actor, update);
}
