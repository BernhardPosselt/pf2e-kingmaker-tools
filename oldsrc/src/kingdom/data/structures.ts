import {Skill} from './skills';
import {Ruin} from './ruin';


export interface ActivityBonusRule {
    value: number;
    activity: string;
}

export interface SkillBonusRule {
    value: number;
    skill: Skill;
    // e.g. 'quell-unrest'
    activity?: string;
}

export interface AvailableItemsRule {
    value: number;
    // e.g. 'alchemical' or 'magic'
    group?: ItemGroup;
    maximumStacks?: number;
}

export interface SettlementEventsRule {
    value: number;
}

export interface LeadershipActivityRule {
    value: number;
}

export interface CommodityStorage {
    ore: number;
    food: number;
    lumber: number;
    stone: number;
    luxuries: number;
}

interface ConstructionSkill {
    skill: Skill;
    proficiencyRank?: number;
}

interface Construction {
    skills: ConstructionSkill[];
    lumber?: number;
    luxuries?: number;
    ore?: number;
    stone?: number;
    rp: number;
    dc: number;
}

interface ReduceUnrestBy {
    value: string;
    moreThanOncePerTurn?: boolean;
    note?: string;
}

interface ReduceRuinBy {
    value: string;
    ruin: Ruin | 'any';
    moreThanOncePerTurn?: boolean;
}

interface GainRuin {
    value: string;
    ruin: Ruin | 'any';
    moreThanOncePerTurn?: boolean;
}

interface IncreaseResourceDice {
    village?: number;
    town?: number;
    city?: number;
    metropolis?: number;
}

export interface Structure {
    name: string;
    stacksWith?: string;
    construction?: Construction;
    notes?: string;
    preventItemLevelPenalty?: boolean;
    enableCapitalInvestment?: boolean,
    skillBonusRules?: SkillBonusRule[];
    activityBonusRules?: ActivityBonusRule[];
    availableItemsRules?: AvailableItemsRule[];
    settlementEventRules?: SettlementEventsRule[];
    leadershipActivityRules?: LeadershipActivityRule[];
    storage?: Partial<CommodityStorage>;
    increaseLeadershipActivities?: boolean;
    isBridge?: boolean;
    consumptionReduction?: number;
    consumptionReductionStacks?: boolean;
    unlockActivities?: string[];
    ignoreConsumptionReductionOf?: string[];
    traits?: BuildingTrait[];
    lots: number;
    affectsEvents?: boolean;
    affectsDowntime?: boolean;
    reducesUnrest?: boolean;
    reducesRuin?: boolean;
    level?: number;
    upgradeFrom?: string[];
    reduceUnrestBy?: ReduceUnrestBy;
    reduceRuinBy?: ReduceRuinBy;
    gainRuin?: GainRuin;
    increaseResourceDice?: IncreaseResourceDice
}

export const allBuildingTraits = ['edifice', 'yard', 'building', 'famous', 'infamous', 'residential', 'infrastructure'];

export type BuildingTrait = typeof allBuildingTraits[number];

export type ActivityBonuses = Partial<Record<string, number>>;

export interface SkillItemBonus {
    value: number;
    activities: ActivityBonuses;
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

export const magicalItemGroups: ItemGroup[] = ['magical', 'divine', 'occult', 'primal', 'arcane'];

export const mundaneItemGroups: ItemGroup[] = ['alchemical', 'luxury', 'other'];
export const itemGroups: ItemGroup[] = mundaneItemGroups.concat(magicalItemGroups);

export type ItemLevelBonuses = {
    alchemical: number;
    magical: number;
    divine: number;
    occult: number;
    primal: number;
    arcane: number;
    luxuryMagical: number;
    luxuryDivine: number;
    luxuryPrimal: number;
    luxuryArcane: number;
    luxuryOccult: number;
    other: number;
};