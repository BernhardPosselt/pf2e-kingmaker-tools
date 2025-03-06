function isArmyActor(actor: Actor | null | undefined): boolean {
    return actor?.type === 'army';
}

export function getSelectedArmies(game: Game): (Actor & ArmyActor) [] {
    return Array.from(game.user?.targets ?? [])
        .filter(t => isArmyActor(t.actor))
        .map(t => t.actor) as (Actor & ArmyActor)[];
}

function isCampaignFeature(item: Item<unknown>): item is CampaignFeaturePF2E {
    return item.type === 'campaignFeature';
}

export function isArmyTactic(item: Item<unknown>): item is CampaignFeaturePF2E {
    return isCampaignFeature(item)
        && item.system.campaign === 'kingmaker'
        && item.system.category === 'army-tactic';
}
