function editTemplate(structureData: object | undefined): string {
    const root = document.createElement('form');
    const textarea = document.createElement('textarea');
    textarea.name = 'json';
    textarea.innerHTML = structureData ? JSON.stringify(structureData, null, 2) : '';
    root.appendChild(textarea);
    return root.outerHTML;
}

export async function showStructureEditDialog(game: Game, actor: Actor): Promise<void> {
    const structureData = actor!.getFlag('pf2e-kingmaker-tools', 'structureData') ?? undefined;
    new Dialog({
        title: 'Edit Structure Data',
        content: editTemplate(structureData),
        buttons: {
            roll: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const json = $html.querySelector('textarea[name=json]') as HTMLInputElement;
                    const value = json.value.trim() === '' ? null : JSON.parse(json.value);
                    await actor.unsetFlag('pf2e-kingmaker-tools', 'structureData');
                    await actor.setFlag('pf2e-kingmaker-tools', 'structureData', value);
                },
            },
        },
        default: 'roll',
    }, {
        jQuery: false,
    }).render(true, {width: 400, classes: ['edit-structure-json', 'kingmaker-tools-app']});
}
