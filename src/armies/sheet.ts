import {calculateArmyAdjustments, getDefaultArmyAdjustment} from './utils';
import {getArmyAdjustment, saveArmyAdjustment} from './storage';
import {ArmyAdjustments} from './data';
import {disableArmyDialog} from './dialogs/disable';
import {showArmyHelpDialog} from './dialogs/help';
import {armySetupDialog} from './dialogs/army-setup';

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
        const adjustments = getArmyAdjustment(this.actor)!;
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

    protected _getHeaderButtons(): Application.HeaderButton[] {
        const buttons = super._getHeaderButtons();
        if (this.game.user?.isGM ?? false) {
            buttons.unshift({
                label: 'Help',
                class: 'something-made-up-help',
                icon: 'fas fa-question',
                onclick: () => showArmyHelpDialog(),
            });
            buttons.unshift({
                label: 'Disable',
                class: 'something-made-up-disable',
                icon: 'fas fa-trash',
                onclick: () => disableArmyDialog({
                    actor: this.actor,
                    onYes: async (): Promise<void> => {
                        await this.close();
                        await this.actor.unsetFlag('pf2e-kingmaker-tools', 'army-adjustment');
                    },
                }),
            });
        }
        return buttons;
    }
}


async function initArmy(actor: Actor, additionalUuids: string[], type: string): Promise<ArmyAdjustments> {
    const defaultItems = await Promise.all([
            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-actions.Item.59vwuUVFAEs0yfKt', // advance
            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-actions.Item.qNxyZfS4Ald098Gy', // battle
            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-actions.Item.21aSK6Dh0FpcdWbb', // disengage
            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-actions.Item.wfy7fmnnhlJ66Gfz', // guard
            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-actions.Item.kKnpTt39LwP8uUvf', // rally
            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-actions.Item.D680ctCFIpsmz82i', // retreat
            ...additionalUuids,
        ].map(uuid => fromUuid(uuid) as Promise<Item>),
    );
    const data = defaultItems.map(i => i.toObject());
    await actor.createEmbeddedDocuments('Item', data);
    await actor.update({'system.details.blurb': type});
    await saveArmyAdjustment(actor, getDefaultArmyAdjustment());
    return getArmyAdjustment(actor)!;
}

export async function editArmyStatistics(game: Game, actor: Actor): Promise<void> {
    if (actor) {
        if (getArmyAdjustment(actor) === undefined) {
            armySetupDialog({
                actor,
                onConfirm: async (additionalUuids, type): Promise<void> => {
                    await initArmy(actor, additionalUuids, type);
                    await editArmyStatistics(game, actor);
                },
            });
        } else {
            new ArmySheet(null, {game, actor}).render(true);
        }
    } else {
        ui.notifications?.error('Please select a token');
    }
}
