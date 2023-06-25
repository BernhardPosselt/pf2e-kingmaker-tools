import {Proficiency} from './data';
import {SkillRequirement} from './activities';
import {capitalize, unslugify} from '../utils';

type ItemType = 'effect' | 'consumable';

export async function getItemSourceIds(uuids: Set<string>): Promise<Set<string>> {
    const applicableItems = (await Promise.all(Array.from(uuids).map(uuid => fromUuid(uuid))) as (Item | null)[])
        .filter(i => i !== null) as Item[];
    const sourceIds = applicableItems
        .filter(item => item !== undefined && item !== null)
        .map(item => item.sourceId);
    return new Set(sourceIds);
}

export function getItemsBySourceId(actor: Actor, type: ItemType, sourceIds: Set<string>): Item[] {
    return actor.itemTypes[type]
        .filter(i => sourceIds.has(i.sourceId));
}

export function isEffectItem(item: Item): item is Item & EffectItem {
    return item.type === 'effect';
}

export function isConsumableItem(item: Item): item is Item & ConsumableItem {
    return item.type === 'consumable';
}

export async function getActorItemsByUuid(actor: Actor, type: ItemType, uuids: Set<string>): Promise<Item[]> {
    return getItemsBySourceId(actor, type, await getItemSourceIds(uuids));
}

export async function getActorEffectsByUuid(actor: Actor, uuids: Set<string>): Promise<(Item & EffectItem)[]> {
    const items = await getActorItemsByUuid(actor, 'effect', uuids);
    return items.filter(isEffectItem);
}

export async function getConsumablesByUuid(actor: Actor, uuids: Set<string>): Promise<(Item & ConsumableItem)[]> {
    const items = await getActorItemsByUuid(actor, 'consumable', uuids);
    return items.filter(isConsumableItem);
}

export async function hasItemByUuid(actor: Actor, type: ItemType, uuids: Set<string>): Promise<boolean> {
    return (await getActorItemsByUuid(actor, type, uuids)).length > 0;
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
            .filter(isEffectItem)
            .filter(a => a.isExpired)
            .map(a => a.id) as string[];
        await actor.deleteEmbeddedDocuments('Item', expiredEffectIds);
    }
}

export function proficiencyToRank(proficiency: Proficiency): number {
    if (proficiency === 'trained') {
        return 1;
    } else if (proficiency === 'expert') {
        return 2;
    } else if (proficiency === 'master') {
        return 3;
    } else {
        return 4;
    }
}

export class NotProficientError extends Error {

}

export async function validateSkillProficiencies(actor: Actor, requirements: SkillRequirement[]): Promise<void> {
    for (const requirement of requirements) {
        const skill = requirement.skill;
        const proficiency = requirement.proficiency;
        if (!await satisfiesSkillProficiency(actor, skill, proficiency)) {
            const name = actor.name;
            throw new NotProficientError(`${name} requires at least ${capitalize(proficiency)} in ${unslugify(skill)}`);
        }
    }
}

export async function satisfiesSkillProficiency(actor: Actor, skill: string, minimumProficiency: Proficiency): Promise<boolean> {
    const skillProp = await getSkill(actor, skill);
    const rank = proficiencyToRank(minimumProficiency);
    if (skillProp) {
        return skillProp.rank >= rank;
    }
    return false;
}

export async function getSkill(actor: Actor, skill: string): Promise<ActorSkill | null> {
    const skills = actor.skills;
    const loreSkill = `${skill}-lore`;
    if (skill in skills) {
        return skills[skill];
    } else if (loreSkill in skills) {
        return skills[loreSkill];
    } else {
        return null;
    }
}

export async function hasCookingLore(actorUuid: string): Promise<boolean> {
    const actor = await fromUuid(actorUuid) as Actor | null;
    if (actor) {
        return await getSkill(actor, 'cooking') !== null;
    } else {
        return false;
    }
}

export async function getActorByUuid(uuid: string): Promise<Actor | null> {
    const document = await fromUuid(uuid);
    if (document instanceof Actor) {
        return document;
    }
    return null;
}

export async function getActorsByUuid(uuids: string[]): Promise<Actor[]> {
    return (await Promise.all(uuids.map(a => getActorByUuid(a))))
        .filter(a => a !== null) as Actor[];
}

export async function getItemByUuid(uuid: string): Promise<Item | null> {
    const document = await fromUuid(uuid);
    if (document instanceof Item) {
        return document;
    }
    return null;
}

export async function getItemsByUuid(uuids: string[]): Promise<Item[]> {
    return (await Promise.all(uuids.map(i => getItemByUuid(i))))
        .filter(a => a !== null) as Item[];
}

export async function getEffectsByUuid(uuids: string[]): Promise<(Item & EffectItem)[]> {
    const items = await getItemsByUuid(uuids);
    return items.filter(isEffectItem) as (Item & EffectItem)[];
}


async function removeActorEffects(actor: Actor, sourceIds: Set<string>): Promise<void> {
    const existingEffects = getItemsBySourceId(actor, 'effect', sourceIds);
    const idsToRemove = existingEffects
        .filter(e => sourceIds.has(e.sourceId))
        .map(e => e.id);
    await actor.deleteEmbeddedDocuments('Item', idsToRemove);
}

export async function removeActorsEffects(actors: Actor[], sourceIds: Set<string>): Promise<void> {
    await Promise.all(actors.map(a => removeActorEffects(a, sourceIds)));
}

