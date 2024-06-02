import {getAllMealEffectUuids, getMealEffectUuids, RecipeData} from './recipes';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {ActivityEffect, ActivityOutcome, CampingActivityData, CampingActivityName} from './activities';
import {groupBySingle, isFirstGm} from '../utils';
import {
    getActorByUuid,
    getActorEffectsByUuid,
    getActorsByUuid,
    getEffectsByUuid,
    getItemsByUuid,
    removeActorsEffectsByUuid,
} from './actor';
import {Camping, getCampingActivityData} from './camping';
import {getCamping, getCampingActor} from './storage';
import {getRecipeData} from './eating';

async function addActorRecipeEffect(
    actor: Actor,
    recipe: RecipeData,
    degree: StringDegreeOfSuccess,
    favoriteMeal: string | undefined,
): Promise<void> {
    const effectUuids = getMealEffectUuids(recipe, favoriteMeal, degree);
    const items = await getItemsByUuid(new Set(effectUuids));
    await actor.createEmbeddedDocuments('Item', items.map(i => i.toObject()));
}

export interface SyncMealEffectOptions {
    actors: Actor[];
    recipe: RecipeData;
    recipes: RecipeData[];
    degree: StringDegreeOfSuccess;
    actorUuidsEatingMeal: Set<string>;
    actorUuidAndFavoriteMeal: Map<string, string>;
}

export async function syncActorMealEffects(options: SyncMealEffectOptions): Promise<void> {
    const uuids = getAllMealEffectUuids(options.recipes);
    await removeActorsEffectsByUuid(options.actors, new Set(uuids));
    await Promise.all(options.actors
        .filter(a => options.actorUuidsEatingMeal.has(a.uuid))
        .map(a => addActorRecipeEffect(a, options.recipe, options.degree, options.actorUuidAndFavoriteMeal.get(a.uuid))));
}

interface CampingActivityResult {
    result: StringDegreeOfSuccess | null;
    activityName: CampingActivityName;
    actor: Actor;
}

export interface SyncActorCampingEffectOptions {
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
    const effectsPerformedByActor = Array.from(options.actorCampingActivities.values())
        .map(e => {
            const data = actorActivityDataByName.get(e.activityName);
            const effects = getActivityEffectsByResult(data, e.result);
            return {actor: e.actor, effects};
        });
    const effectsForActors = new Map();
    const allActors = options.actors;
    const actorUuids = allActors.map(a => a.uuid);
    effectsPerformedByActor.forEach(actorAndEffect => {
        actorAndEffect.effects.forEach(effect => {
            actorUuids.forEach(actorUuid => {
                if (effect.target === 'all'
                    || effect.target === undefined
                    || (effect.target === 'self' && actorUuid === actorAndEffect.actor.uuid)
                    || (effect.target === 'allies' && actorUuid !== actorAndEffect.actor.uuid)) {
                    const existingEffects = effectsForActors.get(actorUuid) ?? [];
                    existingEffects.push(effect.uuid);
                    effectsForActors.set(actorUuid, existingEffects);
                }
            });
        });
    });
    return allActors.map(actor => {
        return {
            actor,
            effectUuids: [...(effectsForActors.get(actor.uuid) ?? [])],
        };
    });
}

async function syncActorCampingEffects(actor: Actor, effects: (Item & EffectItem)[], allUuids: Set<string>): Promise<void> {
    const effectNames = new Set(effects.map(e => e.name));
    const existingEffects = await getActorEffectsByUuid(actor, allUuids);
    const existingEffectNames = new Set(existingEffects.map(e => e.name));
    const effectsToAdd = effects.filter(e => !existingEffectNames.has(e.name));
    const effectsToRemove = existingEffects.filter(e => !effectNames.has(e.name)).map(e => e.id);
    await Promise.all([
        actor.createEmbeddedDocuments('Item', effectsToAdd.map(e => e.toObject())),
        actor.deleteEmbeddedDocuments('Item', effectsToRemove),
    ]);
}

export async function syncActorsCampingEffects(options: SyncActorCampingEffectOptions): Promise<void> {
    const uuids = new Set(getAllCampingEffectUuids(options.campingActivities));
    const effectUuidsForActors = getEffectUuidsForActors(options);
    await Promise.all(effectUuidsForActors
        .filter(a => a.actor.type === 'npc' || a.actor.type === 'character')
        .map(async effectsForActor => {
            const effects = await getEffectsByUuid(new Set(effectsForActor.effectUuids));
            await syncActorCampingEffects(effectsForActor.actor, effects, uuids);
        }));
}

export abstract class DiffListener {
    protected constructor(protected action: string, protected game: Game) {
    }

    async testFireChange(previous: Camping, update: Partial<Camping>): Promise<void> {
        if (this.shouldFireChange(previous, update)) {
            console.log('Firing Sync Event: ' + this.action, previous, update);
            await this.fireChange();
        }
    }

    async fireChange(): Promise<void> {
        // GM has all of the permissions so change can be immediately made
        if (isFirstGm(this.game)) {
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
        const diff = foundry.utils.diffObject(previous, update) as Partial<Camping>;
        return 'campingActivities' in diff
            && update?.campingActivities?.find(a => a.activity === 'Prepare Campsite'
                && a.result !== null
                && a.result !== 'criticalFailure',
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
            }),
        )).filter(([, result]) => result.actor !== null);
        const actorCampingActivities = new Map<CampingActivityName, CampingActivityResult>(activities);
        await syncActorsCampingEffects({
            actors: await getActorsByUuid(new Set(camping.actorUuids)),
            campingActivities: getCampingActivityData(camping),
            actorCampingActivities,
        });
    }
}


export class ClearCampingActivitiesListener extends DiffListener {
    constructor(game: Game) {
        super('clearCampingEffects', game);
    }

    protected shouldFireChange(previous: Camping, update: Partial<Camping>): boolean {
        const diff = foundry.utils.diffObject(previous, update) as Partial<Camping>;
        return 'campingActivities' in diff
            && update?.campingActivities?.find(a => a.activity === 'Prepare Campsite'
                && (a.result === null || a.result === 'criticalFailure'),
            ) !== undefined;
    }

    async onReceive(camping: Camping): Promise<void> {
        await super.onReceive(camping);
        const actors = await getActorsByUuid(new Set(camping.actorUuids));
        const campingActivityData = getCampingActivityData(camping);
        const campingEffectUuids = getAllCampingEffectUuids(campingActivityData);
        await removeActorsEffectsByUuid(actors, new Set(campingEffectUuids));
    }

}

export async function eat(game: Game, camping: Camping): Promise<void> {
    const actorUuidAndMeal = groupBySingle(camping.cooking.actorMeals, a => a.actorUuid);
    const actorUuidAndFavoriteMeal = new Map();
    Array.from(actorUuidAndMeal.values())
        .filter(a => a.favoriteMeal !== null)
        .forEach(a => actorUuidAndFavoriteMeal.set(a.actorUuid, a.favoriteMeal));
    const recipes = getRecipeData(camping);
    const recipe = recipes.find(r => r.name === camping.cooking.chosenMeal);
    const degree = camping.cooking.degreeOfSuccess;
    const actors = await getActorsByUuid(new Set(camping.actorUuids));
    if (degree !== null && recipe !== undefined) {
        const actorUuidsEatingMeal = new Set(camping.cooking.actorMeals
            .filter(a => a.chosenMeal === 'meal')
            .map(a => a.actorUuid));
        await syncActorMealEffects({
            actors,
            degree,
            recipe,
            recipes,
            actorUuidAndFavoriteMeal,
            actorUuidsEatingMeal,
        });
    } else {
        const recipeEffectUuids = getAllMealEffectUuids(recipes);
        await removeActorsEffectsByUuid(actors, new Set(recipeEffectUuids));
    }
}

export function getDiffListeners(game: Game): DiffListener[] {
    return [
        new ClearCampingActivitiesListener(game),
        new CampingActivitiesListener(game),
    ];
}
