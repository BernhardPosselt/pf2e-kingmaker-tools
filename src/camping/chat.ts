import {getActorByUuid} from './actor';
import {isFirstGm, postChatMessage} from '../utils';
import {getCamping, getCampingActor} from './storage';
import {getEncounterDC, rollRandomEncounter} from './random-encounters';
import {
    ActorAndIngredients,
    addDiscoverSpecialMealResult,
    addHuntAndGatherResult,
    FoodAmount,
    getIngredientList,
} from './eating';


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
    const critFailUuids = element.dataset.critFailUuids?.split(',') ?? [];
    const recipe = element.dataset.recipe || null;
    if (actorAndIngredients) {
        if (isFirstGm(game)) {
            await addDiscoverSpecialMealResult(game, actorAndIngredients, recipe, critFailUuids);
        } else {
            game.socket?.emit('module.pf2e-kingmaker-tools', {
                action: 'addDiscoverSpecialMealResult',
                data: {
                    actorUuid: actorAndIngredients.actor.uuid,
                    specialIngredients: actorAndIngredients.specialIngredients,
                    basicIngredients: actorAndIngredients.basicIngredients,
                    critFailUuids,
                    recipe,
                },
            });
        }
    }
}

export async function addIngredients(game: Game, element: HTMLElement): Promise<void> {
    const actorAndIngredients = await parseIngredientButton(element);
    if (actorAndIngredients) {
        if (isFirstGm(game)) {
            await addHuntAndGatherResult(game, actorAndIngredients);
        } else {
            game.socket?.emit('module.pf2e-kingmaker-tools', {
                action: 'addHuntAndGatherResult',
                data: {
                    actorUuid: actorAndIngredients.actor.uuid,
                    specialIngredients: actorAndIngredients.specialIngredients,
                    basicIngredients: actorAndIngredients.basicIngredients,
                },
            });
        }
    }
}


export async function postHuntAndGatherResult(actor: Actor, food: FoodAmount): Promise<void> {
    const basicIngredients = food.basicIngredients;
    const specialIngredients = food.specialIngredients;
    const ingredients = getIngredientList(basicIngredients, specialIngredients);
    const content = `
        <p><b>${actor.name}</b>: Add:</p>
        <ul>${ingredients}</ul>
        <button 
            type="button" 
            class="km-add-food" 
            data-actor-uuid="${actor.uuid}"
            data-basic-ingredients="${basicIngredients}"
            data-special-ingredients="${specialIngredients}"
        >Apply</button>
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
        <p>${recipe ? `Learn ${recipe} and remove` : 'Remove'}${critFailUuids.length > 0 ? ' and suffer the Critical Failure effect' : ''}:</p>
        <ul>${getIngredientList(basicIngredients, specialIngredients)}</ul>
        <button 
            type="button" 
            class="km-add-recipe" 
            data-actor-uuid="${actor.uuid}"
            data-basic-ingredients="${basicIngredients}"
            data-special-ingredients="${specialIngredients}"
            ${recipe ? `data-recipe="${recipe}"` : ''}
            data-crit-fail-uuids="${critFailUuids.join(',')}"
        >Apply</button>
    `;
    await ChatMessage.create({content});
}

export async function checkRandomEncounterMessage(): Promise<void> {
    await postChatMessage('<button class="km-random-encounter" type="button">Check for Random Encounter</button>', 'blindroll');
}

async function checkForRandomEncounter(game: Game): Promise<void> {
    const actor = getCampingActor(game);
    if (actor) {
        const current = getCamping(actor);
        const dc = getEncounterDC(current, game);
        await rollRandomEncounter(game, current.currentRegion, dc, false);
    }
}

export function bindCampingChatEventListeners(game: Game): void {
    const chatLog = $('#chat-log');
    chatLog.on('click', '.km-add-food', (event) => {
        const actor = getCampingActor(game);
        if (actor) {
            return addIngredients(game, event.currentTarget);
        }
    });
    chatLog.on('click', '.km-add-recipe', (event) => {
        const actor = getCampingActor(game);
        if (actor) {
            return addRecipe(game, event.currentTarget);
        }
    });
    chatLog.on('click', '.km-random-encounter', () => {
        const actor = getCampingActor(game);
        if (actor) {
            return checkForRandomEncounter(game);
        }
    });
}
