import {parseCheckbox, parseNumberInput, parseSelect, parseTextInput} from '../../utils';
import {MealEffect, Rarity, RecipeData} from '../recipes';

export interface AddRecipeOptions {
    onSubmit: (recipe: RecipeData) => Promise<void>;
    recipes: RecipeData[];
}

export function addRecipeDialog({onSubmit, recipes}: AddRecipeOptions): void {
    new Dialog({
        title: 'Add Recipe',
        content: `
        <form class="simple-dialog-form">
            <div>
                <label for="km-recipe-name">Name</label>
                <input type="text" name="name" id="km-recipe-name">
            </div>
            <div>
                <label for="km-recipe-uuid">UUID (Check Item Rules Tab)</label>
                <input type="text" name="uuid" id="km-recipe-uuid">
            </div>
            <div>
                <label for="km-recipe-level">Level</label>
                <input type="number" name="level" id="km-recipe-level">
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
                <input type="number" name="cooking-lore-dc" id="km-recipe-cooking-lore-dc">
            </div>
            <div>
                <label for="km-recipe-survival-dc">Survival DC</label>
                <input type="number" name="survival-dc" id="km-recipe-survival-dc">
            </div>
            <div>
                <label for="km-recipe-basic">Basic Ingredients</label>
                <input type="number" name="basic-ingredients" id="km-recipe-basic">
            </div>
            <div>
                <label for="km-recipe-special">Special Ingredients</label>
                <input type="number" name="special-ingredients" id="km-recipe-special">
            </div>
            <div>
                <label for="km-recipe-cost">Cost</label>
                <input type="text" name="cost" id="km-recipe-cost">
            </div>
            
            <div>
                <label for="km-favorite-meal-effect-uuid">Favorite Meal: Effect UUID</label>
                <input type="text" name="favorite-meal-effect-uuid" id="km-favorite-meal-effect-uuid">
            </div>
            <div>
                <label for="km-favorite-meal-sleep-seconds">Favorite Meal: Modify Sleep by Seconds</label>
                <input type="number" name="favorite-meal-sleep-seconds" id="km-favorite-meal-sleep-seconds">
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
                <label for="km-critical-success-sleep-seconds">Critical Success: Modify Sleep by Seconds</label>
                <input type="number" name="critical-success-sleep-seconds" id="km-critical-success-sleep-seconds">
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
                <label for="km-success-sleep-seconds">Success: Modify Sleep by Seconds</label>
                <input type="number" name="success-sleep-seconds" id="km-success-sleep-seconds">
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
                <label for="km-critical-failure-sleep-seconds">Critical Failure: Modify Sleep by Seconds</label>
                <input type="number" name="critical-failure-sleep-seconds" id="km-critical-failure-sleep-seconds">
            </div>
            <div>
                <label for="km-critical-failure-remove-rest">Critical Failure: Remove Effect after Rest</label>
                <input type="checkbox" name="critical-failure-remove-rest" id="km-critical-failure-remove-rest">
            </div>
        </form>
        `,
        buttons: {
            add: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Add',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const uuid = parseTextInput($html, 'uuid');
                    const item = await fromUuid(uuid);
                    const name = parseTextInput($html, 'name');
                    if (item === null) {
                        ui.notifications?.error(`Can not find item with uuid ${uuid}`);
                    } else if (recipes.find(r => r.name === 'name')){
                        ui.notifications?.error(`Recipe with name ${name} exists already`);
                    } else {
                        const favoriteEffect = parseEffects($html, 'favorite-meal');
                        const critSuccEffect = parseEffects($html, 'critical-success');
                        const succEffect = parseEffects($html, 'success');
                        const failEffect = parseEffects($html, 'critical-failure');
                        await onSubmit({
                            isHomebrew: true,
                            specialIngredients: parseNumberInput($html, 'special-ingredients'),
                            level: parseNumberInput($html, 'level'),
                            name,
                            rarity: parseSelect($html, 'rarity') as Rarity,
                            cost: parseTextInput($html, 'cost'),
                            uuid,
                            survivalDC: parseNumberInput($html, 'survival-dc'),
                            basicIngredients: parseNumberInput($html, 'basic-ingredients'),
                            cookingLoreDC: parseNumberInput($html, 'cooking-lore-dc'),
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

function parseEffects($html: HTMLElement, name: string): MealEffect[] {
    const effect = parseTextInput($html, name + '-effect-uuid') || undefined;
    const modifySleepDurationSeconds = parseNumberInput($html, name + '-sleep-seconds') || undefined;
    const removeAfterRest = parseCheckbox($html, name + '-remove-rest') || false;
    if (effect) {
        return [{
            uuid: effect,
            modifySleepDurationSeconds,
            removeAfterRest,
        }];
    } else {
        return [];
    }
}
