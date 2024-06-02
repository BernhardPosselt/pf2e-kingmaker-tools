import {CheckDialog} from './check-dialog';
import {DegreeOfSuccess} from '../../degree-of-success';
import {getArmyTactics, isArmyTactic} from '../../armies/utils';
import {createUUIDLink, getLevelBasedDC, hasArmyTactic} from '../../utils';
import {Kingdom} from '../data/kingdom';

interface ArmyTacticsBrowserOptions {
    game: Game;
    army: Actor & ArmyActor;
    kingdom: Kingdom;
    sheetActor: Actor;
    onRoll: (consumeModifiers: Set<string>) => Promise<void>;
}

interface ArmyTacticView {
    link: string;
    name: string;
    level: number;
    dc: number;
    enabled: boolean;
    uuid: string;
}

interface ArmyBrowserData {
    tactics: ArmyTacticView[];
}

class ArmyTacticsBrowserApp extends FormApplication<
    FormApplicationOptions & ArmyTacticsBrowserOptions,
    object,
    null
> {
    private game: Game;
    private army: Actor & ArmyActor;
    private kingdom: Kingdom;
    private sheetActor: Actor;
    private onRoll: (consumeModifiers: Set<string>) => Promise<void>;

    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'army-tactics-browser-app';
        options.title = 'Army Tactics Browser';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/army-tactics-browser.hbs';
        options.classes = ['kingmaker-tools-app', 'army-tactics-browser-app'];
        options.height = 'auto';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        return options;
    }

    constructor(options: Partial<ApplicationOptions> & ArmyTacticsBrowserOptions) {
        super(null, options);
        this.game = options.game;
        this.army = options.army;
        this.kingdom = options.kingdom;
        this.sheetActor = options.sheetActor;
        this.onRoll = options.onRoll;
    }

    override async getData(): Promise<ArmyBrowserData> {
        const tactics = await getArmyTactics(this.game);
        return {
            tactics: await this.toViewTactics(tactics),
        };
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelectorAll('.km-tactic-link')
            .forEach(el => el.addEventListener('click', async (ev): Promise<void> => {
                ev.preventDefault();
                ev.stopPropagation();
                const target = ev.currentTarget as HTMLElement;
                const uuid = target.dataset.uuid!;
                const item = await fromUuid(uuid) as Item | null;
                item?.sheet?.render(true);
            }));
        $html.querySelectorAll('.km-train-tactic')
            .forEach(el => el.addEventListener('click', (ev) => this.trainTactic(ev)));
    }

    /* eslint-disable @typescript-eslint/no-explicit-any */
    protected async _updateObject(event: Event, formData: any): Promise<void> {
        console.log(formData);
        this.render();
    }

    private async trainTactic(ev: Event): Promise<void> {
        const button = ev.currentTarget as HTMLElement;
        const id = button.dataset.id!;
        const item: Item<unknown> | null = await fromUuid(id) as Item<unknown> | null;
        if (item !== null && isArmyTactic(item)) {
            const tacticLink = await TextEditor.enrichHTML(createUUIDLink(item.uuid));
            new CheckDialog(null, {
                activity: 'train-army',
                kingdom: this.kingdom,
                dc: getLevelBasedDC(item.system.level.value),
                game: this.game,
                type: 'activity',
                onRoll: this.onRoll,
                actor: this.sheetActor,
                afterRoll: async (): Promise<void> => {
                    await this.close();
                },
                additionalChatMessages: [{
                    [DegreeOfSuccess.CRITICAL_SUCCESS]: tacticLink,
                    [DegreeOfSuccess.SUCCESS]: tacticLink,
                }],
            }).render(true);
        }
    }

    private async toViewTactics(tactics: CampaignFeaturePF2E[]): Promise<ArmyTacticView[]> {
        return await Promise.all(tactics
            .filter(t => t.system.traits.value.includes(this.army.system.traits.type) && !hasArmyTactic(this.army, t))
            .sort((a, b) => a.system.level.value - b.system.level.value || a.name.localeCompare(b.name))
            .map(async (t) => {
                const level = t.system.level.value;
                return {
                    enabled: level <= this.army.level,
                    link: await TextEditor.enrichHTML(createUUIDLink(t.uuid)),
                    level,
                    dc: getLevelBasedDC(level),
                    name: t.name,
                    uuid: t.uuid,
                };
            }));
    }
}

export async function showArmyTacticsBrowser(options: ArmyTacticsBrowserOptions): Promise<void> {
    new ArmyTacticsBrowserApp(options).render(true);
}
