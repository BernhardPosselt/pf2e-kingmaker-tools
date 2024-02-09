import {parseNullableSelect} from '../../utils';
import {RealmTileData} from '../scene';

export function realmTileDialog(game: Game): void {
    const drawings = (game.canvas.drawings?.controlled?.map(d => d.document) ?? []) as DrawingDocument[];
    const tiles = (game.canvas.tiles?.controlled?.map(d => d.document) ?? []) as TileDocument[];
    if (drawings.length === 0 && tiles.length === 0) {
        ui.notifications?.error('Please select drawings or tiles!');
    } else {
        const data = (drawings[0]?.getFlag('pf2e-kingmaker-tools', 'realmTile')
            ?? tiles[0]?.getFlag('pf2e-kingmaker-tools', 'realmTile')) as RealmTileData | undefined;
        new Dialog({
            title: 'Edit Selected Realm Tiles/Drawings',
            content: `
        <form class="simple-dialog-form">
            <div>
                <label>Realm Tile Type</label>
                <select name="type">
                    <option value="-">-</option>
                    <option value="mine" ${data?.type === 'mine' ? 'selected' : ''}>Mine</option>
                    <option value="ore" ${data?.type === 'ore' ? 'selected' : ''}>Ore</option>
                    <option value="lumber" ${data?.type === 'lumber' ? 'selected' : ''}>Lumber</option>
                    <option value="lumberCamp" ${data?.type === 'lumberCamp' ? 'selected' : ''}>Lumber Camp</option>
                    <option value="quarry" ${data?.type === 'quarry' ? 'selected' : ''}>Quarry</option>
                    <option value="stone" ${data?.type === 'stone' ? 'selected' : ''}>Stone</option>
                    <option value="luxury" ${data?.type === 'luxury' ? 'selected' : ''}>Luxuries</option>
                    <option value="luxuryWorksite" ${data?.type === 'luxuryWorksite' ? 'selected' : ''}>Luxuries Worksite</option>
                    <option value="farmland" ${data?.type === 'farmland' ? 'selected' : ''}>Farmland</option>
                    <option value="food" ${data?.type === 'food' ? 'selected' : ''}>Food</option>
                    <option value="claimed" ${data?.type === 'claimed' ? 'selected' : ''}>Claimed Hex</option>
                </select>
            </div>
        </form>
        `,
            buttons: {
                save: {
                    icon: '<i class="fa-solid fa-save"></i>',
                    label: 'Save',
                    callback: async (html): Promise<void> => {
                        const $html = html as HTMLElement;
                        const type = parseNullableSelect($html, 'type');
                        if (type === undefined) {
                            await Promise.all(drawings.map(d => d.unsetFlag('pf2e-kingmaker-tools', 'realmTile')));
                            await Promise.all(tiles.map(d => d.unsetFlag('pf2e-kingmaker-tools', 'realmTile')));
                        } else {
                            await Promise.all(drawings.map(d => d.setFlag('pf2e-kingmaker-tools', 'realmTile', {type})));
                            await Promise.all(tiles.map(d => d.setFlag('pf2e-kingmaker-tools', 'realmTile', {type})));
                        }
                    },
                },
            },
            default: 'save',
        }, {
            jQuery: false,
            width: 300,
        }).render(true);
    }
}
