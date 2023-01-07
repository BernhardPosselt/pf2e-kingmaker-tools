import {getWorldTime, TimeChangeMode, TimeOfDay} from './calculation';
import {DateTime} from 'luxon';
import {getNumberSetting, setSetting} from '../settings';
import {createUUIDLink, postDegreeOfSuccessMessage} from '../utils';

function tpl(previousTime: string): string {
    return `<form>
        <input type="time" value="${previousTime}">
    </form>`;
}


async function toTimeOfDay(game: Game, body: HTMLElement, mode: TimeChangeMode): Promise<void> {
    const timeInput = body.querySelector('input[type=time]') as HTMLInputElement;
    const timeValue = DateTime.fromFormat(timeInput.value, 'HH:mm');
    const currentTime = getWorldTime(game);
    const diff = new TimeOfDay({
        hour: timeValue.hour,
        minute: timeValue.minute,
        second: timeValue.second,
    });
    const seconds = diff.diffSeconds(currentTime, mode);
    await game.time.advance(seconds);
    localStorage.setItem('kingmaker-tools.time-input', timeInput.value);
}

export async function toTimeOfDayMacro(game: Game): Promise<void> {
    const previousTime = localStorage.getItem('kingmaker-tools.time-input') ?? '00:00';
    new Dialog({
        title: 'Advance/Retract to Time of Day',
        content: tpl(previousTime),
        buttons: {
            retract: {
                icon: '<i class="fa-solid fa-backward"></i>',
                label: 'Retract',
                callback: async (html): Promise<void> => {
                    await toTimeOfDay(game, html as HTMLElement, TimeChangeMode.RETRACT);
                },
            },
            advance: {
                icon: '<i class="fa-solid fa-forward"></i>',
                label: 'Advance',
                callback: async (html): Promise<void> => {
                    await toTimeOfDay(game, html as HTMLElement, TimeChangeMode.ADVANCE);
                },
            },
        },
        default: 'yes',
    }, {
        jQuery: false,
    }).render(true);
}

interface StopWatchOptions {
    game: Game;
}

class StopWatchApplication extends Application<object & ApplicationOptions> {
    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'stopwatch-app';
        options.title = 'Stopwatch';
        options.template = 'modules/pf2e-kingmaker-tools/templates/stopwatch.html';
        options.classes = ['kingmaker-tools-app'];
        options.width = 100;
        return options;
    }

    private readonly game: Game;

    constructor(options: Partial<FormApplicationOptions> & StopWatchOptions) {
        super(options);
        this.game = options.game;
    }

    override getData(options?: Partial<ApplicationOptions> & StopWatchOptions): object {
        return {
            ...super.getData(options),
            ...this.getElapsedTime(),
        };
    }

    private getElapsedTime(): {seconds: number, formatted: string} {
        const currentSeconds = this.game.time.worldTime;
        const startedSeconds = getNumberSetting(this.game, 'stopWatchStart');
        const sumElapsedSeconds = currentSeconds - startedSeconds;
        const elapsedHours = Math.floor(sumElapsedSeconds / 3600);
        const elapsedMinutes = Math.floor((sumElapsedSeconds % 3600) / 60);
        // const elapsedSeconds = sumElapsedSeconds % 60;
        return {
            seconds: sumElapsedSeconds,
            formatted: `${this.padZero(elapsedHours)}:${this.padZero(elapsedMinutes)}`,
        };
    }

    private padZero(num: number): string {
        return `${num}`.padStart(2, '0');
    }

    private async reset(): Promise<void> {
        return await setSetting(this.game, 'stopWatchStart', this.game.time.worldTime);
    }

    private async advance(): Promise<void> {
        this.render();
    }

    override activateListeners(html: JQuery): void {
        super.activateListeners(html);
        Hooks.on('updateWorldTime', this.advance.bind(this));
        const resetButton = html[0].querySelector('#reset-button') as HTMLButtonElement;
        resetButton?.addEventListener('click', async () => {
            await this.reset();
            this.render();
        });
    }

    override close(options?: Application.CloseOptions): Promise<void> {
        Hooks.off('updateWorldTime', this.advance);
        return super.close(options);
    }
}
export async function stopWatch(game: Game): Promise<void> {
    new StopWatchApplication({game}).render(true);
}
