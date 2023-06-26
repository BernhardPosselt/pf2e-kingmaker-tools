import {RecipeData} from '../recipes';
import {toTemplateRecipe, ViewRecipeData} from './manage-recipes';
import {escapeHtml, parseRadio} from '../../utils';
import {FoodAmount} from '../eating';

export interface LearnRecipeOptions {
    actor: Actor;
    availableFood: FoodAmount;
    availableRecipes: RecipeData[];
    onSubmit: (recipe: string | null) => Promise<void>
}

function tpl(availableRecipes: ViewRecipeData[], availableFood: FoodAmount): string {
    const recipes = availableRecipes.map((r) => {
        return {
            ...r,
            disabled: availableFood.specialIngredients < (r.specialIngredients * 2) ||
                availableFood.basicIngredients < (r.basicIngredients * 2),
        };
    });
    const checkedIndex = recipes.findIndex(r => !r.disabled);
    return `
    <form>
        <h1>Recipes Learnable in Zone</h1>
        <p><b>Available Basic Ingredients</b>: ${availableFood.basicIngredients}</p>
        <p><b>Available Special Ingredients</b>: ${availableFood.specialIngredients}</p>
        <table class="km-table">
            <thead>
            <tr>
                <th>Recipe</th>
                <th>DC</th>
                <th>Cost</th>
                <th>Learn</th>
            </tr>
            </thead>
            <tbody>
            ${recipes.map((r, index) => {
                const checked = checkedIndex === index;
                return `<tr>
                    <td>${r.recipe}</td>
                    <td>${r.cookingLoreDC}</td>
                    <td>${escapeHtml(r.learningCost)}</td>
                    <td><input type="radio" name="recipe" value="${escapeHtml(r.name)}" ${checked ? 'checked' : ''} ${r.disabled ? 'disabled' : ''}></td>
                </tr>`;
    }).join('')}
            </tbody>
        </table>
    </form>
`;
}

export async function discoverSpecialMeal(options: LearnRecipeOptions): Promise<void> {
    const availableRecipes = await Promise.all(options.availableRecipes
        .map(r => toTemplateRecipe(r, new Set())));
    new Dialog({
        title: 'Discover Special Meal',
        content: tpl(availableRecipes, options.availableFood),
        buttons: {
            learn: {
                icon: '<i class="fa-solid fa-dice-d20"></i>',
                label: 'Learn',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const recipe = parseRadio($html, 'recipe');
                    await options.onSubmit(recipe);
                },
            },
        },
        default: 'learn',
    }, {
        jQuery: false,
        width: 510,
    }).render(true);
}
