import {parseSelect} from '../../utils';

export interface ArmySetupOptions {
    actor: Actor;
    onConfirm: (additionalUuids: string[], type: string) => Promise<void>;
}

export function armySetupDialog(options: ArmySetupOptions): void {
    new Dialog({
        title: `Enable Army: ${options.actor.name}`,
        content: `
        <p>Do you want to turn actor ${options.actor.name} into an army? If so, you need 
        to manually create its attacks. After this dialog, change into its inventory and click on the 
        <i class="fa-solid fa-bolt fa-fw"></i> next to its weapon.</p>
        <form class="simple-dialog-form">
            <div>
                <label for="km-army-type">Army Type:</label>
                <select name="type" id="km-army-type">
                    <option value="Infantry">Infantry</option>
                    <option value="Skirmisher">Skirmisher</option>
                    <option value="Cavalry">Cavalry</option>
                    <option value="Siege">Siege</option>
                </select>
            </div>
            <div>
                <label for="km-army-weapon-type">Army Type:</label>
                <select name="weapon-type" id="km-army-weapon-type">
                    <option value="melee">Melee</option>
                    <option value="ranged">Ranged</option>
                </select>
            </div>
        </form>
        `,
        buttons: {
            no: {
                icon: '<i class="fa-solid fa-cancel"></i>',
                label: 'No',
                callback: async (): Promise<void> => {
                },
            },
            yes: {
                icon: '<i class="fa-solid fa-check"></i>',
                label: 'Yes',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const type = parseSelect($html, 'type');
                    const weaponType = parseSelect($html, 'weapon-type');
                    const uuids = weaponType === 'ranged'
                        ? [
                            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-gear.Item.osC5Z0FdEU47WAqp',
                            'Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-gear.Item.UAYsXm698g1iyC2v',
                        ]
                        : ['Compendium.pf2e-kingmaker-tools.kingmaker-tools-army-gear.Item.hIjD8V24RKLlIV2N'];
                    await options.onConfirm(uuids, type);
                },
            },
        },
        default: 'no',
    }, {
        jQuery: false,
        width: 250,
    }).render(true);
}
