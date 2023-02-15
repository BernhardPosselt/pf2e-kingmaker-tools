import {DegreeOfSuccess} from './degree-of-success';

export function isGm(game: Game): boolean {
    return game?.user?.name === 'Gamemaster';
}

export function getLevelBasedDC(level: number): number {
    return 14 + level + Math.floor(level / 3);
}

/* eslint-disable @typescript-eslint/no-explicit-any */
export function getSelectedCharacter(game: Game): any {
    /* eslint-disable @typescript-eslint/no-explicit-any */
    return game.user?.character as unknown as any;
}

export function createUUIDLink(uuid: string, label: string): string {
    return `@UUID[${uuid}]{${label}}`;
}

export async function roll(expression: string, flavor?: string): Promise<number> {
    const roll = await (new Roll(expression).evaluate());
    await roll.toMessage({flavor});
    return roll.total;
}

export interface DegreeOfSuccessMessageConfig {
    critSuccess?: string;
    success?: string;
    failure?: string;
    critFailure?: string;
    isPrivate?: boolean;
}

export async function postDegreeOfSuccessMessage(degreeOfSuccess: DegreeOfSuccess, messageConfig: DegreeOfSuccessMessageConfig): Promise<void> {
    let message = '';
    if (degreeOfSuccess === DegreeOfSuccess.CRITICAL_SUCCESS && messageConfig.critSuccess !== undefined) {
        message = messageConfig.critSuccess;
    } else if (degreeOfSuccess === DegreeOfSuccess.SUCCESS && messageConfig.success !== undefined) {
        message = messageConfig.success;
    } else if (degreeOfSuccess === DegreeOfSuccess.FAILURE && messageConfig.failure !== undefined) {
        message = messageConfig.failure;
    } else if (degreeOfSuccess === DegreeOfSuccess.CRITICAL_FAILURE && messageConfig.critFailure !== undefined) {
        message = messageConfig.critFailure;
    }
    if (message !== '') {
        await ChatMessage.create({
            type: CONST.CHAT_MESSAGE_TYPES.ROLL,
            content: message,
            rollMode: messageConfig.isPrivate ? 'blindroll' : 'publicroll',
        });
    }
}

export function capitalize(word: string): string {
    return word[0].toUpperCase() + word.substring(1);
}

export function unpackFormArray<T>(obj: Record<string, T> | undefined | null): T[] {
    if (obj) {
        return Object.keys(obj)
            .map(index => parseInt(index, 10))
            .sort()
            .map(index => `${index}`)
            .map(index => obj[index]);
    } else {
        return [];
    }
}

export function unslugifyAction(word: string): string {
    return word
        .replaceAll('action:', '')
        .split('-')
        .map(part => capitalize(part))
        .join(' ');
}

export function mergeObjects<A extends Record<string, V>, B extends Record<string, V>, V>(
    obj1: A,
    obj2: B,
    conflictFunction: (a: V, b: V) => V
): Record<string, V> {
    const entries: [string, V][] = [];
    for (const key of [...Object.keys(obj1), ...Object.keys(obj2)]) {
        if (key in obj1 && key in obj2) {
            entries.push([key, conflictFunction(obj1[key], obj2[key])]);
        } else if (key in obj1) {
            entries.push([key, obj1[key]]);
        } else if (key in obj2) {
            entries.push([key, obj2[key]]);
        }
    }
    return Object.fromEntries(entries);
}

export function range(start: number, end: number): number[] {
    return Array.apply(0, Array(end - 1))
        .map((element, index) => index + start);
}

export function groupBy<T, R>(array: T[], criterion: (value: T) => R): Map<R, T[]> {
    const result = new Map<R, T[]>();
    for (const elem of array) {
        const key = criterion(elem);
        const group = result.get(key);
        if (group) {
            group.push(elem);
        } else {
            result.set(key, [elem]);
        }
    }
    return result;
}

export function distinctBy<T, R>(array: T[], criterion: (value: T) => R): T[] {
    const existing = new Set();
    const result = [];

    for (const elem of array) {
        const key = criterion(elem);
        if (!existing.has(key)) {
            result.push(elem);
            existing.add(key);
        }
    }
    return result;
}
