export interface DisableArmyOptions {
    actor: Actor;
    onYes: () => Promise<void>;
}

export function disableArmyDialog(options: DisableArmyOptions): void {
    new Dialog({
        title: `Disable Army: ${options.actor.name}`,
        content: `
        <p>Do you want to delete all army data and remove army sync from actor ${options.actor.name}?</p>
        `,
        buttons: {
            no: {
                icon: '<i class="fa-solid fa-cancel"></i>',
                label: 'No',
                callback: async (): Promise<void> => {
                },
            },
            yes: {
                icon: '<i class="fa-solid fa-trash"></i>',
                label: 'Yes',
                callback: async (): Promise<void> => {
                    await options.onYes();
                },
            },
        },
        default: 'no',
    }, {
        jQuery: false,
        width: 250,
    }).render(true);
}
