import {Kingdom} from '../kingdom/data/kingdom';
import {saveKingdom} from '../kingdom/storage';
import {getBooleanSetting} from '../settings';
import {distinctBy, sum} from '../utils';
import {Modifier} from '../kingdom/modifiers';
import {isEffectItem} from '../camping/actor';

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

function getEffectCount(actor: Actor, slug: string): number {
    return (actor.items
            .find(i => isEffectItem(i) && i.slug === slug) as EffectItem | undefined)
            ?.badge
            ?.value
        ?? 0;
}

export function getSelectedArmies(game: Game): (Actor & ArmyActor) [] {
    return Array.from(game.user?.targets ?? [])
        .filter(t => isArmyActor(t.actor))
        .map(t => t.actor) as (Actor & ArmyActor)[];
}

export function getArmyModifiers(game: Game): Modifier[] {
    const result: Modifier[] = [];
    const selectedArmies = getSelectedArmies(game);
    if (selectedArmies.length > 0) {
        const first = selectedArmies[0];
        const miredCount = getEffectCount(first, 'mired');
        const wearyCount = getEffectCount(first, 'weary');
        const baseModifier: Pick<Modifier, 'type' | 'phases' | 'enabled'> = {
            type: 'circumstance',
            phases: ['army'],
            enabled: true,
        };
        if (wearyCount > 0) {
            result.push({
                ...baseModifier,
                value: -wearyCount,
                name: 'Weary',
            });
        }
        if (miredCount > 0) {
            result.push({
                ...baseModifier,
                value: -miredCount,
                name: 'Mired',
                activities: ['deploy-army'],
            });
        }
    }
    return result;
}

export function getScoutingDC(game: Game): number {
    return Math.max(...getSelectedArmies(game)
        .map(a => a.system.scouting ?? 0), 0);
}

export function isCampaignFeature(item: Item<unknown>): item is CampaignFeaturePF2E {
    return item.type === 'campaignFeature';
}

export function isArmyTactic(item: Item<unknown>): item is CampaignFeaturePF2E {
    return isCampaignFeature(item)
        && item.system.campaign === 'kingmaker'
        && item.system.category === 'army-tactic';
}

function filterArmyTactics(items: Item[]): CampaignFeaturePF2E[] {
    const campaignFeatures = (items?.filter(i => isCampaignFeature(i)) ?? []) as unknown as CampaignFeaturePF2E[];
    return campaignFeatures.filter(i => isArmyTactic(i));
}

export async function getArmyTactics(game: Game): Promise<CampaignFeaturePF2E[]> {
    const documents = (await game.packs.get('pf2e.kingmaker-features')?.getDocuments()) as Item[];
    const items = (game.items ?? []) as Item[];
    return filterArmyTactics([...documents, ...items]);
}

export function isSpecialArmy(actor: Actor & ArmyActor): boolean {
    return actor.system.traits.rarity !== 'common';
}

export function isPlayerArmyActor(actor: Actor): actor is Actor & ArmyActor {
    return isArmyActor(actor) && actor.hasPlayerOwner && actor.folder?.name === 'Recruitable Armies';
}

export function getPlayerArmies(game: Game): (Actor & ArmyActor)[] {
    return (game?.actors?.filter(a => isPlayerArmyActor(a)) ?? []) as (Actor & ArmyActor)[];
}