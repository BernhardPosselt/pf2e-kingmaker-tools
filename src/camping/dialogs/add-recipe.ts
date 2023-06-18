import {parseNumberInput, parseSelect, parseTextInput} from '../../utils';
import {Rarity, RecipeData} from '../recipes';

export function addRecipeDialog(
    onOk: (recipe: RecipeData) => Promise<void>,
): void {
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
                <label for="km-favorite-meal-message">Favorite Meal Description</label>
                <input type="text" name="favorite-meal-message" id="km-favorite-meal-message">
            </div>
            <div>
                <label for="km-favorite-meal-effect-uuid">Favorite Meal Effect UUID</label>
                <input type="text" name="favorite-meal-effect-uuid" id="km-favorite-meal-effect-uuid">
            </div>
            <div>
                <label for="km-critical-success-message">Critical Success Description</label>
                <input type="text" name="critical-success-message" id="km-critical-success-message">
            </div>
            <div>
                <label for="km-critical-success-effect-uuid">Critical Success  Effect UUID</label>
                <input type="text" name="critical-success-effect-uuid" id="km-critical-success-effect-uuid">
            </div>
            <div>
                <label for="km-success-message">Success Description</label>
                <input type="text" name="success-message" id="km-success-message">
            </div>
            <div>
                <label for="km-success-effect-uuid">Success Effect UUID</label>
                <input type="text" name="success-effect-uuid" id="km-success-effect-uuid">
            </div>
            <div>
                <label for="km-critical-failure-message">Critical Failure Description</label>
                <input type="text" name="critical-failure-message" id="km-critical-failure-message">
            </div>
            <div>
                <label for="km-critical-failure-effect-uuid">Critical Failure Effect UUID</label>
                <input type="text" name="critical-failure-effect-uuid" id="km-critical-failure-effect-uuid">
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
                    if (item === null) {
                        ui.notifications?.error(`Can not find item with uuid ${uuid}`);
                    } else {
                        await onOk({
                            isHomebrew: true,
                            specialIngredients: parseNumberInput($html, 'special-ingredients'),
                            level: parseNumberInput($html, 'level'),
                            name: parseTextInput($html, 'name'),
                            rarity: parseSelect($html, 'rarity') as Rarity,
                            cost: parseTextInput($html, 'cost'),
                            uuid,
                            survivalDC: parseNumberInput($html, 'survival-dc'),
                            basicIngredients: parseNumberInput($html, 'basic-ingredients'),
                            cookingLoreDC: parseNumberInput($html, 'cooking-lore-dc'),
                            favoriteMeal: {
                                message: parseTextInput($html, 'favorite-meal-message'),
                                effectUuid: parseTextInput($html, 'favorite-meal-effect-uuid') || undefined,
                            },
                            criticalSuccess: {
                                message: parseTextInput($html, 'critical-success-message'),
                                effectUuid: parseTextInput($html, 'critical-success-effect-uuid') || undefined,
                            },
                            success: {
                                message: parseTextInput($html, 'success-message'),
                                effectUuid: parseTextInput($html, 'success-effect-uuid') || undefined,
                            },
                            criticalFailure: {
                                message: parseTextInput($html, 'critical-failure-message'),
                                effectUuid: parseTextInput($html, 'critical-failure-effect-uuid') || undefined,
                            },
                        });
                    }
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 420,
    }).render(true);
}
