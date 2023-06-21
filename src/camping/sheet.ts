import {toViewActorMeals, toViewActors, toViewCampingActivities, toViewDegrees, ViewCampingData} from './view';
import {formatHours} from '../time/app';
import {rollRandomEncounter} from './random-encounters';
import {regions} from './regions';
import {CampingActivityName} from './activities';
import {
    ActorMeal,
    calculateConsumedFood,
    calculateDailyPreparationsSeconds,
    calculateRestSeconds,
    Camping,
    CookingSkill,
    getActorConsumables,
    getCampingActivityData,
    getChosenMealData,
    getCookingActorByUuid,
    getCookingActorUuid,
    getDefaultConfiguration,
    getRecipeData,
    removeFood,
    rollCampingCheck,
} from './camping';
import {manageActivitiesDialog} from './dialogs/manage-activities';
import {manageRecipesDialog} from './dialogs/manage-recipes';
import {RecipeData} from './recipes';
import {addRecipeDialog} from './dialogs/add-recipe';
import {campingSettingsDialog} from './dialogs/camping-settings';
import {degreeToProperty, StringDegreeOfSuccess} from '../degree-of-success';
import {getTimeOfDayPercent, getWorldTime} from '../time/calculation';
import {formatWorldTime} from '../time/format';
import {camelCase, LabelAndValue, listenClick} from '../utils';
import {postCombatEffects} from './dialogs/post-combat-fx';
import {hasCookingLore, NotProficientError, validateSkillProficiencies} from './actor';

interface CampingOptions {
    game: Game;
}

interface CampingData {
    chosenMeal: string;
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    currentRegion: string;
    mealDegreeOfSuccess: StringDegreeOfSuccess | null;
    cookingSkill: CookingSkill;
}


export class CampingSheet extends FormApplication<CampingOptions & FormApplicationOptions, ViewCampingData, CampingData> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'camping-app';
        options.title = 'Camping Sheet';
        options.template = 'modules/pf2e-kingmaker-tools/templates/camping/sheet.hbs';
        options.classes = ['kingmaker-tools-app', 'camping-app'];
        options.scrollY = ['.camping-sidebar', '.camping-content'];
        options.width = 862;
        options.height = 'auto';
        options.dragDrop = [{
            dropSelector: '.new-camping-actor, .camping-activity',
            dragSelector: '.camping-actor',
        }];
        options.closeOnSubmit = false;
        options.submitOnChange = true;
        options.submitOnClose = false;
        return options;
    }

    private isGM: boolean;
    private readonly game: Game;

    constructor(options: CampingOptions) {
        /* eslint-disable @typescript-eslint/no-explicit-any */
        super({} as any, options);
        this.game = options.game;
        this.isGM = this.game.user?.isGM ?? false;
    }

    override async getData(options?: Partial<CampingOptions & FormApplicationOptions>): Promise<ViewCampingData> {
        const data = await this.read();
        const currentSeconds = this.game.time.worldTime;
        const currentRegion = data.currentRegion;
        const currentRegionData = regions.get(currentRegion);
        const sumElapsedSeconds = Math.abs(currentSeconds - data.dailyPrepsAtTime);
        const isUser = !this.isGM;
        const activityData = getCampingActivityData(data);
        const actors = await this.getActors(data);
        const watchSecondsDuration = calculateRestSeconds(actors.length);
        const currentEncounterDCModifier = data.encounterModifier;
        const actorConsumables = await getActorConsumables(actors);
        const knownRecipes = data.cooking.knownRecipes;
        const chosenMealData = getChosenMealData(data);
        const chosenMeal = chosenMealData.name;
        const viewData: ViewCampingData = {
            isGM: this.isGM,
            isUser,
            ...actorConsumables,
            currentEncounterDCModifier,
            encounterDC: currentEncounterDCModifier + (currentRegionData?.encounterDC ?? 0),
            adventuringSince: formatHours(sumElapsedSeconds, data.dailyPrepsAtTime > currentSeconds),
            regions: Array.from(regions.keys()),
            currentRegion,
            actors: await toViewActors(data.actorUuids, data.campingActivities, getCampingActivityData(data)),
            campingActivities: await toViewCampingActivities(data.campingActivities, activityData, new Set(data.lockedActivities)),
            watchSecondsElapsed: data.watchSecondsElapsed,
            watchElapsed: formatHours(data.watchSecondsElapsed),
            watchSecondsDuration,
            dailyPrepsDuration: formatHours(calculateDailyPreparationsSeconds(data.gunsToClean)),
            watchDuration: formatHours(watchSecondsDuration),
            subsistenceAmount: data.cooking.subsistenceAmount,
            magicalSubsistenceAmount: data.cooking.magicalSubsistenceAmount,
            chosenMeal: chosenMeal,
            knownRecipes: knownRecipes,
            knownFavoriteRecipes: knownRecipes.filter(r => r !== 'Basic Meal'),
            degreesOfSuccesses: toViewDegrees(),
            mealDegreeOfSuccess: data.cooking.degreeOfSuccess,
            time: formatWorldTime(getWorldTime(this.game)),
            // 4px offset is half of the element's width including borders
            timeMarkerPositionPx: Math.floor(getTimeOfDayPercent(getWorldTime(this.game)) * 8.46) - 4,
            hasCookingActor: !!getCookingActorUuid(data),
            consumedFood: calculateConsumedFood(actorConsumables, {
                actorsConsumingRations: data.cooking.actorMeals.filter(a => a.chosenMeal === 'rationsOrSubsistence').length,
                actorsConsumingMeals: data.cooking.actorMeals.filter(a => a.chosenMeal === 'meal').length,
                availableSubsistence: data.cooking.subsistenceAmount,
                availableMagicalSubsistence: data.cooking.magicalSubsistenceAmount,
                recipeBasicIngredientCost: chosenMealData?.basicIngredients ?? 0,
                recipeSpecialIngredientCost: chosenMealData?.specialIngredients ?? 0,
            }),
            actorMeals: await toViewActorMeals(data.actorUuids, data.cooking.actorMeals),
            ...(await this.getCookingSkillData(data)),
            chosenMealDc: await this.getMealDc(data, chosenMealData),
        };
        console.log('viewData', viewData);
        return viewData;
    }


    private async getCookingSkillData(data: Camping): Promise<{
        cookingSkill: CookingSkill,
        cookingSkills: LabelAndValue[]
    }> {
        const hasCookingLore = await this.cookingActorHasCookingLore(data);
        const cookingSkills = [
            ...(hasCookingLore ? [{value: 'cooking', label: 'Cooking'}] : []),
            {value: 'survival', label: 'Survival'},
        ];
        const cookingSkill = hasCookingLore ? data.cooking.cookingSkill : 'survival';
        return {
            cookingSkills,
            cookingSkill,
        };
    }

    private async getMealDc(data: Camping, chosenMealData: RecipeData): Promise<number> {
        const skillData = await this.getCookingSkillData(data);
        if (skillData.cookingSkill === 'survival') {
            return chosenMealData.survivalDC;
        } else {
            return chosenMealData.cookingLoreDC;
        }
    }

    private async cookingActorHasCookingLore(data: Camping): Promise<boolean> {
        const cookingActor = getCookingActorUuid(data);
        if (cookingActor) {
            return await hasCookingLore(cookingActor);
        } else {
            return false;
        }
    }

    private async getActors(data: Camping): Promise<Actor[]> {
        const actors = data.actorUuids.map(async a => await fromUuid(a) as Actor | null);
        return (await Promise.all(actors)).filter(a => a !== null) as Actor[];
    }

    protected _getHeaderButtons(): Application.HeaderButton[] {
        const buttons = super._getHeaderButtons();
        if (this.game.user?.isGM ?? false) {
            buttons.unshift({
                label: 'Show Players',
                class: 'something-made-up',
                icon: 'fas fa-eye',
                onclick: () => this.emit({
                    action: 'openCampingSheet',
                }),
            });
        }
        return buttons;
    }

    private emit(args: object): void {
        this.game.socket!.emit('module.pf2e-kingmaker-tools', args);
    }

    private reRender(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        Hooks.on('updateWorldTime', this.reRender.bind(this));
        Hooks.on('updateItem', this.reRender.bind(this));
        Hooks.on('createItem', this.reRender.bind(this));
        Hooks.on('deleteItem', this.reRender.bind(this));
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '.remove-actor', async (ev) => {
            const button = ev.currentTarget as HTMLButtonElement;
            const uuid = button.dataset.uuid;
            const type = button.dataset.type;
            if (uuid !== undefined && type !== undefined) {
                await this.removeActor(uuid, type);
            }
        });
        listenClick($html, '.post-combat-effects', async () => {
            const current = await this.read();
            await postCombatEffects({
                game: this.game,
                preselectedCompanions: current.combatEffectCompanions,
                onSubmit: async (selectedCompanions) => {
                    await this.update({
                        combatEffectCompanions: selectedCompanions,
                    });
                },
            });
        });
        listenClick($html, '.advance-hours', async (ev) => {
            const target = ev.currentTarget as HTMLButtonElement;
            const hours = target.dataset.hours ?? '0';
            await this.advanceHours(parseInt(hours, 10));
        });
        listenClick($html, '.manage-recipes', async (ev) => {
            const current = await this.read();
            await manageRecipesDialog({
                isGM: this.isGM,
                recipes: getRecipeData(current),
                learnedRecipes: new Set(current.cooking.knownRecipes),
                onSubmit: async (knownRecipes, deletedRecipes) => {
                    current.cooking.knownRecipes = Array.from(knownRecipes);
                    current.cooking.homebrewMeals = current.cooking.homebrewMeals
                        .filter(m => !deletedRecipes.has(m.name));
                    await this.update({cooking: current.cooking});
                },
            });
        });
        listenClick($html, '.add-recipes', async (ev) => {
            const current = await this.read();
            await addRecipeDialog({
                recipes: getRecipeData(current),
                onSubmit: async (recipe) => {
                    current.cooking.homebrewMeals.push(recipe);
                    await this.update({cooking: current.cooking});
                },
            });
        });
        listenClick($html, '.unlock-activities', async (ev) => {
            const current = await this.read();
            await manageActivitiesDialog({
                data: getCampingActivityData(current),
                lockedActivities: new Set(current.lockedActivities),
                onSubmit: async (lockedActivities) => {
                    await this.update({lockedActivities: Array.from(lockedActivities)});
                },
            });
        });
        listenClick($html, '.clear-activities', async () => await this.update({campingActivities: []}));
        listenClick($html, '.roll-encounter', async () => await this.rollRandomEncounter(true));
        listenClick($html, '.check-encounter', async () => await this.rollRandomEncounter());
        listenClick($html, '.consume-food', async () => await this.consumeFood());
        listenClick($html, '.decrease-zone-dc-modifier', async () => {
            const current = await this.read();
            await this.update({encounterModifier: current.encounterModifier - 1});
        });
        listenClick($html, '.increase-zone-dc-modifier', async () => {
            const current = await this.read();
            await this.update({encounterModifier: current.encounterModifier + 1});
        });
        listenClick($html, '.reset-zone-dc-modifier', async () => await this.update({encounterModifier: 0}));
        listenClick($html, '.cook-food', async () => {
            const current = await this.read();
            const cookingActor = await getCookingActorByUuid(current);
            if (cookingActor) {
                const {cookingSkill} = await this.getCookingSkillData(current);
                const chosenMealData = getChosenMealData(current);
                const dc = await this.getMealDc(current, chosenMealData);
                const result = await rollCampingCheck({
                    game: this.game,
                    actor: cookingActor,
                    activity: 'Cook Meal',
                    dc,
                    skill: cookingSkill,
                });
                if (result !== null) {
                    current.cooking.degreeOfSuccess = degreeToProperty(result);
                    await this.update({
                        cooking: current.cooking,
                    });
                }
            }
        });
        listenClick($html, '.roll-check', async (ev) => {
            // TODO
        });
        listenClick($html, '.recipe-info', async (ev) => {
            const current = await this.read();
            const recipe = getChosenMealData(current);
            const item = await fromUuid(recipe.uuid) as Item | null;
            item?.sheet?.render(true);
        });
        listenClick($html, '.camping-settings', async (ev) => {
            const current = await this.read();
            campingSettingsDialog({
                data: {
                    restRollMode: current.restRollMode,
                    gunsToClean: current.gunsToClean,
                },
                onSubmit: async (data) => await this.update(data),
            });
        });
        listenClick($html, '.camping-actors .actor-image', async (ev) => {
            const button = ev.currentTarget as HTMLButtonElement;
            const uuid = button.dataset.uuid;
            if (uuid !== undefined) {
                await this.openUuidSheet(uuid);
            }
        });
        listenClick($html, '.camping-actors .actor-header', async (ev) => {
            const button = ev.currentTarget as HTMLElement;
            const uuid = button.dataset.uuid;
            if (uuid !== undefined) {
                await this.openUuidJournal(uuid);
            }
        });
    }

    private async rollRandomEncounter(forgoFlatCheck = false): Promise<void> {
        const current = await this.read();
        const modifier = current.encounterModifier; // FIXME: include activity modifiers
        await rollRandomEncounter(this.game, current.currentRegion, modifier, forgoFlatCheck);
    }

    override close(options?: Application.CloseOptions): Promise<void> {
        Hooks.off('updateWorldTime', this.render);
        Hooks.off('updateItem', this.render);
        Hooks.off('createItem', this.render);
        Hooks.off('deleteItem', this.render);
        return super.close(options);
    }

    private async advanceHours(hours: number): Promise<void> {
        await this.game.time.advance(hours * 3600);
    }

    private async removeActor(uuid: string, type: string): Promise<void> {
        const current = await this.read();
        if (type === 'camping-actors') {
            await this.update({
                actorUuids: current.actorUuids.filter(id => id !== uuid),
                campingActivities: current.campingActivities.filter(a => a.actorUuid !== uuid),
            });
        } else {
            const campingConfiguration = await this.read();
            const activity = campingConfiguration.campingActivities
                .find(a => a.activity === type);
            if (activity) {
                activity.actorUuid = null;
                await this.update({campingActivities: campingConfiguration.campingActivities});
            }
        }
    }

    protected _onDragStart(event: DragEvent): void {
        super._onDragStart(event);
        const target = event.currentTarget as HTMLLIElement;
        const uuid = target.dataset.uuid;
        event?.dataTransfer?.setData('text/plain', JSON.stringify({type: 'Actor', uuid: uuid}));
    }

    protected async _onDrop(event: DragEvent): Promise<void> {
        const target = event.currentTarget as HTMLLIElement;
        const uuid = await this.parseCharacterUuid(event);
        if (uuid) {
            const campingConfiguration = await this.read();
            if (target.classList.contains('camping-activity')) {
                const activityName = target.dataset.name as CampingActivityName;
                const current = await this.read();
                const data = getCampingActivityData(current);
                const proficiencyRequirements = data
                    .find(a => a.name === activityName)?.skillRequirements ?? [];
                const actor = await fromUuid(uuid) as Actor | null;
                if (actor) {
                    try {
                        await validateSkillProficiencies(actor, proficiencyRequirements);
                        await this.setActivityActor(campingConfiguration, activityName, uuid);
                    } catch (e) {
                        if (e instanceof NotProficientError) {
                            ui.notifications?.error(e.message);
                        } else {
                            throw e;
                        }
                    }
                }
            } else if (target.classList.contains('new-camping-actor') && !campingConfiguration.actorUuids.includes(uuid)) {
                await this.update({actorUuids: [...(campingConfiguration.actorUuids), uuid]});
            }
        }
    }

    private async setActivityActor(campingConfiguration: Camping, activityName: CampingActivityName, uuid: string): Promise<void> {
        const activity = campingConfiguration.campingActivities
            .find(a => a.activity === activityName);
        if (activity) {
            activity.actorUuid = uuid;
        } else {
            campingConfiguration.campingActivities.push({
                activity: activityName,
                actorUuid: uuid,
                result: null,
                selectedSkill: null,
            });
        }
        await this.update({campingActivities: campingConfiguration.campingActivities});
    }

    private async parseCharacterUuid(event: DragEvent): Promise<string | null> {
        const dropData = event.dataTransfer?.getData('text/plain');
        if (!dropData) return null;
        try {
            const data = JSON.parse(dropData);
            const uuid = data.uuid;
            if (typeof uuid !== 'string') {
                console.error('No uuid found on drop data: ' + dropData);
                return null;
            }
            const isActor = 'type' in data && data.type === 'Actor';
            if (!isActor) {
                console.error('No actor on drop data: ' + dropData);
                return null;
            }
            const actor = await fromUuid(uuid) as Actor | null;
            const actorType = actor?.type;
            if (actorType !== 'character') {
                console.error('No character actor type, instead found: ' + actorType);
                return null;
            }
            return uuid;
        } catch (e) {
            console.error(e);
            console.info(dropData);
            return null;
        }
    }

    private async update(data: Partial<Camping>): Promise<void> {
        const existing = await this.read();
        const toSave = Object.assign({}, existing, data);
        console.log('toSave', toSave);
        localStorage.setItem('campingConfig', JSON.stringify(toSave));
        this.render();
    }

    private async read(): Promise<Camping> {
        const config = localStorage.getItem('campingConfig');
        if (config === null) {
            localStorage.setItem('campingConfig', JSON.stringify(getDefaultConfiguration(this.game)));
            return await this.read();
        }
        return JSON.parse(config);
    }

    private async openUuidSheet(uuid: string): Promise<void> {
        const item = await fromUuid(uuid) as Item | null;
        item?.sheet?.render(true);
    }

    private async openUuidJournal(uuid: string): Promise<void> {
        const journal = await fromUuid(uuid) as JournalEntry | JournalEntryPage | null;
        if (journal instanceof JournalEntryPage) {
            journal?.parent?.sheet?.render(true, {pageId: journal.id});
        } else {
            (journal as any).sheet.render();
        }

    }

    protected async _updateObject(event: Event, formData: CampingData): Promise<void> {
        console.log('formdata', formData);
        const current = await this.read();
        const mealDegreeOfSuccess = this.parseNullableSelect(formData.mealDegreeOfSuccess) as StringDegreeOfSuccess | null;
        await this.update({
            currentRegion: formData.currentRegion,
            campingActivities: getCampingActivityData(current).map(activityData => {
                const data = formData as any;
                const degreeKey = `actorActivityDegreeOfSuccess.${camelCase(activityData.name)}`;
                const skillKey = `selectedSkill.${camelCase(activityData.name)}`;
                return {
                    activity: activityData.name,
                    selectedSkill: this.parseNullableSelect(data[skillKey]),
                    result: this.parseNullableSelect(data[degreeKey]) as StringDegreeOfSuccess | null,
                    actorUuid: current.campingActivities
                        .find(activity => activity.activity === activityData.name)?.actorUuid ?? null,
                };
            }),
            cooking: {
                magicalSubsistenceAmount: formData.magicalSubsistenceAmount,
                subsistenceAmount: formData.subsistenceAmount,
                chosenMeal: formData.chosenMeal,
                homebrewMeals: current.cooking.homebrewMeals,
                knownRecipes: current.cooking.knownRecipes,
                degreeOfSuccess: mealDegreeOfSuccess,
                actorMeals: await this.parseActorMeals(formData),
                cookingSkill: formData.cookingSkill as CookingSkill,
            },
        });
        // TODO: add effect syncing
        // TODO: add listener
        if (mealDegreeOfSuccess !== null) {
            this.emit({
                action: 'syncMealEffects',
            });
        }
        this.render();
    }

    private parseNullableSelect(formData: string | null | undefined): string | null {
        if (formData === null || formData === undefined || formData.length === 0 || formData === '-') {
            return null;
        }
        return formData;
    }

    private async consumeFood(): Promise<void> {
        const current = await this.read();
        const actors = await this.getActors(current);
        const actorConsumables = await getActorConsumables(actors);
        const chosenMealData = getRecipeData(current)
            .find(a => a.name === current.cooking.chosenMeal);
        const consumed = calculateConsumedFood(actorConsumables, {
            actorsConsumingRations: current.cooking.actorMeals.filter(a => a.chosenMeal === 'rationsOrSubsistence').length,
            actorsConsumingMeals: current.cooking.actorMeals.filter(a => a.chosenMeal === 'meal').length,
            availableSubsistence: current.cooking.subsistenceAmount,
            availableMagicalSubsistence: current.cooking.magicalSubsistenceAmount,
            recipeBasicIngredientCost: chosenMealData?.basicIngredients ?? 0,
            recipeSpecialIngredientCost: chosenMealData?.specialIngredients ?? 0,
        });
        const config = {
            rations: consumed.rations.value,
            specialIngredients: consumed.specialIngredients.value,
            basicIngredients: consumed.basicIngredients.value,
        };
        await removeFood(actors, config);
        await ChatMessage.create({
            content: `Removed up to the following amounts from actor inventories
                <ul>
                    <li><b>Rations</b>: ${config.rations}</li>
                    <li><b>Basic Ingredients</b>: ${config.basicIngredients}</li>
                    <li><b>Special Ingredients</b>: ${config.specialIngredients}</li>
                </ul>
            `,
        });
    }

    private async parseActorMeals(formData: CampingData): Promise<ActorMeal[]> {
        const current = await this.read();
        const data = formData as any;
        return current.actorUuids.map(uuid => {
            const x: ActorMeal = {
                actorUuid: uuid,
                favoriteMeal: this.parseNullableSelect(data['actorFavoriteMeal.' + uuid]),
                chosenMeal: data['actorChosenMeal.' + uuid],
            };
            return x;
        });
    }
}

export function openCampingSheet(game: Game): void {
    new CampingSheet({game}).render(true);
}


