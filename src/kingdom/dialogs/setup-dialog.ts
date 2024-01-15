export function setupDialog(game: Game, sheet: 'Kingdom' | 'Camping', actorId: string, onOk: () => void): void {
    new Dialog({
        title: `${sheet} Sheet Setup`,
        content: `
        <p>No ${sheet} Sheet found! Click "Import" which creates an NPC actor called "${sheet} Sheet" which will be used to store your data. Adjust permissions as usual to give your players access</p>
        `,
        buttons: {
            importSheet: {
                icon: '<i class="fa-solid fa-plus"></i>',
                label: 'Import',
                callback: async (): Promise<void> => {
                    /* eslint-disable @typescript-eslint/no-explicit-any */
                    const pack = game.packs.get('pf2e-kingmaker-tools.kingmaker-tools-sheets') as any;
                    await game?.actors?.importFromCompendium(pack, actorId);
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
