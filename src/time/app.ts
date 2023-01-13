import {getWorldTime, TimeChangeMode, TimeOfDay} from './calculation';
import {DateTime} from 'luxon';
import {getNumberSetting, setSetting} from '../settings';

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

function formatHours(seconds: number, isNegative = false): string {
    const padZero = (num: number): string => `${num}`.padStart(2, '0');
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${isNegative ? '-' : ''}${padZero(hours)}:${padZero(minutes)}`;
}

class StopWatchApplication extends Application<object & ApplicationOptions> {
    static override get defaultOptions(): ApplicationOptions {
        const options = super.defaultOptions;
        options.id = 'stopwatch-app';
        options.title = 'Stopwatch';
        options.template = 'modules/pf2e-kingmaker-tools/templates/stopwatch.html';
        options.classes = ['kingmaker-tools-app'];
        options.width = 300;
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

    private getElapsedTime(): { seconds: number, formatted: string } {
        const currentSeconds = this.game.time.worldTime;
        const startedSeconds = getNumberSetting(this.game, 'stopWatchStart');
        const sumElapsedSeconds = Math.abs(currentSeconds - startedSeconds);
        return {
            seconds: sumElapsedSeconds,
            formatted: formatHours(sumElapsedSeconds, startedSeconds > currentSeconds),
        };
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

        const prepsButton = html[0].querySelector('#daily-preps-button') as HTMLButtonElement;
        prepsButton?.addEventListener('click', async () => {
            const gunsToClean = getNumberSetting(this.game, 'gunsToClean');
            new Dialog({
                title: 'Daily Preparations',
                content: `<p class="input-row"><label for="guns-to-clean">Guns to Clean</label><input id="guns-to-clean" type="number" value="${gunsToClean}"></p>`,
                buttons: {
                    advance: {
                        icon: '<i class="fa-solid fa-forward"></i>',
                        label: 'Advance Time',
                        callback: async (html): Promise<void> => {
                            const $html = html as HTMLElement;
                            const gunsToClean = parseInt($html.querySelector('input')?.value ?? '0', 10);
                            await setSetting(this.game, 'gunsToClean', gunsToClean);
                            const seconds = gunsToClean === 0 ? 30 * 60 : Math.ceil(gunsToClean / 4) * 3600;
                            await this.game.time.advance(seconds);
                            await ChatMessage.create({content: `Daily Preparations completed in ${formatHours(seconds)}`});
                            await this.reset();
                            this.render();
                        },
                    },
                },
                default: 'yes',
            }, {
                jQuery: false,
                width: 200,
                classes: ['kingmaker-tools-app'],
            }).render(true);
        });

        const restButton = html[0].querySelector('#rest-button') as HTMLButtonElement;
        restButton?.addEventListener('click', async () => {
            const partySize = getNumberSetting(this.game, 'partySize');
            new Dialog({
                title: 'Rest',
                content: `<p class="input-row"><label for="party-size">Party Size</label><input id="party-size" type="number" value="${partySize}"></p>`,
                buttons: {
                    advance: {
                        icon: '<i class="fa-solid fa-forward"></i>',
                        label: 'Advance Time',
                        callback: async (html): Promise<void> => {
                            const $html = html as HTMLElement;
                            const partySize = parseInt($html.querySelector('input')?.value ?? '0', 10);
                            if (partySize < 2) {
                                ui.notifications?.error('No watch possible with less than 2 characters');
                            } else {
                                await setSetting(this.game, 'partySize', partySize);
                                let seconds = 8 * 3600 + Math.floor(8 * 3600 / (partySize - 1));
                                // round up seconds to next minute
                                if (seconds % 60 !== 0) {
                                    seconds += 60 - (seconds % 60);
                                }
                                await this.game.time.advance(seconds);
                                await ChatMessage.create({content: `Rest completed in ${formatHours(seconds)}`});
                            }
                            this.render();
                        },
                    },
                },
                default: 'yes',
            }, {
                jQuery: false,
                width: 150,
                classes: ['kingmaker-tools-app'],
            }).render(true);
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
