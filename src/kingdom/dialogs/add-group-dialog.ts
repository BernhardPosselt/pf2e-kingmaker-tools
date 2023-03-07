import {Group} from '../data/kingdom';

export function addGroupDialog(onOk: (group: Group) => void): void {
    new Dialog({
        title: 'Add Group',
        content: `
        <form class="simple-dialog-form">
            <div>
                <label>Name</label> 
                <input name="name" type="text">
            </div>
            <div>
                <label>Negotiation DC</label>
                <input name="negotiationDC" type="number">
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
                    const negotiationDC = $html.querySelector('input[name=negotiationDC]') as HTMLInputElement;
                    const dc = parseInt(negotiationDC.value, 10);
                    const group: Group = {
                        name: name.value,
                        negotiationDC: isNaN(dc) ? 0 : dc,
                        atWar: false,
                        relations: 'none',
                    };
                    onOk(group);
                },
            },
        },
        default: 'add',
    }, {
        jQuery: false,
        width: 380,
    }).render(true);
}
