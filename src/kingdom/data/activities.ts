import {Skill} from './skills';
import {mergeObjects, unslugify} from '../../utils';
import {SkillRanks} from './kingdom';
import {ActivityContent, activityData} from './activityData';

export const allKingdomPhases = [
    'army',
    'civic',
    'commerce',
    'event',
    'leadership',
    'region',
    'upkeep',
] as const;

export type KingdomPhase = typeof allKingdomPhases[number];

export const allActivities = [
    'abandon-hex',
    'build-roads',
    'build-structure',
    'capital-investment',
    'celebrate-holiday',
    'claim-hex',
    'clandestine-business',
    'clear-hex',
    'collect-taxes',
    'craft-luxuries',
    'create-a-masterpiece',
    'creative-solution',
    'decadent-feasts',
    'deliberate-planning',
    'demolish',
    'deploy-army',
    'disband-army',
    'establish-farmland',
    'establish-settlement',
    'establish-trade-agreement',
    'establish-work-site-lumber',
    'establish-work-site-mine',
    'establish-work-site-quarry',
    'evangelize-the-dead',
    'false-victory',
    'focused-attention',
    'fortify-hex',
    'garrison-army',
    'gather-livestock',
    'go-fishing',
    'harvest-azure-lily-pollen',
    'harvest-crops',
    'hire-adventurers',
    'improve-lifestyle',
    'infiltration',
    'irrigation',
    'manage-trade-agreements',
    'new-leadership',
    'offensive-gambit',
    'outfit-army',
    'pledge-of-fealty',
    'preventative-measures',
    'process-hidden-fees',
    'prognostication',
    'provide-care',
    'purchase-commodities',
    'quell-unrest',
    'read-all-about-it',
    'recover-army',
    'recruit-army',
    'recruit-monsters',
    'relocate-capital',
    'repair-reputation-corruption',
    'repair-reputation-crime',
    'repair-reputation-decay',
    'repair-reputation-strife',
    'repair-the-flooded-mine',
    'request-foreign-aid',
    'rest-and-relax',
    'restore-the-temple-of-the-elk',
    'send-diplomatic-envoy',
    'show-of-force',
    'spread-the-legend',
    'supernatural-solution',
    'supplementary-hunting',
    'tap-treasury',
    'trade-commodities',
    'train-army',
    'warfare-exercises',
] as const;

export type Activity = typeof allActivities[number];


export function getActivitySkills(activity: Activity, skillRanks?: SkillRanks): Skill[] {
    const skills = activityData[activity].skills;
    if (skillRanks) {
        return (Object.entries(skills) as [Skill, number][])
            .filter(([skill, rank]) => rank <= skillRanks[skill])
            .map(([skill]) => skill);
    } else {
        return Object.keys(skills) as Skill[];
    }
}

export const oncePerRoundActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => data.oncePerRound)
        .map(([activity]) => activity),
);
export const trainedActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => Object.values(data.skills).every(rank => rank === 1))
        .map(([activity]) => activity),
);

export const expertActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => Object.values(data.skills).every(rank => rank === 2))
        .map(([activity]) => activity),
);

export const masterActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => Object.values(data.skills).every(rank => rank === 3))
        .map(([activity]) => activity),
);

export function getActivityPhase(activity: Activity): KingdomPhase {
    return activityData[activity].phase;
}

export const companionActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => data.companion)
        .map(([activity]) => activity),
);

const activitiesByPhase: Record<KingdomPhase, Activity[]> = (Object.entries(activityData) as [Activity, ActivityContent][])
    .map(([activity, data]) => {
        const result: Record<KingdomPhase, Activity[]> = {
            'leadership': [],
            'region': [],
            'event': [],
            'army': [],
            'commerce': [],
            'upkeep': [],
            'civic': [],
        };
        result[data.phase].push(activity);
        return result;
    })
    .reduce((prev, curr) => {
        return mergeObjects(prev, curr, (a, b) => [...a, ...b]);
    }, {'leadership': [], 'region': [], 'event': [], 'army': [], 'commerce': [], 'upkeep': [], 'civic': []});

export function enableCompanionActivities(type: KingdomPhase, unlockedCompanionActivities: Set<Activity>): Activity[] {
    return activitiesByPhase[type]
        .filter(activity => {
            if (companionActivities.has(activity)) {
                return unlockedCompanionActivities.has(activity);
            } else {
                return true;
            }
        });
}

export function createActivityLabel(activity: Activity, kingdomLevel: number): string {
    let label = unslugify(activity);
    if (activity === 'claim-hex') {
        if (kingdomLevel >= 9) {
            label += ' (three times per round)';
        } else if (kingdomLevel >= 4) {
            label += ' (twice per round)';
        } else {
            label += ' (once per round)';
        }
    }
    if (trainedActivities.has(activity) && oncePerRoundActivities.has(activity)) {
        label += ' (once per round, trained)';
    } else if (trainedActivities.has(activity)) {
        label += ' (trained)';
    } else if (expertActivities.has(activity)) {
        label += ' (expert)';
    } else if (masterActivities.has(activity)) {
        label += ' (master)';
    } else if (oncePerRoundActivities.has(activity)) {
        label += ' (once per round)';
    }
    return label;
}

export function getPerformableActivities(ranks: SkillRanks, enableCapitalInvestment: boolean): Record<Activity, boolean> {
    return Object.fromEntries(allActivities.map(activity => {
        const activityRanks = activityData[activity].skills;
        const noBuildingPreventsActivity = activity !== 'capital-investment' || enableCapitalInvestment;
        const enabled = (Object.entries(activityRanks) as [Skill, number][])
            .some(([skill, rank]) => ranks[skill] >= rank) && noBuildingPreventsActivity;
        return [activity, enabled];
    })) as Record<Activity, boolean>;
}
