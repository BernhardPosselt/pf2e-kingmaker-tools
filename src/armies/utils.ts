import {Kingdom} from '../kingdom/data/kingdom';
import {saveKingdom} from '../kingdom/storage';
import {getBooleanSetting} from '../settings';
import {distinctBy, sum} from '../utils';

function isArmyActor(actor: Actor | null | undefined): boolean {
    return actor?.type === 'army';
}

export async function updateKingdomArmyConsumption(
    {
        actor = null,
        kingdomActor,
        game,
        forceUpdate = false,
    }: {
        actor?: Actor | null | undefined,
        kingdomActor: Actor | null | undefined,
        game: Game,
        forceUpdate?: boolean,
    },
): Promise<void> {
    const autoCalculationEnabled = getBooleanSetting(game, 'autoCalculateArmyConsumption');
    if (autoCalculationEnabled && kingdomActor && (forceUpdate || isArmyActor(actor))) {
        await saveKingdom(kingdomActor, {
            consumption: {
                armies: Math.max(calculateTotalArmyConsumption(game), 0),
            },
        } as Partial<Kingdom>);
    }
}


export function calculateTotalArmyConsumption(game: Game): number {
    const consumption: number[] = [];
    // fucking foundry collection APIs :/
    const actors: Actor[] = [];
    game?.scenes?.map(s => s.tokens)
        ?.forEach(tokenCollection => {
            tokenCollection.forEach(token => {
                const actor = token.actor;
                if (actor && !token.hidden && actor.hasPlayerOwner) {
                    actors.push(actor);
                }
            });
        });
    distinctBy(actors, (a) => a.uuid)
        .map(a => (a as unknown as ArmyActor).system.consumption ?? 0)
        .forEach(c => consumption.push(c));
    return sum(consumption);
}