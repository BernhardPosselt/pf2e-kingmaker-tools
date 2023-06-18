import {toViewActors, toViewCampingActivities, toViewPrepareCamp, ViewCampingData} from './view';
import {getNumberSetting, getStringSetting, setSetting} from '../settings';
import {formatHours} from '../time/app';
import {RandomEncounterFormData} from './random-encounters';
import {regions} from './regions';
import {CampingActivityName, getCampingActivityData} from './activities';
import {calculateRestSeconds, Camping, getActorConsumables, getDefaultConfiguration} from './camping';

interface CampingOptions {
    game: Game;
}

type CampingData = RandomEncounterFormData

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
            dropSelector: '.new-camping-actor, .camping-activity, .prepare-camp-actor',
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
        const currentSeconds = this.game.time.worldTime;
        const startedSeconds = getNumberSetting(this.game, 'stopWatchStart');
        const currentEncounterDCModifier = getNumberSetting(this.game, 'currentEncounterDCModifier');
        const currentRegion = getStringSetting(this.game, 'currentRegion') || 'Rostland';
        const currentRegionData = regions.get(currentRegion);
        const sumElapsedSeconds = Math.abs(currentSeconds - startedSeconds);
        const data = await this.read();
        const isGM = this.game.user?.isGM ?? false;
        const isUser = !isGM;
        const activityData = getCampingActivityData();
        const actors = await Promise.all(data.actorUuids.map(a => fromUuid(a))) as Actor[];
        console.log(data);
        const watchSecondsDuration = calculateRestSeconds(actors.length);
        return {
            isGM,
            isUser,
            ...(await getActorConsumables(actors)),
            currentEncounterDCModifier,
            encounterDC: currentEncounterDCModifier + (currentRegionData?.encounterDC ?? 0),
            adventuringSince: formatHours(sumElapsedSeconds, startedSeconds > currentSeconds),
            regions: Array.from(regions.keys()),
            currentRegion,
            prepareCamp: await toViewPrepareCamp(data.campingActivities, activityData),
            actors: await toViewActors(data.actorUuids),
            campingActivities: await toViewCampingActivities(data.campingActivities, activityData, new Set(data.lockedActivities)),
            watchSecondsElapsed: data.watchSecondsElapsed,
            watchElapsed: formatHours(data.watchSecondsElapsed),
            watchSecondsDuration,
            watchDuration: formatHours(watchSecondsDuration),
            subsistenceAmount: data.cooking.subsistenceAmount,
            magicalSubsistenceAmount: data.cooking.magicalSubsistenceAmount,
            servings: data.cooking.servings,
        };
    }

    protected _getHeaderButtons(): Application.HeaderButton[] {
        const buttons = super._getHeaderButtons();
        if (this.game.user?.isGM ?? false) {
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

    private onUpdateWorldTime(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        Hooks.on('updateWorldTime', this.onUpdateWorldTime.bind(this));
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
        $html.querySelectorAll('.roll-encounter')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {

                });
            });
        $html.querySelectorAll('.add-activities')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {

                });
            });
        $html.querySelectorAll('.unlock-activities')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {

                });
            });
        $html.querySelectorAll('.clear-activities')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {

                });
            });
        $html.querySelectorAll('.roll-check')
            .forEach(el => {
                el.addEventListener('click', async (ev) => {

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

    override close(options?: Application.CloseOptions): Promise<void> {
        Hooks.off('updateWorldTime', this.render);
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
            if (target.classList.contains('prepare-camp-actor')) {
                await this.setActivityActor(campingConfiguration, 'Prepare Campsite', uuid);
            } else if (target.classList.contains('camping-activity')) {
                const activityName = target.dataset.name as CampingActivityName;
                await this.setActivityActor(campingConfiguration, activityName, uuid);
            } else if (target.classList.contains('new-camping-actor') && !campingConfiguration.actorUuids.includes(uuid)) {
                await this.update({actorUuids: [...(campingConfiguration.actorUuids), uuid]});
            }
        }
    }

    private async setActivityActor(campingConfiguration: Camping, activityName: CampingActivityName, uuid: string): Promise<void> {
        const data = getCampingActivityData().find(a => a.name === activityName);
        const activity = campingConfiguration.campingActivities
            .find(a => a.activity === activityName);
        if (activity) {
            activity.actorUuid = uuid;
        } else {
            campingConfiguration.campingActivities.push({
                activity: activityName,
                actorUuid: uuid,
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
            return getDefaultConfiguration();
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

    protected async _updateObject(event: Event, formData: RandomEncounterFormData): Promise<void> {
        // const modifier = formData?.currentEncounterDCModifier ?? 0;
        const region = formData?.currentRegion ?? 'Rostland';
        await setSetting(this.game, 'currentRegion', region);
        // await setSetting(this.game, 'currentEncounterDCModifier', modifier);
        this.render();
    }
}

export function openCampingSheet(game: Game): void {
    new CampingSheet({game}).render(true);
}


