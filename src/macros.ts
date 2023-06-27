import {parseNumberInput} from './utils';

async function resetPoints(game: Game, actors: Actor[]): Promise<void> {
    for (const actor of actors) {
        await actor.update({
            'system.resources.heroPoints.value': 1,
        });
    }
    await ChatMessage.create({
        user: game?.user?.id,
        speaker: ChatMessage.getSpeaker(),
        content: 'Reset hero point values to 1',
        type: CONST.CHAT_MESSAGE_TYPES.OTHER,
    });
}

export async function resetHeroPoints(game: Game): Promise<void> {
    const actors = game.actors?.contents
        ?.filter(e => e.type === 'character' && e.hasPlayerOwner) ?? [];
    await resetPoints(game, actors);
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
    await ChatMessage.create({
        user: game?.user?.id,
        speaker: ChatMessage.getSpeaker(),
        content: `Players gained ${amount} XP!`,
        type: CONST.CHAT_MESSAGE_TYPES.OTHER,
    });
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

