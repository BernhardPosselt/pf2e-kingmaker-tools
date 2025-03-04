import {KingdomPhase} from './data/activities';
import {capitalize, groupBy, isNonNullable, unslugify} from '../utils';
import {getLevelData, Kingdom, LeaderKingdomSkills, Leaders, LeaderSkills, Ruin, Settlement} from './data/kingdom';
import {getAllSelectedFeats} from './data/feats';
import {allActorSkills, CharacterSkill, Skill, skillAbilities} from './data/skills';
import {Ability, AbilityScores, calculateAbilityModifier} from './data/abilities';
import {isInvested, Leader} from './data/leaders';
import {calculateUnrestPenalty} from './data/unrest';
import {abilityRuins} from './data/ruin';
import {ActivityBonuses, SkillItemBonus} from './data/structures';
import {ActiveSettlementStructureResult} from './scene';
import {KingdomActivityById} from './data/activityData';
import {LeaderPerformingCheck} from "./skills";
import {getAllFeatures} from "./data/features";

export type Proficiency = 'trained' | 'expert' | 'master' | 'legendary';

export type UntrainedProficiencyMode = 'half' | 'full' | 'none';

export const allModifierTypes = [
    'ability',
    'proficiency',
    'item',
    'status',
    'circumstance',
    'vacancy',
    'untyped',
    'leadership'
] as const;

export type ModifierType = typeof allModifierTypes[number];

interface GtePredicate {
    gte: [string, string];
}

interface GtPredicate {
    gt: [string, string];
}

interface LtePredicate {
    lte: [string, string];
}

interface LtPredicate {
    lt: [string, string];
}

interface EqPredicate {
    eq: [string, string];
}

interface OrPredicate {
    some: Predicate[];
}

interface AndPredicate {
    all: Predicate[];
}

interface NotPredicate {
    not: Predicate;
}

interface InPredicate {
    in: [string, string[]];
}

interface HasFlagPredicate {
    hasFlag: string;
}

interface HasRollOptionPredicate {
    hasRollOption: string;
}

export type Predicate = GtePredicate
    | InPredicate
    | GtPredicate
    | LtPredicate
    | LtePredicate
    | EqPredicate
    | OrPredicate
    | HasFlagPredicate
    | AndPredicate
    | NotPredicate
    | HasRollOptionPredicate;

export type Expression = When;

interface Case {
    case: [Predicate, number | string | boolean | null];
}

interface When {
    when: {
        cases: Case[];
        default: number | string | boolean | null;
    }
}

export interface Modifier {
    type: ModifierType;
    value: number;
    valueExpression?: When;
    name: string;
    enabled: boolean;
    turns?: number;
    consumeId?: string;
    isConsumedAfterRoll?: boolean;
    rollOptions?: string[];
    applyIf?: Predicate[];
}

function createPredicateName(values: string[] | undefined, label: string): string | undefined {
    return values ? `${label}: ${values.map(v => unslugify(v)).join(', ')}` : undefined;
}

function extractPredicateTargets(label: string, modifier: Modifier, selector: '@phase' | '@skill' | '@ability' | '@activity'): string | undefined {
    const mods = modifier.applyIf
        ?.flatMap(p => {
            if ('in' in p && p.in[0] === selector) {
                return p.in[1];
            } else if ('eq' in p && p.eq[0] === selector) {
                return [p.eq[1]];
            } else {
                return [];
            }
        }) ?? [];
    if (mods.length === 0) return undefined
    else return label + ': ' + mods.map(a => unslugify(a)).join(', ')
}

export function modifierToLabel(modifier: Modifier): string {
    let value = ''
    if (!isNonNullable(modifier.valueExpression)) {
        value = modifier.value >= 0 ? `+${modifier.value} ` : `${modifier.value} `;
    }
    const type = modifier.value >= 0 ? capitalize(modifier.type) + ' Bonus' : capitalize(modifier.type) + ' Penalty';
    const predicates = [
        extractPredicateTargets("Phases", modifier, '@phase'),
        extractPredicateTargets("Skills", modifier, '@skill'),
        extractPredicateTargets("Abilities", modifier, '@ability'),
        extractPredicateTargets("Activities", modifier, '@activity'),
    ]
        .filter(v => v !== undefined)
        .join('; ');
    const to = predicates.length > 0 ? ' to: ' : '';
    return `${value}${type}${to}${predicates}`;
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
    activity: string | undefined,
    skill: Skill,
    rank: number,
    activities: KingdomActivityById,
): ModifierWithId[] {
    return modifiers
        .filter(modifier => {
            const predicates: ((modifier: Modifier) => boolean)[] = [];
            let keepModifier = true;
            for (const predicate of predicates) {
                keepModifier = keepModifier && predicate(modifier);
            }
            return keepModifier;
        }).map(modifier => {
            let enabled = modifier.enabled;
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
            const highestEnabledValue = Math.max(0, ...modifiers
                .filter(modifier => modifier.enabled)
                .map(modifier => Math.abs(modifier.value)));
            const hasEnabled = modifiers.some(modifier => modifier.enabled);
            // only take the highest modifier of a type if there is an enabled one
            // if no modifier is enabled, offer all as a choice#
            if (hasEnabled) {
                // only keep first one active, disable all other modifiers
                const highest = modifiers.find(m => m.enabled && Math.abs(m.value) === highestEnabledValue)!;
                modifiers.forEach(modifier => {
                    if (modifier.id !== highest.id) {
                        result.push({
                            ...modifier,
                            enabled: false,
                        });
                    } else {
                        result.push(modifier);
                    }
                });
            } else {
                modifiers.forEach(mod => result.push(mod));
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
    leadership: ModifierTotal;
    vacancyPenalty: number;
    value: number;
    assurance: number;
    rollOptions: string[];
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
        leadership: {
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
        rollOptions: [],
    };
    const enabledModifiers = modifiers.filter(modifier => modifier.enabled && modifier.value !== 0);
    for (const modifier of enabledModifiers) {
        modifier.rollOptions?.forEach(option => result.rollOptions.push(option));
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
            return m.type === 'proficiency' && (m.value > 0 || m.value < 0);
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
 */
export function createActiveSettlementModifiers(
    game: Game,
    kingdom: Kingdom,
    activeSettlement: Settlement | undefined,
    activeSettlementStructureResult: ActiveSettlementStructureResult | undefined,
    settlementsWithoutLandBorders: number,
): Modifier[] {
    const levelData = getLevelData(kingdom.level);
    const result = getAllSelectedFeats(game, kingdom).flatMap(f => f.modifiers ?? [])
    kingdom.modifiers.forEach(modifier => result.push(modifier));
    const isSecondaryTerritory = activeSettlement?.secondaryTerritory;
    getAllFeatures(game, kingdom).filter(f => f.level <= kingdom.level)
        .flatMap(f => f.modifiers ?? [])
        .forEach(f => result.push(f));
    if (settlementsWithoutLandBorders > 0) {
        result.push({
            name: 'Settlements Without Land Borders',
            value: settlementsWithoutLandBorders * -1,
            type: 'item',
            applyIf: [{"eq": ["@skill", "trade"]}],
            enabled: true,
        });
    }
    if (isSecondaryTerritory) {
        result.push({
            name: 'Check in Secondary Territory',
            type: 'circumstance',
            value: -4,
            enabled: true,
        });
    }
    const settlementEventBonus = activeSettlementStructureResult?.active.settlementEventBonus ?? 0;
    if (settlementEventBonus > 0) {
        result.push({
            name: 'Event Phase Structure Bonus',
            type: 'circumstance',
            value: settlementEventBonus,
            enabled: true,
            applyIf: [{"eq": ["@phase", "event"]}],
        });
    }
    const leadershipBonus = activeSettlementStructureResult?.leadershipActivityBonus ?? 0;
    if (leadershipBonus > 0) {
        result.push({
            name: 'Ruler performs Leadership Activity',
            type: 'item',
            value: leadershipBonus,
            enabled: false,
            applyIf: [{"eq": ["@phase", "leadership"]}],
        });
    }
    result.push({
        name: 'Invested, Non-Vacant Leader Handles Event',
        type: 'circumstance',
        value: levelData.investedLeadershipBonus,
        enabled: false,
        applyIf: [{"eq": ["@phase", "event"]}],
    });
    return result;
}

function resolveValue(
    kingdom: Kingdom,
    value: string | number | boolean | null,
    skill: Skill,
    phase: KingdomPhase | null,
    activity: string | null
): string | number | boolean | null {
    if (value === "@unrest") {
        return kingdom.unrest;
    } else if (value === "@magicRank") {
        return kingdom.skillRanks.magic;
    } else if (value === "@kingdomLevel") {
        return kingdom.level;
    } else if (value === "@skillRank") {
        return kingdom.skillRanks[skill];
    } else if (value === "@skill") {
        return skill;
    } else if (value === "@phase") {
        return phase;
    } else if (value === "@activity") {
        return activity;
    } else if (value === "@ability") {
        return skillAbilities[skill];
    } else {
        return value;
    }
}

function processExpression(
    kingdom: Kingdom,
    p: Expression,
    flags: string[],
    rollOptions: string[],
    skill: Skill,
    phase: KingdomPhase | null,
    activity: string | null,
): boolean | string | number | null {
    if ('when' in p) {
        const when = p.when;
        const value = when.cases.find(c => processPredicate(kingdom, c.case[0], flags, rollOptions, skill, phase, activity));
        if (isNonNullable(value)) {
            return resolveValue(kingdom, value.case[1], skill, phase, activity)
        } else {
            return when.default;
        }
    } else {
        return null;
    }
}

function processPredicate(
    kingdom: Kingdom,
    p: Predicate,
    flags: string[],
    rollOptions: string[],
    skill: Skill,
    phase: KingdomPhase | null,
    activity: string | null,
): boolean {
    if ('gte' in p) {
        return (resolveValue(kingdom, p.gte[0], skill, phase, activity) ?? 0) >= (resolveValue(kingdom, p.gte[1], skill, phase, activity) ?? 0);
    } else if ('gt' in p) {
        return (resolveValue(kingdom, p.gt[0], skill, phase, activity) ?? 0) > (resolveValue(kingdom, p.gt[1], skill, phase, activity) ?? 0);
    } else if ('lte' in p) {
        return (resolveValue(kingdom, p.lte[0], skill, phase, activity) ?? 0) <= (resolveValue(kingdom, p.lte[1], skill, phase, activity) ?? 0);
    } else if ('lt' in p) {
        return (resolveValue(kingdom, p.lt[0], skill, phase, activity) ?? 0) < (resolveValue(kingdom, p.lt[1], skill, phase, activity) ?? 0);
    } else if ('eq' in p) {
        return resolveValue(kingdom, p.eq[0], skill, phase, activity) == resolveValue(kingdom, p.eq[1], skill, phase, activity);
    } else if ('some' in p) {
        return p.some.some(predicate => processPredicate(kingdom, predicate, flags, rollOptions, skill, phase, activity));
    } else if ('all' in p) {
        return p.all.every(predicate => processPredicate(kingdom, predicate, flags, rollOptions, skill, phase, activity));
    } else if ('hasRollOption' in p) {
        return rollOptions.includes(p.hasRollOption);
    } else if ('hasFlag' in p) {
        return flags.includes(p.hasFlag);
    } else if ('in' in p) {
        const value = resolveValue(kingdom, p.in[0], skill, phase, activity);
        const values = p.in[1].map(v => resolveValue(kingdom, v, skill, phase, activity))
        return values.includes(value);
    } else if ('not' in p) {
        return !processPredicate(kingdom, p.not, flags, rollOptions, skill, phase, activity);
    } else {
        return true;
    }
}

export function evaluateModifierValue(
    modifier: Modifier,
    kingdom: Kingdom,
    flags: string[],
    rollOptions: string[],
    skill: Skill,
    ability: Ability,
    phase: KingdomPhase | null,
    activity: string | null,
): Modifier {
    const valueExpression = modifier.valueExpression;
    if (valueExpression !== undefined) {
        const result = processExpression(kingdom, valueExpression, flags, rollOptions, skill, phase, activity);
        if (typeof result === "number") {
            return {
                ...modifier,
                value: result,
            }
        }
    }
    return modifier;
}

export function filterPredicates<T>(
    kingdom: Kingdom,
    elements: T[],
    flags: string[],
    rollOptions: string[],
    skill: Skill,
    predicatesGetter: (modifier: T) => Predicate[] | undefined,
    phase: KingdomPhase | null,
    activity: string | null,
) {
    return elements.filter(m => {
        const predicate = predicatesGetter(m);
        if (predicate) {
            return predicate.every(p => processPredicate(kingdom, p, flags, rollOptions, skill, phase, activity));
        } else {
            return true;
        }
    })
}

export function processModifiers(
    {
        modifiers,
        skill,
        rank,
        phase,
        activity,
        overrides = {},
        activities,
        kingdom,
        flags,
    }: {
        modifiers: Modifier[];
        skill: Skill;
        rank: number;
        phase?: KingdomPhase;
        activity?: string;
        overrides?: Record<string, boolean>;
        activities: KingdomActivityById;
        kingdom: Kingdom;
        flags: string[];
    },
): ModifierWithId[] {
    const ability = skillAbilities[skill];
    const rollOptions = filterPredicates(kingdom, modifiers, flags, [], skill, m => m.applyIf, phase ?? null, activity ?? null)
        .filter(m => m.enabled)
        .flatMap(m => m.rollOptions ?? []);
    const copied = filterPredicates(kingdom, modifiers, flags, rollOptions, skill, m => m.applyIf, phase ?? null, activity ?? null)
        .map((modifier, index) => {
            // make a copy and assign every modifier an id
            const cloned = foundry.utils.deepClone(modifier);
            return {
                ...(evaluateModifierValue(cloned, kingdom, flags, rollOptions, skill, ability, phase ?? null, activity ?? null)),
                id: `${index}`,
            };
        });
    const withoutZeroes = removeUninterestingZeroModifiers(copied);
    const withoutMismatchedPhaseOrActivity = removePredicatedModifiers(withoutZeroes, phase, activity, skill, rank, activities);
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
        applyIf: phase ? [{"eq": ["@phase", phase]}] : [],
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
    useLeadershipModifiers: boolean,
): Modifier | undefined {
    if (isInvested(ability, leaders) && !useLeadershipModifiers) {
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

export function rankToProficiency(rank: number): Proficiency | undefined {
    if (rank === 1) {
        return 'trained';
    } else if (rank === 2) {
        return 'expert';
    } else if (rank === 3) {
        return 'master';
    } else if (rank === 4) {
        return 'legendary';
    }
}

export function proficiencyToRank(proficiency: Proficiency | undefined): number {
    if (proficiency === 'trained') {
        return 1;
    } else if (proficiency === 'expert') {
        return 2;
    } else if (proficiency === 'master') {
        return 3;
    } else if (proficiency === 'legendary') {
        return 4;
    } else {
        return 0;
    }
}

function calculateUntrainedProficiency(rank: number, kingdomLevel: number, untrainedProficiencyMode: UntrainedProficiencyMode): number {
    if (rank > 0) {
        return kingdomLevel + rank * 2;
    } else if (untrainedProficiencyMode === 'full') {
        return kingdomLevel;
    } else if (untrainedProficiencyMode === 'half') {
        return Math.floor(kingdomLevel / 2);
    } else {
        return 0;
    }
}

export function createProficiencyModifier(rank: number, untrainedProficiencyMode: UntrainedProficiencyMode, kingdomLevel: number): Modifier {
    const value = calculateUntrainedProficiency(rank, kingdomLevel, untrainedProficiencyMode);
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

function createActivityModifiers(activities: ActivityBonuses, kingdomActivities: KingdomActivityById): Modifier[] {
    return (Object.entries(activities) as ([string, number])[])
        .map(([activity, value]) => {
            const phases = [kingdomActivities[activity].phase];
            return {
                type: 'item',
                enabled: true,
                value,
                name: `Structure - ${unslugify(activity)}`,
                activities: [activity],
                phases,
            };
        });
}

export function createStructureModifiers(skillItemBonus: SkillItemBonus, kingdomActivities: KingdomActivityById): Modifier[] {
    const result = createActivityModifiers(skillItemBonus.activities, kingdomActivities);
    const skillBonus = createSkillModifier(skillItemBonus.value);
    if (skillBonus) {
        result.push(skillBonus);
    }
    return result;
}


function normalizeLore(key: string) {
    return key.endsWith('-lore') ? key : key + '-lore';
}

function findHighestSkillProficiencyRanks(
    relevantSkills: string[],
    skillRanks: Record<string, number>,
): number {
    const skills = relevantSkills.filter(s => allActorSkills.includes(s as CharacterSkill)) as CharacterSkill[];
    const lores = relevantSkills
        .filter(s => !allActorSkills.includes(s as CharacterSkill))
        .map(s => normalizeLore(s));
    const highestRelevantLoreRank = Math.max(0, ...Object.entries(skillRanks)
        .filter(([key]) => !allActorSkills.includes(key as CharacterSkill) && lores.includes(normalizeLore(key)))
        .map(([, value]) => value));
    const relevantSkillRanks = Object.entries(skillRanks)
        .filter(([key]) => allActorSkills.includes(key as CharacterSkill) && skills.includes(key as CharacterSkill))
        .map(([, value]) => value);
    const allRelevantRanks = [highestRelevantLoreRank, ...relevantSkillRanks];
    return Math.max(
        0,
        allRelevantRanks.filter(a => a >= 1).length >= 2 ? 1 : 0,
        allRelevantRanks.filter(a => a >= 2).length >= 2 ? 2 : 0,
        allRelevantRanks.filter(a => a >= 3).length >= 2 ? 3 : 0,
        allRelevantRanks.filter(a => a >= 4).length >= 2 ? 4 : 0,
    );
}


export function calculateLeadershipModifier(
    leader: LeaderPerformingCheck,
    skill: Skill,
    leaderKingdomSkills: LeaderKingdomSkills,
    leaderSkills: LeaderSkills,
): { value: number, usesSpecializedSkill: boolean } {
    const type = leader.type;
    const level = leader.level;
    const relevantKingdomSkills = leaderKingdomSkills[leader.position];
    const usesSpecializedSkill = relevantKingdomSkills.includes(skill);
    if (type === 'highlyMotivatedNpc') {
        if (level >= 1 && level < 4) {
            return {value: 1, usesSpecializedSkill};
        } else if (level >= 4 && level < 8) {
            return {value: 2, usesSpecializedSkill};
        } else if (level >= 8 && level < 16) {
            return {value: 3, usesSpecializedSkill};
        } else {
            return {value: 4, usesSpecializedSkill};
        }
    } else if (type === 'regularNpc') {
        if (level >= 1 && level < 6) {
            return {value: 1, usesSpecializedSkill};
        } else if (level >= 6 && level < 10) {
            return {value: 2, usesSpecializedSkill};
        } else {
            return {value: 3, usesSpecializedSkill};
        }
    } else if (type === 'nonPathfinderNpc') {
        if (level >= 1 && level < 5) {
            return {value: 1, usesSpecializedSkill};
        } else if (level >= 5 && level < 9) {
            return {value: 2, usesSpecializedSkill};
        } else if (level >= 9 && level < 17) {
            return {value: 3, usesSpecializedSkill};
        } else {
            return {value: 4, usesSpecializedSkill};
        }
    } else {
        const relevantSkills = leaderSkills[leader.position];
        const value = findHighestSkillProficiencyRanks(relevantSkills, leader.skillRanks);
        return {value, usesSpecializedSkill};
    }
}

export function createLeadershipModifier(
    leader: LeaderPerformingCheck | undefined,
    skill: Skill,
    leaderKingdomSkills: LeaderKingdomSkills,
    leaderSkills: LeaderSkills,
    useLeadershipModifiers: boolean,
): Modifier | undefined {
    if (useLeadershipModifiers && leader) {
        const {value, usesSpecializedSkill} = calculateLeadershipModifier(
            leader,
            skill,
            leaderKingdomSkills,
            leaderSkills,
        )
        const label = usesSpecializedSkill ? 'specialized' : 'unspecialized';
        return {
            type: 'leadership',
            value: usesSpecializedSkill ? value : Math.floor(value / 2),
            name: `Leadership: ${capitalize(leader.position)} (${label})`,
            enabled: true,
        }
    }
}

export async function parseLeaderPerformingCheck(
    leader: Leader,
    kingdom: Kingdom,
): Promise<LeaderPerformingCheck | undefined> {
    const leaderValues = kingdom.leaders[leader];
    const uuid = leaderValues.uuid;
    if (uuid) {
        const actor = await fromUuid(uuid) as Actor | null;
        if (actor) {
            return {
                position: leader,
                type: leaderValues.type,
                level: actor.level,
                skillRanks: Object.fromEntries(Object.entries(actor.skills).map(([key, value]) => {
                    return [key, value.rank];
                })),
            }
        }
    }
}