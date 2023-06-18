import {RecipeData} from '../recipes';
import {DegreeOfSuccess} from '../../degree-of-success';
import {toTemplateRecipe, ViewRecipeData} from './manage-recipes';

export interface LearnRecipeOptions {
    actor: Actor;
    availableRecipes: RecipeData[];
    onSubmit: (recipe: RecipeData, degreeOfSuccess: DegreeOfSuccess) => Promise<void>
}

class LearnRecipeApp extends Application<LearnRecipeOptions & ApplicationOptions> {
    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'recipe-app';
        options.title = 'Recipes';
        options.template = 'modules/pf2e-kingmaker-tools/templates/camping/recipes.hbs';
        options.classes = ['kingmaker-tools-app'];
        return options;
    }

    private onSubmit: (recipe: RecipeData, degreeOfSuccess: DegreeOfSuccess) => Promise<void>;
    private availableRecipes: RecipeData[];
    private readonly actor: Actor;

    constructor(options: Partial<ApplicationOptions> & LearnRecipeOptions) {
        super(options);
        this.actor = options.actor;
        this.availableRecipes = options.availableRecipes;
        this.onSubmit = options.onSubmit;
    }

    override async getData(options?: Partial<ApplicationOptions> & LearnRecipeOptions): Promise<{recipes: ViewRecipeData[]}> {
        return {
            recipes: await Promise.all(this.availableRecipes.map(recipe => toTemplateRecipe(recipe, new Set()))),
        };
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        const learnRecipeButtons = $html.querySelectorAll('.learn-recipe-button') as NodeListOf<HTMLButtonElement>;
        learnRecipeButtons.forEach(learnRecipeButton => {
            learnRecipeButton?.addEventListener('click', async (event) => {
                console.log('learn', event);
                const button = event.target as HTMLButtonElement;
                const recipeName = button.dataset.recipe!;
                const selectedRecipe = this.availableRecipes.find(r => r.name === recipeName)!;
                /* eslint-disable @typescript-eslint/no-explicit-any */
                const skill = 'cooking-lore' in (this.actor as any).skills ? 'cooking-lore' : 'cooking';
                const result = await (this.actor as any).skills[skill].roll({
                    dc: selectedRecipe.cookingLoreDC ?? 0,
                });
                const degreeOfSuccess = result.degreeOfSuccess;
                await this.onSubmit(selectedRecipe, degreeOfSuccess);
                await this.close();
            });
        });
    }
}

export function discoverSpecialMeal(options: LearnRecipeOptions): void {
    new LearnRecipeApp(options).render(true);
}
