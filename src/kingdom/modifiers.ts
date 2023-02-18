import {Activity, getActivityPhase, KingdomPhase} from './data/activities';
import {capitalize, groupBy, unslugify} from '../utils';
import {getLevelData, Kingdom, Leaders, Ruin} from './data/kingdom';
import {SettlementSceneData} from './scene';
import {allFeatsByName} from './data/feats';
import {Skill, skillAbilities} from './data/skills';
import {activityData} from './data/activityData';
import {Ability, AbilityScores, calculateAbilityModifier} from './data/abilities';
import {applyLeaderCompanionRules} from './data/companions';
import {isInvested} from './data/leaders';
import {calculateUnrestPenalty} from './data/unrest';
import {abilityRuins} from './data/ruin';
import {ActivityBonuses, SkillItemBonus} from './data/structures';

export const allModifierTypes = [
    'ability',
    'proficiency',
    'item',
    'status',
    'circumstance',
    'vacancy',
    'untyped',
] as const;

export type ModifierType = typeof allModifierTypes[number];

export interface Modifier {
    type: ModifierType;
    value: number;
    name: string;
    phases?: KingdomPhase[];
    activities?: Activity[];
    skills?: Skill[];
    abilities?: Ability[];
    enabled: boolean;
    turns?: number;
    consumeId?: string;
}

function createPredicateName(values: string[] | undefined, label: string): string | undefined {
    return values ? `${label}: ${values.map(v => unslugify(v)).join(', ')}` : undefined;
}

export function modifierToLabel(modifier: Modifier): string {
    const value = modifier.value >= 0 ? `+${modifier.value}` : `${modifier.value}`;
    const type = modifier.value >= 0 ? capitalize(modifier.type) + ' Bonus' : capitalize(modifier.type) + ' Penalty';
    const to = modifier.skills || modifier.abilities || modifier.activities || modifier.phases ? ' to: ' : '';
    const predicates = [
        createPredicateName(modifier.phases, 'Phases'),
        createPredicateName(modifier.activities, 'Activities'),
        createPredicateName(modifier.abilities, 'Abilities'),
        createPredicateName(modifier.skills, 'Skills'),
    ]
        .filter(v => v !== undefined)
        .join('; ');
    return `${value} ${type}${to}${predicates}`;
}

export interface ModifierWithId extends Modifier {
    id: string;
}

export function removeUninterestingZeroModifiers(modifiers: ModifierWithId[]): ModifierWithId[] {
    return modifiers.filter(modifier => modifier.value !== 0);
}

/**
 * Deletes all modifiers that are not in a given phase or activity
 */
export function removePredicatedModifiers(
    modifiers: ModifierWithId[],
    phase: KingdomPhase | undefined,
    activity: Activity | undefined,
    skill: Skill,
    rank: number,
): ModifierWithId[] {
    return modifiers
        .filter(modifier => {
            const predicates: ((modifier: Modifier) => boolean)[] = [];
            if (modifier.abilities) {
                predicates.push((m) => m.abilities?.includes(skillAbilities[skill]) === true);
            }
            if (modifier.skills) {
                predicates.push((m) => m.skills?.includes(skill) === true);
            }

            // if we aren't running in a phase, remove all modifiers relevant to a phase
            if (phase && modifier.phases) {
                predicates.push((m) => m.phases?.includes(phase) === true);
            }
            // if we aren't running in an activity, remove all modifiers not matching the activity
            if (activity && modifier.activities) {
                predicates.push((m) => m.activities?.includes(activity) === true);
            }

            // regardless of if we are running in an activity, we need to check
            // if it is actually possible to skill check for the given skill and rank
            if (modifier.activities) {
                predicates.push((m) => m.activities!
                    .some(activity => {
                        const data = activityData[activity];
                        const activitySkillRank = data.skills[skill];
                        if (activitySkillRank === undefined) {
                            return false;
                        } else {
                            return activitySkillRank <= rank;
                        }
                    })
                );
            }
            let keepModifier = true;
            for (const predicate of predicates) {
                keepModifier = keepModifier && predicate(modifier);
            }
            return keepModifier;
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

/**
 * Disables all modifiers that do not have the highest/lowest number
 * @param modifiers
 */
export function removeLowestModifiers(modifiers: ModifierWithId[]): ModifierWithId[] {
    const groupedModifiers = groupBy(modifiers, (modifier) => {
        if (modifier.value > 0) {
            return modifier.type;
        } else {
            return modifier.type + '-penalty';
        }
    });
    const result: ModifierWithId[] = [];
    for (const [type, modifiers] of groupedModifiers.entries()) {
        if (type === 'untyped' || type === 'untyped-penalty') {
            modifiers.forEach(m => result.push(m));
        } else {
            // only take the highest modifier of a type if there is an enabled one
            // if no modifier is enabled, offer all as a choice
            const highest = modifiers
                .reduce((prev, curr) => Math.abs(prev.value) < Math.abs(curr.value) ? curr : prev);
            const hasEnabled = modifiers.some(modifier => modifier.enabled);
            for (const mod of modifiers) {
                // dedup modifiers that are enabled and have the same value
                const isHighestUniquePerType =
                    hasEnabled &&
                    !result.find(existingMod => existingMod.type === mod.type && (mod.value < 0 === existingMod.value < 0)) &&
                    mod.value === highest.value;

                if (isHighestUniquePerType || !hasEnabled) {
                    result.push(mod);
                }
            }
        }
    }
    return result;
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
    result.assurance = 10 + enabledModifiers
        .filter(m => {
            // proficiency includes only negative modifiers or positive proficiency ones
            return (m.type === 'proficiency' && m.value > 0) || m.value < 0;
        })
        .map(m => m.value)
        .reduce((a, b) => a + b, 0);
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
    kingdom.modifiers.forEach(modifier => result.push(modifier));
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
    const leadershipBonus = activeSettlement?.settlement.leadershipActivityBonus ?? 0;
    if (leadershipBonus > 0) {
        result.push({
            name: 'PC in Ruler Leadership Role',
            type: 'item',
            value: leadershipBonus,
            enabled: false,
            phases: ['leadership'],
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
    {
        modifiers,
        skill,
        rank,
        phase,
        activity,
        overrides = {},
    }: {
        modifiers: Modifier[];
        skill: Skill;
        rank: number;
        phase?: KingdomPhase;
        activity?: Activity;
        overrides?: Record<string, boolean>;
    }
): ModifierWithId[] {
    const copied = modifiers.map((modifier, index) => {
        // make a copy and assign every modifier an id
        return {
            ...modifier,
            phases: modifier.phases ? [...modifier.phases] : undefined,
            activities: modifier.activities ? [...modifier.activities] : undefined,
            id: `${index}`,
        };
    });
    const withoutZeroes = removeUninterestingZeroModifiers(copied);
    const withoutMismatchedPhaseOrActivity = removePredicatedModifiers(withoutZeroes, phase, activity, skill, rank);
    // enable/disable overrides
    const withOverrides = withoutMismatchedPhaseOrActivity
        .map(modifier => {
            const override = overrides[modifier.id];
            if (override === undefined) {
                return modifier;
            } else {
                return {
                    ...modifier,
                    enabled: override,
                };
            }
        });
    return removeLowestModifiers(withOverrides);
}


function createVacancyModifier(value: number, name: string, rulerVacant: boolean, phase?: KingdomPhase): Modifier {
    return {
        name,
        value: -(rulerVacant ? value + 1 : value),
        phases: phase ? [phase] : undefined,
        type: 'vacancy',
        enabled: true,
    };
}

export function createVacancyModifiers(
    ability: Ability,
    leaders: Leaders,
): Modifier[] {
    const modifiers = [];
    const rulerVacant = leaders.ruler.vacant;
    if (leaders.counselor.vacant && ability === 'culture') {
        modifiers.push(createVacancyModifier(1, 'Vacancy: Counselor', rulerVacant));
    }
    if (leaders.general.vacant) {
        modifiers.push(createVacancyModifier(4, 'Vacancy: General', rulerVacant, 'army'));
    }
    if (leaders.emissary.vacant && ability === 'loyalty') {
        modifiers.push(createVacancyModifier(1, 'Vacancy: Emissary', rulerVacant));
    }
    if (leaders.magister.vacant) {
        modifiers.push(createVacancyModifier(4, 'Vacancy: Magister', rulerVacant, 'army'));
    }
    if (leaders.treasurer.vacant && ability === 'economy') {
        modifiers.push(createVacancyModifier(1, 'Vacancy: Treasurer', rulerVacant));
    }
    if (leaders.viceroy.vacant && ability === 'stability') {
        modifiers.push(createVacancyModifier(1, 'Vacancy: Viceroy', rulerVacant));
    }
    if (leaders.warden.vacant) {
        modifiers.push(createVacancyModifier(4, 'Vacancy: Warden', rulerVacant, 'region'));
    }
    if (rulerVacant) {
        modifiers.push(createVacancyModifier(0, 'Vacancy: Ruler', rulerVacant));
    }
    return modifiers;
}

export function createInvestedModifier(
    kingdomLevel: number,
    ability: Ability,
    leaders: Leaders,
): Modifier | undefined {
    const appliedLeaders = applyLeaderCompanionRules(leaders);
    if (isInvested(ability, appliedLeaders)) {
        return {
            value: getLevelData(kingdomLevel).investedLeadershipBonus,
            enabled: true,
            name: 'Invested Leadership Role',
            type: 'status',
        };
    }
}

export function createAbilityModifier(ability: Ability, abilityScores: AbilityScores): Modifier {
    return {
        type: 'ability',
        name: capitalize(ability),
        enabled: true,
        value: calculateAbilityModifier(abilityScores[ability]),
    };
}

export function rankToLabel(rank: number): string {
    if (rank === 0) {
        return 'Untrained';
    } else if (rank === 1) {
        return 'Trained';
    } else if (rank === 2) {
        return 'Expert';
    } else if (rank === 3) {
        return 'Master';
    } else {
        return 'Legendary';
    }
}

export function createProficiencyModifier(rank: number, alwaysAddLevel: boolean, kingdomLevel: number): Modifier {
    const value = rank > 0 ? (kingdomLevel + rank * 2) : (alwaysAddLevel ? kingdomLevel : 0);
    const name = rank > 0 ? rankToLabel(rank) : 'Kingdom Level';
    return {
        value,
        enabled: true,
        name,
        type: 'proficiency',
    };
}

export function createUnrestModifier(unrest: number): Modifier | undefined {
    const penalty = calculateUnrestPenalty(unrest);
    if (penalty > 0) {
        return {
            type: 'status',
            name: 'Unrest',
            enabled: true,
            value: -penalty,
        };
    }
}

export function createRuinModifier(ability: Ability, ruin: Ruin): Modifier | undefined {
    const ruinAbility = abilityRuins[ability];
    const itemPenalty = ruin[ruinAbility].penalty;
    if (itemPenalty > 0) {
        return {
            value: -itemPenalty,
            enabled: true,
            name: 'Ruin',
            type: 'item',
        };
    }
}

function createSkillModifier(value: number): Modifier | undefined {
    if (value > 0) {
        return {
            value,
            enabled: true,
            name: 'Structure',
            type: 'item',
        };
    }
}

export function createActivityModifiers(activities: ActivityBonuses): Modifier[] {
    return (Object.entries(activities) as ([Activity, number])[])
        .map(([activity, value]) => {
            const phases = [getActivityPhase(activity)];
            return {
                type: 'item',
                enabled: true,
                value,
                name: unslugify(activity),
                activities: [activity],
                phases,
            };
        });
}

export function createStructureModifiers(skillItemBonus: SkillItemBonus): Modifier[] {
    const result = createActivityModifiers(skillItemBonus.activities);
    const skillBonus = createSkillModifier(skillItemBonus.value);
    if (skillBonus) {
        result.push(skillBonus);
    }
    return result;
}
