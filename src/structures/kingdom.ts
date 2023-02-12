import {getNumberSetting, setSetting} from '../settings';

export interface KingdomData {
    type: 'Territory' | 'Province' | 'State' | 'Country' | 'Dominion';
    resourceDie: 'd4' | 'd6' | 'd8' | 'd10' | 'd12';
    controlDCModifier: number;
    commodityStorage: number;
}

export function getKingdomData(kingdomSize: number): KingdomData {
    if (kingdomSize < 10) {
        return {
            type: 'Territory',
            resourceDie: 'd4',
            controlDCModifier: 0,
            commodityStorage: 4,
        };
    } else if (kingdomSize < 25) {
        return {
            type: 'Province',
            resourceDie: 'd6',
            controlDCModifier: 1,
            commodityStorage: 8,
        };
    } else if (kingdomSize < 50) {
        return {
            type: 'State',
            resourceDie: 'd8',
            controlDCModifier: 2,
            commodityStorage: 12,
        };
    } else if (kingdomSize < 100) {
        return {
            type: 'Country',
            resourceDie: 'd10',
            controlDCModifier: 3,
            commodityStorage: 16,
        };
    } else {
        return {
            type: 'Dominion',
            resourceDie: 'd12',
            controlDCModifier: 4,
            commodityStorage: 20,
        };
    }
}

export function getKingdomSize(game: Game): number {
    return getNumberSetting(game, 'kingdomSize');
}

export async function saveKingdomSize(game: Game, size: number): Promise<void> {
    return await setSetting(game, 'kingdomSize', size);
}
