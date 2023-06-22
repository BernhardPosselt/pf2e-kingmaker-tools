import {allCampingActivities, CampingActivityData, CampingActivityName} from './activities';
import {getRegionInfo} from './regions';
import {getLevelBasedDC, slugify, unslugify} from '../utils';
import {DegreeOfSuccess, StringDegreeOfSuccess} from '../degree-of-success';
import {basicIngredientUuid, DcType, rationUuid, specialIngredientUuid} from './data';
import {allRecipes, RecipeData} from './recipes';
import {
    getConsumablesByUuid,
    getItemsBySourceId,
    hasItemByUuid,
    isConsumableItem,
    removeExpiredEffects,
    removeItemsBySourceId,
} from './actor';

export type RestRollMode = 'one' | 'none' | 'one-every-4-hours';

export interface CampingActivity {
    activity: CampingActivityName;
    actorUuid: string | null;
    result: StringDegreeOfSuccess | null,
    selectedSkill: string | null;
}

export type ChosenMeal = 'meal' | 'rationsOrSubsistence' | 'nothing';

export interface ActorMeal {
    actorUuid: string;
    favoriteMeal: string | null;
    chosenMeal: ChosenMeal;
}

export type CookingSkill = 'survival' | 'cooking';

export interface Cooking {
    knownRecipes: string[];
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    chosenMeal: string;
    cookingSkill: CookingSkill;
    actorMeals: ActorMeal[];
    homebrewMeals: RecipeData[];
    degreeOfSuccess: StringDegreeOfSuccess | null;
}


export interface Camping {
    actorUuids: string[];
    campingActivities: CampingActivity[];
    homebrewCampingActivities: CampingActivityData[];
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
            actorMeals: [],
            magicalSubsistenceAmount: 0,
            subsistenceAmount: 0,
            knownRecipes: ['Basic Meal', 'Hearty Meal'],
            homebrewMeals: [],
            cookingSkill: 'survival',
            degreeOfSuccess: null,
        },
        restRollMode: 'one',
        currentRegion: 'Rostland Hinterlands',
        dailyPrepsAtTime: game.time.worldTime,
        homebrewCampingActivities: [],
        encounterModifier: 0,
        gunsToClean: 0,
        watchSecondsElapsed: 0,
        lockedActivities: allCampingActivities
            .filter(a => a.isLocked)
            .map(a => a.name),
    };
}

export function getDC(game: Game, actor: Actor, dcType: DcType, region: string): number {
    if (dcType === 'zone') {
        return getRegionInfo(game, region).zoneDC;
    } else if (dcType === 'actorLevel') {
        return getLevelBasedDC(actor.level);
    } else {
        return dcType;
    }
}

export interface SkillCheckOptions {
    game: Game,
    actor: Actor,
    dc?: DcType,
    skill: string,
    secret?: boolean,
    activity?: string,
    region: string;
}

export async function rollCampingCheck(
    {
        game,
        actor,
        dc,
        skill,
        secret = false,
        region,
        activity,
    }: SkillCheckOptions): Promise<DegreeOfSuccess | null> {
    const rollData: RollOptions = {
        extraRollOptions: ['camping'],
    };
    if (activity) {
        rollData['extraRollOptions']?.push('action:' + slugify(activity));
    }
    if (dc) {
        rollData['dc'] = getDC(game, actor, dc, region);
    }
    if (secret) {
        rollData['rollMode'] = 'blindroll';
    }
    let result;
    const skills = actor.skills;
    const loreSkill = `${skill}-lore`;
    const skillToRoll = skill in skills ? skill : (loreSkill in skills ? loreSkill : null);
    if (skill === 'perception') {
        result = await actor.perception.roll(rollData);
    } else if (skillToRoll === null) {
        ui.notifications?.error(`Actor does not have skill ${unslugify(skill)}`);
        return null;
    } else {
        result = await skills[skill].roll(rollData);
    }
    return result?.degreeOfSuccess ?? null;
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
        const existingEffects = getItemsBySourceId(actor, 'effect', applicableSourceIds);
        const existingEffectIds = new Set(existingEffects.map(a => a.id) as string[]);
        const effectsToSync = (await Promise.all(uuids.map(uuid => fromUuid(uuid))) as (Item | null)[])
            .filter(eff => eff !== undefined && eff !== null) as Item[];
        const effectIdsToSync = new Set(effectsToSync.map(a => a.id) as string[]);

        const effectsToAdd = effectsToSync
            .filter(eff => !existingEffectIds.has(eff.sourceId))
            .map(eff => eff.toObject());
        const effectIdsToRemove = Array.from(existingEffects)
            .filter(eff => !effectIdsToSync.has(eff.id))
            .map(eff => eff.id);

        await actor.deleteEmbeddedDocuments('Item', effectIdsToRemove);
        await actor.createEmbeddedDocuments('Item', effectsToAdd);
    }
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
    activitySourceIds: Set<string>,
    data: Camping,
): Promise<void> {
    for (const actor of actors) {
        const healMoreHp = await hasItemByUuid(actor, 'effect', new Set([
            getRecipeData(data).find(r => r.name === 'Basic Meal')!.criticalSuccess!.effects![0].uuid!,
            getCampingActivityData(data).find(a => a.name === 'Dawnflower\'s Blessing')!.effectUuid!,
        ]));
        if (healMoreHp) {
            const currentHp = actor.attributes.hp.value;
            const maxHp = actor.attributes.hp.max;
            const conMod = actor.abilities.con.mod;
            const level = actor.level;
            const maxRestored = Math.max(conMod, 1) * level;
            const hpLost = maxHp - currentHp;
            const hpRestored = hpLost >= maxRestored ? maxRestored : hpLost;
            if (hpRestored > 0) {
                await actor.update({'system.attributes.hp.value': currentHp + hpRestored});
            }
        }
    }
    await removeItemsBySourceId(actors, 'effect', activitySourceIds);
    await game.pf2e.actions.restForTheNight(actors);
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

export async function getConsumableFromUuid(uuid: string): Promise<(Item & ConsumableItem) | null> {
    const item = await fromUuid(uuid) as Item | null;
    if (item && isConsumableItem(item)) {
        return item;
    }
    return null;
}

export async function getActorConsumables(actors: Actor[]): Promise<ActorConsumables> {
    const rationSourceId = (await getConsumableFromUuid(rationUuid))?.sourceId;
    const specialIngredientsSourceId = (await getConsumableFromUuid(specialIngredientUuid))?.sourceId;
    const basicIngredientsId = (await getConsumableFromUuid(basicIngredientUuid))?.sourceId;
    const result: ActorConsumables = {
        rations: 0,
        specialIngredients: 0,
        basicIngredients: 0,
    };
    for (const actor of actors) {
        const consumables = actor.itemTypes.consumable;
        const ration = consumables
            .find(c => c.sourceId === rationSourceId && rationSourceId !== undefined) as ConsumableItem | undefined;
        const specialIngredient = consumables
            .find(c => c.sourceId === specialIngredientsSourceId && specialIngredientsSourceId !== undefined) as ConsumableItem | undefined;
        const basicIngredient = consumables
            .find(c => c.sourceId === basicIngredientsId && basicIngredientsId !== undefined) as ConsumableItem | undefined;
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
}

interface FoodCost {
    recipeSpecialIngredientCost: number;
    recipeBasicIngredientCost: number;
    actorsConsumingRations: number;
    actorsConsumingMeals: number;
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
        actorsConsumingRations,
        actorsConsumingMeals,
    } = foodCost;
    const requiredRationsTotal = actorsConsumingMeals + actorsConsumingRations;
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
            value: recipeSpecialIngredientCost * actorsConsumingMeals,
            warning: recipeSpecialIngredientCost * actorsConsumingMeals > availableSpecialIngredients,
        },
        basicIngredients: {
            value: recipeBasicIngredientCost * actorsConsumingMeals,
            warning: recipeBasicIngredientCost * actorsConsumingMeals > availableBasicIngredients,
        },
    };
}

export interface RemoveFoodAmount {
    rations: number;
    specialIngredients: number;
    basicIngredients: number;
}

/**
 * Special case for rations because these are fucked up
 * @param actors
 * @param amount
 */
async function removeRations(actors: Actor[], amount: number): Promise<void> {
    let remainingToRemove = amount;
    for (const actor of actors) {
        if (remainingToRemove > 0) {
            const updates = [];
            const rations = await getConsumablesByUuid(actor, new Set([rationUuid]));
            for (const ration of rations) {
                if (remainingToRemove > 0) {
                    const system = ration.system;
                    const quantity = ration.quantity;
                    const charges = system.charges.value;
                    const quantitiesOf7 = Math.max(0, quantity - 1);
                    const available = quantitiesOf7 * 7 + charges;
                    if (remainingToRemove >= available) {
                        await ration.delete();
                        remainingToRemove -= available;
                    } else {
                        const leftOver = available - remainingToRemove;
                        const leftOverCharges = leftOver % 7;
                        const leftOverQuantity = Math.ceil(leftOver / 7);
                        updates.push({
                            _id: ration.id,
                            'system.quantity': leftOverQuantity,
                            'system.charges.value': leftOverCharges === 0 ? 7 : leftOverCharges,
                        });
                        remainingToRemove = 0;
                    }
                }
            }
            if (updates.length > 0) {
                await actor.updateEmbeddedDocuments('Item', updates);
            }
        }
    }
}

async function removeItems(actors: Actor[], amount: number, uuid: string): Promise<void> {
    let remainingToRemove = amount;
    for (const actor of actors) {
        if (remainingToRemove > 0) {
            const updates = [];
            const items = await getConsumablesByUuid(actor, new Set([uuid]));
            for (const item of items) {
                if (remainingToRemove > 0) {
                    const quantity = item.quantity;
                    if (remainingToRemove >= quantity) {
                        await item.delete();
                        remainingToRemove -= quantity;
                    } else {
                        const leftOverQuantity = quantity - remainingToRemove;
                        updates.push({
                            _id: item.id,
                            'system.quantity': leftOverQuantity,
                        });
                        remainingToRemove = 0;
                    }
                }
            }
            if (updates.length > 0) {
                await actor.updateEmbeddedDocuments('Item', updates);
            }
        }
    }
}

export async function removeFood(actors: Actor[], config: RemoveFoodAmount): Promise<void> {
    await removeRations(actors, config.rations);
    await removeItems(actors, config.basicIngredients, basicIngredientUuid);
    await removeItems(actors, config.specialIngredients, specialIngredientUuid);
}

export function getCookingActorUuid(data: Camping): string | null {
    return data.campingActivities
        .find(a => a.activity === 'Cook Meal')?.actorUuid ?? null;
}

export async function getCookingActorByUuid(data: Camping): Promise<Actor | null> {
    const uuid = getCookingActorUuid(data);
    if (uuid) {
        return await fromUuid(uuid) as Actor | null;
    }
    return null;
}

export function getChosenMealData(data: Camping): RecipeData {
    const knownRecipes = data.cooking.knownRecipes;
    const chosenMeal = knownRecipes.includes(data.cooking.chosenMeal) ? data.cooking.chosenMeal : 'Basic Meal';
    const recipeData = getRecipeData(data);
    return recipeData.find(a => a.name === chosenMeal) ??
        recipeData.find(a => a.name === 'Basic Meal')!;
}

export function getRecipeData(data: Camping): RecipeData[] {
    return allRecipes.concat(data.cooking.homebrewMeals);
}

export function getCampingActivityData(current: Camping): CampingActivityData[] {
    return allCampingActivities.concat(current.homebrewCampingActivities);
}

export async function subsist(game: Game, actor: Actor, region: string): Promise<void> {
    const {zoneDC} = getRegionInfo(game, region);
    game.pf2e.actions.subsist({
        actors: [actor],
        skill: 'survival',
        difficultyClass: zoneDC,
    });
}

const combatEffects: Partial<Record<CampingActivityName, string>> = {
    'Enhance Weapons': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.ZKJlIqyFgbKDACnG]{Enhance Weapons}',
    'Set Traps': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.PSBOS7ZEl9RGWBqD]{Set Traps}',
    'Undead Guardians': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.KysTaC245mOnSnmE]{Undead Guardians}',
    'Water Hazards': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.LN6mH7Muj4hgvStt]{Water Hazards}',
};

export function getCombatEffects(data: Camping): Partial<Record<CampingActivityName, string>> {
    const result: Partial<Record<CampingActivityName, string>> = {};
    data.campingActivities.forEach(a => {
        const activityName = a.activity;
        if (activityName in combatEffects && a.actorUuid) {
            result[activityName] = combatEffects[activityName];
        }
    });
    return result;
}
