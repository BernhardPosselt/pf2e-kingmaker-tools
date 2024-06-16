import {getActorsByUuid, getConsumablesByUuid, getItemsByUuid, isConsumableItem} from './actor';
import {basicIngredientUuid, rationUuid, specialIngredientUuid} from './data';
import {addIngredientsToActor} from './activities';
import {allRecipes, getKnownRecipes, RecipeData} from './recipes';
import {Camping, getHuntAndGatherActor} from './camping';
import {getRegionInfo} from './regions';
import {addOf, sum} from '../utils';
import {getCamping, getCampingActor, saveCamping} from './storage';

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
        const rations = sum((consumables
            .filter(c => c.sourceId === rationSourceId && rationSourceId !== undefined) as unknown as ConsumableItem[])
            .map(c => c.system.uses.value + (Math.max(0, c.quantity - 1) * c.system.uses.max)));
        const specialIngredients = sum((consumables
            .filter(c => c.sourceId === specialIngredientsSourceId && specialIngredientsSourceId !== undefined) as unknown as ConsumableItem[])
            .map(c => c.quantity));
        const basicIngredients = sum((consumables
            .filter(c => c.sourceId === basicIngredientsId && basicIngredientsId !== undefined) as unknown as ConsumableItem[])
            .map(c => c.quantity));
        result.rations += rations;
        result.basicIngredients += basicIngredients;
        result.specialIngredients += specialIngredients;
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

export function canCook(camping: Camping): boolean {
    const hasPreparedCampsite = camping.campingActivities
        .find(a => a.activity === 'Prepare Campsite'
            && (a.result === 'success' || a.result === 'criticalSuccess' || a.result === 'failure')) !== undefined;
    const actorIsCooking = camping.campingActivities
        .find(a => a.activity === 'Cook Meal' && a.actorUuid) !== undefined;
    return hasPreparedCampsite && actorIsCooking;
}

export function calculateConsumedFood(canCook: boolean, actorConsumables: FoodAmount, foodCost: FoodCost): ConsumedFood {
    const availableBasicIngredients = actorConsumables.basicIngredients;
    const availableSpecialIngredients = actorConsumables.specialIngredients;
    const availableRations = actorConsumables.rations;
    const {
        availableMagicalSubsistence,
        availableSubsistence,
        actorsConsumingRations,
        actorsConsumingMeals,
    } = foodCost;
    const recipeSpecialIngredientCost = canCook ? foodCost.recipeSpecialIngredientCost : 0;
    const recipeBasicIngredientCost = canCook ? foodCost.recipeBasicIngredientCost : 0;
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
                    const charges = system.uses.value;
                    const max = system.uses.max;
                    const quantitiesOf7 = Math.max(0, quantity - 1);
                    const available = quantitiesOf7 * max + charges;
                    if (remainingToRemove >= available) {
                        await ration.delete();
                        remainingToRemove -= available;
                    } else {
                        const leftOver = available - remainingToRemove;
                        const leftOverCharges = leftOver % max;
                        const leftOverQuantity = Math.ceil(leftOver / max);
                        updates.push({
                            _id: ration.id,
                            'system.quantity': leftOverQuantity,
                            'system.uses.value': leftOverCharges === 0 ? max : leftOverCharges,
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

export interface ActorAndIngredients {
    specialIngredients: number;
    basicIngredients: number;
    actor: Actor;
}

export function getIngredientList(basicIngredients: number, specialIngredients: number): string {
    const result = [];
    if (basicIngredients) result.push(`<b>Basic Ingredients</b>: ${basicIngredients}`);
    if (specialIngredients) result.push(`<b>Special Ingredients</b>: ${specialIngredients}`);
    return result
        .map(a => `<li>${a}</li>`)
        .join('');
}

export async function addDiscoverSpecialMealResult(
    game: Game,
    actorAndIngredients: ActorAndIngredients,
    recipe: string | null,
    critFailUuids: string[],
): Promise<void> {
    const campingActor = getCampingActor(game);
    if (campingActor) {
        const camping = getCamping(campingActor);
        const actors = await getActorsByUuid(new Set(camping.actorUuids));
        const actor = actorAndIngredients.actor;
        const basicIngredients = actorAndIngredients.basicIngredients;
        const specialIngredients = actorAndIngredients.specialIngredients;
        await removeFood(actors, {
            specialIngredients,
            basicIngredients,
            rations: 0,
        });
        const itemsToAdd = (await getItemsByUuid(new Set(critFailUuids))).map(i => i.toObject());
        if (itemsToAdd.length > 0) {
            await actor.createEmbeddedDocuments('Item', itemsToAdd);
        }
        const content = `<p>Removed:</p>
        <ul>${getIngredientList(basicIngredients, specialIngredients)}</ul>
        ${recipe || itemsToAdd.length > 0 ? `
            <p>Added:</p>
            <ul>
                ${recipe ? `<li><b>Recipe</b>: ${recipe}</li>` : ''}
                ${itemsToAdd.length === 0 ? '' : `<li><b>Meal Effects</b>: ${itemsToAdd.map(i => i.name).join(', ')}</li>`}
            </ul>
        ` : ''}
        `;
        if (recipe) {
            camping.cooking.knownRecipes = Array.from(new Set([...camping.cooking.knownRecipes, recipe]));
            await saveCamping(game, campingActor, camping);
        }
        await ChatMessage.create({content});
    }
}

export async function addHuntAndGatherResult(game: Game, actorAndIngredients: ActorAndIngredients): Promise<void> {
    const campingActor = getCampingActor(game);
    if (campingActor) {
        const camping = getCamping(campingActor);
        const actor = (await getHuntAndGatherActor(game, camping)) || actorAndIngredients.actor;
        const specialIngredients = actorAndIngredients.specialIngredients;
        const basicIngredients = actorAndIngredients.basicIngredients;
        await addFood([actor], {
            specialIngredients,
            basicIngredients,
            rations: 0,
        });
        const content = `<p>Added to ${addOf(actor.name ?? actor.uuid)} inventory:</p>
        <ul>
            <li><b>Basic Ingredients</b>: ${basicIngredients}</li>
            <li><b>Special Ingredients</b>: ${specialIngredients}</li>
        </ul>
        `;
        await ChatMessage.create({content});
    }
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
        difficultyClass: {value: zoneDC},
    });
}
