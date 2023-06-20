import {Proficiency} from './data';
import {SkillRequirement} from './activities';
import {capitalize, unslugify} from '../utils';

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
    return skillProp?.rank >= rank ?? false;
}

export async function getSkill(actor: Actor, skill: string): Promise<any | null> {
    const skills = (actor as any).skills;
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
    const actor = await fromUuid(actorUuid);
    if (actor) {
        return await getSkill((actor as any), 'cooking') !== null;
    } else {
        return false;
    }
}

