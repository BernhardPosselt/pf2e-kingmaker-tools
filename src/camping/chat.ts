import {addFood, FoodAmount, removeFood} from './camping';
import {getActorByUuid, getActorsByUuid, getItemsByUuid} from './actor';
import {addOf} from '../utils';
import {getCamping, getCampingActor, saveCamping} from './storage';

interface ActorAndIngredients {
    specialIngredients: number;
    basicIngredients: number;
    actor: Actor;
}

async function parseIngredientButton(element: HTMLElement): Promise<ActorAndIngredients | null> {
    const actorUuid = element.dataset.actorUuid ?? '';
    const specialIngredients = parseInt(element.dataset.specialIngredients ?? '0');
    const basicIngredients = parseInt(element.dataset.basicIngredients ?? '0');
    const actor = await getActorByUuid(actorUuid);
    if (actor) {
        return {specialIngredients, basicIngredients, actor};
    }
    return null;
}

export async function addRecipe(game: Game, element: HTMLElement): Promise<void> {
    const actorAndIngredients = await parseIngredientButton(element);
    const critFailUuids = JSON.parse(element.dataset.critFailUuids ?? '[]');
    const recipe = element.dataset.recipe || null;
    const campingActor = getCampingActor(game);
    if (actorAndIngredients && campingActor) {
        const camping = getCamping(campingActor);
        const actors = await getActorsByUuid(camping.actorUuids);
        const actor = actorAndIngredients.actor;
        const basicIngredients = actorAndIngredients.basicIngredients;
        const specialIngredients = actorAndIngredients.specialIngredients;
        await removeFood(actors, {
            specialIngredients,
            basicIngredients,
            rations: 0,
        });
        const itemsToAdd = (await getItemsByUuid(critFailUuids)).map(i => i.toObject());
        if (itemsToAdd.length > 0) {
            await actor.createEmbeddedDocuments('Item', itemsToAdd);
        }
        const content = `<p>Removed:</p>
        <ul>
            <li><b>Basic Ingredients</b>: ${basicIngredients}</li>
            <li><b>Special Ingredients</b>: ${specialIngredients}</li>
        </ul>
        ${recipe || itemsToAdd.length > 0 ? `
            <p>Added:</p>
            <ul>
                ${recipe === null ? '' : `<li><b>Recipe</b>: ${recipe}</li>`}
                ${itemsToAdd.length > 0 ? '' : `<li><b>Meal Effects</b>: ${itemsToAdd.map(i => i.name).join(', ')}</li>`}
            </ul>
        ` : ''}
        `;
        if (recipe) {
            camping.cooking.knownRecipes.push(recipe);
            await saveCamping(campingActor, camping);
        }
        await ChatMessage.create({content});
    }
}

export async function addIngredients(element: HTMLElement): Promise<void> {
    const actorAndIngredients = await parseIngredientButton(element);
    if (actorAndIngredients) {
        const actor = actorAndIngredients.actor;
        const specialIngredients = actorAndIngredients.specialIngredients;
        const basicIngredients = actorAndIngredients.basicIngredients;
        await addFood([actorAndIngredients.actor], {
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

export async function postHuntAndGatherResult(actor: Actor, food: FoodAmount): Promise<void> {
    const basicIngredients = food.basicIngredients;
    const specialIngredients = food.specialIngredients;
    const content = `
        <p><b>${actor.name}</b>: Add ${basicIngredients} basic ingredients, ${specialIngredients} special ingredients:</p>
        <button 
            type="button" 
            class="km-add-food" 
            data-actor-uuid="${actor.uuid}"
            data-basic-ingredients="${basicIngredients}"
            data-special-ingredients="${specialIngredients}"
        >Add</button>
    `;
    await ChatMessage.create({content});
}

export async function postDiscoverSpecialMealResult(
    actor: Actor,
    cost: FoodAmount,
    recipe: string | null,
    critFailUuids: string[],
): Promise<void> {
    const basicIngredients = cost.basicIngredients;
    const specialIngredients = cost.specialIngredients;
    const content = `
        <p><b>${actor.name}</b>: Learn ${recipe} and remove ${basicIngredients} basic ingredients, ${specialIngredients} special ingredients:</p>
        <button 
            type="button" 
            class="km-add-recipe" 
            data-actor-uuid="${actor.uuid}"
            data-basic-ingredients="${basicIngredients}"
            data-special-ingredients="${specialIngredients}"
            data-recipe="${recipe}"
            data-crit-fail-uuids="${JSON.stringify(critFailUuids)}"
        >Add</button>
    `;
    await ChatMessage.create({content});
}

export function bindCampingChatEventListeners(game: Game): void {
    const actor = getCampingActor(game);
    if (actor) {
        const chatLog = $('#chat-log');
        chatLog.on('click', '.km-add-food', (event) => addIngredients(event.currentTarget));
        chatLog.on('click', '.km-add-recipe', (event) => addRecipe(game, event.currentTarget));
    }
}