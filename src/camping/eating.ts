import {getConsumablesByUuid, isConsumableItem} from './actor';
import {basicIngredientUuid, rationUuid, specialIngredientUuid} from './data';
import {addIngredientsToActor} from './activities';
import {allRecipes, getKnownRecipes, RecipeData} from './recipes';
import {Camping} from './camping';
import {getRegionInfo} from './regions';

export async function getConsumableFromUuid(uuid: string): Promise<(Item & ConsumableItem) | null> {
    const item = await fromUuid(uuid) as Item | null;
    if (item && isConsumableItem(item)) {
        return item;
    }
    return null;
}

export async function getActorConsumables(actors: Actor[]): Promise<FoodAmount> {
    const rationSourceId = (await getConsumableFromUuid(rationUuid))?.sourceId;
    const specialIngredientsSourceId = (await getConsumableFromUuid(specialIngredientUuid))?.sourceId;
    const basicIngredientsId = (await getConsumableFromUuid(basicIngredientUuid))?.sourceId;
    const result: FoodAmount = {
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
        result.rations += ration ? ration.system.charges.value + (Math.max(0, ration.quantity - 1) * ration.system.charges.max) : 0;
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

export function calculateConsumedFood(actorConsumables: FoodAmount, foodCost: FoodCost): ConsumedFood {
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

export interface FoodAmount {
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

export async function removeFood(actors: Actor[], config: FoodAmount): Promise<void> {
    await removeRations(actors, config.rations);
    await removeItems(actors, config.basicIngredients, basicIngredientUuid);
    await removeItems(actors, config.specialIngredients, specialIngredientUuid);
}

export async function addFood(actors: Actor[], food: FoodAmount): Promise<void> {
    await Promise.all(actors.map(a => addIngredientsToActor(a, food)));
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
    const knownRecipes = getKnownRecipes(data);
    const chosenMeal = knownRecipes.includes(data.cooking.chosenMeal) ? data.cooking.chosenMeal : 'Basic Meal';
    const recipeData = getRecipeData(data);
    return recipeData.find(a => a.name === chosenMeal) ??
        recipeData.find(a => a.name === 'Basic Meal')!;
}

export function getRecipeData(data: Camping): RecipeData[] {
    return allRecipes.concat(data.cooking.homebrewMeals);
}

export async function subsist(game: Game, actor: Actor, region: string): Promise<void> {
    const {zoneDC} = getRegionInfo(game, region);
    game.pf2e.actions.subsist({
        actors: [actor],
        skill: 'survival',
        difficultyClass: zoneDC,
    });
}
