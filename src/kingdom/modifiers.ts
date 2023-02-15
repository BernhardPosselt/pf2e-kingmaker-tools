import {Activity, KingdomPhase} from './data/activities';
import {groupBy} from '../utils';

export interface Modifier {
    type: 'ability' | 'proficiency' | 'item' | 'status' | 'circumstance' | 'vacancy' | 'untyped';
    value: number;
    name: string;
    phases?: KingdomPhase[];
    activities?: Activity[];
    enabled: boolean;
}

export function disableModifiers(modifiers: Modifier[], phase?: KingdomPhase, activity?: Activity): Modifier[] {
    // first check if any modifiers need to be disabled based on phase or activity
    const enabledModifiers = modifiers
        .map(modifier => {
            let enabled = modifier.enabled;
            // if we aren't running in a phase, disable all modifiers relevant to a phase
            // if we are, check if a modifier is only green lit for a certain phase
            if (enabled && phase && modifier.phases) {
                enabled = modifier.phases.includes(phase);
            } else if (enabled && !phase && modifier.phases) {
                enabled = false;
            }
            // if we aren't running an activity, disable all modifiers relevant only to activities
            // if we are, check if the modifier applies to the activity
            if (enabled && activity && modifier.activities) {
                enabled = modifier.activities.includes(activity);
            } else if (enabled && !activity && modifier.activities) {
                enabled = false;
            }
            return {
                ...modifier,
                enabled,
            };
        });
    // then disable modifiers that are lower than others in their group
    const groupedModifiers = groupBy(modifiers, (modifier) => {
        if (modifier.value > 0) {
            return modifier.type;
        } else {
            return modifier.type + '-penalty';
        }
    });
    for (const [type, modifiers] of groupedModifiers.entries()) {
        if (type !== 'untyped' && type !== 'untyped-penalty') {
            const highest = modifiers
                .reduce((prev, curr) => Math.abs(prev.value) < Math.abs(curr.value) ? curr : prev);
            for (const mod of modifiers) {
                if (mod !== highest) {
                    mod.enabled = false;
                }
            }
        }
    }
    return enabledModifiers;
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
    };
    const enabledModifiers = modifiers.filter(modifier => modifier.enabled);
    for (const modifier of enabledModifiers) {
        if (modifier.type === 'vacancy') {
            result.vacancyPenalty = modifier.value;
        } else {
            const part = result[modifier.type];
            const key = modifier.value > 0 ? 'bonus' : 'penalty';
            // adding here because untyped values stack and no other duplicates
            // for certain types should exist anymore
            part[key] += modifier.value;
        }
    }
    result.value = enabledModifiers
        .map(m => m.value)
        .reduce((a, b) => a + b, 0);
    return result;
}
