type ItemType = 'effect' | 'consumable';

/* eslint-disable @typescript-eslint/no-explicit-any */
async function getSourceIds(uuids: Set<string>): Promise<Set<string>> {
    const applicableItems = await Promise.all(Array.from(uuids).map(uuid => {
        return fromUuid(uuid);
    }));
    const sourceIds = applicableItems
        .filter(item => item !== undefined && item !== null)
        .map((item: any) => item.sourceId);
    return new Set(sourceIds);
}

export function getItemsBySourceId(actor: Actor, type: ItemType, sourceIds: Set<string>): Item[] {
    return actor.itemTypes[type]
        .filter((a: any) => sourceIds.has(a.sourceId));
}

export async function getItemsByUuid(actor: Actor, type: ItemType, uuids: Set<string>): Promise<Item[]> {
    return getItemsBySourceId(actor, type, await getSourceIds(uuids));
}

export async function hasItemByUuid(actor: Actor, type: ItemType, uuids: Set<string>): Promise<boolean> {
    return (await getItemsByUuid(actor, type, uuids)).length > 0;
}

export async function removeItemsBySourceId(actors: Actor[], type: ItemType, applicableSourceIds: Set<string>): Promise<void> {
    for (const actor of actors) {
        const existingEffects = getItemsBySourceId(actor, type, applicableSourceIds);
        const existingEffectIds = existingEffects.map((a) => a.id) as string[];
        await actor.deleteEmbeddedDocuments('Item', existingEffectIds);
    }
}

export async function removeExpiredEffects(actors: Actor[], applicableSourceIds: Set<string>): Promise<void> {
    for (const actor of actors) {
        const expiredEffectIds = getItemsBySourceId(actor, 'effect', applicableSourceIds)
            .filter((a: any) => a.isExpired)
            .map(a => a.id) as string[];
        await actor.deleteEmbeddedDocuments('Item', expiredEffectIds);
    }
}
