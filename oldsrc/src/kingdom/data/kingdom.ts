import {Leader} from './leaders';
import {getKingdomActivities, KingdomActivity} from './activityData';
import {AbilityScores} from './abilities';
import {Skill} from "./skills";
import {LeadershipLeaderType} from "../skills";
import {UntrainedProficiencyMode} from "../../utils";

export type ResourceDieSize = 'd4' | 'd6' | 'd8' | 'd10' | 'd12';

export interface KingdomSizeData {
    sizeFrom: number;
    sizeTo?: number;
    type: 'territory' | 'province' | 'state' | 'country' | 'dominion';
    resourceDieSize: ResourceDieSize;
    controlDCModifier: number;
    commodityCapacity: number;
}

export type Leaders = Record<Leader, LeaderValues>;

export interface LeaderValues {
    uuid: string | null | undefined;
    invested: boolean;
    type: LeadershipLeaderType;
    vacant: boolean;
}

export interface RuinValues {
    value: number;
    penalty: number;
    threshold: number;
}

export interface Ruin {
    corruption: RuinValues;
    crime: RuinValues;
    decay: RuinValues;
    strife: RuinValues;
}


export interface WorkSite {
    quantity: number;
    resources: number;
}

export interface WorkSites {
    farmlands: WorkSite;
    lumberCamps: WorkSite;
    mines: WorkSite;
    quarries: WorkSite;
    luxurySources: WorkSite;
}

export interface Resources {
    now: number;
    next: number;
}

export interface Commodities {
    food: number;
    lumber: number;
    luxuries: number;
    ore: number;
    stone: number;
}

export interface Group {
    name: string;
    negotiationDC: number;
    atWar: boolean;
    relations: 'none' | 'diplomatic-relations' | 'trade-agreement';
}

export interface SkillRanks {
    agriculture: number;
    arts: number;
    boating: number;
    defense: number;
    engineering: number;
    exploration: number;
    folklore: number;
    industry: number;
    intrigue: number;
    magic: number;
    politics: number;
    scholarship: number;
    statecraft: number;
    trade: number;
    warfare: number;
    wilderness: number;
}

export const allHeartlands = [
    'forest-or-swamp',
    'hill-or-plain',
    'lake-or-river',
    'mountain-or-ruins',
] as const;

export type Heartland = typeof allHeartlands[number];

export const allFameTypes = ['famous', 'infamous'] as const;

export type FameType = typeof allFameTypes[number];

export interface Feat {
    id: string;
    level: number;
}

export interface BonusFeat {
    id: string;
}

export interface MileStone {
    name: string;
    xp: number;
    completed: boolean;
    homebrew: boolean;
}

export interface OngoingEvent {
    name: string;
}

export interface Settlement {
    sceneId: string;
    lots: number;
    level: number;
    type: 'capital' | 'settlement';
    secondaryTerritory: boolean;
    manualSettlementLevel?: boolean;
    waterBorders: number;
}

export type ResourceAutomationMode = 'kingmaker' | 'tileBased' | 'manual';

export interface KingdomSettings {
    expandMagicUse: boolean;
    rpToXpConversionRate: number;
    rpToXpConversionLimit: number;
    resourceDicePerVillage: number;
    resourceDicePerTown: number;
    resourceDicePerCity: number;
    resourceDicePerMetropolis: number;
    xpPerClaimedHex: number;
    includeCapitalItemModifier: boolean;
    cultOfTheBloomEvents: boolean;
    autoCalculateSettlementLevel: boolean;
    vanceAndKerensharaXP: boolean;
    capitalInvestmentInCapital: boolean;
    reduceDCToBuildLumberStructures: boolean;
    kingdomSkillIncreaseEveryLevel: boolean;
    kingdomAllStructureItemBonusesStack: boolean;
    kingdomIgnoreSkillRequirements: boolean;
    autoCalculateArmyConsumption: boolean;
    enableLeadershipModifiers: boolean;
    kingdomEventRollMode: keyof CONFIG.Dice.RollModes;
    automateResources: ResourceAutomationMode;
    proficiencyMode: UntrainedProficiencyMode;
    kingdomEventsTable?: string;
    kingdomCultTable?: string;
    maximumFamePoints: number;
    leaderKingdomSkills: LeaderKingdomSkills;
    leaderSkills: LeaderSkills;
}

export interface LeaderKingdomSkills {
    ruler: Skill[];
    counselor: Skill[];
    emissary: Skill[];
    general: Skill[];
    magister: Skill[];
    treasurer: Skill[];
    viceroy: Skill[];
    warden: Skill[];
}

export interface LeaderSkills {
    ruler: string[];
    counselor: string[];
    emissary: string[];
    general: string[];
    magister: string[];
    treasurer: string[];
    viceroy: string[];
    warden: string[];
}


export interface Kingdom {
    name: string;
    atWar: boolean;
    charter: string;
    government: string;
    settings: KingdomSettings;
    fame: {
        now: number;
        next: number;
        type: FameType;
    };
    level: number;
    xpThreshold: number;
    xp: number;
    size: number;
    unrest: number;
    resourcePoints: Resources;
    resourceDice: Resources;
    workSites: WorkSites;
    heartland: Heartland;
    realmSceneId: string | null;
    consumption: {
        armies: number;
        now: number;
        next: number;
    };
    notes: {
        public: string;
        gm: string;
    };
    homebrewActivities: KingdomActivity[];
    supernaturalSolutions: number;
    turnsWithoutCultEvent: number;
    creativeSolutions: number;
    leaders: Leaders;
    commodities: {
        now: Commodities;
        next: Commodities;
    };
    groups: Group[];
    feats: Feat[];
    bonusFeats: BonusFeat[];
    skillRanks: SkillRanks;
    abilityScores: AbilityScores;
    ruin: Ruin;
    activeSettlement: string;
    milestones: MileStone[];
    ongoingEvents: OngoingEvent[];
    turnsWithoutEvent: number;
    activityBlacklist: string[];
    modifiers: unknown[];
    settlements: Settlement[];
}


interface KingdomLevelData {
    investedLeadershipBonus: number;
    resourceDice: number;
}

export function getLevelData(kingdomLevel: number): KingdomLevelData {
    const investedLeadershipBonus = kingdomLevel < 8 ? 1 : (kingdomLevel < 16 ? 2 : 3);
    return {
        investedLeadershipBonus,
        resourceDice: kingdomLevel + 4,
    };
}

export const kingdomSizeData: KingdomSizeData[] = [{
    sizeFrom: 0,
    sizeTo: 9,
    type: 'territory',
    resourceDieSize: 'd4',
    controlDCModifier: 0,
    commodityCapacity: 4,
}, {
    sizeFrom: 10,
    sizeTo: 24,
    type: 'province',
    resourceDieSize: 'd6',
    controlDCModifier: 1,
    commodityCapacity: 8,
}, {
    sizeFrom: 25,
    sizeTo: 49,
    type: 'state',
    resourceDieSize: 'd8',
    controlDCModifier: 2,
    commodityCapacity: 12,
}, {
    sizeFrom: 50,
    sizeTo: 99,
    type: 'country',
    resourceDieSize: 'd10',
    controlDCModifier: 3,
    commodityCapacity: 16,
}, {
    sizeFrom: 100,
    type: 'dominion',
    resourceDieSize: 'd12',
    controlDCModifier: 4,
    commodityCapacity: 20,
}];

export function getSizeData(kingdomSize: number): KingdomSizeData {
    return kingdomSizeData.find(d => kingdomSize >= d.sizeFrom
        && kingdomSize <= (d.sizeTo ?? Number.MAX_SAFE_INTEGER))!;
}

export function getControlDC(level: number, size: number, leaderVacant: boolean): number {
    const sizeModifier = getSizeData(size).controlDCModifier;
    const adjustedLevel = level < 5 ? level - 1 : level;
    const vacancyPenalty = leaderVacant ? 2 : 0;
    return 14 + adjustedLevel + Math.floor(adjustedLevel / 3) + sizeModifier + vacancyPenalty;
}

function getDefaultMilestones(): MileStone[] {
    return [
        {name: 'Build Roads for the first time', xp: 20, completed: false, homebrew: true},
        {name: 'Build your first Famous/Infamous Structure', xp: 20, completed: false, homebrew: true},
        {
            name: 'Build your first seat of government (Town Hall, Castle, or Palace)',
            xp: 20,
            completed: false,
            homebrew: true,
        },
        {
            name: 'Build your first Structure requiring Expert in a Kingdom Skill',
            xp: 20,
            completed: false,
            homebrew: true,
        },
        {name: 'Celebrate your first successful Holiday', xp: 20, completed: false, homebrew: true},
        {name: 'Claim your first new Hex (2nd hex overall)', xp: 20, completed: false, homebrew: true},
        {name: 'Complete your First successful Infiltration', xp: 20, completed: false, homebrew: true},
        {name: 'Create your first Masterpiece', xp: 20, completed: false, homebrew: true},
        {name: 'Establish your first Farmland', xp: 20, completed: false, homebrew: true},
        {name: 'Establish your first Lumber Camp', xp: 20, completed: false, homebrew: true},
        {name: 'Establish your first Mine', xp: 20, completed: false, homebrew: true},
        {name: 'Establish your first Quarry', xp: 20, completed: false, homebrew: true},
        {name: 'Fortify your first hex', xp: 20, completed: false, homebrew: true},
        {name: 'Successfully use your first Creative Solution', xp: 20, completed: false, homebrew: true},
        {name: 'Successfully use your first Supernatural Solution', xp: 20, completed: false, homebrew: true},

        {name: 'Claim your first Landmark', xp: 40, completed: false, homebrew: false},
        {name: 'Claim your first Refuge', xp: 40, completed: false, homebrew: false},
        {name: 'Establish your first village', xp: 40, completed: false, homebrew: false},
        {name: 'Establish your second Village', xp: 40, completed: false, homebrew: true},
        {name: 'Reach kingdom Size 10', xp: 40, completed: false, homebrew: false},
        {name: 'Recruit your first regular Army', xp: 40, completed: false, homebrew: true},
        {name: 'Successfully resolve a random Kingdom Event', xp: 40, completed: false, homebrew: true},

        {name: 'Achieve your first successful Pledge of Fealty', xp: 60, completed: false, homebrew: true},
        {name: 'All eight leadership roles are assigned', xp: 60, completed: false, homebrew: false},
        {
            name: 'Build your first Structure requiring Master in a Kingdom Skill',
            xp: 60,
            completed: false,
            homebrew: true,
        },
        {name: 'Establish diplomatic relations for the first time', xp: 60, completed: false, homebrew: false},
        {name: 'Expand a village into your first town', xp: 60, completed: false, homebrew: false},
        {name: 'Reach kingdom Size 25', xp: 60, completed: false, homebrew: false},
        {name: 'Recruit your first Specialized Army', xp: 60, completed: false, homebrew: true},
        {name: 'Win your first War Encounter', xp: 60, completed: false, homebrew: true},

        {name: 'Establish your first trade agreement', xp: 80, completed: false, homebrew: false},
        {name: 'Expand a town into your first city', xp: 80, completed: false, homebrew: false},
        {name: 'Reach kingdom Size 50', xp: 80, completed: false, homebrew: false},
        {name: 'Spend 100 RP during a Kingdom turn', xp: 80, completed: false, homebrew: false},
        ...getCultEventMilestones(),
        {name: 'Expand a city into your first metropolis', xp: 120, completed: false, homebrew: false},
        {name: 'Reach kingdom Size 100', xp: 120, completed: false, homebrew: false},
    ];
}

export function getCultEventMilestones(): MileStone[] {
    return [
        {name: 'Cult Event (Chapter 5): Cult Activity', xp: 80, completed: false, homebrew: false},
        {name: 'Cult Event (Chapter 5): Public Outburst', xp: 80, completed: false, homebrew: false},
        {name: 'Cult Event (Chapter 5): Too Close to Home', xp: 80, completed: false, homebrew: false},
        {name: 'Cult Event (Chapter 5): Urban Outburst', xp: 80, completed: false, homebrew: false},
    ];
}

export function getDefaultKingdomData(game: Game): Kingdom {
    return {
        turnsWithoutEvent: 0,
        turnsWithoutCultEvent: 0,
        name: '',
        atWar: false,
        charter: '',
        government: '',
        activeSettlement: '',
        notes: {
            public: '',
            gm: '',
        },
        fame: {
            type: 'famous',
            now: 0,
            next: 0,
        },
        supernaturalSolutions: 0,
        creativeSolutions: 0,
        level: 1,
        xpThreshold: 1000,
        xp: 0,
        size: 1,
        unrest: 0,
        settlements: [],
        feats: [],
        bonusFeats: [],
        ongoingEvents: [],
        groups: [],
        milestones: getDefaultMilestones(),
        realmSceneId: null,
        settings: {
            expandMagicUse: false,
            rpToXpConversionRate: 1,
            rpToXpConversionLimit: 120,
            resourceDicePerVillage: 0,
            resourceDicePerTown: 0,
            resourceDicePerCity: 0,
            resourceDicePerMetropolis: 0,
            xpPerClaimedHex: 10,
            includeCapitalItemModifier: true,
            cultOfTheBloomEvents: false,
            autoCalculateSettlementLevel: true,
            vanceAndKerensharaXP: false,
            capitalInvestmentInCapital: false,
            reduceDCToBuildLumberStructures: false,
            kingdomSkillIncreaseEveryLevel: false,
            kingdomAllStructureItemBonusesStack: false,
            kingdomIgnoreSkillRequirements: false,
            autoCalculateArmyConsumption: true,
            enableLeadershipModifiers: false,
            kingdomEventRollMode: "gmroll",
            automateResources: "kingmaker",
            proficiencyMode: "none",
            kingdomEventsTable: undefined,
            kingdomCultTable: undefined,
            maximumFamePoints: 3,
            leaderKingdomSkills: {
                ruler: ["industry", "intrigue", "politics", "statecraft", "warfare"],
                counselor: ["arts", "folklore", "politics", "scholarship", "trade"],
                emissary: ["intrigue", "magic", "politics", "statecraft", "trade"],
                general: ["boating", "defense", "engineering", "exploration", "warfare"],
                magister: ["defense", "folklore", "magic", "scholarship", "wilderness"],
                treasurer: ["arts", "boating", "industry", "intrigue", "trade"],
                viceroy: ["agriculture", "engineering", "industry", "scholarship", "wilderness"],
                warden: ["agriculture", "boating", "defense", "exploration", "wilderness"],
            },
            leaderSkills: {
                ruler: ["diplomacy", "deception", "intimidation", "performance", "society", "heraldry", "politics", "ruler"],
                counselor: ["diplomacy", "deception", "performance", "religion", "society", "academia", "art", "counselor"],
                emissary: ["diplomacy", "deception", "intimidation", "stealth", "thievery", "politics", "underworld", "emissary"],
                general: ["diplomacy", "athletics", "crafting", "intimidation", "survival", "scouting", "warfare", "general"],
                magister: ["diplomacy", "arcana", "nature", "occultism", "religion", "academia", "scribing", "magister"],
                treasurer: ["diplomacy", "crafting", "medicine", "society", "thievery", "labor", "mercantile", "treasurer"],
                viceroy: ["diplomacy", "crafting", "medicine", "nature", "society", "architecture", "engineering", "viceroy"],
                warden: ["diplomacy", "athletics", "nature", "stealth", "survival", "farming", "hunting", "warden"],
            },
        },
        workSites: {
            farmlands: {
                quantity: 0,
                resources: 0,
            },
            quarries: {
                quantity: 0,
                resources: 0,
            },
            lumberCamps: {
                quantity: 0,
                resources: 0,
            },
            mines: {
                quantity: 0,
                resources: 0,
            },
            luxurySources: {
                quantity: 0,
                resources: 0,
            },
        },
        commodities: {
            now: {
                food: 0,
                ore: 0,
                lumber: 0,
                stone: 0,
                luxuries: 0,
            },
            next: {
                food: 0,
                ore: 0,
                lumber: 0,
                stone: 0,
                luxuries: 0,
            },
        },
        resourceDice: {
            now: 0,
            next: 0,
        },
        resourcePoints: {
            next: 0,
            now: 0,
        },
        heartland: 'hill-or-plain',
        consumption: {
            armies: 0,
            now: 0,
            next: 0,
        },
        leaders: {
            ruler: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            counselor: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            general: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            emissary: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            magister: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            treasurer: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            viceroy: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
            warden: {
                invested: false,
                vacant: false,
                type: 'pc',
                uuid: null,
            },
        },
        skillRanks: {
            agriculture: 0,
            arts: 0,
            boating: 0,
            defense: 0,
            engineering: 0,
            exploration: 0,
            folklore: 0,
            industry: 0,
            intrigue: 0,
            magic: 0,
            politics: 0,
            scholarship: 0,
            statecraft: 0,
            trade: 0,
            warfare: 0,
            wilderness: 0,
        },
        abilityScores: {
            culture: 10,
            economy: 10,
            loyalty: 10,
            stability: 10,
        },
        ruin: {
            corruption: {
                penalty: 0,
                threshold: 10,
                value: 0,
            },
            crime: {
                penalty: 0,
                threshold: 10,
                value: 0,
            },
            decay: {
                penalty: 0,
                threshold: 10,
                value: 0,
            },
            strife: {
                penalty: 0,
                threshold: 10,
                value: 0,
            },
        },
        activityBlacklist: getKingdomActivities(game, [])
            .filter((data) => !data.enabled)
            .map((data) => data.id),
        modifiers: [],
        homebrewActivities: [],
    };
}