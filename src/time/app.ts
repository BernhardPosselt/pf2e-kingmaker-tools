import {getWorldTime, TimeChangeMode, TimeOfDay} from './calculation';
import {DateTime, HourNumbers, MinuteNumbers, SecondNumbers} from 'luxon';

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
        hour: timeValue.hour as HourNumbers,
        minute: timeValue.minute as MinuteNumbers,
        second: timeValue.second as SecondNumbers,
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

export function formatHours(seconds: number, isNegative = false): string {
    const padZero = (num: number): string => `${num}`.padStart(2, '0');
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${isNegative ? '-' : ''}${padZero(hours)}:${padZero(minutes)}`;
}
