import {allCampingActivities, CampingActivityName} from './activities';
import {getRegionInfo} from './regions';
import {getLevelBasedDC, slugify} from '../utils';
import {DegreeOfSuccess} from '../degree-of-success';
import {DcType} from './data';
import {allRecipes} from './recipes';


export interface CampingActivity {
    activity: CampingActivityName;
    actorUuid: string;
    result?: 'Critical Success' | 'Success' | 'Failure' | 'Critical Failure',
    selectedSkill?: string;
}

interface ActorMeals {
    actorUuid: string;
    favoriteMeal?: string;
    consume: 'meal' | 'rationsOrSubsistence' | 'nothing';
}

interface Cooking {
    knownRecipes: string[];
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    chosenMeal: string;
    servings: number;
    actorMeals: ActorMeals[];
}


export interface Camping {
    actorUuids: string[];
    campingActivities: CampingActivity[];
    cooking: Cooking;
}

export function getDefaultConfiguration(): Camping {
    return {
        actorUuids: [],
        campingActivities: [],
        cooking: {
            chosenMeal: 'Basic Meal',
            servings: 0,
            actorMeals: [],
            magicalSubsistenceAmount: 0,
            subsistenceAmount: 0,
            knownRecipes: ['Basic Meal'],
        },
    };
}

export function getDC(game: Game, actor: Actor, dcType: DcType): number {
    if (dcType === 'zone') {
        return getRegionInfo(game).zoneDC;
    } else if (dcType === 'actorLevel') {
        /* eslint-disable @typescript-eslint/no-explicit-any */
        return getLevelBasedDC((actor as any).level);
    } else {
        return dcType;
    }
}

export async function rollCampingCheck(
    {
        game,
        actor,
        dc,
        skill,
        secret = false,
        activity,
    }: {
        game: Game,
        actor: Actor,
        dc?: DcType,
        skill: string,
        secret?: boolean,
        activity?: string,
    }): Promise<DegreeOfSuccess> {
    /* eslint-disable @typescript-eslint/no-explicit-any */
    const rollData: Record<string, any> = {
        extraRollOptions: ['camping'],
    };
    if (activity) {
        rollData['extraRollOptions'].push('action:' + slugify(activity));
    }
    if (dc) {
        rollData['dc'] = getDC(game, actor, dc);
    }
    if (secret) {
        rollData['rollMode'] = 'blindroll';
    }
    let result;
    if (skill === 'perception') {
        result = await (actor as any).perception.roll(rollData);
    } else {
        result = await (actor as any).skills[skill].roll(rollData);
    }
    return result.degreeOfSuccess;
}

/**
 * Given an array of uuids of existing effects, retrieve their sourceIds
 * @param applicableUuids
 */

/* eslint-disable @typescript-eslint/no-explicit-any */
async function getApplicableSourceIds(applicableUuids: string[]): Promise<Set<string>> {
    const applicableEffects = await Promise.all(applicableUuids.map(uuid => {
        return fromUuid(uuid);
    }));
    const sourceIds = applicableEffects
        .filter(eff => eff !== undefined && eff !== null)
        .map((eff: any) => eff.sourceId);
    return new Set(sourceIds);
}

/**
 * Retrieve all effects on an actor whose sourceId matches the given source ids
 * @param actor
 * @param applicableSourceIds
 */

/* eslint-disable @typescript-eslint/no-explicit-any */
function getApplicableEffectsOnActor(actor: Actor, applicableSourceIds: Set<string>): any[] {
    return actor.itemTypes.effect
        .filter((a: any) => applicableSourceIds.has(a.sourceId));
}

/**
 * Remove all expired effects whose sourceIds match the given array
 * @param actors
 * @param applicableSourceIds
 */
export async function removeExpiredEffects(actors: Actor[], applicableSourceIds: Set<string>): Promise<void> {
    for (const actor of actors) {
        /* eslint-disable @typescript-eslint/no-explicit-any */
        const expiredEffectIds = getApplicableEffectsOnActor(actor, applicableSourceIds)
            .filter((a: any) => a.isExpired)
            .map(a => a.id) as string[];
        await actor.deleteEmbeddedDocuments('Item', expiredEffectIds);
    }
}

/**
 * Retrieves all effects from all actors that are in the applicableSourceIds whitelist
 * Then adds all effects from the given uuids and deletes effects not present in the given uuids
 *
 * @param actors
 * @param uuids
 * @param applicableSourceIds
 */
export async function syncEffects(actors: Actor[], uuids: string[], applicableSourceIds: Set<string>): Promise<void> {
    for (const actor of actors) {
        const existingEffects = getApplicableEffectsOnActor(actor, applicableSourceIds);
        const existingEffectIds = new Set(existingEffects.map(a => a.id) as string[]);
        /* eslint-disable @typescript-eslint/no-explicit-any */
        const effectsToSync = (await Promise.all(uuids.map(uuid => fromUuid(uuid))))
            .filter((eff: any) => eff !== undefined && eff !== null);
        const effectIdsToSync = new Set(effectsToSync.map((a: any) => a.id) as string[]);

        const effectsToAdd = effectsToSync
            .filter((eff: any) => !existingEffectIds.has(eff.sourceId))
            .map((eff: any) => eff.toObject());
        const effectIdsToRemove = Array.from(existingEffects)
            .filter((eff: any) => !effectIdsToSync.has(eff.id))
            .map((eff: any) => eff.id);

        await actor.deleteEmbeddedDocuments('Item', effectIdsToRemove);
        await actor.createEmbeddedDocuments('Item', effectsToAdd);
    }
}

export async function removeEffects(actors: Actor[], applicableSourceIds: Set<string>): Promise<void> {
    for (const actor of actors) {
        const existingEffects = getApplicableEffectsOnActor(actor, applicableSourceIds);
        const existingEffectIds = existingEffects.map(a => a.id) as string[];
        await actor.deleteEmbeddedDocuments('Item', existingEffectIds);
    }
}

export async function hasAnyEffectOf(actor: Actor, uuids: string[]): Promise<boolean> {
    const sourceIds = await getApplicableSourceIds(uuids);
    return getApplicableEffectsOnActor(actor, sourceIds).length > 0;
}

export async function afterPrepareCamp(
    actors: Actor[],
    activityUuids: string[],
    activitySourceIds: Set<string>
): Promise<void> {
    await removeExpiredEffects(actors, activitySourceIds);
    await syncEffects(actors, activityUuids, activitySourceIds);
}

export async function afterCampingChange(
    actors: Actor[],
    activityUuids: string[],
    activitySourceIds: Set<string>
): Promise<void> {
    await syncEffects(actors, activityUuids, activitySourceIds);
}

export async function afterCookingChange(
    actors: Actor[],
    mealEffectUuids: string[],
    mealEffectSourceIds: Set<string>
): Promise<void> {
    await syncEffects(actors, mealEffectUuids, mealEffectSourceIds);
}

export async function afterDailyPreparations(
    game: Game,
    actors: Actor[],
    activitySourceIds: Set<string>
): Promise<void> {
    for (const actor of actors) {
        const healMoreHp = await hasAnyEffectOf(actor, [
            allRecipes.find(r => r.name === 'Basic Meal')!.criticalSuccess!.effectUuid!,
            allCampingActivities.find(a => a.name === 'Dawnflower\'s Blessing')!.effectUuid!,
        ]);
        if (healMoreHp) {
            /* eslint-disable @typescript-eslint/no-explicit-any */
            const currentHp = (actor as any).attributes.hp.value;
            const maxHp = (actor as any).attributes.hp.max;
            const conMod = (actor as any).abilities.con.mod;
            const level = (actor as any).level;
            const maxRestored = Math.max(conMod, 1) * level;
            const hpLost = maxHp - currentHp;
            const hpRestored = hpLost >= maxRestored ? maxRestored : hpLost;
            if (hpRestored > 0) {
                await actor.update({'system.attributes.hp.value': currentHp + hpRestored});
            }
        }
    }
    await removeEffects(actors, activitySourceIds);
    /* eslint-disable @typescript-eslint/no-explicit-any */
    await (game.pf2e as any).actions.restForTheNight(actors);
}

export function calculateRestSeconds(partySize: number): number {
    if (partySize < 2) {
        return 8 * 3600;
    } else {
        let seconds = 8 * 3600 + Math.floor(8 * 3600 / (partySize - 1));
        // round up seconds to next minute
        if (seconds % 60 !== 0) {
            seconds += 60 - (seconds % 60);
        }
        return seconds;
    }
}

export function calculateDailyPreparationsSeconds(gunsToClean: number): number {
    return gunsToClean === 0 ? 30 * 60 : Math.ceil(gunsToClean / 4) * 3600;
}
