import {isFirstGm, mergeObjects, mergePartialObjects, postChatMessage, sum} from '../utils';
import {getActivitySkills} from './data/activities';
import {
    ActivityBonusRule,
    CommodityStorage,
    ItemGroup,
    ItemLevelBonuses,
    SkillBonusRule,
    SkillItemBonus,
    SkillItemBonuses,
    Structure,
} from './data/structures';
import {Skill} from './data/skills';
import {gainRuin, KingdomActivityById, loseRuin, loseUnrest} from './data/activityData';
import {getStructureFromActor, isStructureActor} from './scene';
import {allRuins} from './data/ruin';

export interface StructureResult {
    allowCapitalInvestment: boolean;
    notes: string[];
    skillBonuses: SkillItemBonuses;
    itemLevelBonuses: ItemLevelBonuses;
    settlementEventBonus: number;
    leadershipActivityBonus: number;
    storage: CommodityStorage;
    increaseLeadershipActivities: boolean;
    consumptionReduction: number;
    consumption: number;
    consumptionSurplus: number;
    config: SettlementConfig;
    unlockActivities: string[];
    residentialLots: number;
    hasBridge: boolean;
    lots: number;
}

function count<T>(items: T[], idFunction: (item: T) => string): Map<string, { count: number, item: T }> {
    return items.reduce((map, item) => {
        const id = idFunction(item);
        const count = (map.get(id)?.count ?? 0) + 1;
        return map.set(id, {count, item});
    }, new Map());
}

export function countStructureOccurrences<T extends Structure>(structures: T[]): Map<string, {
    count: number,
    item: T
}> {
    return count(structures, s => s.stacksWith ?? s.name);
}

/**
 * Add up item bonuses of same structure
 */
export function groupStructures(structures: Structure[], maxItemBonus: number): Structure[] {
    const structureOccurrences = countStructureOccurrences(structures);
    return Array.from(structureOccurrences.values())
        .map((data) => {
            const structure = data.item;
            const result: Structure = {
                ...structure,
                skillBonusRules: structure?.skillBonusRules?.map(rule => {
                    return {
                        ...rule,
                        value: Math.min(rule.value * data.count, maxItemBonus),
                    };
                }),
                availableItemsRules: structure?.availableItemsRules?.map(rule => {
                    const count = rule.maximumStacks === undefined ? data.count : Math.min(data.count, rule.maximumStacks);
                    const value = rule.value * count;
                    return {
                        ...rule,
                        value,
                    };
                }),
                settlementEventRules: structure?.settlementEventRules?.map(rule => {
                    return {
                        ...rule,
                        value: Math.min(rule.value * data.count, maxItemBonus),
                    };
                }),
                leadershipActivityRules: structure?.leadershipActivityRules?.map(rule => {
                    return {
                        ...rule,
                        value: Math.min(rule.value * data.count, maxItemBonus),
                    };
                }),
            };
            return result;
        });
}

function applySkillBonusRules(result: SkillItemBonuses, structures: Structure[]): void {
    // apply skills
    structures.forEach(structure => {
        structure.skillBonusRules?.forEach(rule => {
            const skill = result[rule.skill];
            if (!rule.activity) {
                if (rule.value > skill.value) {
                    skill.value = rule.value;
                }
            }
        });
    });
    // apply activities
    structures.forEach(structure => {
        structure.skillBonusRules?.forEach(rule => {
            const skill = result[rule.skill];
            const activity = rule.activity;
            if (activity) {
                if (rule.value > skill.value &&
                    rule.value > (skill.activities[activity] ?? 0)) {
                    skill.activities[activity] = rule.value;
                }
            }
        });
    });
}

function calculateItemLevelGroupBonus(
    structures: Structure[],
    group: ItemGroup | undefined,
    stackMode: 'always-stack' | 'never-stack' = 'never-stack',
): number {
    const structureValues = structures
        .flatMap(structures => structures.availableItemsRules ?? [])
        .filter(rule => rule.group === group)
        .map(rule => rule.value);
    if (stackMode === 'always-stack') {
        return sum(structureValues);
    } else {
        return Math.max(...structureValues, 0);
    }
}

function applyItemLevelRules(itemLevelBonuses: ItemLevelBonuses, structures: Structure[]): void {
    const defaultPenalty = structures.some(structure => structure.preventItemLevelPenalty === true) ? 0 : -2;

    // calculate untyped bonuses
    const otherBonuses = calculateItemLevelGroupBonus(structures, undefined, 'always-stack');
    const alchemicalBonuses = calculateItemLevelGroupBonus(structures, 'alchemical');
    const luxuryBonuses = calculateItemLevelGroupBonus(structures, 'luxury');
    const magicalBonuses = calculateItemLevelGroupBonus(structures, 'magical');
    const divineBonuses = calculateItemLevelGroupBonus(structures, 'divine');
    const primalBonuses = calculateItemLevelGroupBonus(structures, 'primal');
    const arcaneBonuses = calculateItemLevelGroupBonus(structures, 'arcane');
    const occultBonuses = calculateItemLevelGroupBonus(structures, 'occult');

    itemLevelBonuses.other = otherBonuses + defaultPenalty;
    itemLevelBonuses.alchemical = alchemicalBonuses + itemLevelBonuses.other;
    itemLevelBonuses.magical = magicalBonuses + itemLevelBonuses.other;
    itemLevelBonuses.divine = itemLevelBonuses.magical + divineBonuses;
    itemLevelBonuses.primal = itemLevelBonuses.magical + primalBonuses;
    itemLevelBonuses.arcane = itemLevelBonuses.magical + arcaneBonuses;
    itemLevelBonuses.occult = itemLevelBonuses.magical + occultBonuses;
    itemLevelBonuses.luxuryMagical = itemLevelBonuses.magical + luxuryBonuses;
    itemLevelBonuses.luxuryOccult = itemLevelBonuses.occult + luxuryBonuses;
    itemLevelBonuses.luxuryArcane = itemLevelBonuses.arcane + luxuryBonuses;
    itemLevelBonuses.luxuryPrimal = itemLevelBonuses.primal + luxuryBonuses;
    itemLevelBonuses.luxuryDivine = itemLevelBonuses.divine + luxuryBonuses;
}

function applyLeadershipActivityBonuses(result: StructureResult, structures: Structure[]): void {
    structures.forEach(structure => {
        structure.leadershipActivityRules?.forEach(rule => {
            if (rule.value > result.leadershipActivityBonus) {
                result.leadershipActivityBonus = rule.value;
            }
        });
    });
}

function applySettlementEventBonuses(result: StructureResult, structures: Structure[]): void {
    structures.forEach(structure => {
        structure.settlementEventRules?.forEach(rule => {
            if (rule.value > result.settlementEventBonus) {
                result.settlementEventBonus = rule.value;
            }
        });
    });
}

function simplifyRules(rules: ActivityBonusRule[], activities: KingdomActivityById): SkillBonusRule[] {
    return rules.flatMap(rule => {
        const activity = rule.activity;
        const skills = getActivitySkills(activities[activity].skills);
        return skills.map(skill => {
            return {
                value: rule.value,
                skill,
                activity: activity,
            };
        });
    });
}

function unionizeStructures(structures: Structure[], activities: KingdomActivityById): Structure[] {
    return structures.map(structure => {
        const simplifiedRules = simplifyRules(structure.activityBonusRules ?? [], activities);
        return {
            ...structure,
            skillBonusRules: [...(structure.skillBonusRules ?? []), ...simplifiedRules],
        };
    });
}

function applyStorageIncreases(storage: CommodityStorage, structures: Structure[]): void {
    structures
        .filter(structures => structures.storage)
        .forEach(structure => {
            const keys = ['ore', 'lumber', 'food', 'stone', 'luxuries'] as (keyof CommodityStorage)[];
            keys.forEach(key => {
                const structureStorage = structure.storage;
                if (structureStorage && key in structureStorage) {
                    storage[key] += structureStorage[key] ?? 0;
                }
            });
        });
}

function calculateConsumptionReduction(structures: Structure[]): number {
    const consumptionPerUniqueBuilding = new Map<string, number>();
    structures
        .filter(structure => structure.consumptionReduction)
        .forEach(structure => {
            const id = structure.name;
            const existingReduction = consumptionPerUniqueBuilding.get(id);
            const newReduction = structure?.consumptionReduction ?? 0;
            if (existingReduction === undefined || existingReduction < newReduction) {
                consumptionPerUniqueBuilding.set(id, newReduction);
            }
        });
    return Array.from(consumptionPerUniqueBuilding.values())
        .reduce((a, b) => a + b, 0);
}

export interface SettlementTypeData {
    type: 'Village' | 'Town' | 'City' | 'Metropolis';
    maximumLots: string;
    requiredKingdomLevel: number;
    population: string;
    consumption: number;
    maxItemBonus: number;
    influence: number;
    levelFrom: number;
    levelTo?: number;
}

export interface SettlementConfig extends SettlementTypeData {
    level: number;
}

export const settlementTypeData: SettlementTypeData[] = [{
    type: 'Village',
    consumption: 1,
    influence: 0,
    maximumLots: '1',
    requiredKingdomLevel: 1,
    levelFrom: 1,
    levelTo: 1,
    maxItemBonus: 1,
    population: '400 or less',
}, {
    type: 'Town',
    consumption: 2,
    influence: 1,
    requiredKingdomLevel: 3,
    maximumLots: '4',
    levelFrom: 2,
    levelTo: 4,
    maxItemBonus: 1,
    population: '401-2000',
}, {
    type: 'City',
    consumption: 4,
    influence: 2,
    requiredKingdomLevel: 9,
    maximumLots: '9',
    levelFrom: 5,
    levelTo: 9,
    maxItemBonus: 2,
    population: '2001–25000',
}, {
    type: 'Metropolis',
    consumption: 6,
    influence: 3,
    maximumLots: '10+',
    requiredKingdomLevel: 15,
    levelFrom: 10,
    maxItemBonus: 3,
    population: '25001+',
}] as const;

export function getSettlementConfig(settlementLevel: number): SettlementConfig {
    return {
        ...settlementTypeData.find(d => settlementLevel >= d.levelFrom
            && settlementLevel <= (d.levelTo ?? Number.MAX_SAFE_INTEGER))!,
        level: settlementLevel,
    };
}

function mergeBonuses(capital: SkillItemBonus, settlement: SkillItemBonus): SkillItemBonus {
    const skillValue = capital.value > settlement.value ? capital.value : settlement.value;
    const activities = mergePartialObjects(capital.activities, settlement.activities, (a: number | undefined, b: number | undefined) => Math.max(a ?? 0, b ?? 0));
    const filteredActivities = Object.fromEntries(
        Object.entries(activities)
            .filter(([, value]) => (value ?? 0) > skillValue),
    );
    return {
        value: skillValue,
        activities: filteredActivities,
    };
}

export function includeCapital(capital: StructureResult, settlement: StructureResult): StructureResult {
    return {
        ...settlement,
        increaseLeadershipActivities: capital.increaseLeadershipActivities,
        unlockActivities: capital.unlockActivities.concat(settlement.unlockActivities),
        leadershipActivityBonus: Math.max(capital.leadershipActivityBonus, settlement.leadershipActivityBonus),
        skillBonuses: mergeObjects(capital.skillBonuses, settlement.skillBonuses, mergeBonuses) as SkillItemBonuses,
    };
}

function applyUnlockedActivities(unlockActivities: string[], groupedStructures: Structure[]): void {
    groupedStructures
        .flatMap(structure => structure?.unlockActivities ?? [])
        .forEach(activity => {
            unlockActivities.push(activity);
        });
}

export type StructureStackRule = 'all-structures-stack' | 'same-structures-stack'

// brainfuck ahead :(
function stackAllStructureBonuses(groupedStructures: Structure[], maxItemBonus: number): Structure[] {
    const allSkillBonusRules = groupedStructures.flatMap(s => s.skillBonusRules ?? []);
    const activityBonusRules = allSkillBonusRules.filter(r => r.activity);
    const skillOnlyBonusRules = allSkillBonusRules.filter(r => !r.activity);
    // first calculate all skill modifiers
    const skillOnlyBonuses: Map<Skill, number> = new Map();
    skillOnlyBonusRules.forEach(rule => {
        const existingValue = skillOnlyBonuses.get(rule.skill) ?? 0;
        skillOnlyBonuses.set(rule.skill, Math.min(maxItemBonus, existingValue + rule.value));
    });
    // then calculate all modifiers only applicable for an activity
    const activityBonuses: Map<Skill, Map<string, number>> = new Map();
    activityBonusRules.forEach(rule => {
        const skill = rule.skill;
        activityBonuses.set(skill, activityBonuses.get(skill) ?? new Map());
        const activity = rule.activity!;
        const existingValue = activityBonuses.get(skill)?.get(activity) ?? 0;
        const newValue = Math.min(maxItemBonus, existingValue + rule.value);
        activityBonuses.get(skill)?.set(activity, newValue);
    });
    // then add skill modifiers to skill modifiers limited to an activity
    Array.from(activityBonuses.keys()).forEach(skill => {
        const map = activityBonuses.get(skill) ?? new Map();
        Array.from(map.keys()).forEach(activity => {
            const existingValue = map.get(activity) ?? 0;
            const newValue = Math.min(maxItemBonus, existingValue + (skillOnlyBonuses.get(skill) ?? 0));
            activityBonuses.get(skill)?.set(activity, newValue);
        });
    });
    // finally, apply value overrides
    return groupedStructures.map(s => {
        return {
            ...s,
            skillBonusRules: s.skillBonusRules
                ?.map(r => {
                    const value = r.activity
                        ? activityBonuses.get(r.skill)?.get(r.activity)
                        : skillOnlyBonuses.get(r.skill);
                    return {
                        ...r,
                        value: value ?? 0,
                    };
                }),
        };
    });
}

/**
 * Calculate all Bonuses of a settlement
 */
export function evaluateStructures(
    structures: Structure[],
    settlementLevel: number,
    mode: StructureStackRule,
    activities: KingdomActivityById,
): StructureResult {
    const settlementData = getSettlementConfig(settlementLevel);
    const maxItemBonus = settlementData.maxItemBonus;
    const allowCapitalInvestment = structures.some(structure => structure.enableCapitalInvestment === true);
    const notes = Array.from(new Set(structures.flatMap(result => result.notes ?? [])));
    const consumptionReduction = calculateConsumptionReduction(structures);
    const result: StructureResult = {
        config: settlementData,
        allowCapitalInvestment,
        notes,
        skillBonuses: {
            agriculture: {value: 0, activities: {}},
            arts: {value: 0, activities: {}},
            boating: {value: 0, activities: {}},
            defense: {value: 0, activities: {}},
            engineering: {value: 0, activities: {}},
            exploration: {value: 0, activities: {}},
            folklore: {value: 0, activities: {}},
            industry: {value: 0, activities: {}},
            intrigue: {value: 0, activities: {}},
            magic: {value: 0, activities: {}},
            politics: {value: 0, activities: {}},
            scholarship: {value: 0, activities: {}},
            statecraft: {value: 0, activities: {}},
            trade: {value: 0, activities: {}},
            warfare: {value: 0, activities: {}},
            wilderness: {value: 0, activities: {}},
        },
        itemLevelBonuses: {
            divine: 0,
            alchemical: 0,
            primal: 0,
            occult: 0,
            arcane: 0,
            magical: 0,
            other: 0,
            luxuryOccult: 0,
            luxuryArcane: 0,
            luxuryDivine: 0,
            luxuryMagical: 0,
            luxuryPrimal: 0,
        },
        settlementEventBonus: 0,
        leadershipActivityBonus: 0,
        storage: {
            ore: 0,
            food: 0,
            lumber: 0,
            luxuries: 0,
            stone: 0,
        },
        increaseLeadershipActivities: structures.some(structure => structure.increaseLeadershipActivities === true),
        consumptionReduction,
        consumption: Math.max(0, settlementData.consumption - consumptionReduction),
        consumptionSurplus: Math.max(0, consumptionReduction - settlementData.consumption),
        unlockActivities: [],
        residentialLots: structures
            .filter(structure => structure?.traits?.includes('residential'))
            .map(s => s.lots ?? 1)
            .reduce((a, b) => a + b, 0),
        lots: structures.map(s => s.lots ?? 1).reduce((a, b) => a + b, 0),
        hasBridge: structures.some(structure => structure.isBridge),
    };
    const unionizedStructures = unionizeStructures(structures, activities);
    applyStorageIncreases(result.storage, structures);
    const groupedStructures = groupStructures(unionizedStructures, maxItemBonus);
    const allGroupedStructures = mode === 'all-structures-stack'
        ? stackAllStructureBonuses(groupedStructures, maxItemBonus)
        : groupedStructures;
    applyUnlockedActivities(result.unlockActivities, allGroupedStructures);
    applySettlementEventBonuses(result, allGroupedStructures);
    applyLeadershipActivityBonuses(result, allGroupedStructures);
    applySkillBonusRules(result.skillBonuses, allGroupedStructures);
    applyItemLevelRules(result.itemLevelBonuses, allGroupedStructures);
    return result;
}

export function calculateAvailableItems(
    itemLevelBonuses: ItemLevelBonuses,
    settlementLevel: number,
    additionalMagicItemLevel: number = 0,
): ItemLevelBonuses {
    return {
        alchemical: itemLevelBonuses.alchemical + settlementLevel,
        magical: itemLevelBonuses.magical + additionalMagicItemLevel + settlementLevel,
        luxuryMagical: itemLevelBonuses.luxuryMagical + additionalMagicItemLevel + settlementLevel,
        arcane: itemLevelBonuses.arcane + additionalMagicItemLevel + settlementLevel,
        luxuryArcane: itemLevelBonuses.luxuryArcane + additionalMagicItemLevel + settlementLevel,
        divine: itemLevelBonuses.divine + additionalMagicItemLevel + settlementLevel,
        luxuryDivine: itemLevelBonuses.luxuryDivine + additionalMagicItemLevel + settlementLevel,
        occult: itemLevelBonuses.occult + additionalMagicItemLevel + settlementLevel,
        luxuryOccult: itemLevelBonuses.luxuryOccult + additionalMagicItemLevel + settlementLevel,
        primal: itemLevelBonuses.primal + additionalMagicItemLevel + settlementLevel,
        luxuryPrimal: itemLevelBonuses.luxuryPrimal + additionalMagicItemLevel + settlementLevel,
        other: itemLevelBonuses.other + settlementLevel,
    };
}

function groupByItemType(
    itemLevelBonuses: ItemLevelBonuses,
    superCategory: keyof ItemLevelBonuses,
    categories: (keyof ItemLevelBonuses)[],
): Partial<ItemLevelBonuses> {
    const result: Partial<ItemLevelBonuses> = {};
    const superCategoryBonus = itemLevelBonuses[superCategory];
    categories.forEach(category => {
        const itemLevelBonus = itemLevelBonuses[category];
        if (itemLevelBonus !== superCategoryBonus) {
            result[category] = itemLevelBonus;
        }
    });
    return result;
}

export function groupAvailableItems(itemLevelBonuses: ItemLevelBonuses): Partial<ItemLevelBonuses> {
    return {
        other: itemLevelBonuses.other,
        ...groupByItemType(itemLevelBonuses, 'luxuryMagical',
            ['luxuryArcane', 'luxuryDivine', 'luxuryOccult', 'luxuryPrimal']),
        ...groupByItemType(itemLevelBonuses, 'magical',
            ['arcane', 'divine', 'occult', 'primal', 'luxuryMagical']),
        ...groupByItemType(itemLevelBonuses, 'other',
            ['magical', 'alchemical']),
    };
}


export async function showStructureHints(game: Game, actor: Actor | null): Promise<void> {
    if (isFirstGm(game) && actor && isStructureActor(actor)) {
        const data = getStructureFromActor(actor);
        if (data) {
            const messages = [];
            // reduce unrest
            const reduceUnrestBy = data.reduceUnrestBy;
            if (reduceUnrestBy) {
                const prefix = !reduceUnrestBy.moreThanOncePerTurn ? 'The first time you build this structure each turn ' : 'Each time you build this structure ';
                const postfix = reduceUnrestBy.note === undefined ? '' : ` ${reduceUnrestBy.note}`;
                messages.push(prefix + loseUnrest(reduceUnrestBy.value) + postfix);
            }
            // reduce ruin
            const reduceRuinBy = data.reduceRuinBy;
            if (reduceRuinBy) {
                const prefix = !reduceRuinBy.moreThanOncePerTurn ? 'The first time you build this structure each turn ' : 'Each time you build this structure ';
                const value = reduceRuinBy.value;
                if (reduceRuinBy.ruin === 'any') {
                    messages.push(`${prefix}choose one of: <ul>${allRuins.map(ruin => `<li>${loseRuin(ruin, value)}</li>`).join('')}</ul>`);
                } else {
                    messages.push(prefix + loseRuin(reduceRuinBy.ruin, value));
                }
            }
            // gain ruin
            const gainRuinBy = data.gainRuin;
            if (gainRuinBy) {
                const prefix = !gainRuinBy.moreThanOncePerTurn ? 'The first time you build this structure each turn ' : 'Each time you build this structure ';
                const value = gainRuinBy.value;
                if (gainRuinBy.ruin === 'any') {
                    messages.push(`${prefix}choose one of: <ul>${allRuins.map(ruin => `<li>${gainRuin(ruin, value)}</li>`).join('')}</ul>`);
                } else {
                    messages.push(prefix + gainRuin(gainRuinBy.ruin, value));
                }
            }
            if (messages.length > 0) {
                await postChatMessage(messages
                    .map(m => `<div>${m}</div>`)
                    .join(''));
            }
        }
    }
}