import {toViewActors, toViewCampingActivities, toViewDegrees, ViewCampingData} from './view';
import {formatHours} from '../time/app';
import {rollRandomEncounter} from './random-encounters';
import {regions} from './regions';
import {CampingActivityName, getCampingActivityData} from './activities';
import {
    calculateConsumedFood,
    calculateDailyPreparationsSeconds,
    calculateRestSeconds,
    Camping,
    getActorConsumables,
    getDefaultConfiguration,
} from './camping';
import {manageActivitiesDialog} from './dialogs/manage-activities';
import {manageRecipesDialog} from './dialogs/manage-recipes';
import {getRecipeData} from './recipes';
import {addRecipeDialog} from './dialogs/add-recipe';
import {campingSettingsDialog} from './dialogs/camping-settings';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {getTimeOfDayPercent, getWorldTime} from '../time/calculation';
import {formatWorldTime} from '../time/format';

interface CampingOptions {
    game: Game;
}

interface CampingData {
    chosenMeal: string;
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    servings: number;
    currentRegion: string;
    mealDegreeOfSuccess: StringDegreeOfSuccess | null;
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

    private readonly game: Game;

    constructor(options: CampingOptions) {
        /* eslint-disable @typescript-eslint/no-explicit-any */
        super({} as any, options);
        this.game = options.game;
    }

    override async getData(options?: Partial<CampingOptions & FormApplicationOptions>): Promise<ViewCampingData> {
        const data = await this.read();
        const currentSeconds = this.game.time.worldTime;
        const currentRegion = data.currentRegion;
        const currentRegionData = regions.get(currentRegion);
        const sumElapsedSeconds = Math.abs(currentSeconds - data.dailyPrepsAtTime);
        const isGM = this.game.user?.isGM ?? false;
        const isUser = !isGM;
        const activityData = getCampingActivityData();
        const actors = await Promise.all(data.actorUuids.map(a => fromUuid(a))) as Actor[];
        console.log(data);
        const watchSecondsDuration = calculateRestSeconds(actors.length);
        const currentEncounterDCModifier = data.encounterModifier;
        const actorConsumables = await getActorConsumables(actors);
        const chosenMealData = getRecipeData().concat(data.cooking.homebrewMeals).find(a => a.name === data.cooking.chosenMeal);
        return {
            isGM,
            isUser,
            ...actorConsumables,
            currentEncounterDCModifier,
            encounterDC: currentEncounterDCModifier + (currentRegionData?.encounterDC ?? 0),
            adventuringSince: formatHours(sumElapsedSeconds, data.dailyPrepsAtTime > currentSeconds),
            regions: Array.from(regions.keys()),
            currentRegion,
            actors: await toViewActors(data.actorUuids),
            campingActivities: await toViewCampingActivities(data.campingActivities, activityData, new Set(data.lockedActivities)),
            watchSecondsElapsed: data.watchSecondsElapsed,
            watchElapsed: formatHours(data.watchSecondsElapsed),
            watchSecondsDuration,
            dailyPrepsDuration: formatHours(calculateDailyPreparationsSeconds(data.gunsToClean)),
            watchDuration: formatHours(watchSecondsDuration),
            subsistenceAmount: data.cooking.subsistenceAmount,
            magicalSubsistenceAmount: data.cooking.magicalSubsistenceAmount,
            servings: data.cooking.servings,
            chosenMeal: data.cooking.chosenMeal,
            knownRecipes: data.cooking.knownRecipes,
            degreesOfSuccesses: toViewDegrees(),
            mealDegreeOfSuccess: data.cooking.degreeOfSuccess,
            time: formatWorldTime(getWorldTime(this.game)),
            // 4px offset is half of the element's width including borders
            timeMarkerPositionPx: Math.floor(getTimeOfDayPercent(getWorldTime(this.game)) * 8.46) - 4,
            consumedFood: calculateConsumedFood(actorConsumables, {
                actorsConsumingRations: data.cooking.actorMeals.filter(a => a.consume === 'rationsOrSubsistence').length,
                actorsConsumingMeals: data.cooking.actorMeals.filter(a => a.consume === 'meal').length,
                mealServings: data.cooking.servings,
                availableSubsistence: data.cooking.subsistenceAmount,
                availableMagicalSubsistence: data.cooking.magicalSubsistenceAmount,
                recipeBasicIngredientCost: chosenMealData?.basicIngredients ?? 0,
                recipeSpecialIngredientCost: chosenMealData?.specialIngredients ?? 0,
            }),
        };
    }

    protected _getHeaderButtons(): Application.HeaderButton[] {
        const buttons = super._getHeaderButtons();
        if (this.game.user?.isGM ?? false) {
            // TODO
            buttons.unshift({
                label: 'Show Players',
                class: '',
                icon: 'fas fa-eye',
                // for sockets: https://github.com/League-of-Foundry-Developers/foundryvtt-forien-quest-log/blob/master/src/view/log/QuestLog.js#L65-L84
                onclick: () => console.log('show'),
            });
        }
        return buttons;
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
        $html.querySelectorAll('.remove-actor')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const button = ev.currentTarget as HTMLButtonElement;
                    const uuid = button.dataset.uuid;
                    const type = button.dataset.type;
                    if (uuid !== undefined && type !== undefined) {
                        await this.removeActor(uuid, type);
                    }
                });
            });
        $html.querySelectorAll('.advance-hours')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const target = ev.currentTarget as HTMLButtonElement;
                    const hours = target.dataset.hours ?? '0';
                    await this.advanceHours(parseInt(hours, 10));
                });
            });
        $html.querySelectorAll('.manage-recipes')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const current = await this.read();
                    await manageRecipesDialog({
                        recipes: getRecipeData().concat(current.cooking.homebrewMeals),
                        learnedRecipes: new Set(current.cooking.knownRecipes),
                        onSubmit: async (knownRecipes, deletedRecipes) => {
                            current.cooking.knownRecipes = Array.from(knownRecipes);
                            current.cooking.homebrewMeals = current.cooking.homebrewMeals
                                .filter(m => !deletedRecipes.has(m.name));
                            await this.update({cooking: current.cooking});
                        },
                    });
                });
            });
        $html.querySelectorAll('.add-recipes')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const current = await this.read();
                    await addRecipeDialog({
                        recipes: getRecipeData().concat(current.cooking.homebrewMeals),
                        onSubmit: async (recipe) => {
                            current.cooking.homebrewMeals.push(recipe);
                            await this.update({cooking: current.cooking});
                        },
                    });
                });
            });
        $html.querySelectorAll('.unlock-activities')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const current = await this.read();
                    await manageActivitiesDialog({
                        data: getCampingActivityData(),
                        lockedActivities: new Set(current.lockedActivities),
                        onSubmit: async (lockedActivities) => {
                            await this.update({lockedActivities: Array.from(lockedActivities)});
                        },
                    });
                });
            });
        $html.querySelectorAll('.clear-activities')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const current = await this.read();
                    current.campingActivities.forEach(a => a.actorUuid = null);
                    await this.update({campingActivities: current.campingActivities});
                });
            });
        $html.querySelectorAll('.roll-encounter')
            .forEach(el => {
                el.addEventListener('click', async () => await this.rollRandomEncounter(true));
            });
        $html.querySelectorAll('.check-encounter')
            .forEach(el => {
                el.addEventListener('click', async () => await this.rollRandomEncounter());
            });
        $html.querySelectorAll('.decrease-zone-dc-modifier')
            .forEach(el => {
                el.addEventListener('click', async () => {
                    const current = await this.read();
                    await this.update({encounterModifier: current.encounterModifier - 1});
                });
            });
        $html.querySelectorAll('.increase-zone-dc-modifier')
            .forEach(el => {
                el.addEventListener('click', async () => {
                    const current = await this.read();
                    await this.update({encounterModifier: current.encounterModifier + 1});
                });
            });
        $html.querySelectorAll('.reset-zone-dc-modifier')
            .forEach(el => {
                el.addEventListener('click', async () => await this.update({encounterModifier: 0}));
            });
        $html.querySelectorAll('.roll-check')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    // TODO
                });
            });
        $html.querySelectorAll('.camping-settings')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const current = await this.read();
                    campingSettingsDialog({
                        data: {
                            restRollMode: current.restRollMode,
                            gunsToClean: current.gunsToClean,
                        },
                        onSubmit: async (data) => await this.update(data),
                    });
                });
            });
        $html.querySelectorAll('.camping-actors .actor-image')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const button = ev.currentTarget as HTMLButtonElement;
                    const uuid = button.dataset.uuid;
                    if (uuid !== undefined) {
                        await this.openUuidSheet(uuid);
                    }
                });
            });
        $html.querySelectorAll('.camping-actors .actor-header')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {
                    const button = ev.currentTarget as HTMLElement;
                    const uuid = button.dataset.uuid;
                    if (uuid !== undefined) {
                        await this.openUuidJournal(uuid);
                    }
                });
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
            console.log(uuid);
            if (target.classList.contains('camping-activity')) {
                const activityName = target.dataset.name as CampingActivityName;
                await this.setActivityActor(campingConfiguration, activityName, uuid);
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
            const actor = await fromUuid(uuid);
            /* eslint-disable @typescript-eslint/no-explicit-any */
            const actorType = (actor as any)?.type;
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
        console.log(existing, data, toSave);
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
        const actor = await fromUuid(uuid);
        // @ts-ignore
        actor.sheet.render(true);
    }

    private async openUuidJournal(uuid: string): Promise<void> {
        // @ts-ignore
        const journal = await fromUuid(uuid) as JournalEntry | JournalEntryPage | null;
        // @ts-ignore
        if (journal instanceof JournalEntryPage) {
            journal?.parent?.sheet?.render(true, {pageId: journal.id});
        } else {
            journal.sheet.render();
        }

    }

    protected async _updateObject(event: Event, formData: CampingData): Promise<void> {
        console.log('formdata', formData);
        const current = await this.read();
        await this.update({
            currentRegion: formData.currentRegion,
            cooking: {
                servings: formData.servings,
                magicalSubsistenceAmount: formData.magicalSubsistenceAmount,
                subsistenceAmount: formData.subsistenceAmount,
                chosenMeal: formData.chosenMeal,
                homebrewMeals: current.cooking.homebrewMeals,
                knownRecipes: current.cooking.knownRecipes,
                degreeOfSuccess: this.parseFormDegreeOfSuccess(formData.mealDegreeOfSuccess),
                actorMeals: [], // FIXME
            },
        });
        this.render();
    }

    private parseFormDegreeOfSuccess(formData: string | null): StringDegreeOfSuccess | null {
        if (formData === null || formData.length === 0 || formData === '-') {
            return null;
        }
        return formData as StringDegreeOfSuccess;
    }
}

export function openCampingSheet(game: Game): void {
    new CampingSheet({game}).render(true);
}


