import {Proficiency} from './data';
import {SkillRequirement} from './activities';
import {capitalize, unslugify} from '../utils';

type ItemType = 'effect' | 'consumable';

async function getItemNames(uuids: Set<string>): Promise<Set<string>> {
    const applicableItems = (await Promise.all(Array.from(uuids).map(uuid => fromUuid(uuid))) as (Item | null)[])
        .filter(i => i !== null) as Item[];
    const names = applicableItems
        .filter(item => item !== undefined && item !== null)
        .map(item => item.name);
    return new Set(names);
}

function getItemsByName(actor: Actor, type: ItemType, names: Set<string>): Item[] {
    return actor.itemTypes[type]
        .filter(i => names.has(i.name));
}

export async function getActorItemsByUuid(actor: Actor, type: ItemType, uuids: Set<string>): Promise<Item[]> {
    const names = await getItemNames(uuids);
    return getItemsByName(actor, type, names);
}

export function isEffectItem(item: Item<unknown>): item is Item<ItemSystem> & EffectItem {
    return item.type === 'effect';
}

export function isConsumableItem(item: Item): item is Item & ConsumableItem {
    return item.type === 'consumable';
}

export async function getActorEffectsByUuid(actor: Actor, uuids: Set<string>): Promise<(Item & EffectItem)[]> {
    const items = await getActorItemsByUuid(actor, 'effect', uuids);
    return items.filter(isEffectItem);
}

export async function actorHasEffectByUuid(actor: Actor, uuid: string): Promise<boolean> {
    const effects = await getActorEffectsByUuid(actor, new Set([uuid]));
    return effects.length > 0;
}


export async function getConsumablesByUuid(actor: Actor, uuids: Set<string>): Promise<(Item & ConsumableItem)[]> {
    const items = await getActorItemsByUuid(actor, 'consumable', uuids);
    return items.filter(isConsumableItem);
}

export async function hasItemByUuid(actor: Actor, type: ItemType, uuids: Set<string>): Promise<boolean> {
    return (await getActorItemsByUuid(actor, type, uuids)).length > 0;
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
    if (skill === 'perception') {
        return actor.perception;
    } else if (skill in skills) {
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

export async function getActorsByUuid(uuids: Set<string>): Promise<Actor[]> {
    return (await Promise.all(Array.from(uuids).map(a => getActorByUuid(a))))
        .filter(a => a !== null) as Actor[];
}

export async function getItemByUuid(uuid: string): Promise<Item | null> {
    const document = await fromUuid(uuid);
    if (document instanceof Item) {
        return document;
    }
    return null;
}

export async function getItemsByUuid(uuids: Set<string>): Promise<Item[]> {
    return (await Promise.all(Array.from(uuids).map(i => getItemByUuid(i))))
        .filter(a => a !== null) as Item[];
}

export async function getEffectByUuid(uuid: string): Promise<Item & EffectItem | null> {
    const document = await fromUuid(uuid);
    if (document instanceof Item && isEffectItem(document)) {
        return document;
    }
    return null;
}

export async function getEffectsByUuid(uuids: Set<string>): Promise<(Item & EffectItem)[]> {
    const items = await getItemsByUuid(uuids);
    return items.filter(isEffectItem) as (Item & EffectItem)[];
}


async function removeActorEffectsByUuid(actor: Actor, uuids: Set<string>): Promise<void> {
    const existingEffects = await getActorEffectsByUuid(actor, uuids);
    await actor.deleteEmbeddedDocuments('Item', existingEffects.map(e => e.id));
}

async function removeActorEffectsByName(actor: Actor, names: Set<string>): Promise<void> {
    const existingEffects = getItemsByName(actor, 'effect', names);
    await actor.deleteEmbeddedDocuments('Item', existingEffects.map(e => e.id));
}

export async function removeActorsEffectsByUuid(actors: Actor[], uuids: Set<string>): Promise<void> {
    await Promise.all(actors.map(a => removeActorEffectsByUuid(a, uuids)));
}

export async function removeActorsEffectsByName(actors: Actor[], names: Set<string>): Promise<void> {
    await Promise.all(actors.map(a => removeActorEffectsByName(a, names)));
}

