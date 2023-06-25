import {getAllMealEffectUuids, getMealEffectUuids, RecipeData} from './recipes';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {ActivityEffect, ActivityOutcome, CampingActivityData, CampingActivityName} from './activities';
import {groupBy} from '../utils';
import {getActorEffectsByUuid, getEffectsByUuid, getItemsByUuid, getItemSourceIds, removeActorsEffects} from './actor';

async function addActorRecipeEffect(
    actor: Actor,
    recipe: RecipeData,
    degree: StringDegreeOfSuccess,
    favoriteMeal: string | undefined,
): Promise<void> {
    const effectUuids = getMealEffectUuids(recipe, favoriteMeal, degree);
    const items = await getItemsByUuid(effectUuids);
    await actor.createEmbeddedDocuments('Item', items.map(i => i.toObject()));
}

export async function syncActorMealEffects(
    actors: Actor[],
    recipe: RecipeData,
    recipes: RecipeData[],
    degree: StringDegreeOfSuccess,
    actorUuidAndFavoriteMeal: Map<string, string>,
): Promise<void> {
    const uuids = getAllMealEffectUuids(recipes);
    const sourceIds = await getItemSourceIds(new Set(uuids));
    await removeActorsEffects(actors, sourceIds);
    await Promise.all(actors.map(a => addActorRecipeEffect(a, recipe, degree, actorUuidAndFavoriteMeal.get(a.uuid))));
}

interface CampingActivityResult {
    result: StringDegreeOfSuccess;
    activityName: CampingActivityName;
    actor: Actor;
}

export interface SyncActorCampingEffectOptions {
    removeExisting: boolean;
    actors: Actor[];
    campingActivities: CampingActivityData[];
    actorCampingActivities: Map<string, CampingActivityResult>;
}

function getActivityOutcomeUuids(outcome: ActivityOutcome | undefined): string[] {
    if (outcome !== undefined && outcome.effectUuids !== undefined) {
        return outcome.effectUuids.map(e => e.uuid);
    }
    return [];
}

export function getCampingEffectUuids(activity: CampingActivityData): string[] {
    return [
        ...(getActivityOutcomeUuids(activity.criticalSuccess)),
        ...(getActivityOutcomeUuids(activity.success)),
        ...(getActivityOutcomeUuids(activity.failure)),
        ...(getActivityOutcomeUuids(activity.criticalFailure)),
        ...(activity.effectUuids === undefined ? [] : activity.effectUuids.map(e => e.uuid)),
    ];
}

export function getAllCampingEffectUuids(activities: CampingActivityData[]): string[] {
    return activities.flatMap(a => getCampingEffectUuids(a));
}

interface EffectsForActor {
    actor: Actor;
    effectUuids: string[];
}

function getActivityEffectsByResult(data: CampingActivityData | undefined, result: StringDegreeOfSuccess): ActivityEffect[] {
    return [
        ...(data?.effectUuids ?? []),
        ...(data?.[result]?.effectUuids ?? []),
    ];
}

function getEffectUuidsForActors(options: SyncActorCampingEffectOptions): EffectsForActor[] {
    const actorActivityDataByName = groupBy(options.campingActivities, a => a.name);
    const allEffects = Array.from(options.actorCampingActivities.values())
        .map(e => {
            const data = actorActivityDataByName.get(e.activityName)?.[0];
            const effects = getActivityEffectsByResult(data, e.result);
            return {actor: e.actor, effects};
        });
    const effectsUuidsForAllActors = allEffects
        .flatMap(e => e.effects)
        .filter(e => e?.targetAll !== false)
        .map(e => e.uuid);
    const effectsForActors = allEffects
        .map(e => {
            return {actor: e.actor, effects: e.effects.filter(e => e.targetAll === false)};
        });
    const effectsByActor = groupBy(effectsForActors, e => e.actor);
    return Array.from(effectsByActor.entries())
        .map(([actor, e]) => {
            const effectUuids = e.flatMap(e => e.effects)
                .map(e => e.uuid);
            const x: EffectsForActor = {
                actor,
                effectUuids: [...effectsUuidsForAllActors, ...effectUuids],
            };
            return x;
        });
}

async function syncActorCampingEffects(e: EffectsForActor, uuids: Set<string>): Promise<void> {
    const existingEffects = await getActorEffectsByUuid(e.actor, uuids);
    const existingEffectIds = new Set(existingEffects.map(e => e.id));
    const effectsToSync = await getEffectsByUuid(e.effectUuids);
    const effectToSyncIds = new Set(effectsToSync.map(e => e.id));
    const effectsToAdd = effectsToSync.filter(e => !existingEffectIds.has(e.id));
    const effectsToRemove = existingEffects.filter(e => !effectToSyncIds.has(e.id));
    await e.actor.deleteEmbeddedDocuments('Item', effectsToRemove.map(e => e.id));
    await e.actor.createEmbeddedDocuments('Item', effectsToAdd.map(e => e.toObject()));
}

export async function syncActorsCampingEffects(options: SyncActorCampingEffectOptions): Promise<void> {
    const uuids = new Set(getAllCampingEffectUuids(options.campingActivities));
    if (options.removeExisting) {
        await removeActorsEffects(options.actors, await getItemSourceIds(uuids));
    }
    await Promise.all(getEffectUuidsForActors(options).map(e => syncActorCampingEffects(e, uuids)));
}