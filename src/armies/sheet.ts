import {calculateArmyAdjustments, getDefaultArmyAdjustment} from './utils';
import {getArmyAdjustment, saveArmyAdjustment} from './storage';
import {ArmyAdjustments} from './data';

interface ArmyOptions {
    game: Game;
    actor: Actor;
}

interface ArmyData {
    adjustments: ArmyAdjustments;
    actorLevel: number,
    calculated: ArmyAdjustments;
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

    override async getData(): Promise<ArmyData> {
        const adjustments = getArmyAdjustment(this.actor) || getDefaultArmyAdjustment();
        const actorLevel = this.actor.system.details.level.value;
        return {
            adjustments,
            actorLevel,
            calculated: calculateArmyAdjustments(this.actor, actorLevel, adjustments),
        };
    }


    /* eslint-disable @typescript-eslint/no-explicit-any */
    override async _updateObject(event: Event, formData: any): Promise<void> {
        const data = expandObject(formData);
        console.log('form', formData);
        const update = {
            melee: data.adjustments?.melee ?? 0,
            ac: data.adjustments?.ac ?? 0,
            maneuver: data.adjustments?.maneuver ?? 0,
            morale: data.adjustments?.morale ?? 0,
            scouting: data.adjustments?.scouting ?? 0,
            ranged: data.adjustments?.ranged ?? 0,
            recruitmentDC: data.adjustments?.recruitmentDC ?? 0,
        };
        console.log(update);
        await saveArmyAdjustment(this.actor, update);
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
