import {Modifier} from '../kingdom/modifiers';

export function isEffectItem(item: Item<unknown>): item is Item & EffectItem {
    return item.type === 'effect';
}

function isArmyActor(actor: Actor | null | undefined): boolean {
    return actor?.type === 'army';
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
        if (wearyCount > 0) {
            result.push({
                type: 'circumstance',
                predicates: [{"eq": ["@phase", "army"]}],
                enabled: true,
                value: -wearyCount,
                name: 'Weary',
            });
        }
        if (miredCount > 0) {
            result.push({
                type: 'circumstance',
                predicates: [{"eq": ["@phase", "army"]}, {"eq":["@activity", "deploy-army"]}],
                enabled: true,
                value: -miredCount,
                name: 'Mired',
            });
        }
    }
    return result;
}

export function getScoutingDC(game: Game): number {
    return Math.max(...getSelectedArmies(game)
        .map(a => a.system.scouting ?? 0), 0);
}

function isCampaignFeature(item: Item<unknown>): item is CampaignFeaturePF2E {
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