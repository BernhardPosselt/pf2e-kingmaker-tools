import {
    combatEffectsToChat,
    toViewActorMeals,
    toViewActors,
    toViewCampingActivities,
    toViewDegrees,
    ViewCampingData,
} from './view';
import {formatHours} from '../time/app';
import {getEncounterDC, rollRandomEncounter} from './random-encounters';
import {regions} from './regions';
import {CampingActivityData, CampingActivityName, huntAndGather} from './activities';
import {
    ActorMeal,
    Camping,
    CookingSkill,
    getCampingActivityData,
    getDefaultConfiguration,
    getPartyActors,
    rollCampingCheck,
    SkillCheckOptions,
} from './camping';
import {manageActivitiesDialog} from './dialogs/manage-activities';
import {manageRecipesDialog} from './dialogs/manage-recipes';
import {getKnownRecipes, getRecipesKnownInRegion, RecipeData} from './recipes';
import {addRecipeDialog} from './dialogs/add-recipe';
import {campingSettingsDialog} from './dialogs/camping-settings';
import {DegreeOfSuccess, degreeToProperty, StringDegreeOfSuccess} from '../degree-of-success';
import {getTimeOfDayPercent, getWorldTime, isDayOrNight} from '../time/calculation';
import {formatWorldTime} from '../time/format';
import {LabelAndValue, listenClick, postChatMessage, toLabelAndValue} from '../utils';
import {getActorsByUuid, hasCookingLore, NotProficientError, validateSkillProficiencies} from './actor';
import {askDcDialog} from './dialogs/ask-dc';
import {discoverSpecialMeal} from './dialogs/learn-recipe';
import {setupDialog} from '../kingdom/dialogs/setup-dialog';
import {getCamping, saveCamping} from './storage';
import {postDiscoverSpecialMealResult, postHuntAndGatherResult} from './chat';
import {DiffListener, eat, getDiffListeners} from './effect-syncing';
import {
    calculateDailyPreparationsSeconds,
    getActorsKeepingWatch,
    getRestHours,
    getWatchSecondsDuration,
    rest,
} from './resting';
import {
    calculateConsumedFood,
    canCook,
    getActorConsumables,
    getChosenMealData,
    getCookingActorByUuid,
    getCookingActorUuid,
    getRecipeData,
    removeFood,
    subsist,
} from './eating';
import {allowedActors} from './data';
import {openJournal} from '../foundry-utils';
import {getStringSetting} from '../settings';

interface CampingOptions {
    game: Game;
    actor: Actor;
}

interface CampingData {
    chosenMeal: string;
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    currentRegion: string;
    mealDegreeOfSuccess: StringDegreeOfSuccess | null;
    cookingSkill: CookingSkill;
}


export class CampingSheet extends FormApplication<CampingOptions & FormApplicationOptions, ViewCampingData, null> {
    private diffListeners: DiffListener[];

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
            dropSelector: '.new-camping-actor, .camping-activity, .camping-actor, .eating-actor',
            dragSelector: '.camping-actor, .content-link[data-type=Item]',
        }];
        options.closeOnSubmit = false;
        options.submitOnChange = true;
        options.submitOnClose = false;
        options.editable = true;
        return options;
    }

    private isGM: boolean;
    private readonly game: Game;
    private actor: Actor;

    constructor(options: CampingOptions) {
        super(null, options);
        this.game = options.game;
        this.isGM = this.game.user?.isGM ?? false;
        this.actor = options.actor;
        this.actor.apps[this.appId] = this;
        this.diffListeners = getDiffListeners(this.game);
    }

    override async getData(): Promise<ViewCampingData> {
        const data = await this.read();
        const currentSeconds = this.game.time.worldTime;
        const currentRegion = data.currentRegion;
        const sumElapsedSeconds = Math.abs(currentSeconds - data.dailyPrepsAtTime);
        const activityData = getCampingActivityData(data);
        const actors = await this.getActors(data);
        const watchSecondsDuration = await getWatchSecondsDuration(actors, data);
        const actorsKeepingWatch = getActorsKeepingWatch(data, actors);
        const {total: encounterDC, modifier: currentEncounterDCModifier} = getEncounterDC(data, this.game);
        const actorConsumables = await getActorConsumables([...actors, ...getPartyActors(this.game)]);
        const knownRecipes = getKnownRecipes(data);
        const chosenMealData = getChosenMealData(data);
        const chosenMeal = chosenMealData.name;
        const isUser = !this.isGM;
        const viewActors = await toViewActors(data.actorUuids, data.campingActivities, getCampingActivityData(data), isUser);
        const dailyPrepsSeconds = calculateDailyPreparationsSeconds(data.gunsToClean);
        const campingActivities = await toViewCampingActivities(data.campingActivities, activityData, new Set(data.lockedActivities));
        const visibleActivities = campingActivities.filter(c => !c.isHidden);
        const needsPrepareCampsite = visibleActivities.length === 1 && visibleActivities[0].name === 'Prepare Campsite';
        const worldTime = getWorldTime(this.game);
        const actorMeals = data.cooking.actorMeals;
        const consumedFood = calculateConsumedFood(canCook(data), actorConsumables, {
            actorsConsumingRations: actorMeals.filter(a => a.chosenMeal === 'rationsOrSubsistence').length,
            actorsConsumingMeals: actorMeals.filter(a => a.chosenMeal === 'meal').length,
            availableSubsistence: data.cooking.subsistenceAmount,
            availableMagicalSubsistence: data.cooking.magicalSubsistenceAmount,
            recipeBasicIngredientCost: chosenMealData?.basicIngredients ?? 0,
            recipeSpecialIngredientCost: chosenMealData?.specialIngredients ?? 0,
        });
        console.log(consumedFood);
        const viewData: ViewCampingData = {
            isGM: this.isGM,
            isUser,
            ...actorConsumables,
            currentEncounterDCModifier,
            encounterDC,
            watchEnabled: viewActors.length > 0,
            adventuringSince: formatHours(sumElapsedSeconds, data.dailyPrepsAtTime > currentSeconds),
            regions: toLabelAndValue(Array.from(regions.keys())),
            currentRegion,
            isDay: isDayOrNight(worldTime) === 'day',
            actors: viewActors,
            campingActivities,
            needsPrepareCampsite,
            watchSecondsDuration,
            dailyPrepsDuration: formatHours(dailyPrepsSeconds),
            watchDuration: formatHours(watchSecondsDuration),
            subsistenceAmount: data.cooking.subsistenceAmount,
            magicalSubsistenceAmount: data.cooking.magicalSubsistenceAmount,
            chosenMeal: chosenMeal,
            knownRecipes: toLabelAndValue(knownRecipes),
            knownFavoriteRecipes: toLabelAndValue(knownRecipes.filter(r => r !== 'Basic Meal')),
            mealChoices: [
                {value: 'meal', label: 'Meal'},
                {value: 'rationsOrSubsistence', label: 'Rations/Subsistence'},
                {value: 'nothing', label: 'Nothing'},
            ],
            degreesOfSuccesses: toViewDegrees(),
            mealDegreeOfSuccess: data.cooking.degreeOfSuccess,
            time: formatWorldTime(worldTime),
            // subtract maximum image width
            timeMarkerPositionPx: Math.floor(getTimeOfDayPercent(worldTime) * 8.16),
            hasCookingActor: canCook(data),
            consumedFood,
            actorMeals: await toViewActorMeals(data.actorUuids, actorMeals, getRecipeData(data)),
            ...(await this.getCookingSkillData(data)),
            chosenMealDc: await this.getMealDc(data, chosenMealData),
            showContinueRest: data.watchSecondsRemaining !== 0,
            ...getRestHours(watchSecondsDuration, dailyPrepsSeconds, data.watchSecondsRemaining),
            actorsKeepingWatch,
        };
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

    protected _canDragDrop(): boolean {
        return true;
    }

    protected _canDragStart(): boolean {
        return true;
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
        return await getActorsByUuid(new Set(data.actorUuids));
    }

    protected _getHeaderButtons(): Application.HeaderButton[] {
        const buttons = super._getHeaderButtons();
        buttons.unshift({
            label: 'Help',
            class: 'pf2e-kingmaker-tools-hb1',
            icon: 'fas fa-question',
            onclick: () => openJournal('Compendium.pf2e-kingmaker-tools.kingmaker-tools-journals.JournalEntry.iAQCUYEAq4Dy8uCY.JournalEntryPage.7z4cDr3FMuSy22t1'),
        });
        if (this.game.user?.isGM ?? false) {
            buttons.unshift({
                label: 'Show Players',
                class: 'pf2e-kingmaker-tools-hb2',
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
            await combatEffectsToChat(current);
        });
        listenClick($html, '.advance-hours', async (ev) => {
            const target = ev.currentTarget as HTMLButtonElement;
            const hours = target.dataset.hours ?? '0';
            await this.advanceHours(parseInt(hours, 10));
        });
        listenClick($html, '.manage-recipes', async () => {
            const current = await this.read();
            await manageRecipesDialog({
                isGM: this.isGM,
                recipes: getRecipeData(current),
                learnedRecipes: new Set(getKnownRecipes(current)),
                onSubmit: async (knownRecipes, deletedRecipes) => {
                    current.cooking.knownRecipes = Array.from(knownRecipes);
                    current.cooking.homebrewMeals = current.cooking.homebrewMeals
                        .filter(m => !deletedRecipes.has(m.name));
                    await this.update({cooking: current.cooking});
                },
            });
        });
        listenClick($html, '.add-recipes', async () => {
            const current = await this.read();
            await addRecipeDialog({
                recipes: getRecipeData(current),
                onSubmit: async (recipe) => {
                    current.cooking.homebrewMeals.push(recipe);
                    await this.update({cooking: current.cooking});
                },
            });
        });
        listenClick($html, '.manage-activities', async () => {
            await manageActivitiesDialog({
                game: this.game,
                actor: this.actor,
            });
        });
        listenClick($html, '.subsist', async (ev) => {
            const button = ev.currentTarget as HTMLButtonElement;
            const uuid = button.dataset.uuid!;
            const current = await this.read();
            const actor = await fromUuid(uuid) as Actor | null;
            if (actor) {
                await subsist(this.game, actor, current.currentRegion);
            }
        });
        listenClick($html, '.roll-encounter', async () => await this.rollRandomEncounter(true));
        listenClick($html, '.check-encounter', async () => await this.rollRandomEncounter());
        listenClick($html, '.consume-food', async () => await this.consumeFood());
        listenClick($html, '.decrease-zone-dc-modifier', async () => {
            const current = await this.read();
            await this.update({encounterModifier: current.encounterModifier - 1});
        });
        listenClick($html, '.reset-adventuring-time', async () => await this.resetAdventuringSince());
        listenClick($html, '.eat-food', async () => {
            const camping = await this.read();
            const recipes = getRecipeData(camping);
            const recipe = recipes.find(r => r.name === camping.cooking.chosenMeal);
            if (recipe) {
                await postChatMessage(`Eating ${recipe.name}`);
            }
            await eat(this.game, camping);
        });
        listenClick($html, '.increase-zone-dc-modifier', async () => {
            const current = await this.read();
            await this.update({encounterModifier: current.encounterModifier + 1});
        });
        listenClick($html, '.reset-zone-dc-modifier', async () => await this.update({encounterModifier: 0}));
        listenClick($html, '.reset-activities', async () => await this.update({campingActivities: []}));
        listenClick($html, '.cook-food', async () => {
            const current = await this.read();
            const cookingActor = await getCookingActorByUuid(current);
            if (cookingActor) {
                const {cookingSkill} = await this.getCookingSkillData(current);
                const chosenMealData = getChosenMealData(current);
                const dc = await this.getMealDc(current, chosenMealData);
                const activity = getCampingActivityData(current)
                    .find(a => a.name === 'Cook Meal')!;
                const result = await rollCampingCheck({
                    game: this.game,
                    actor: cookingActor,
                    activity,
                    dc,
                    skill: cookingSkill,
                    region: current.currentRegion,
                });
                if (result !== null) {
                    current.cooking.degreeOfSuccess = degreeToProperty(result);
                    await this.update({
                        cooking: current.cooking,
                    });
                }
            }
        });
        listenClick($html, '.rest', async () => {
            const camping = await this.read();
            const actors = await this.getActors(camping);
            await rest({
                camping,
                sheetActor: this.actor,
                game: this.game,
                watchDurationSeconds: await getWatchSecondsDuration(actors, camping),
            });
        });
        listenClick($html, '.roll-check', async (ev) => {
            const button = ev.currentTarget as HTMLButtonElement;
            const activity = button.dataset.activity!;
            const skill = button.dataset.skill!;
            await this.rollCheck(activity, skill);
        });
        listenClick($html, '.recipe-info', async () => {
            const current = await this.read();
            const recipe = getChosenMealData(current);
            const item = await fromUuid(recipe.uuid) as Item | null;
            item?.sheet?.render(true);
        });
        listenClick($html, '.camping-settings', async () => {
            const current = await this.read();
            const actors = await getActorsByUuid(new Set(current.actorUuids));
            const partyActors = getPartyActors(this.game);
            campingSettingsDialog({
                data: {
                    actors: [...actors, ...partyActors],
                    huntAndGatherTargetActorUuid: current.huntAndGatherTargetActorUuid,
                    restRollMode: current.restRollMode,
                    gunsToClean: current.gunsToClean,
                    increaseWatchActorNumber: current.increaseWatchActorNumber,
                    ignoreSkillRequirements: current.ignoreSkillRequirements,
                    proxyRandomEncounterTable: getStringSetting(this.game, 'proxyEncounterTable'),
                    randomEncounterRollMode: getStringSetting(this.game, 'randomEncounterRollMode') as RollMode,
                    actorsKeepingWatch: actors
                        .map(a => {
                            return {
                                uuid: a.uuid,
                                name: a.name ?? 'Unknown Actor',
                                watchEnabled: !current.actorUuidsNotKeepingWatch.includes(a.uuid),
                            };
                        }),
                },
                game: this.game,
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
                await openJournal(uuid);
            }
        });
    }

    private async rollCheck(activity: string, skill: string): Promise<void> {
        const current = await this.read();
        const activityData = getCampingActivityData(current)
            .find(a => a.name === activity);
        const actorUuid = current.campingActivities
            .find(a => a.activity === activity)
            ?.actorUuid;
        const actor = actorUuid ? (await fromUuid(actorUuid)) as Actor | null : null;
        if (activityData && actor) {
            if (activity === 'Discover Special Meal') {
                await this.discoverSpecialMeal(activityData, skill, actor, getRecipeData(current), current.currentRegion);
            } else if (activity === 'Hunt and Gather') {
                await this.huntAndGather(activityData, skill, actor, current.currentRegion);
            } else {
                await this.rollStandardCheck(activityData, skill, actor, current.currentRegion);
            }
        }
    }

    private async rollStandardCheck(
        activity: CampingActivityData,
        skill: string,
        actor: Actor,
        currentRegion: string,
    ): Promise<void> {
        const dcType = activity.dc;
        const rollOptions: SkillCheckOptions = {
            skill,
            secret: activity.isSecret,
            activity,
            actor,
            game: this.game,
            region: currentRegion,
        };
        const activityName = activity.name;
        if (dcType) {
            rollOptions.dc = dcType;
            await this.setActivityResult(activityName, await rollCampingCheck(rollOptions));
        } else {
            askDcDialog({
                activity: activityName,
                onSubmit: async (dc) => {
                    rollOptions.dc = dc;
                    await this.setActivityResult(activityName, await rollCampingCheck(rollOptions));
                },
            });
        }
    }

    private async rollRandomEncounter(forgoFlatCheck = false): Promise<void> {
        const current = await this.read();
        const dc = getEncounterDC(current, this.game);
        await rollRandomEncounter(this.game, current.currentRegion, dc, forgoFlatCheck);
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
                cooking: {
                    ...current.cooking,
                    actorMeals: current.cooking.actorMeals.filter(a => a.actorUuid !== uuid),
                },
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
        const document = await this.parseDropData(event);
        if (document) {
            const campingConfiguration = await this.read();
            if (target.classList.contains('camping-activity') && (document.type === 'character' || document.type === 'npc')) {
                const activityName = target.dataset.name as CampingActivityName;
                const current = await this.read();
                const data = getCampingActivityData(current);
                const proficiencyRequirements = data
                    .find(a => a.name === activityName)?.skillRequirements ?? [];
                if (document instanceof Actor) {
                    try {
                        if (!current.ignoreSkillRequirements) {
                            await validateSkillProficiencies(document, proficiencyRequirements);
                        }
                        await this.setActivityActor(campingConfiguration, activityName, document.uuid);
                    } catch (e) {
                        if (e instanceof NotProficientError) {
                            ui.notifications?.error(e.message);
                        } else {
                            throw e;
                        }
                    }
                } else {
                    await this.handleItemDrop(document, target.dataset.actorUuid!);
                }
            } else if (target.classList.contains('new-camping-actor') && !campingConfiguration.actorUuids.includes(document.uuid)) {
                await this.update({
                    actorUuids: [
                        ...(campingConfiguration.actorUuids), document.uuid],
                    cooking: {
                        ...campingConfiguration.cooking,
                        actorMeals: [...campingConfiguration.cooking.actorMeals, {
                            actorUuid: document.uuid,
                            chosenMeal: 'meal',
                            favoriteMeal: null,
                        }],
                    },
                });
            } else if (target.classList.contains('camping-actor') && document instanceof Item) {
                await this.handleItemDrop(document, target.dataset.actorUuid!);
            } else if (target.classList.contains('eating-actor') && document instanceof Item) {
                await this.handleItemDrop(document, target.dataset.actorUuid!);
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

    private async parseDropData(event: DragEvent): Promise<Actor | Item | null> {
        const dropData = event.dataTransfer?.getData('text/plain');
        if (!dropData) return null;
        try {
            const data = JSON.parse(dropData);
            const uuid = data.uuid;
            if (typeof uuid !== 'string') {
                console.error('No uuid found on drop data: ' + dropData);
                return null;
            }
            const document = await fromUuid(uuid) as Actor | Item | null;
            if (document) {
                const type = document?.type;
                if (document instanceof Actor && allowedActors.has(type) || document instanceof Item) {
                    return document;
                } else {
                    console.error('No character or item document type, instead found: ' + type);
                    return null;
                }
            }
        } catch (e) {
            console.error(e);
            console.info(dropData);
        }
        return null;
    }

    private async update(data: Partial<Camping>): Promise<void> {
        await saveCamping(this.game, this.actor, data);
    }

    private async read(): Promise<Camping> {
        return getCamping(this.actor);
    }

    private async openUuidSheet(uuid: string): Promise<void> {
        const item = await fromUuid(uuid) as Item | null;
        item?.sheet?.render(true);
    }

    protected async _updateObject(event: Event, formData: CampingData): Promise<void> {
        console.log('formdata', formData);
        const current = await this.read();
        const mealDegreeOfSuccess = this.parseNullableSelect(formData.mealDegreeOfSuccess) as StringDegreeOfSuccess | null;
        const update = {
            currentRegion: formData.currentRegion,
            campingActivities: getCampingActivityData(current).map(activityData => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const data = formData as any;
                const degreeKey = `actorActivityDegreeOfSuccess.${activityData.name}`;
                const skillKey = `selectedSkill.${activityData.name}`;
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
                knownRecipes: getKnownRecipes(current),
                degreeOfSuccess: mealDegreeOfSuccess,
                actorMeals: await this.parseActorMeals(formData),
                cookingSkill: formData.cookingSkill as CookingSkill,
            },
        };
        await this.update(update);
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
        const actors = [...(await this.getActors(current)), ...getPartyActors(this.game)];
        const actorConsumables = await getActorConsumables(actors);
        const chosenMealData = getRecipeData(current)
            .find(a => a.name === current.cooking.chosenMeal);
        const consumed = calculateConsumedFood(canCook(current), actorConsumables, {
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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
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

    private async setActivityResult(activityName: string, degreeOfSuccess: DegreeOfSuccess | null): Promise<void> {
        const current = await this.read();
        const activity = current.campingActivities.find(a => a.activity === activityName);
        if (degreeOfSuccess !== null && activity) {
            activity.result = degreeToProperty(degreeOfSuccess);
            await this.update({
                campingActivities: current.campingActivities,
            });
        }
    }

    private async handleItemDrop(document: Item, actorUuid: string): Promise<void> {
        const actor = await fromUuid(actorUuid) as Actor | null;
        if (actor) {
            await actor.createEmbeddedDocuments('Item', [document.toObject()]);
        }
    }

    private async discoverSpecialMeal(
        activityData: CampingActivityData,
        skill: string,
        actor: Actor,
        recipeData: RecipeData[],
        currentRegion: string,
    ): Promise<void> {
        const current = await this.read();
        const availableFood = await getActorConsumables(await this.getActors(current));
        const availableRecipes = getRecipesKnownInRegion(this.game, currentRegion, recipeData)
            .filter(r => !getKnownRecipes(current).includes(r.name));
        await discoverSpecialMeal({
            actor,
            availableFood,
            availableRecipes,
            onSubmit: async (recipeName) => {
                const data = availableRecipes.find(r => r.name === recipeName);
                if (recipeName !== null && data) {
                    const current = await this.read();
                    const result = await rollCampingCheck({
                        game: this.game,
                        actor: actor,
                        dc: data.cookingLoreDC,
                        skill,
                        activity: activityData,
                        region: currentRegion,
                    });
                    if (result !== null) {
                        const isCriticalSuccess = result === DegreeOfSuccess.CRITICAL_SUCCESS;
                        const isSuccess = result === DegreeOfSuccess.SUCCESS;
                        const isCriticalFailure = result === DegreeOfSuccess.CRITICAL_FAILURE;
                        const removeIngredients = {
                            specialIngredients: isCriticalSuccess ? data.specialIngredients : data.specialIngredients * 2,
                            basicIngredients: isCriticalSuccess ? data.basicIngredients : data.basicIngredients * 2,
                            rations: 2,
                        };
                        const recipeToAdd = isSuccess || isCriticalSuccess ? data.name : null;
                        const critFailUuids = isCriticalFailure
                            ? data.criticalFailure.effects?.map(e => e.uuid) ?? []
                            : [];
                        await postDiscoverSpecialMealResult(actor, removeIngredients, recipeToAdd, critFailUuids);
                        await this.persistActivityDegreeOfSuccess(current, result, 'Discover Special Meal');
                    }
                }
            },
        });
    }

    private async persistActivityDegreeOfSuccess(
        current: Camping,
        degreeOfSuccess: DegreeOfSuccess,
        activity: string,
    ): Promise<void> {
        current.campingActivities.forEach(a => {
            if (a.activity === activity) {
                a.result = degreeToProperty(degreeOfSuccess);
            }
        });
        await this.update({
            campingActivities: current.campingActivities,
        });
    }

    private async huntAndGather(
        activity: CampingActivityData,
        skill: string,
        actor: Actor,
        currentRegion: string,
    ): Promise<void> {
        const current = await this.read();
        const result = await rollCampingCheck({
            game: this.game,
            actor: actor,
            region: currentRegion,
            skill,
            activity,
            dc: activity.dc,
        });
        if (result !== null) {
            const ingredients = await huntAndGather(this.game, actor, result, currentRegion);
            await postHuntAndGatherResult(actor, ingredients);
            await this.persistActivityDegreeOfSuccess(current, result, activity.name);
        }
    }

    private async resetAdventuringSince(): Promise<void> {
        await saveCamping(this.game, this.actor, {
            dailyPrepsAtTime: this.game.time.worldTime,
        });
    }
}

export function openCampingSheet(game: Game): void {
    const sheetActor = game?.actors?.find(a => a.name === 'Camping Sheet');
    if (sheetActor) {
        new CampingSheet({game, actor: sheetActor}).render(true);
    } else {
        setupDialog(game, 'Camping', async () => {
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            const sheetActor: Actor = await Actor.create({
                type: 'npc',
                name: 'Camping Sheet',
                img: 'icons/magic/fire/flame-burning-campfire-orange.webp',
            });
            // migrate old recipes
            const migratedRecipes = game?.actors?.filter(a => a.type === 'character')
                ?.flatMap(a => {
                    const knownRecipes = a.getFlag('pf2e-kingmaker-tools', 'knownRecipes');
                    if (Array.isArray(knownRecipes)) {
                        return knownRecipes as string[];
                    } else {
                        return [];
                    }
                }) ?? [];
            await sheetActor?.setFlag('pf2e-kingmaker-tools', 'camping-sheet', getDefaultConfiguration(game, migratedRecipes));
            await openCampingSheet(game);
        });
    }
}


