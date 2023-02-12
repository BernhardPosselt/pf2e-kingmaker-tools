export interface KingdomSizeData {
    type: 'Territory' | 'Province' | 'State' | 'Country' | 'Dominion';
    resourceDie: 'd4' | 'd6' | 'd8' | 'd10' | 'd12';
    controlDCModifier: number;
    commodityStorage: number;
}

export interface Leader {
    name: string;
    invested: boolean;
    pc: boolean;
    vacant: boolean;
}

export interface Ruin {
    value: number;
    penalty: number;
    threshold: number;
}

interface WorkSite {
    locations: number;
    tappedResources: number;
}

interface Income {
    food: number;
    lumber: number;
    luxuries: number;
    ore: number;
    stone: number;
    bonusResourceDice: number;
    bonusResourcePoints: number;
}

export interface Kingdom {
    name: string;
    charter: string;
    government: string;
    fame: number;
    level: number;
    xp: number;
    size: number;
    unrest: number;
    rp: number;
    futureIncome: Income;
    income: Income;
    workSites: {
        farmlands: WorkSite;
        lumberCamps: WorkSite;
        mines: WorkSite;
        quarries: WorkSite;
    }
    heartland: 'swamp' | 'hills' | 'plains' | 'mountains' | 'forest';
    armyConsumption: number;
    leaders: {
        ruler: Leader;
        counselor: Leader;
        general: Leader;
        emissary: Leader;
        treasurer: Leader;
        viceroy: Leader;
        warden: Leader;
    }
    commodities: {
        food: number;
        lumber: number;
        luxuries: number;
        ore: number;
        stone: number;
    }
    tradeAgreements: number;

    skills: {
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
    abilities: {
        culture: number;
        economy: number;
        loyalty: number;
        stability: number;
    }
    ruin: {
        corruption: Ruin;
        crime: Ruin;
        decay: Ruin;
        strife: Ruin;
    }
}

export function getUnrestPenalty(unrest: number): number {
    if (unrest < 1) {
        return 0;
    } else if (unrest < 5) {
        return -1;
    } else if (unrest < 10) {
        return -2;
    } else if (unrest < 15) {
        return -3;
    } else {
        return -4;
    }
}

interface KingdomLevelData {
    claimHexAttempts: number;
    claimHexCircumstanceBonus: number;
    investedLeadershipBonus: number;
    resourceDice: number;
}

export function getLevelData(kingdomLevel: number): KingdomLevelData {
    const claimHexAttempts = kingdomLevel < 4 ? 1 : (kingdomLevel < 9 ? 2 : 3);
    const claimHexCircumstanceBonus = kingdomLevel < 4 ? 0 : 2;
    const investedLeadershipBonus = kingdomLevel < 8 ? 1 : (kingdomLevel < 16 ? 2 : 3);
    return {
        claimHexAttempts,
        claimHexCircumstanceBonus,
        investedLeadershipBonus,
        resourceDice: kingdomLevel + 4,
    };
}

export function getSizeData(kingdomSize: number): KingdomSizeData {
    if (kingdomSize < 10) {
        return {
            type: 'Territory',
            resourceDie: 'd4',
            controlDCModifier: 0,
            commodityStorage: 4,
        };
    } else if (kingdomSize < 25) {
        return {
            type: 'Province',
            resourceDie: 'd6',
            controlDCModifier: 1,
            commodityStorage: 8,
        };
    } else if (kingdomSize < 50) {
        return {
            type: 'State',
            resourceDie: 'd8',
            controlDCModifier: 2,
            commodityStorage: 12,
        };
    } else if (kingdomSize < 100) {
        return {
            type: 'Country',
            resourceDie: 'd10',
            controlDCModifier: 3,
            commodityStorage: 16,
        };
    } else {
        return {
            type: 'Dominion',
            resourceDie: 'd12',
            controlDCModifier: 4,
            commodityStorage: 20,
        };
    }
}

export function getControlDC(level: number, size: number): number {
    const sizeModifier = getSizeData(size).controlDCModifier;
    const adjustedLevel = level < 5 ? level - 1 : level;
    return 14 + adjustedLevel + Math.floor(adjustedLevel / 3) + sizeModifier;
}

export function getDefaultKingdomData(): Kingdom {
    return {
        name: '',
        charter: '',
        government: '',
        rp: 0,
        fame: 0,
        level: 0,
        xp: 0,
        size: 0,
        unrest: 0,
        workSites: {
            farmlands: {
                locations: 0,
                tappedResources: 0,
            },
            quarries: {
                locations: 0,
                tappedResources: 0,
            },
            lumberCamps: {
                locations: 0,
                tappedResources: 0,
            },
            mines: {
                locations: 0,
                tappedResources: 0,
            },
        },
        futureIncome: {
            food: 0,
            ore: 0,
            lumber: 0,
            bonusResourceDice: 0,
            stone: 0,
            luxuries: 0,
            bonusResourcePoints: 0,
        },
        income: {
            food: 0,
            ore: 0,
            lumber: 0,
            bonusResourceDice: 0,
            stone: 0,
            luxuries: 0,
            bonusResourcePoints: 0,
        },
        heartland: 'plains',
        armyConsumption: 0,
        leaders: {
            ruler: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
            counselor: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
            general: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
            emissary: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
            treasurer: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
            viceroy: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
            warden: {
                invested: false,
                pc: false,
                vacant: false,
                name: '',
            },
        },
        commodities: {
            food: 0,
            lumber: 0,
            luxuries: 0,
            ore: 0,
            stone: 0,
        },
        tradeAgreements: 0,
        skills: {
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
        abilities: {
            culture: 0,
            economy: 0,
            loyalty: 0,
            stability: 0,
        },
        ruin: {
            corruption: {
                penalty: 0,
                threshold: 0,
                value: 0,
            },
            crime: {
                penalty: 0,
                threshold: 0,
                value: 0,
            },
            decay: {
                penalty: 0,
                threshold: 0,
                value: 0,
            },
            strife: {
                penalty: 0,
                threshold: 0,
                value: 0,
            },
        },
    };
}
