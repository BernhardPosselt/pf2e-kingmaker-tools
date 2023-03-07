export function addOngoingEventDialog(onOk: (name: string) => void): void {
    new Dialog({
        title: 'Add Ongoing Event',
        content: `
        <form class="simple-dialog-form">
            <div>
                <label>Name</label> 
                <input name="name" type="text">
            </div>
        </form>
        `,
        buttons: {
            add: {
                icon: '<i class="fa-solid fa-plus"></i>',
                label: 'Add',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const name = $html.querySelector('input[name=name]') as HTMLInputElement;
                    onOk(name.value);
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 380,
    }).render(true);
}
