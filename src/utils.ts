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
