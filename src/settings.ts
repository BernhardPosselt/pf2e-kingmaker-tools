export type RollMode = keyof CONFIG.Dice.RollModes

const namespace = 'pf2e-kingmaker-tools';

export function getRollMode(game: Game, settingsKey: string): RollMode {
    const rollMode = getStringSetting(game, settingsKey) as RollMode;
    return rollMode ?? 'publicroll';

}

export function getNumberSetting(game: Game, settingsKey: string): number {
    return game.settings.get(namespace, settingsKey) as number | undefined ?? 0;

}

export function getStringSetting(game: Game, settingsKey: string): string {
    return game.settings.get(namespace, settingsKey) as string | undefined ?? '';
}

export function getStringArraySetting(game: Game, settingsKey: string): string[] {
    return game.settings.get(namespace, settingsKey) as string[] | undefined ?? [];
}

export function getBooleanSetting(game: Game, settingsKey: string): boolean {
    return game.settings.get(namespace, settingsKey) as boolean | undefined ?? false;
}

export async function setSetting<T>(game: Game, settingsKey: string, value: T): Promise<void> {
    await game.settings.set(namespace, settingsKey, value);
}
