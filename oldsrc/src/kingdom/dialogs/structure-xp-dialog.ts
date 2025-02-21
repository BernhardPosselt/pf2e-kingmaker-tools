import {showStructureBrowser} from "./structure-browser";
import {getAllImportedStructureActors} from "../structures";
import {getStructuresFromActors} from "../scene";
import {escapeHtml, parseSelect} from "../../utils";

export function structureXpDialog(game: Game, onOk: (xp: number) => void): void {
    const actors = getAllImportedStructureActors(game);
    const structures = getStructuresFromActors(actors);
    new Dialog({
        title: `Gain Structure XP`,
        content: `
        <form class="simple-dialog-form">
            <div>
                <label>Structure</label> 
                <select name="structure">
                    ${structures.map(s => `<option value="${escapeHtml(s.name)}">${escapeHtml(s.name)}</option>`).join('')}
                </select>
            </div>
        </form>
        `,
        buttons: {
            ok: {
                label: 'Gain XP',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const structureName = parseSelect($html, 'structure');
                    const structure = structures.find(s => s.name === structureName);
                    if (structure) {
                        const baseXp = 5 + Math.floor((structure.construction?.rp ?? 0) / 10) * 5;
                        const xp = (structure.traits?.includes('edifice') ?? false) ? baseXp * 2 : baseXp;
                        onOk(xp);
                    }
                },
            },
        },
        default: 'ok',
    }, {
        jQuery: false,
        width: 380,
    }).render(true);
}
