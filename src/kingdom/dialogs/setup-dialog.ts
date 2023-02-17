
export function setupDialog(game: Game, onOk: () => void): void {
    new Dialog({
        title: 'Kingdom Sheet Setup',
        content: `
        <p><b>This is an alpha preview! Updates may completely break it forcing a re-import or just get lost! Keep a backup!</b></p>
        <p>No Kingdom Sheet found! Click "Import" which creates an NPC actor called"Kingdom Sheet"</p>
        <p>This actor will be used to store your kingdom sheet.</p>
        <p>You can only open the sheet using the Kingdom Macro!</p>
        <p>Adjust permissions as usual to give your players access</p>
        `,
        buttons: {
            importSheet: {
                icon: '<i class="fa-solid fa-plus"></i>',
                label: 'Import',
                callback: async (): Promise<void> => {
                    /* eslint-disable @typescript-eslint/no-explicit-any */
                    const pack = game.packs.get('pf2e-kingmaker-tools.kingmaker-tools-structures') as any;
                    await game?.actors?.importFromCompendium(pack, 'rRFZtEjqw2foI0GJ');
                    onOk();
                },
            },
        },
        default: 'importSheet',
    }, {
        jQuery: false,
        width: 380,
    }).render(true);
}
