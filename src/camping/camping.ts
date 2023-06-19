import {CampingActivityName, getCampingActivityData} from './activities';
import {getRegionInfo} from './regions';
import {getLevelBasedDC, slugify} from '../utils';
import {DegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {basicIngredientUuid, DcType, rationUuid, specialIngredientUuid} from './data';
import {getRecipeData, RecipeData} from './recipes';

export type RestRollMode = 'one' | 'none' | 'one-every-4-hours';

export interface CampingActivity {
    activity: CampingActivityName;
    actorUuid: string | null;
    result: StringDegreeOfSuccess | null,
    selectedSkill: string | null;
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
    homebrewMeals: RecipeData[];
    degreeOfSuccess: StringDegreeOfSuccess | null;
}


export interface Camping {
    actorUuids: string[];
    campingActivities: CampingActivity[];
    lockedActivities: CampingActivityName[];
    cooking: Cooking;
    watchSecondsElapsed: number;
    gunsToClean: number;
    dailyPrepsAtTime: number;
    currentRegion: string;
    encounterModifier: number;
    restRollMode: RestRollMode;
}

export function getDefaultConfiguration(game: Game): Camping {
    return {
        actorUuids: [],
        campingActivities: [],
        cooking: {
            chosenMeal: 'Basic Meal',
            servings: 0,
            actorMeals: [],
            magicalSubsistenceAmount: 0,
            subsistenceAmount: 0,
            knownRecipes: ['Basic Meal', 'Hearty Meal'],
            homebrewMeals: [],
            degreeOfSuccess: null,
        },
        restRollMode: 'one',
        currentRegion: 'Rostland Hinterlands',
        dailyPrepsAtTime: game.time.worldTime,
        encounterModifier: 0,
        gunsToClean: 0,
        watchSecondsElapsed: 0,
        lockedActivities: getCampingActivityData()
            .filter(a => a.isLocked)
            .map(a => a.name),
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
            getRecipeData().find(r => r.name === 'Basic Meal')!.criticalSuccess!.effectUuid!,
            getCampingActivityData().find(a => a.name === 'Dawnflower\'s Blessing')!.effectUuid!,
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

export interface ActorConsumables {
    rations: number;
    specialIngredients: number;
    basicIngredients: number;
}

export async function getActorConsumables(actors: Actor[]): Promise<ActorConsumables> {
    const rationSourceId = ((await fromUuid(rationUuid)) as any).sourceId;
    const specialIngredientsSourceId = ((await fromUuid(specialIngredientUuid)) as any).sourceId;
    const basicIngredientsId = ((await fromUuid(basicIngredientUuid)) as any).sourceId;
    const result: ActorConsumables = {
        rations: 0,
        specialIngredients: 0,
        basicIngredients: 0,
    };
    for (const actor of actors) {
        const consumables = actor.itemTypes.consumable;
        const ration = consumables.find(c => (c as any).sourceId === rationSourceId) as any | undefined;
        const specialIngredient = consumables.find(c => (c as any).sourceId === specialIngredientsSourceId) as any | undefined;
        const basicIngredient = consumables.find(c => (c as any).sourceId === basicIngredientsId) as any | undefined;
        result.rations += ration ? ration.system.charges.value * ration.quantity : 0;
        result.basicIngredients += basicIngredient ? basicIngredient.quantity : 0;
        result.specialIngredients += specialIngredient ? specialIngredient.quantity : 0;
    }
    return result;
}


interface ConsumedResource {
    value: number;
    warning: boolean;
}

export interface ConsumedFood {
    magicalSubsistence: number;
    subsistence: number;
    rations: ConsumedResource;
    basicIngredients: ConsumedResource;
    specialIngredients: ConsumedResource;
    meals: ConsumedResource;
}

interface FoodCost {
    recipeSpecialIngredientCost: number;
    recipeBasicIngredientCost: number;
    actorsConsumingRations: number;
    actorsConsumingMeals: number;
    mealServings: number;
    availableSubsistence: number;
    availableMagicalSubsistence: number;
}

export function calculateConsumedFood(actorConsumables: ActorConsumables, foodCost: FoodCost): ConsumedFood {
    const availableBasicIngredients = actorConsumables.basicIngredients;
    const availableSpecialIngredients = actorConsumables.specialIngredients;
    const availableRations = actorConsumables.rations;
    const {
        recipeSpecialIngredientCost,
        recipeBasicIngredientCost,
        availableMagicalSubsistence,
        availableSubsistence,
        mealServings,
        actorsConsumingRations,
        actorsConsumingMeals,
    } = foodCost;
    const requiredRationsTotal = mealServings + actorsConsumingRations;
    const availableRationsTotal = availableRations + availableSubsistence + availableMagicalSubsistence;

    const consumedRations = Math.max(0, requiredRationsTotal - availableSubsistence - availableMagicalSubsistence);
    const consumedSubsistence = Math.max(0, requiredRationsTotal - consumedRations - availableMagicalSubsistence);
    const consumedMagicalSubsistence = Math.max(0, requiredRationsTotal - consumedSubsistence - consumedRations);
    return {
        magicalSubsistence: consumedMagicalSubsistence,
        subsistence: consumedSubsistence,
        rations: {
            value: consumedRations,
            warning: (availableRationsTotal - requiredRationsTotal) < 0,
        },
        specialIngredients: {
            value: recipeSpecialIngredientCost * mealServings,
            warning: recipeSpecialIngredientCost * mealServings > availableSpecialIngredients,
        },
        basicIngredients: {
            value: recipeBasicIngredientCost * mealServings,
            warning: recipeBasicIngredientCost * mealServings > availableBasicIngredients,
        },
        meals: {
            value: actorsConsumingMeals,
            warning: actorsConsumingMeals > mealServings,
        },
    };
}
