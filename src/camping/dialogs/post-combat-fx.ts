import {CombatEffectCompanions} from '../data';

function companionTpl(allCompanions: string[], preselectedCompanions: string[]): string {
    return `<form>
        ${allCompanions.map(name => {
        const companionId = `combat-fx-checkbox-${name}`;
        const checked = preselectedCompanions.includes(name) ? 'checked="checked"' : '';
        return `<div><input name="${name}" type="checkbox" value="${name}" ${checked} id="${companionId}"><label for="${companionId}">${name}</label></div>`;
    }).join('\n')}
    </form>`;
}

export interface CombatEffects {
    game: Game;
    preselectedCompanions: CombatEffectCompanions[];
    onSubmit: (selectedCompanions: CombatEffectCompanions[]) => Promise<void>;
}

export async function postCombatEffects(options: CombatEffects): Promise<void> {
    const companionConfig = {
        'Amiri': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.ZKJlIqyFgbKDACnG]{Enhance Weapons}',
        'Nok-Nok': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.PSBOS7ZEl9RGWBqD]{Set Traps}',
        'Jaethal': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.KysTaC245mOnSnmE]{Undead Guardians}',
        'Kalikke': '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.LN6mH7Muj4hgvStt]{Water Hazards}',
    };
    new Dialog({
        title: 'Companion Effects to Chat',
        content: companionTpl(Object.keys(companionConfig), options.preselectedCompanions),
        buttons: {
            post: {
                label: 'Post Effects',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLInputElement;
                    const companionsAndLabels = Object.entries(companionConfig)
                        .map(([name, link]) => {
                            const checkbox = $html.querySelector(`input[name=${name}]`) as HTMLInputElement;
                            if (checkbox.checked) {
                                return {[name]: `${name}: ${link}`};
                            } else {
                                return null;
                            }
                        })
                        .filter(v => v !== null)
                        .reduce((a: object, b) => Object.assign(a, b), {});
                    await ChatMessage.create({content: Object.values(companionsAndLabels).join('<br>')});
                    await options.onSubmit(Object.keys(companionsAndLabels) as CombatEffectCompanions[]);
                },
            },
        },
        default: 'post',
    }, {
        jQuery: false,
        width: 230,
    }).render(true);
}
