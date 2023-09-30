import {getDefaultArmyAdjustment} from './utils';
import {getArmyAdjustment} from './storage';
import {ArmyAdjustments} from './data';

interface ArmyOptions {
    game: Game;
    actor: Actor;
}

class ArmySheet extends FormApplication<FormApplicationOptions & ArmyOptions, object, null> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'army-app';
        options.title = 'Army';
        options.template = 'modules/pf2e-kingmaker-tools/templates/army/sheet.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'army-app'];
        options.width = 450;
        options.height = 'auto';
        options.scrollY = ['.km-content', '.km-sidebar'];
        return options;
    }

    private actor: Actor;
    private readonly game: Game;


    constructor(object: null, options: Partial<FormApplicationOptions> & ArmyOptions) {
        super(object, options);
        this.game = options.game;
        this.actor = options.actor;
        this.actor.apps[this.appId] = this;
    }

    override async getData(): Promise<{ adjustments: ArmyAdjustments }> {
        const adjustments = getArmyAdjustment(this.actor) || getDefaultArmyAdjustment();
        return {
            adjustments,
        };
    }


    /* eslint-disable @typescript-eslint/no-explicit-any */
    override async _updateObject(event: Event, formData: any): Promise<void> {
        const update = {
            melee: formData.melee ?? 0,
            ac: formData.melee ?? 0,
            maneuver: formData.maneuver ?? 0,
            morale: formData.morale ?? 0,
            scouting: formData.scouting ?? 0,
            ranged: formData.ranged ?? 0,
            recruitmentDC: formData.recruitmentDC ?? 0,
        };
        console.log(update);
        // await saveArmyAdjustment(this.actor, update);
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
    }
}


export async function editArmyStatistics(game: Game, actor: Actor): Promise<void> {
    if (actor) {
        new ArmySheet(null, {game, actor}).render(true);
    } else {
        ui.notifications?.error('Please select a token');
    }
}
