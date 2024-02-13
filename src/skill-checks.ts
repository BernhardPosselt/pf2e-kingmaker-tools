async function rollSkillCheck(
    actors: Actor[],
    skill: string,
    dc: number | undefined = undefined,
): Promise<void> {
    const checkDc = dc === undefined ? undefined : {value: dc};
    for (const actor of actors) {
        const check = skill === 'perception' ? actor.perception : actor.skills[skill];
        await check.roll({
            rollMode: 'gmroll',
            dc: checkDc,
        });
    }
}

function getActiveExplorationEffects(actor: Actor): Set<string> {
    const explorationActionItemIds = actor.system.exploration ?? [];
    const names = explorationActionItemIds
        .map(id => actor.items.get(id)?.name)
        .filter(name => name !== undefined && name !== null) as string[];
    return new Set(names);
}

export async function rollExplorationSkillCheck(
    game: Game,
    skill: string,
    effect: string,
    dc: number | undefined = undefined,
): Promise<void> {
    const actors = game.actors
        ?.filter(a =>
            (a.type === 'character' || a.type === 'familiar')
            && a.hasPlayerOwner
            && getActiveExplorationEffects(a).has(effect)) ?? [];
    if (effect === 'Search' && skill === 'perception') {
        game.pf2e.actions.get('seek')?.use({actors});
    } else if (effect === 'Avoid Notice' && skill === 'stealth') {
        game.pf2e.actions.get('avoid-notice')?.use({actors});
    } else {
        await rollSkillCheck(actors, skill, dc);
    }
}

function tpl(): string {
    return `
    <div style="display: table; border-spacing: 10px">
        <div style="display: table-row">
            <label style="display: table-cell">Skill</label> 
            <select name="skill" style="display: table-cell">
                <option value="perception">Perception</option>
                <option value="nature">Nature</option>
                <option value="survival">Survival</option>
            </select>
        </div>
        <div style="display: table-row">
            <label style="display: table-cell">DC</label>
            <input name="dc" type="number" value="0" style="display: table-cell">
        </div>
    </div>
    `;
}

export async function rollSkillDialog(actors: Actor[]): Promise<void> {
    new Dialog({
        title: 'Roll Skill for PCs',
        content: tpl(),
        buttons: {
            roll: {
                icon: '<i class="fa-solid fa-dice-d20"></i>',
                label: 'Roll',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const skillInput = $html.querySelector('select[name=skill]') as HTMLSelectElement;
                    const dcInput = $html.querySelector('input[name=dc]') as HTMLInputElement;
                    const skill = skillInput.value;
                    const dc = parseInt(dcInput.value, 10);
                    await rollSkillCheck(actors, skill, dc);
                },
            },
        },
        default: 'roll',
    }, {
        jQuery: false,
    }).render(true, {width: 200});
}
