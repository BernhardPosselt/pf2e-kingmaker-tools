export type Action = 'establish-trade-agreement'
    | 'provide-care'
    | 'hire-adventurers'
    | 'celebrate-holiday'
    | 'demolish'
    | 'quell-unrest'
    | 'rest-and-relax'
    | 'harvest-crops'
    | 'garrison-army'
    | 'recover-army'
    | 'recruit-army'
    | 'deploy-army'
    | 'train-army'
    | 'outfit-army'
    | 'establish-work-site-mine'
    | 'establish-work-site-lumber'
    | 'establish-work-site-quarry'
    | 'establish-work-site'
    | 'establish-farmland'
    | 'create-a-masterpiece'
    | 'fortify-hex'
    | 'go-fishing'
    | 'trade-commodities'
    | 'gather-lifestock'
    | 'purchase-commodities'
    | 'improve-lifestyle'
    | 'craft-luxuries'
    | 'tap-treasury'
    | 'infiltration'
    | 'clandestine-business'
    | 'send-diplomatic-envoy'
    | 'request-foreign-aid'
    | 'supernatural-solution'
    | 'new-leadership'
    | 'pledge-of-fealty'
    | 'creative-solution'
    | 'build-structure'
    | 'repair-reputation-decay'
    | 'repair-reputation-corruption'
    | 'repair-reputation-crime'
    | 'repair-reputation-strife'
    | 'prognostication'
    | 'capital-investment'
    | 'collect-taxes'
    | 'build-roads'
    | 'clear-hex'
    | 'establish-settlement'
    | 'irrigation'
    | 'abandon-hex'
    | 'claim-hex'
    | 'relocate-capital'
    | 'manage-trade-agreements'
    ;

export type Skill = 'agriculture'
    | 'arts'
    | 'boating'
    | 'defense'
    | 'engineering'
    | 'exploration'
    | 'folklore'
    | 'industry'
    | 'intrigue'
    | 'magic'
    | 'politics'
    | 'scholarship'
    | 'statecraft'
    | 'trade'
    | 'warfare'
    | 'wilderness'
    ;

export const allSkills: Skill[] = [
    'agriculture',
    'arts',
    'boating',
    'defense',
    'engineering',
    'exploration',
    'folklore',
    'industry',
    'intrigue',
    'magic',
    'politics',
    'scholarship',
    'statecraft',
    'trade',
    'warfare',
    'wilderness',
];

export const actionSkills: Record<Action, (Skill)[] | ['*']> = {
    // agriculture
    'establish-farmland': ['agriculture'],
    'harvest-crops': ['agriculture'],
    // arts
    'craft-luxuries': ['arts'],
    'rest-and-relax': ['arts', 'boating', 'scholarship', 'trade', 'wilderness'],
    'quell-unrest': ['arts', 'folklore', 'intrigue', 'magic', 'politics', 'warfare'],
    'create-a-masterpiece': ['arts'],
    'repair-reputation-corruption': ['arts'],
    // boating
    'establish-trade-agreement': ['boating', 'magic', 'trade'],
    'go-fishing': ['boating'],
    // defense
    'fortify-hex': ['defense'],
    'provide-care': ['defense'],
    // engineering
    'build-roads': ['engineering'],
    'clear-hex': ['engineering', 'exploration'],
    'demolish': ['engineering'],
    'establish-settlement': ['engineering', 'industry', 'politics', 'scholarship'],
    'establish-work-site': ['engineering'],
    'establish-work-site-quarry': ['engineering'],
    'establish-work-site-lumber': ['engineering'],
    'establish-work-site-mine': ['engineering'],
    'irrigation': ['engineering'],
    'repair-reputation-decay': ['engineering'],
    // exploration
    'abandon-hex': ['exploration', 'wilderness'],
    'claim-hex': ['exploration', 'wilderness'],
    'hire-adventurers': ['exploration'],
    // folklore
    'celebrate-holiday': ['folklore'],
    // industry
    'relocate-capital': ['industry'],
    'trade-commodities': ['industry'],
    // intrigue
    'infiltration': ['intrigue'],
    'new-leadership': ['intrigue', 'politics', 'statecraft', 'warfare'],
    'clandestine-business': ['intrigue'],
    'pledge-of-fealty': ['intrigue', 'statecraft', 'warfare'],
    'repair-reputation-strife': ['intrigue'],
    // magic
    'supernatural-solution': ['magic'],
    'prognostication': ['magic'],
    // politics
    'improve-lifestyle': ['politics'],
    // scholarship
    'creative-solution': ['scholarship'],
    // statecraft
    'tap-treasury': ['statecraft'],
    'request-foreign-aid': ['statecraft'],
    'send-diplomatic-envoy': ['statecraft'],
    // trade
    'capital-investment': ['trade'],
    'manage-trade-agreements': ['trade'],
    'purchase-commodities': ['trade'],
    'collect-taxes': ['trade'],
    'repair-reputation-crime': ['trade'],
    // warfare
    'garrison-army': ['warfare'],
    'deploy-army': ['warfare'],
    'outfit-army': ['warfare'],
    'train-army': ['warfare'],
    'recover-army': ['warfare'],
    'recruit-army': ['warfare'],

    // wilderness
    'gather-lifestock': ['wilderness'],

    // other
    'build-structure': ['*'],

    // TODO: companion actions
};

export interface ActionBonusRule {
    value: number;
    action: Action;
}

export interface SkillBonusRule {
    value: number;
    skill: Skill;
    // e.g. 'quell-unrest'
    action?: Action;
}

export interface AvailableItemsRule {
    value: number;
    // e.g. 'alchemical' or 'magic'
    group?: ItemGroup;
}

export interface SettlementEventsRule {
    value: number;
}

export interface LeadershipActivityRule {
    value: number;
}

export interface Storage {
    ore: number;
    food: number;
    lumber: number;
    stone: number;
    luxuries: number;
}

export interface Structure {
    name: string;
    notes?: string;
    preventItemLevelPenalty?: boolean;
    enableCapitalInvestment?: boolean,
    skillBonusRules?: SkillBonusRule[];
    actionBonusRules?: ActionBonusRule[];
    availableItemsRules?: AvailableItemsRule[];
    settlementEventRules?: SettlementEventsRule[];
    leadershipActivityRules?: LeadershipActivityRule[];
    storage?: Partial<Storage>;
    increaseLeadershipActivities?: boolean;
    consumptionReduction?: number;
}

export type ActionBonuses = Partial<Record<Action, number>>;

export interface SkillItemBonus {
    value: number;
    actions: ActionBonuses;
}

export type SkillItemBonuses = Record<Skill, SkillItemBonus>;

export type ItemGroup = 'divine'
    | 'alchemical'
    | 'primal'
    | 'occult'
    | 'arcane'
    | 'luxury'
    | 'magical'
    | 'other';

export const itemGroups: ItemGroup[] = ['divine', 'alchemical', 'primal', 'occult', 'arcane', 'luxury', 'magical', 'other'];

export type ItemLevelBonuses = Record<ItemGroup, number>;

export interface SettlementData {
    allowCapitalInvestment: boolean;
    notes: string[];
    skillBonuses: SkillItemBonuses;
    itemLevelBonuses: ItemLevelBonuses;
    settlementEventBonus: number;
    leadershipActivityBonus: number;
    storage: Storage;
    increaseLeadershipActivities: boolean;
    consumptionReduction: number;
    consumption: number;
    config: SettlementConfig;
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
            if (!rule.action) {
                if (rule.value > skill.value) {
                    skill.value = rule.value;
                }
            }
        });
    });
    // apply actions
    structures.forEach(structure => {
        structure.skillBonusRules?.forEach(rule => {
            const skill = result[rule.skill];
            const action = rule.action;
            if (action) {
                if (rule.value > skill.value &&
                    rule.value > (skill.actions[action] ?? 0)) {
                    skill.actions[action] = rule.value;
                }
            }
        });
    });
}

function calculateItemLevelBonus(
    defaultPenalty: number,
    globallyStackingBonuses: number,
    value: number,
    maxItemLevelBonus: number
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
        maxItemLevelBonus
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
                const types = ['magical', 'divine', 'occult', 'primal', 'arcane'] as (ItemGroup)[];
                types.forEach(type => {
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

function simplifyRules(rules: ActionBonusRule[]): SkillBonusRule[] {
    return rules.flatMap(rule => {
        const action = rule.action;
        const skills = actionSkills[action];
        const flattenedSkills = skills[0] === '*' ? allSkills : skills as Skill[];
        return flattenedSkills.map(skill => {
            return {
                value: rule.value,
                skill,
                action,
            };
        });
    });
}

function unionizeStructures(structures: Structure[]): Structure[] {
    return structures.map(structure => {
        const simplifiedRules = simplifyRules(structure.actionBonusRules ?? []);
        return {
            ...structure,
            skillBonusRules: [...(structure.skillBonusRules ?? []), ...simplifiedRules],
        };
    });
}

function applyStorageIncreases(storage: Storage, structures: Structure[]): void {
    structures
        .filter(structures => structures.storage)
        .forEach(structure => {
            const keys = ['ore', 'lumber', 'food', 'stone', 'luxuries'] as (keyof Storage)[];
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

function mergeObjects<A extends Record<string, V>, B extends Record<string, V>, V>(
    obj1: A,
    obj2: B,
    conflictFunction: (a: V, b: V) => V
): Record<string, V> {
    const entries: [string, V][] = [];
    for (const key of [...Object.keys(obj1), ...Object.keys(obj2)]) {
        if (key in obj1 && key in obj2) {
            entries.push([key, conflictFunction(obj1[key], obj2[key])]);
        } else if (key in obj1) {
            entries.push([key, obj1[key]]);
        } else if (key in obj2) {
            entries.push([key, obj2[key]]);
        }
    }
    return Object.fromEntries(entries);
}

function mergeBonuses(capital: SkillItemBonus, settlement: SkillItemBonus): SkillItemBonus {
    const skillValue = capital.value > settlement.value ? capital.value : settlement.value;
    const actions = mergeObjects(capital.actions, settlement.actions, (a: number, b: number) => Math.max(a, b));
    const filteredActions = Object.fromEntries(
        Object.entries(actions)
            .filter(([, value]) => value > skillValue)
    );
    return {
        value: skillValue,
        actions: filteredActions,
    };
}

export function includeCapital(capital: SettlementData, settlement: SettlementData): SettlementData {
    return {
        ...settlement,
        increaseLeadershipActivities: capital.increaseLeadershipActivities,
        leadershipActivityBonus: Math.max(capital.leadershipActivityBonus, settlement.leadershipActivityBonus),
        skillBonuses: mergeObjects(capital.skillBonuses, settlement.skillBonuses, mergeBonuses) as SkillItemBonuses,
    };
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
            agriculture: {value: 0, actions: {}},
            arts: {value: 0, actions: {}},
            boating: {value: 0, actions: {}},
            defense: {value: 0, actions: {}},
            engineering: {value: 0, actions: {}},
            exploration: {value: 0, actions: {}},
            folklore: {value: 0, actions: {}},
            industry: {value: 0, actions: {}},
            intrigue: {value: 0, actions: {}},
            magic: {value: 0, actions: {}},
            politics: {value: 0, actions: {}},
            scholarship: {value: 0, actions: {}},
            statecraft: {value: 0, actions: {}},
            trade: {value: 0, actions: {}},
            warfare: {value: 0, actions: {}},
            wilderness: {value: 0, actions: {}},
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
    };
    const unionizedStructures = unionizeStructures(structures);
    applyStorageIncreases(result.storage, structures);
    const groupedStructures = groupStructures(unionizedStructures, maxItemBonus);
    applySettlementEventBonuses(result, groupedStructures);
    applyLeadershipActivityBonuses(result, groupedStructures);
    applySkillBonusRules(result.skillBonuses, groupedStructures);
    applyItemLevelRules(result.itemLevelBonuses, groupedStructures, maxItemBonus);
    return result;
}
