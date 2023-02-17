import {Activity, KingdomPhase} from './data/activities';
import {groupBy} from '../utils';
import {getLevelData, Kingdom} from './data/kingdom';
import {SettlementSceneData} from '../structures/scene';
import {allFeatsByName} from './data/feats';
import {Skill} from './data/skills';
import {activityData} from './data/activityData';

export type ModifierType = 'ability' | 'proficiency' | 'item' | 'status' | 'circumstance' | 'vacancy' | 'untyped';

export interface Modifier {
    type: ModifierType;
    value: number;
    name: string;
    phases?: KingdomPhase[];
    activities?: Activity[];
    enabled: boolean;
}

/**
 * Disables all modifiers that do not have the highest/lowest number
 * @param modifiers
 */
export function removeLowestModifiers(modifiers: Modifier[]): Modifier[] {
    const groupedModifiers = groupBy(modifiers, (modifier) => {
        if (modifier.value > 0) {
            return modifier.type;
        } else {
            return modifier.type + '-penalty';
        }
    });
    const result = [];
    for (const [type, modifiers] of groupedModifiers.entries()) {
        if (type !== 'untyped' && type !== 'untyped-penalty') {
            // only take the highest modifier of a type
            // if multiple modifiers are the same, choose the one that's enabled
            // otherwise push all
            const highest = modifiers
                .reduce((prev, curr) => Math.abs(prev.value) < Math.abs(curr.value) ? curr : prev);
            const hasEnabled = modifiers.some(modifier => modifier.enabled);
            for (const mod of modifiers) {
                if (mod.value === highest.value && mod.enabled === hasEnabled) {
                    result.push(mod);
                }
            }
        }
    }
    return result;
}

export function removeUninterestingZeroModifiers(modifiers: Modifier[]): Modifier[] {
    return modifiers.filter(modifier => {
        // keep ability and proficiency
        return modifier.type === 'ability' || modifier.type === 'proficiency' || modifier.value !== 0;
    });
}

/**
 * Deletes all modifiers that are not in a given phase or activity
 */
export function removePhaseActivityModifiers(
    modifiers: Modifier[],
    phase: KingdomPhase | undefined,
    activity: Activity | undefined,
    skill: Skill,
    rank: number,
): Modifier[] {
    return modifiers
        .filter(modifier => {
            // if we aren't running in a phase, remove all modifiers relevant to a phase
            // if we are, check if a modifier is only green lit for a certain phase
            // repeat for activities
            if (phase && modifier.phases && activity && modifier.activities) {
                return modifier.phases.includes(phase) &&
                    modifier.activities.includes(activity);
            } else if (phase && modifier.phases) {
                return modifier.phases.includes(phase);
            } else if (activity && modifier.activities) {
                return modifier.activities.includes(activity);
            } else {
                // lastly, if a modifier has an activity, check if that activity
                // is actually possible to skill check for the given skill and rank
                if (modifier.activities) {
                    return modifier.activities
                        .some(activity => {
                            if (activity === 'create-a-masterpiece') {
                                console.log('bb', activityData[activity], skill, rank);
                            }
                            const data = activityData[activity];
                            const activitySkillRank = data.skills[skill];
                            if (activitySkillRank) {
                                return activitySkillRank <= rank;
                            } else {
                                return false;
                            }
                        });
                }
                return true;
            }
        }).map(modifier => {
            let enabled = modifier.enabled;
            // modifiers that are running outside a phase or activity should be
            // disabled by default if they belong to one
            if (!phase && modifier.phases) {
                enabled = false;
            }
            if (!activity && modifier.activities) {
                enabled = false;
            }
            return {
                ...modifier,
                enabled,
            };
        });
}

export interface ModifierTotal {
    bonus: number;
    penalty: number;
}

export interface ModifierTotals {
    item: ModifierTotal;
    circumstance: ModifierTotal;
    status: ModifierTotal;
    ability: ModifierTotal;
    proficiency: ModifierTotal;
    untyped: ModifierTotal;
    vacancyPenalty: number;
    value: number;
    assurance: number;
}

export function calculateModifiers(modifiers: Modifier[]): ModifierTotals {
    const result: ModifierTotals = {
        item: {
            bonus: 0,
            penalty: 0,
        },
        circumstance: {
            bonus: 0,
            penalty: 0,
        },
        status: {
            bonus: 0,
            penalty: 0,
        },
        ability: {
            bonus: 0,
            penalty: 0,
        },
        proficiency: {
            bonus: 0,
            penalty: 0,
        },
        untyped: {
            bonus: 0,
            penalty: 0,
        },
        vacancyPenalty: 0,
        value: 0,
        assurance: 10,
    };
    const enabledModifiers = modifiers.filter(modifier => modifier.enabled && modifier.value !== 0);
    for (const modifier of enabledModifiers) {
        if (modifier.type === 'vacancy') {
            result.vacancyPenalty = modifier.value;
        } else {
            const part = result[modifier.type];
            const key = modifier.value > 0 ? 'bonus' : 'penalty';
            if (modifier.type === 'untyped') {
                part[key] += modifier.value;
            } else {
                part[key] = modifier.value;
            }
        }
    }
    result.assurance = 10 + (enabledModifiers.find(m => m.type === 'proficiency' && m.value > 0)?.value ?? 0);
    result.value = enabledModifiers
        .map(m => m.value)
        .reduce((a, b) => a + b, 0);
    return result;
}

/**
 * Add modifiers from feats or other rules
 * @param kingdom
 * @param activeSettlement
 */
export function createAdditionalModifiers(kingdom: Kingdom, activeSettlement: SettlementSceneData | undefined): Modifier[] {
    const levelData = getLevelData(kingdom.level);
    const feats = new Set([...kingdom.feats.map(f => f.id), ...kingdom.feats.map(f => f.id)]);
    const result: Modifier[] = Array.from(feats)
        .flatMap(feat => allFeatsByName[feat]?.modifiers ?? []);
    if (activeSettlement?.scenedData?.secondaryTerritory) {
        result.push({
            name: 'Check in Secondary Territory',
            type: 'circumstance',
            value: -4,
            enabled: true,
        });
    }
    if (kingdom.level >= 4) {
        result.push({
            name: 'Expansion Expert',
            type: 'circumstance',
            activities: ['claim-hex'],
            value: 2,
            enabled: true,
        });
    }
    const settlementEventBonus = activeSettlement?.settlement?.settlementEventBonus ?? 0;
    if (settlementEventBonus > 0) {
        result.push({
            name: 'Event Phase Structure Bonus',
            type: 'circumstance',
            value: settlementEventBonus,
            enabled: true,
            phases: ['event'],
        });
    }
    result.push({
        name: 'Invested, non Vacant Leader involved in Event',
        type: 'circumstance',
        value: levelData.investedLeadershipBonus,
        enabled: true,
        phases: ['event'],
    });
    return result;
}

export function processModifiers(
    modifiers: Modifier[],
    skill: Skill,
    rank: number,
    phase?: KingdomPhase,
    activity?: Activity,
): Modifier[] {
    const copied = modifiers.map(modifier => {
        // make a copy
        return {
            ...modifier,
            phases: modifier.phases ? [...modifier.phases] : undefined,
            activities: modifier.activities ? [...modifier.activities] : undefined,
        };
    });
    const withoutZeroes = removeUninterestingZeroModifiers(copied);
    const withoutMismatchedPhaseOrActivity = removePhaseActivityModifiers(withoutZeroes, phase, activity, skill, rank);
    return removeLowestModifiers(withoutMismatchedPhaseOrActivity);
}
