import {getKingdom, saveKingdom} from './storage';
import {ActivityResults, getKingdomActivitiesById} from './data/activityData';
import {updateResources} from './resources';
import {gainFame} from './kingdom-utils';
import {parsePayButton, payStructure} from './dialogs/structure-browser';

interface KingdomChatButton {
    selector: string;
    callback: (game: Game, actor: Actor, ev: Event) => Promise<void>;
}

export const kingdomChatButtons: KingdomChatButton[] = [
    {
        selector: '.km-pay-structure',
        callback: async (game: Game, actor: Actor, event: Event): Promise<void> => {
            event.preventDefault();
            const target = event.currentTarget as HTMLButtonElement;
            const costs = parsePayButton(target);
            await payStructure(actor, costs);
        },
    },
    {
        selector: '.km-gain-fame-button',
        callback: async (game: Game, actor: Actor, event: Event): Promise<void> => {
            event.preventDefault();
            const update = gainFame(getKingdom(actor), 1);
            await ChatMessage.create({content: 'Gaining 1 Fame'});
            await saveKingdom(actor, update);
        },
    },
    {
        selector: '.km-gain-lose',
        callback: async (game: Game, actor: Actor, event: Event): Promise<void> => {
            event.preventDefault();
            const target = event.currentTarget as HTMLButtonElement;
            await updateResources(game, actor, target);
        },
    },
    {
        selector: '.km-apply-modifier-effect',
        callback: async (game: Game, actor: Actor, event: Event): Promise<void> => {
            event.preventDefault();
            const target = event.currentTarget as HTMLButtonElement;
            const activity = target.dataset.activity!;
            const degree = target.dataset.degree! as keyof ActivityResults;
            const index = parseInt(target.dataset.index ?? '0', 10);
            const kingdom = getKingdom(actor);
            const activityData = getKingdomActivitiesById(kingdom.homebrewActivities);
            const modifier = activityData[activity]?.[degree]?.modifiers?.(kingdom)?.[index];
            if (modifier !== undefined) {
                // copy modifier because we alter the consumeId
                const modifierCopy = {
                    ...modifier,
                };
                if (modifierCopy.consumeId !== undefined) {
                    modifierCopy.consumeId = crypto.randomUUID();
                }
                const modifiers = [...kingdom.modifiers, modifierCopy];
                await saveKingdom(actor, {modifiers});
            } else {
                console.error(`Can not find modifier ${activity}.${degree}.modifiers.${index}`);
            }
        },
    },
];

