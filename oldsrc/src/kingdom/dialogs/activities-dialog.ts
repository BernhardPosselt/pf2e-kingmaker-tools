import {listenClick} from '../../utils';
import {getKingdom, saveKingdom} from '../storage';
import {Kingdom} from '../data/kingdom';
import {getKingdomActivities, KingdomActivity} from '../data/activityData';
import {addActivityDialog} from './add-activity-dialog';


interface ManageKingdomActivitiesOptions {
    game: Game;
    actor: Actor;
}

interface ActivityData {
    activities: ViewActivity[];
}

interface ViewActivity {
    enabled: boolean;
    isHomebrew: boolean;
    id: string;
    title: string;
}

class ManageKingdomActivitiesDialog extends FormApplication<FormApplicationOptions & ManageKingdomActivitiesOptions, object, null> {
    private game: Game;
    private actor: Actor;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-manage-kingdom-activities';
        options.title = 'Manage Kingdom Activities';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/manage-activities.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = [];
        options.height = 'auto';
        return options;
    }

    constructor(object: null, options: Partial<FormApplicationOptions> & ManageKingdomActivitiesOptions) {
        super(object, options);
        this.game = options.game;
        this.actor = options.actor;
    }

    override getData(): Promise<ActivityData> | ActivityData {
        const kingdom = getKingdom(this.actor);
        return {
            activities: this.buildActivities(kingdom),
        };
    }

    protected async _updateObject(event: Event, formData: Record<string, boolean>): Promise<void> {
        await saveKingdom(this.actor, {
            activityBlacklist: Object.entries(formData)
                .filter(([, enabled]) => !enabled)
                .map(([id]) => id),
        });
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        listenClick($html, '.add-activity', async (): Promise<void> => {
            const kingdom = getKingdom(this.actor);
            addActivityDialog({
                onOk: this.onSave.bind(this),
                homebrewActivities: kingdom.homebrewActivities,
            });
        });
        listenClick($html, '.edit-activity', async (ev: Event): Promise<void> => {
            const kingdom = getKingdom(this.actor);
            const target = ev.currentTarget as HTMLButtonElement;
            const id = target.dataset.id!;
            const activity = kingdom.homebrewActivities.find(a => a.id === id);
            addActivityDialog({
                onOk: this.onSave.bind(this),
                activity: activity,
                homebrewActivities: kingdom.homebrewActivities,
            });
        });
        listenClick($html, '.delete-activity', async (ev: Event): Promise<void> => {
            const target = ev.currentTarget as HTMLButtonElement;
            const id = target.dataset.id!;
            const kingdom = getKingdom(this.actor);
            await saveKingdom(this.actor, {
                homebrewActivities: kingdom.homebrewActivities.filter(a => a.id !== id),
            });
            this.render();
        });
    }

    private async onSave(activity: KingdomActivity): Promise<void> {
        const kingdom = getKingdom(this.actor);
        const homebrewActivities = [
            ...kingdom.homebrewActivities.filter(a => a.id !== activity.id),
            activity,
        ];
        await saveKingdom(this.actor, {
            homebrewActivities,
        });
        this.render();
    }

    private buildActivities(kingdom: Kingdom): ViewActivity[] {
        const blacklist = new Set(kingdom.activityBlacklist);
        const homebrewIds = new Set(kingdom.homebrewActivities.map(a => a.id));
        const kingdomActivities = getKingdomActivities(kingdom.homebrewActivities);
        return kingdomActivities
            .map(a => {
                return {
                    enabled: !blacklist.has(a.id),
                    title: a.title,
                    id: a.id,
                    isHomebrew: homebrewIds.has(a.id),
                };
            })
            .sort((a, b) => a.title.localeCompare(b.title));
    }
}


export function manageKingdomActivitiesDialog(game: Game, actor: Actor): void {
    new ManageKingdomActivitiesDialog(null, {game, actor}).render(true);
}