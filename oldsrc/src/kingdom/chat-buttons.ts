import {getKingdom, saveKingdom} from './storage';
import {gainFame} from './kingdom-utils';
import {parsePayButton, payStructure} from './dialogs/structure-browser';
import {decodeJson, isNonNullable, uuidv4} from "../utils";
import {updateResources} from "./resources";

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
            const data = target.dataset.data as string | undefined | null;
            const kingdom = getKingdom(actor);
            if (isNonNullable(data)) {
                const modifier = decodeJson(data) as any;
                // copy modifier because we alter the consumeId
                const modifierCopy = {
                    ...modifier,
                };
                if (modifierCopy.consumeId !== undefined) {
                    modifierCopy.consumeId = uuidv4();
                }
                const modifiers = [...kingdom.modifiers, modifierCopy];
                await saveKingdom(actor, {modifiers});
            }
        },
    },
];

