import {parseNumberInput, postChatMessage} from './utils';

async function resetPoints(game: Game, actors: Actor[]): Promise<void> {
    for (const actor of actors) {
        await actor.update({
            'system.resources.heroPoints.value': 1,
        });
    }
    await postChatMessage('Reset hero point values to 1');
}

async function awardPoints(game: Game, points: { actor: Actor, points: number }[]): Promise<void> {
    for (const point of points) {
        const actor = point.actor;
        await actor.update({
            'system.resources.heroPoints.value': actor.system.resources.heroPoints.value + point.points,
        });
    }
    const content = `<p>Awarded Hero Points:</p><ul>${points.map(p => {
        return `<li><b>${p.actor.name}</b>: ${p.points}</li>`;
    }).join('\n')}</ul>`;
    await postChatMessage(content);
}

export async function awardHeroPoints(game: Game): Promise<void> {
    const actors = game.actors?.contents
        ?.filter(e => e.type === 'character' && e.hasPlayerOwner) ?? [];
    new Dialog({
        title: 'Award Hero Points',
        content: `<form class="simple-dialog-form">
                <div>
                    <label for="km-award-all">All</label>
                    <input type="number" value="0" name="award-all" id="km-award-all">
                </div>
            ${actors.map(a => `<div>
                    <label for="km-award-hero-point-${a.uuid}">${a.name}</label>
                    <input type="number" value="0" name="${a.uuid}" id="km-award-hero-point-${a.uuid}">
                </div>`,
        ).join('\n')}
        </form>`,
        buttons: {
            confirm: {
                icon: '<i class="fas fa-check"></i>',
                label: 'Award',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const all = parseNumberInput($html, 'award-all');
                    const points = actors
                        .map(actor => {
                            return {
                                actor,
                                points: parseNumberInput($html, actor.uuid) + all,
                            };
                        })
                        .filter(point => point.points > 0);
                    if (points.length > 0) {
                        await awardPoints(game, points);
                    }
                },
            },
        },
        default: 'confirm',
    }).render(true, {jQuery: false, width: 300});
}

export async function resetHeroPoints(game: Game): Promise<void> {
    const actors = game.actors?.contents
        ?.filter(e => e.type === 'character' && e.hasPlayerOwner) ?? [];
    new Dialog({
        title: 'Reset Hero Points to 1?',
        content: '',
        buttons: {
            cancel: {
                icon: '<i class="fas fa-times"></i>',
                label: 'Cancel',
            },
            confirm: {
                icon: '<i class="fas fa-check"></i>',
                label: 'Confirm',
                callback: async (): Promise<void> => {
                    await resetPoints(game, actors);
                },
            },
        },
        default: 'confirm',
    }).render(true, {jQuery: false});
}


async function awardXP(game: Game, actors: Actor[], amount: number): Promise<void> {
    for (const actor of actors) {
        const currentXP = actor.system.details.xp.value;
        const xpThreshold = actor.system.details.xp.max;
        const currentLevel = actor.system.details.level.value;
        const addLevels = Math.floor((currentXP + amount) / xpThreshold);
        const xpGain = (currentXP + amount) % xpThreshold;
        await actor.update({
            'system.details.xp.value': xpGain,
            'system.details.level.value': currentLevel + addLevels,
        });
    }
    await postChatMessage(`Players gained ${amount} XP!`);
}

async function awardXPDialog(game: Game, actors: Actor[]): Promise<void> {
    new Dialog({
        title: 'Award Party XP',
        content: `
     <form>
      <div class="form-group">
       <label>Amount</label>
       <input type="number" name="amount">
      </div>
     </form>
     `,
        buttons: {
            cancel: {
                icon: '<i class="fas fa-times"></i>',
                label: 'Cancel',
            },
            confirm: {
                icon: '<i class="fas fa-check"></i>',
                label: 'Confirm',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const amount = parseNumberInput($html, 'amount') || 0;
                    await awardXP(game, actors, amount);
                },
            },
        },
        default: 'confirm',
    }).render(true, {jQuery: false});
}

export async function showAwardXPDialog(game: Game): Promise<void> {
    const actors = game?.actors?.contents
        ?.filter(e => e.type === 'character' && e.hasPlayerOwner) ?? [];
    await awardXPDialog(game, actors);
}

