import {getStringSetting, setSetting} from '../settings';
import {getDefaultKingdomData, Kingdom, SkillRanks} from './data';
import {capitalize} from '../utils';
import {calculateSkills} from './skills';

export function getKingdom(game: Game): Kingdom {
    const kingdomString = getStringSetting(game, 'kingdom');
    return kingdomString === '' ? getDefaultKingdomData() : JSON.parse(kingdomString);
}

export async function saveKingdom(game: Game, kingdom: Kingdom): Promise<void> {
    return await setSetting(game, 'kingdom', JSON.stringify(kingdom));
}

interface KingdomOptions {
    game: Game;
}

type KingdomTabs = 'status' | 'skills' | 'turn' | 'feats' | 'trade-agreements';

class KingdomApp extends FormApplication<FormApplicationOptions & KingdomOptions, object, null> {
    static override get defaultOptions(): FormApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'kingdom-app';
        options.title = 'Kingdom';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/sheet.hbs';
        options.submitOnChange = true;
        options.closeOnSubmit = false;
        options.classes = ['kingmaker-tools-app', 'kingdom-app'];
        options.width = 800;
        options.height = 'auto';
        return options;
    }

    private readonly game: Game;
    private nav: KingdomTabs = 'turn';

    constructor(object: null, options: Partial<FormApplicationOptions> & KingdomOptions) {
        super(object, options);
        this.game = options.game;
    }

    override getData(options?: Partial<FormApplicationOptions>): object {
        const isGM = this.game.user?.isGM ?? false;
        const kingdomData = getDefaultKingdomData();
        return {
            ...super.getData(options),
            isGM,
            isUser: !isGM,
            ...this.getActiveTabs(),
            skills: calculateSkills({
                ruin: kingdomData.ruin,
                skillRanks: kingdomData.skillRanks,
                leaders: kingdomData.leaders,
                abilityScores: kingdomData.abilityScores,
                unrest: kingdomData.unrest,
                kingdomLevel: kingdomData.level,
            }),
            tradeAgreementsSize: kingdomData.tradeAgreements.filter(t => t.relations === 'trade-agreement').length,
            ranks: [
                {label: 'Untrained', rank: 0},
                {label: 'Trained', rank: 1},
                {label: 'Expert', rank: 2},
                {label: 'Master', rank: 3},
                {label: 'Legendary', rank: 4},
            ],
        };
    }

    private buildSkills(skills: SkillRanks): object[] {
        return Object.entries(skills)
            .map(([skill, rank]) => {
                return {
                    name: capitalize(skill),
                    rank,
                    value: 0,
                };
            });
    }

    private getActiveTabs(): object {
        return {
            statusTab: this.nav === 'status',
            skillsTab: this.nav === 'skills',
            turnTab: this.nav === 'turn',
            tradeAgreementsTab: this.nav === 'trade-agreements',
            featsTab: this.nav === 'feats',
        };
    }

    override async _updateObject(event: Event, formData: Kingdom): Promise<void> {
        await saveKingdom(this.game, formData);
        this.render();
    }

    public sceneChange(): void {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('canvasReady', this.sceneChange.bind(this));
        Hooks.on('createToken', this.sceneChange.bind(this));
        Hooks.on('deleteToken', this.sceneChange.bind(this));
        const $html = html[0];
        $html.querySelectorAll('.km-nav a')?.forEach(el => {
            el.addEventListener('click', (event) => {
                const tab = event.target as HTMLAnchorElement;
                this.nav = tab.dataset.tab as KingdomTabs;
                this.render();
            });
        });
    }

    override close(options?: FormApplication.CloseOptions): Promise<void> {
        Hooks.off('canvasReady', this.sceneChange);
        Hooks.off('createToken', this.sceneChange);
        Hooks.off('deleteToken', this.sceneChange);
        return super.close(options);
    }

}

export async function showKingdom(game: Game): Promise<void> {
    new KingdomApp(null, {game}).render(true);
}
