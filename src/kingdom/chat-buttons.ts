import {getKingdom, getKingdomSheetActorOrThrow, saveKingdom} from './storage';
import {Activity} from './data/activities';
import {activityData, ActivityResults} from './data/activityData';
import {updateResources} from './resources';

interface KingdomChatButton {
    selector: string;
    callback: (ev: Event) => Promise<void>;
}

export const kingdomChatButtons: KingdomChatButton[] = [
    {
        selector: '.km-gain-lose',
        callback: async (event: Event): Promise<void> => {
            event.preventDefault();
            const target = event.currentTarget as HTMLButtonElement;
            await updateResources(target);
        },
    },
    {
        selector: '.km-apply-modifier-effect',
        callback: async (event: Event): Promise<void> => {
            event.preventDefault();
            const target = event.currentTarget as HTMLButtonElement;
            const activity = target.dataset.activity! as Activity;
            const degree = target.dataset.degree! as keyof ActivityResults;
            const index = parseInt(target.dataset.index ?? '0', 10);
            const sheetActor = getKingdomSheetActorOrThrow();
            const kingdom = getKingdom(sheetActor);
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
                await saveKingdom(sheetActor, {modifiers});
            } else {
                console.error(`Can not find modifier ${activity}.${degree}.modifiers.${index}`);
            }
        },
    },
];

