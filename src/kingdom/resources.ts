import {parseNumberInput, unslugify} from '../utils';
import {getSizeData, Kingdom} from './data/kingdom';
import {getKingdom, saveKingdom} from './storage';
import {getCapacity} from './kingdom-utils';
import {getStringSetting} from '../settings';
import {getStolenLandsData, ResourceAutomationMode} from './scene';

interface ResourceValues {
    value: number;
    message: string;
    missingMessage?: string;
}

async function askFactor(): Promise<number> {
    return new Promise(resolve => {
        new Dialog({
            title: 'Perform How Many Times?',
            content: `<form class="simple-dialog-form">
            <div>
                <label>Perform How Many Times?</label>
                <input type="number" value="1" name="times">
            </div>
        </form>`,
            buttons: {
                multiply: {
                    icon: '<i class="fa-solid fa-check"></i>',
                    label: 'Apply',
                    callback: async (html): Promise<void> => {
                        const $html = html as HTMLElement;
                        const turns = parseNumberInput($html, 'times');
                        resolve(turns);
                    },
                },
            },
            default: 'multiply',
        }, {
            jQuery: false,
            width: 400,
        }).render(true);
    });
}

export async function calculateNewValue(
    {
        currentValue,
        newValue,
        type,
        turn,
        mode,
        limit,
        multiple,
        showMissing,
    }: {
        currentValue: number,
        newValue: number,
        type: RolledResources,
        turn: ResourceTurn,
        mode: ResourceMode,
        multiple: boolean,
        limit?: number,
        showMissing: boolean,
    },
): Promise<ResourceValues> {
    const factor = multiple ? await askFactor() : 1;
    const multipliedValue = newValue * factor;
    const value = mode === 'gain' ? currentValue + multipliedValue : currentValue - multipliedValue;
    const missing = value < 0 ? Math.abs(value) : 0;
    const limitedValue = limit === undefined ? value : Math.min(limit, value);
    const label = type === 'rolled-resource-dice' ? 'Resource Points' : unslugify(type);
    const turnLabel = turn === 'now' ? 'this turn' : 'next turn';
    const message = `${mode === 'gain' ? 'Gaining' : 'Losing'} ${Math.abs(multipliedValue)} ${label} ${turnLabel}`;
    const missingMessage = showMissing && missing > 0 && turn === 'now' ? `Missing ${missing} ${label}` : '';
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
    } else if (type === 'xp') {
        return kingdom.xp;
    } else if (type === 'fame') {
        return kingdom.fame[turn];
    } else if (type === 'supernatural-solution') {
        return kingdom.supernaturalSolutions;
    } else if (type === 'creative-solution') {
        return kingdom.creativeSolutions;
    } else {
        throw Error(`Unhandled type ${type}`);
    }
}

export function getLimit(game: Game, kingdom: Kingdom, type: RolledResources, turn: ResourceTurn): number | undefined {
    if (turn === 'now' && (type === 'food' || type === 'luxuries' || type === 'lumber' || type === 'ore' || type === 'stone')) {
        return (getCapacity(game, kingdom))[type];
    } else if (type === 'fame') {
        return 3;
    } else {
        return undefined;
    }
}


export async function evaluateValue(game: Game, kingdom: Kingdom, resources: RolledResources, value: string): Promise<number> {
    if (resources === 'rolled-resource-dice') {
        const num = value.includes('d') ? `(${value})` : value;
        const automateResourceMode = getStringSetting(game, 'automateResources') as ResourceAutomationMode;
        const {size: kingdomSize} = getStolenLandsData(game, automateResourceMode, kingdom);
        const dice = getSizeData(kingdomSize).resourceDieSize;
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
    } else if (type === 'xp') {
        return {xp: value};
    } else if (type === 'fame') {
        return {
            fame: {
                ...kingdom.fame,
                [turn]: value,
            },
        };
    } else if (type === 'supernatural-solution') {
        return {supernaturalSolutions: value};
    } else if (type === 'creative-solution') {
        return {creativeSolutions: value};
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
    | 'xp'
    | 'supernatural-solution'
    | 'creative-solution'
    | 'rolled-resource-dice';
export type ResourceTurn = 'now' | 'next';

const ignoreMissing: Set<RolledResources> = new Set(['crime', 'decay', 'strife', 'corruption', 'unrest']);

interface ResourceButton {
    type: RolledResources,
    mode: ResourceMode,
    turn: ResourceTurn,
    value: string,
    multiple: boolean;
}

export function parseResourceButton(element: HTMLButtonElement): ResourceButton {
    const type = element.dataset.type! as RolledResources;
    const mode = element.dataset.mode! as 'gain' | 'lose';
    const turn = element.dataset.turn! as 'now' | 'next';
    const multiple = element.dataset.multiple === 'true';
    const value = element.dataset.value!;
    return {
        type,
        mode,
        turn,
        value,
        multiple,
    };
}

export async function updateResources(game: Game, actor: Actor, target: HTMLButtonElement): Promise<void> {
    const {multiple, type, mode, turn, value: parsedValue} = parseResourceButton(target);
    const kingdom = getKingdom(actor);

    const evaluatedValue = await evaluateValue(game, kingdom, type, parsedValue);
    const currentValue = getCurrentValue(kingdom, type, turn);
    const limit = getLimit(game, kingdom, type, turn);
    const {missingMessage, message, value} = await calculateNewValue({
        mode,
        limit,
        turn,
        newValue: evaluatedValue,
        type,
        currentValue,
        multiple,
        showMissing: !ignoreMissing.has(type),
    });

    await ChatMessage.create({'content': `${message}`});
    if (missingMessage) {
        await ChatMessage.create({'content': `${missingMessage}`});
    }

    const update = createUpdate(kingdom, type, turn, value);
    await saveKingdom(actor, update);
}
