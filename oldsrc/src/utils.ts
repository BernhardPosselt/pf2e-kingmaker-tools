import {decode} from 'js-base64';
import {v4} from 'uuid';

export function escapeHtml(html: string): string {
    const text = document.createTextNode(html);
    const p = document.createElement('p');
    p.appendChild(text);
    return p.innerHTML;
}

export function proficiencyToRank(proficiency: Proficiency | undefined): number {
    if (proficiency === 'trained') {
        return 1;
    } else if (proficiency === 'expert') {
        return 2;
    } else if (proficiency === 'master') {
        return 3;
    } else if (proficiency === 'legendary') {
        return 4;
    } else {
        return 0;
    }
}

export type Proficiency = 'trained' | 'expert' | 'master' | 'legendary';

export type UntrainedProficiencyMode = 'half' | 'full' | 'none';

export function rankToProficiency(rank: number): Proficiency | undefined {
    if (rank === 1) {
        return 'trained';
    } else if (rank === 2) {
        return 'expert';
    } else if (rank === 3) {
        return 'master';
    } else if (rank === 4) {
        return 'legendary';
    }
}

export function rankToLabel(rank: number): string {
    if (rank === 0) {
        return 'Untrained';
    } else if (rank === 1) {
        return 'Trained';
    } else if (rank === 2) {
        return 'Expert';
    } else if (rank === 3) {
        return 'Master';
    } else {
        return 'Legendary';
    }
}

export function isFirstGm(game: Game): boolean {
    return game?.user?.id === game.users?.find((u) => u.isGM && u.active)?.id;
}

export function isGm(game: Game): boolean {
    return game.users?.find((u) => u.isGM && u.active && u.id === game?.user?.id) !== undefined;
}

export function createUUIDLink(uuid: string, label?: string): string {
    if (label === undefined) {
        return `@UUID[${uuid}]`;
    } else {
        return `@UUID[${uuid}]{${label}}`;
    }
}

export function capitalize(word: string): string {
    return word[0].toUpperCase() + word.substring(1);
}

export function unpackFormArray<T>(obj: Record<string, T> | undefined | null): T[] {
    if (obj) {
        return Object.keys(obj)
            .map(index => parseInt(index, 10))
            .sort((a, b) => a - b)
            .map(index => `${index}`)
            .map(index => obj[index]);
    } else {
        return [];
    }
}

type RollMode = keyof CONFIG.Dice.RollModes

export async function postChatMessage(message: string, rollMode?: RollMode): Promise<void> {
    const msgData = {content: message};
    if (rollMode) {
        ChatMessage.applyRollMode(msgData, rollMode);
    }
    await ChatMessage.create(msgData);
}


export function slugify(word: string): string {
    return word
        .trim()
        .replaceAll(' ', '-')
        .toLowerCase();
}

export function loreToLoreSkill(value: string): string {
    return slugify(value.replace(/\s[lL]ore$/, ''));
}

export function unslugify(word: string, joiner: string = ' '): string {
    return word
        .replaceAll('action:', '')
        .split('-')
        .map(part => capitalize(part))
        .join(joiner);
}

export function mergeObjects<A extends Record<string, V>, B extends Record<string, V>, V>(
    obj1: A,
    obj2: B,
    conflictFunction: (a: V, b: V) => V,
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

export function mergePartialObjects<A extends Partial<Record<string, V>>, B extends Partial<Record<string, V>>, V>(
    obj1: A,
    obj2: B,
    conflictFunction: (a: V | undefined, b: V | undefined) => V,
): Partial<Record<string, V>> {
    const entries: [string, V | undefined][] = [];
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

export function range(start: number, endExclusive: number): number[] {
    const result = [];
    for (let i = start; i < endExclusive; i += 1) {
        result.push(i);
    }
    return result;
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

export interface LabelAndValue {
    label: string;
    value: string;
}

export function listenClick(html: HTMLElement, selector: string, callback: (ev: Event) => Promise<void>): void {
    html.querySelectorAll(selector)
        .forEach(el => {
            el.addEventListener('click', async (ev) => await callback(ev));
        });
}


export function parseNumberInput($html: HTMLElement, name: string): number {
    const input = $html.querySelector(`input[name="${name}"]`) as HTMLInputElement;
    return parseInt(input.value, 10);
}

export function parseTextInput($html: HTMLElement, name: string): string {
    const input = $html.querySelector(`input[name="${name}"]`) as HTMLInputElement;
    return input.value?.trim();
}

export function parseRadio($html: HTMLElement, name: string): string | null {
    const input = Array.from($html.querySelectorAll(`input[name="${name}"]`)) as HTMLInputElement[];
    return input.find(i => i.checked)?.value ?? null;
}


export function clamped(value: number, min: number, max: number): number {
    return Math.min(Math.max(value, min), max);
}

export function deCamelCase(value: string): string {
    const val = value.trim();
    if (val === '') return '';
    return val.replace(/([a-z])([A-Z])/g, '$1 $2')
        .split(' ')
        .map(capitalize)
        .join(' ');
}

export function slugifyable(value: string): boolean {
    return /^([a-zA-Z0-9]*)(\s[a-zA-Z0-9]*)*$/.test(value);
}

export function isBlank(value: string | null | undefined): boolean {
    if (value === null || value === undefined) {
        return true;
    } else {
        return value.trim().length === 0;
    }
}

export function blankToUndefined<T extends string>(value: T): T | undefined {
    if (isBlank(value)) {
        return undefined;
    }
    return value;
}

export function isKingmakerInstalled(game: Game): boolean {
    return game.modules.get('pf2e-kingmaker')?.active === true;
}

export function sum(values: number[]): number {
    return values.reduce((a, b) => a + b, 0);
}

export function isNonNullable<T>(value: T | undefined | null): value is T {
    return value !== undefined && value !== null;
}

export function decodeJson(jsonString: string): object {
    return JSON.parse(decode(jsonString));
}


interface LabelAndValueOptions {
    capitalizeLabel?: boolean;
    emptyChoice?: string;
}

export function toLabelAndValue(values: (string | number)[], {
    capitalizeLabel = false,
    emptyChoice,
}: LabelAndValueOptions = {}): LabelAndValue[] {
    const empty = emptyChoice === undefined ? [] : [{label: emptyChoice, value: emptyChoice}];
    const labels = values.map(v => {
        const label = v.toString();
        return {label: capitalizeLabel ? label.capitalize() : label, value: v.toString()};
    });
    return [...empty, ...labels];
}

export function uuidv4(): string {
    return v4()
}