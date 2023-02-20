import {mergeObjects} from '../utils';
import {Activity, getActivitySkills} from './data/activities';
import {
    ActivityBonusRule,
    CommodityStorage,
    ItemGroup,
    ItemLevelBonuses,
    magicalItemGroups,
    SkillBonusRule,
    SkillItemBonus,
    SkillItemBonuses,
    Structure,
} from './data/structures';

export interface SettlementData {
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
    config: SettlementConfig;
    unlockActivities: Activity[];
    residentialBuildings: number;
}

function count<T>(items: T[], idFunction: (item: T) => string): Map<string, { count: number, item: T }> {
    return items.reduce((map, item) => {
        const id = idFunction(item);
        const count = (map.get(id)?.count ?? 0) + 1;
        return map.set(id, {count, item});
    }, new Map());
}

/**
 * Add up item bonuses of same structure
 */
function groupStructures(structures: Structure[], maxItemBonus: number): Structure[] {
    const structureOccurrences = count(structures, s => s.name);
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
                    return {
                        ...rule,
                        value: Math.min(rule.value * data.count, maxItemBonus),
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

function calculateItemLevelBonus(
    defaultPenalty: number,
    globallyStackingBonuses: number,
    value: number,
    maxItemLevelBonus: number,
): number {
    return Math.min(value + globallyStackingBonuses + defaultPenalty, maxItemLevelBonus);
}

function applyItemLevelRules(itemLevelBonuses: ItemLevelBonuses, structures: Structure[], maxItemLevelBonus: number): void {
    const defaultPenalty = structures.some(structure => structure.preventItemLevelPenalty === true) ? 0 : -2;

    // apply base values that stack with everything
    const globallyStackingBonuses = Math.min(
        structures
            .flatMap(structures => structures.availableItemsRules ?? [])
            .filter(rule => rule.group === undefined)
            .map(rule => rule.value)
            .reduce((a, b) => a + b, 0),
        maxItemLevelBonus,
    );

    const defaultBonus = calculateItemLevelBonus(defaultPenalty, globallyStackingBonuses, 0, maxItemLevelBonus);
    (Object.keys(itemLevelBonuses) as (ItemGroup)[]).forEach((key) => {
        itemLevelBonuses[key] = defaultBonus;
    });

    // magical overrides primal, divine, arcane, occult
    structures.forEach(structure => {
        structure.availableItemsRules?.forEach(rule => {
            const group = rule.group;
            if (group === 'magical') {
                const value = calculateItemLevelBonus(defaultPenalty, globallyStackingBonuses, rule.value, maxItemLevelBonus);
                magicalItemGroups.forEach(type => {
                    if (value > itemLevelBonuses[type]) {
                        itemLevelBonuses[type] = value;
                    }
                });
            }
        });
    });

    structures.forEach(structure => {
        structure.availableItemsRules?.forEach(rule => {
            const group = rule.group;
            if (group) {
                const value = calculateItemLevelBonus(defaultPenalty, globallyStackingBonuses, rule.value, maxItemLevelBonus);
                if (value > itemLevelBonuses[group]) {
                    itemLevelBonuses[group] = value;
                }
            }
        });
    });
}

function applyLeadershipActivityBonuses(result: SettlementData, structures: Structure[]): void {
    structures.forEach(structure => {
        structure.leadershipActivityRules?.forEach(rule => {
            if (rule.value > result.leadershipActivityBonus) {
                result.leadershipActivityBonus = rule.value;
            }
        });
    });
}

function applySettlementEventBonuses(result: SettlementData, structures: Structure[]): void {
    structures.forEach(structure => {
        structure.settlementEventRules?.forEach(rule => {
            if (rule.value > result.settlementEventBonus) {
                result.settlementEventBonus = rule.value;
            }
        });
    });
}

function simplifyRules(rules: ActivityBonusRule[]): SkillBonusRule[] {
    return rules.flatMap(rule => {
        const activity = rule.activity;
        const skills = getActivitySkills(activity);
        return skills.map(skill => {
            return {
                value: rule.value,
                skill,
                activity: activity,
            };
        });
    });
}

function unionizeStructures(structures: Structure[]): Structure[] {
    return structures.map(structure => {
        const simplifiedRules = simplifyRules(structure.activityBonusRules ?? []);
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

export interface SettlementConfig {
    type: 'Village' | 'Town' | 'City' | 'Metropolis';
    lots: string;
    requiredKingdomLevel: number;
    population: string;
    level: number;
    consumption: number;
    maxItemBonus: number;
    influence: number;
}

export function getSettlementConfig(settlementLevel: number): SettlementConfig {
    if (settlementLevel < 2) {
        return {
            type: 'Village',
            consumption: 1,
            influence: 0,
            lots: '1',
            requiredKingdomLevel: 1,
            level: settlementLevel,
            maxItemBonus: 1,
            population: '400 or less',
        };
    }
    if (settlementLevel < 5) {
        return {
            type: 'Town',
            consumption: 2,
            influence: 1,
            requiredKingdomLevel: 3,
            lots: '4',
            level: settlementLevel,
            maxItemBonus: 1,
            population: '401-2000',
        };
    } else if (settlementLevel < 10) {
        return {
            type: 'City',
            consumption: 4,
            influence: 2,
            requiredKingdomLevel: 9,
            lots: '9',
            level: settlementLevel,
            maxItemBonus: 2,
            population: '2001–25000',
        };
    } else {
        return {
            type: 'Metropolis',
            consumption: 6,
            influence: 3,
            lots: '10+',
            requiredKingdomLevel: 15,
            level: settlementLevel,
            maxItemBonus: 3,
            population: '25001+',
        };
    }
}

function mergeBonuses(capital: SkillItemBonus, settlement: SkillItemBonus): SkillItemBonus {
    const skillValue = capital.value > settlement.value ? capital.value : settlement.value;
    const activities = mergeObjects(capital.activities, settlement.activities, (a: number, b: number) => Math.max(a, b));
    const filteredActivities = Object.fromEntries(
        Object.entries(activities)
            .filter(([, value]) => value > skillValue),
    );
    return {
        value: skillValue,
        activities: filteredActivities,
    };
}

export function includeCapital(capital: SettlementData, settlement: SettlementData): SettlementData {
    return {
        ...settlement,
        increaseLeadershipActivities: capital.increaseLeadershipActivities,
        unlockActivities: capital.unlockActivities.concat(settlement.unlockActivities),
        leadershipActivityBonus: Math.max(capital.leadershipActivityBonus, settlement.leadershipActivityBonus),
        skillBonuses: mergeObjects(capital.skillBonuses, settlement.skillBonuses, mergeBonuses) as SkillItemBonuses,
    };
}

function applyUnlockedActivities(unlockActivities: Activity[], groupedStructures: Structure[]): void {
    groupedStructures
        .flatMap(structure => structure?.unlockActivities ?? [])
        .forEach(activity => {
            unlockActivities.push(activity);
        });
}

/**
 * Calculate all Bonuses of a settlement
 */
export function evaluateStructures(structures: Structure[], settlementLevel: number): SettlementData {
    const settlementData = getSettlementConfig(settlementLevel);
    const maxItemBonus = settlementData.maxItemBonus;
    const allowCapitalInvestment = structures.some(structure => structure.enableCapitalInvestment === true);
    const notes = Array.from(new Set(structures.flatMap(result => result.notes ?? [])));
    const consumptionReduction = calculateConsumptionReduction(structures);
    const result: SettlementData = {
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
            luxury: 0,
            magical: 0,
            other: 0,
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
        unlockActivities: [],
        residentialBuildings: structures.filter(structure => structure?.traits?.includes('residential')).length,
    };
    const unionizedStructures = unionizeStructures(structures);
    applyStorageIncreases(result.storage, structures);
    const groupedStructures = groupStructures(unionizedStructures, maxItemBonus);
    applyUnlockedActivities(result.unlockActivities, groupedStructures);
    applySettlementEventBonuses(result, groupedStructures);
    applyLeadershipActivityBonuses(result, groupedStructures);
    applySkillBonusRules(result.skillBonuses, groupedStructures);
    applyItemLevelRules(result.itemLevelBonuses, groupedStructures, maxItemBonus);
    return result;
}
