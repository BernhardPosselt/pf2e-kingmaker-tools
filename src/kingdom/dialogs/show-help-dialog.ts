import {Activity} from '../data/activities';
import {findHelp} from '../data/activityData';
import {capitalize} from '../../utils';
import {rankToLabel} from '../modifiers';
import {updateResources} from '../resources';

interface HelpOptions {
    activity: Activity;
}

class HelpApplication extends Application<ApplicationOptions & HelpOptions> {
    private activity: Activity;

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

    constructor(options: Partial<ApplicationOptions> & HelpOptions) {
        super(options);
        this.activity = options.activity;
    }

    async getData(options?: Partial<ApplicationOptions>): Promise<unknown> {
        const data = findHelp(this.activity);
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
            companion: await this.enrichIfDefined(data.companion),
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
                await updateResources(target);
            });
        });
    }

    private async enrichIfDefined(msg: string | undefined): Promise<string | undefined> {
        if (msg) {
            return await TextEditor.enrichHTML(msg);
        }
    }
}

export async function showHelpDialog(activity: Activity): Promise<void> {
    new HelpApplication({activity}).render(true);
}
