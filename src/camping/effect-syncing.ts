import {getAllMealEffectUuids, getMealEffectUuids, RecipeData} from './recipes';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {ActivityEffect, ActivityOutcome, CampingActivityData, CampingActivityName} from './activities';
import {groupBySingle, isGm} from '../utils';
import {
    getActorByUuid,
    getActorEffectsByUuid,
    getActorsByUuid,
    getEffectsByUuid,
    getItemsByUuid,
    getItemSourceIds,
    removeActorsEffects,
} from './actor';
import {Camping, getCampingActivityData, getRecipeData} from './camping';
import {getCamping, getCampingActor} from './storage';

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

export interface SyncMealEffectOptions {
    actors: Actor[];
    recipe: RecipeData;
    recipes: RecipeData[];
    degree: StringDegreeOfSuccess;
    actorUuidAndFavoriteMeal: Map<string, string>;
}

export async function syncActorMealEffects(options: SyncMealEffectOptions): Promise<void> {
    const uuids = getAllMealEffectUuids(options.recipes);
    const sourceIds = await getItemSourceIds(new Set(uuids));
    await removeActorsEffects(options.actors, sourceIds);
    await Promise.all(options.actors.map(a =>
        addActorRecipeEffect(a, options.recipe, options.degree, options.actorUuidAndFavoriteMeal.get(a.uuid))));
}

interface CampingActivityResult {
    result: StringDegreeOfSuccess | null;
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

function getActivityEffectsByResult(data: CampingActivityData | undefined, result: StringDegreeOfSuccess | null): ActivityEffect[] {
    return [
        ...(data?.effectUuids ?? []),
        ...(result === null ? [] : (data?.[result]?.effectUuids ?? [])),
    ];
}

function getEffectUuidsForActors(options: SyncActorCampingEffectOptions): EffectsForActor[] {
    const actorActivityDataByName = groupBySingle(options.campingActivities, a => a.name);
    const allEffects = Array.from(options.actorCampingActivities.values())
        .map(e => {
            const data = actorActivityDataByName.get(e.activityName);
            const effects = getActivityEffectsByResult(data, e.result);
            return {actor: e.actor, effects};
        });
    const effectsUuidsForAllActors = allEffects
        .flatMap(e => e.effects)
        .filter(e => e?.targetAll !== false)
        .map(e => e.uuid);
    const effectsForActors = new Map();
    allEffects.forEach(e => {
        const actorUuid = e.actor.uuid;
        const uuids = e.effects
            .filter(e => e.targetAll === false)
            .map(e => e.uuid);
        effectsForActors.set(actorUuid, [...(effectsForActors.get(actorUuid) ?? []), ...uuids]);
    });
    return options.actors.map(a => {
        return {
            actor: a,
            effectUuids: [...effectsUuidsForAllActors, ...(effectsForActors.get(a.uuid) ?? [])],
        };
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
    console.log(options);
    const uuids = new Set(getAllCampingEffectUuids(options.campingActivities));
    if (options.removeExisting) {
        await removeActorsEffects(options.actors, await getItemSourceIds(uuids));
    }
    await Promise.all(getEffectUuidsForActors(options).map(e => syncActorCampingEffects(e, uuids)));
}

export abstract class DiffListener {
    protected constructor(protected action: string, protected game: Game) {
    }

    async shouldFire(previous: Camping, update: Partial<Camping>): Promise<void> {
        if (this.shouldFireChange(previous, update)) {
            console.log('Firing Sync Event: ' + this.action, previous, update);
            // GM has all of the permissions so change can be immediately made
            if (isGm(this.game)) {
                const current = this.getCurrent();
                if (current) {
                    await this.onReceive(current);
                }
            } else {
                // otherwise we're a player and need to send the event to the GM in index.ts
                this.game.socket?.emit('module.pf2e-kingmaker-tools', {
                    action: this.action,
                });
            }
        }
    }

    protected getCurrent(): Camping | null {
        const actor = getCampingActor(this.game);
        if (actor) {
            return getCamping(actor);
        }
        return null;
    }

    canHandle(action: string): boolean {
        return this.action === action;
    }

    async onReceive(camping: Camping): Promise<void> {
        console.log('Receiving update event: ' + this.action, camping);
    }

    protected abstract shouldFireChange(previous: Camping, update: Partial<Camping>): boolean;
}

export class CampingActivitiesListener extends DiffListener {
    constructor(game: Game) {
        super('syncCampingEffects', game);
    }

    protected shouldFireChange(previous: Camping, update: Partial<Camping>): boolean {
        const diff = diffObject(previous, update) as Partial<Camping>;
        return 'campingActivities' in diff
            && update?.campingActivities?.find(a => a.activity === 'Prepare Campsite'
                && a.result !== null
                && a.result !== 'criticalFailure'
            ) !== undefined;
    }

    async onReceive(camping: Camping): Promise<void> {
        await super.onReceive(camping);
        const activities = (await Promise.all(camping.campingActivities
            .filter(a => a.actorUuid !== null)
            .map(async (a) => {
                const result: CampingActivityResult = {
                    result: a.result,
                    activityName: a.activity,
                    actor: await getActorByUuid(a.actorUuid!) as Actor,
                };
                return [a.activity, result] as [CampingActivityName, CampingActivityResult];
            })
        )).filter(([, result]) => result.actor !== null);
        const actorCampingActivities = new Map<CampingActivityName, CampingActivityResult>(activities);
        await syncActorsCampingEffects({
            actors: await getActorsByUuid(camping.actorUuids),
            campingActivities: getCampingActivityData(camping),
            removeExisting: true,
            actorCampingActivities,
        });
    }
}


export class ClearCampingActivitiesListener extends DiffListener {
    constructor(game: Game) {
        super('clearCampingEffects', game);
    }

    protected shouldFireChange(previous: Camping, update: Partial<Camping>): boolean {
        const diff = diffObject(previous, update) as Partial<Camping>;
        return 'campingActivities' in diff
            && update?.campingActivities?.find(a => a.activity === 'Prepare Campsite'
                && (a.result === null || a.result === 'criticalFailure')
            ) !== undefined;
    }

    async onReceive(camping: Camping): Promise<void> {
        await super.onReceive(camping);
        const actors = await getActorsByUuid(camping.actorUuids);
        const campingActivityData = getCampingActivityData(camping);
        const campingEffectUuids = getAllCampingEffectUuids(campingActivityData);
        await removeActorsEffects(actors, await getItemSourceIds(new Set(campingEffectUuids)));
    }

}

export class MealListener extends DiffListener {
    constructor(game: Game) {
        super('syncMealEffects', game);
    }

    protected shouldFireChange(previous: Camping, update: Partial<Camping>): boolean {
        const diff = diffObject(previous, update) as Partial<Camping>;
        return 'cooking' in diff
            && update?.cooking?.degreeOfSuccess !== null;
    }

    async onReceive(camping: Camping): Promise<void> {
        await super.onReceive(camping);
        const actorUuidAndMeal = groupBySingle(camping.cooking.actorMeals, a => a.actorUuid);
        const actorUuidAndFavoriteMeal = new Map();
        Array.from(actorUuidAndMeal.values())
            .filter(a => a.favoriteMeal !== null)
            .forEach(a => actorUuidAndFavoriteMeal.set(a.actorUuid, a.favoriteMeal));
        const recipes = getRecipeData(camping);
        const recipe = recipes.find(r => r.name === camping.cooking.chosenMeal);
        const degree = camping.cooking.degreeOfSuccess;
        const actors = await getActorsByUuid(camping.actorUuids);
        if (degree !== null && recipe !== undefined) {
            await syncActorMealEffects({
                actors,
                degree,
                recipe,
                recipes,
                actorUuidAndFavoriteMeal,
            });
        } else {
            const recipeEffectUuids = getAllMealEffectUuids(recipes);
            await removeActorsEffects(actors, await getItemSourceIds(new Set(recipeEffectUuids)));
        }
    }
}

export function getDiffListeners(game: Game): DiffListener[] {
    return [
        new ClearCampingActivitiesListener(game),
        new CampingActivitiesListener(game),
        new MealListener(game),
    ];
}
