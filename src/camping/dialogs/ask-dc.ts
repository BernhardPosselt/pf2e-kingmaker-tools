import {parseNumberInput} from '../../utils';

export interface AskDcOptions {
    onSubmit: (dc: number) => Promise<void>;
    activity: string;
}
export function askDcDialog(options: AskDcOptions): void {
    new Dialog({
        title: `Set DC: ${options.activity}`,
        content: `
        <form class="simple-dialog-form">
            <div>
                <label for="km-dc">DC</label>
                <input type="number" name="dc" id="km-dc">
            </div>
        </form>
        `,
        buttons: {
            roll: {
                icon: '<i class="fa-solid fa-dice-d20"></i>',
                label: 'Roll',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    await options.onSubmit(parseNumberInput($html, 'dc'));
                },
            },
        },
        default: 'roll',
    }, {
        jQuery: false,
        width: 250,
    }).render(true);
}
