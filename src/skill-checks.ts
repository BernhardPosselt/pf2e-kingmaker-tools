async function rollSkillCheck(
    actors: Actor[],
    skill: string,
    dc: number | undefined = undefined
) {
    const checkDc = dc === undefined ? undefined : {value: dc};
    for (const actor of actors) {
        const pf2eActor = actor as any;
        const check = skill === 'perception' ? pf2eActor.perception : pf2eActor.skills[skill];
        await check.roll({
            rollMode: 'gmroll',
            dc: checkDc
        });
    }
}

export async function rollExplorationSkillCheck(
    actors: Actor[],
    skill: string,
    effect: string,
    dc: number | undefined = undefined
): Promise<void> {
    const actorsWithEffectApplied = actors
        .filter(actor => actor.items.find(item => item.name === effect && item.type === 'effect'));
    await rollSkillCheck(actorsWithEffectApplied, skill, dc);
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
    }).render(true);
}
