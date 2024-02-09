import {listenClick} from '../../utils';
import {CampingActivityName} from '../activities';
import {getCampingActivityData} from '../camping';
import {getCamping, saveCamping} from '../storage';
import {addActivityDialog} from './add-activity';

interface ManageActivitiesOptions {
    game: Game;
    actor: Actor;
}

interface ViewActivity {
    isHomebrew: boolean;
    name: string;
    enabled: boolean;
}

interface ManageActivityView {
    activities: ViewActivity[];
}

type ManageActivitiesFormData = Record<string, boolean>;

class ManageCampingActivities extends FormApplication<FormApplicationOptions & ManageActivitiesOptions, object, null> {
    private game: Game;
    private actor: Actor;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-manage-camping-activities';
        options.title = 'Manage Camping Activities';
        options.template = 'modules/pf2e-kingmaker-tools/templates/camping/manage-activities.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        return options;
    }

    constructor(object: null, options: Partial<FormApplicationOptions> & ManageActivitiesOptions) {
        super(object, options);
        this.game = options.game;
        this.actor = options.actor;
    }

    override async getData(): Promise<ManageActivityView> {
        const camping = getCamping(this.actor);
        const activities = getCampingActivityData(camping);
        const lockedActivities = new Set(camping.lockedActivities);
        return {
            activities: activities
                .sort((a, b) => a.name.localeCompare(b.name))
                .map(a => {
                    return {
                        name: a.name,
                        isHomebrew: !!a.isHomebrew,
                        enabled: !lockedActivities.has(a.name),
                    };
                }),
        };
    }

    protected async _updateObject(event: Event, formData: ManageActivitiesFormData): Promise<void> {
        console.log(formData);
        await saveCamping(this.game, this.actor, {
            lockedActivities: Object.entries(formData)
                .filter(([, enabled]) => !enabled)
                .map(([name]) => name) as CampingActivityName[],
        });
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '.add-activity', async (): Promise<void> => {
            const camping = getCamping(this.actor);
            await addActivityDialog({
                homebrewActivities: camping.homebrewCampingActivities,
                onSubmit: async (activity): Promise<void> => {
                    await saveCamping(this.game, this.actor, {
                        homebrewCampingActivities: [...camping.homebrewCampingActivities, activity],
                    });
                    this.render();
                },
            });
        });
        listenClick($html, '.edit-activity', async (ev: Event): Promise<void> => {
            const button = ev.currentTarget as HTMLButtonElement;
            const name = button.dataset.name!;
            const camping = getCamping(this.actor);
            await addActivityDialog({
                activity: camping.homebrewCampingActivities.find(a => a.name === name),
                homebrewActivities: camping.homebrewCampingActivities,
                onSubmit: async (activity): Promise<void> => {
                    await saveCamping(this.game, this.actor, {
                        homebrewCampingActivities: [...camping.homebrewCampingActivities.filter(a => a.name !== name), activity],
                    });
                    this.render();
                },
            });
        });
        listenClick($html, '.delete-activity', async (ev: Event): Promise<void> => {
            const button = ev.currentTarget as HTMLButtonElement;
            const name = button.dataset.name!;
            const camping = getCamping(this.actor);
            await saveCamping(this.game, this.actor, {
                homebrewCampingActivities: camping.homebrewCampingActivities.filter(a => a.name !== name),
            });
            this.render();
        });
    }
}


export async function manageActivitiesDialog(options: ManageActivitiesOptions): Promise<void> {
    new ManageCampingActivities(null, options).render(true);
}
