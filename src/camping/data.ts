export type DcType = 'zone' | 'actorLevel' | number;
export const specialIngredientUuid = 'Compendium.pf2e.equipment-srd.Item.OCTireuX60MaPcEi';
export const basicIngredientUuid = 'Compendium.pf2e.equipment-srd.Item.kKnMlymiqZLVEAtI';
export const rationUuid = 'Compendium.pf2e.equipment-srd.Item.L9ZV076913otGtiB';

export type CombatEffectCompanions = 'Amiri' | 'Nok-Nok' | 'Jaethal' | 'Kalikke';

export type Proficiency = 'trained' | 'expert' | 'master' | 'legendary';

export const allowedActors = new Set([
    'character',
    'npc',
    'loot',
    'vehicle',
]);