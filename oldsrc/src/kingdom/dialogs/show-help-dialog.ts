import {getKingdomActivitiesById} from '../data/activityData';
import {capitalize} from '../../utils';
import {rankToLabel} from '../modifiers';
import {updateResources} from '../resources';
import {getKingdom} from '../storage';

interface HelpOptions {
    activity: string;
    game: Game;
    actor: Actor;
}

class HelpApplication extends Application<ApplicationOptions & HelpOptions> {
    private activity: string;
    private game: Game;
    private actor: Actor;

    constructor(options: Partial<ApplicationOptions> & HelpOptions) {
        super(options);
        this.activity = options.activity;
        this.game = options.game;
        this.actor = options.actor;
    }

    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'help-app';
        options.title = 'Help';
        options.template = 'modules/pf2e-kingmaker-tools/templates/kingdom/help.hbs';
        options.classes = ['kingmaker-tools-app', 'help-app'];
        options.width = 500;
        options.height = 'auto';
        return options;
    }

    async getData(): Promise<unknown> {
        const kingdom = getKingdom(this.actor);
        const data = getKingdomActivitiesById(kingdom.homebrewActivities)[this.activity]!;
        const traits = (data.fortune ? [capitalize(data.phase), 'Downtime', 'Fortune'] : [capitalize(data.phase), 'Downtime'])
            .join(', ');
        const skills = Object.entries(data.skills)
            .map(([skill, rank]) => {
                if (rank === 0) {
                    return capitalize(skill);
                } else {
                    return `${capitalize(skill)}: ${rankToLabel(rank)}`;
                }
            })
            .join(', ');
        return {
            ...data,
            criticalSuccess: await this.enrichIfDefined(data.criticalSuccess?.msg),
            success: await this.enrichIfDefined(data.success?.msg),
            failure: await this.enrichIfDefined(data.failure?.msg),
            criticalFailure: await this.enrichIfDefined(data.criticalFailure?.msg),
            description: await this.enrichIfDefined(data.description),
            requirement: await this.enrichIfDefined(data.requirement),
            title: data.title,
            special: data.special,
            traits,
            skills,
        };
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        const $html = html[0];
        $html.querySelector('#km-close-help')
            ?.addEventListener('click', async () => {
                await this.close();
            });
        $html.querySelectorAll('.km-gain-lose')?.forEach(el => {
            el.addEventListener('click', async (event) => {
                event.preventDefault();
                const target = event.currentTarget as HTMLButtonElement;
                await updateResources(this.game, this.actor, target);
            });
        });
        // bind pf2e flat checks :/
        $html.querySelectorAll('[data-pf2-check]')?.forEach(el => {
            el.addEventListener('click', async (event) => {
                event.preventDefault();
                const target = event.currentTarget as HTMLSpanElement;
                const dc = target.dataset.pf2Dc ?? 1;
                /* eslint-disable @typescript-eslint/no-explicit-any */
                const pf2eGame = this.game as any;
                const checkModifier = new pf2eGame.pf2e.CheckModifier('Flat Check', {modifiers: []});
                await pf2eGame.pf2e.Check.roll(checkModifier, {type: 'flat-check', dc: {value: dc}});
            });
        });
    }

    private async enrichIfDefined(msg: string | undefined): Promise<string | undefined> {
        if (msg) {
            /* eslint-disable @typescript-eslint/no-explicit-any */
            const mode = {async: true} as any;
            return await TextEditor.enrichHTML(msg, mode);
        }
    }
}

export async function showHelpDialog(game: Game, actor: Actor, activity: string): Promise<void> {
    new HelpApplication({activity, actor, game}).render(true);
}
