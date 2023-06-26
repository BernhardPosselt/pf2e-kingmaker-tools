import {parseCheckbox, parseNumberInput, parseSelect, parseTextInput} from '../../utils';
import {MealEffect, Rarity, RecipeData} from '../recipes';
import {getEffectByUuid} from '../actor';

export interface AddRecipeOptions {
    onSubmit: (recipe: RecipeData) => Promise<void>;
    recipes: RecipeData[];
}

export function addRecipeDialog({onSubmit, recipes}: AddRecipeOptions): void {
    new Dialog({
        title: 'Add Recipe',
        content: `
        <div>
        <p>Hint: UUIDs can be found in the item's <b>Rules</b> tab</p>
        <form class="simple-dialog-form">
            <div>
                <label for="km-recipe-name">Name</label>
                <input type="text" name="name" id="km-recipe-name" placeholder="Unknown Meal">
            </div>
            <div>
                <label for="km-recipe-uuid">UUID</label>
                <input type="text" name="uuid" id="km-recipe-uuid">
            </div>
            <div>
                <label for="km-recipe-level">Level</label>
                <input type="number" name="level" id="km-recipe-level"  placeholder="0">
            </div>
            <div>
                <label for="km-recipe-rarity">Rarity</label>
                <select name="rarity">
                    <option value="common">Common</option>
                    <option value="uncommon">Uncommon</option>
                    <option value="rare">Rare</option>
                    <option value="unique">Unique</option>
                </select>
            </div>
            <div>
                <label for="km-recipe-cooking-lore-dc">Cooking Lore DC</label>
                <input type="number" name="cooking-lore-dc" id="km-recipe-cooking-lore-dc" placeholder="0">
            </div>
            <div>
                <label for="km-recipe-survival-dc">Survival DC</label>
                <input type="number" name="survival-dc" id="km-recipe-survival-dc" placeholder="0">
            </div>
            <div>
                <label for="km-recipe-basic">Basic Ingredients</label>
                <input type="number" name="basic-ingredients" id="km-recipe-basic" placeholder="0">
            </div>
            <div>
                <label for="km-recipe-special">Special Ingredients</label>
                <input type="number" name="special-ingredients" id="km-recipe-special" placeholder="0">
            </div>
            <div>
                <label for="km-recipe-cost">Cost</label>
                <input type="text" name="cost" id="km-recipe-cost" placeholder="0 gp">
            </div>
            
            <div>
                <label for="km-favorite-meal-effect-uuid">Favorite Meal: Effect UUID</label>
                <input type="text" name="favorite-meal-effect-uuid" id="km-favorite-meal-effect-uuid">
            </div>
            <div>
                <label for="km-favorite-meal-remove-rest">Favorite Meal: Remove Effect after Rest</label>
                <input type="checkbox" name="favorite-meal-remove-rest" id="km-favorite-meal-remove-rest">
            </div>
            
            <div>
                <label for="km-critical-success-effect-uuid">Critical Success: Effect UUID</label>
                <input type="text" name="critical-success-effect-uuid" id="km-critical-success-effect-uuid">
            </div>
            <div>
                <label for="km-critical-success-remove-rest">Critical Success: Remove Effect after Rest</label>
                <input type="checkbox" name="critical-success-remove-rest" id="km-critical-success-remove-rest">
            </div>
            
            <div>
                <label for="km-success-effect-uuid">Success: Effect UUID</label>
                <input type="text" name="success-effect-uuid" id="km-success-effect-uuid">
            </div>
            <div>
                <label for="km-success-remove-rest">Success: Remove Effect after Rest</label>
                <input type="checkbox" name="success-remove-rest" id="km-success-remove-rest">
            </div>
            
            <div>
                <label for="km-critical-failure-effect-uuid">Critical Failure: Effect UUID</label>
                <input type="text" name="critical-failure-effect-uuid" id="km-critical-failure-effect-uuid">
            </div>
            <div>
                <label for="km-critical-failure-remove-rest">Critical Failure: Remove Effect after Rest</label>
                <input type="checkbox" name="critical-failure-remove-rest" id="km-critical-failure-remove-rest">
            </div>
        </form>
        </div>
        `,
        buttons: {
            add: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Add',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const uuid = parseTextInput($html, 'uuid');
                    const item = await fromUuid(uuid);
                    const name = parseTextInput($html, 'name') || 'Unknown Meal';
                    if (item === null) {
                        ui.notifications?.error(`Can not find item with uuid ${uuid}`);
                    } else if (recipes.find(r => r.name === name)) {
                        ui.notifications?.error(`Recipe with name ${name} exists already`);
                    } else {
                        const favoriteEffect = await parseEffects($html, 'favorite-meal');
                        const critSuccEffect = await parseEffects($html, 'critical-success');
                        const succEffect = await parseEffects($html, 'success');
                        const failEffect = await parseEffects($html, 'critical-failure');
                        await onSubmit({
                            isHomebrew: true,
                            specialIngredients: parseNumberInput($html, 'special-ingredients') || 0,
                            level: parseNumberInput($html, 'level') || 0,
                            name,
                            rarity: parseSelect($html, 'rarity') as Rarity,
                            cost: parseTextInput($html, 'cost') || '0 gp',
                            uuid,
                            survivalDC: parseNumberInput($html, 'survival-dc') || 0,
                            basicIngredients: parseNumberInput($html, 'basic-ingredients') || 0,
                            cookingLoreDC: parseNumberInput($html, 'cooking-lore-dc') || 0,
                            favoriteMeal: {
                                effects: favoriteEffect,
                            },
                            criticalSuccess: {
                                effects: critSuccEffect,
                            },
                            success: {
                                effects: succEffect,
                            },
                            criticalFailure: {
                                effects: failEffect,
                            },
                        });
                    }
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 510,
    }).render(true);
}

async function parseEffects($html: HTMLElement, name: string): Promise<MealEffect[]> {
    const effect = parseTextInput($html, name + '-effect-uuid') || undefined;
    const removeAfterRest = parseCheckbox($html, name + '-remove-rest') || false;
    if (effect) {
        const item = await getEffectByUuid(effect);
        if (item === null) {
            ui.notifications?.error(`Can not find effect item with uuid ${effect}`);
        } else {
            return [{
                uuid: effect,
                removeAfterRest,
            }];
        }
    }
    return [];
}
