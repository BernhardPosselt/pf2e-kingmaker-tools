import {Skill} from './skills';
import {Ability} from './abilities';
import {mergeObjects, unslugifyActivity} from '../../utils';
import {SkillRanks} from './kingdom';
import {ActivityContent, activityData} from './activityData';

export type KingdomPhase = 'leadership' | 'region' | 'event' | 'army' | 'commerce' | 'upkeep' | 'civic';
export const allActivities = [
    'repair-the-flooded-mine',
    'establish-trade-agreement',
    'provide-care',
    'hire-adventurers',
    'celebrate-holiday',
    'demolish',
    'quell-unrest',
    'rest-and-relax',
    'harvest-crops',
    'garrison-army',
    'recover-army',
    'recruit-army',
    'deploy-army',
    'train-army',
    'outfit-army',
    'establish-work-site-mine',
    'establish-work-site-lumber',
    'establish-work-site-quarry',
    'establish-farmland',
    'create-a-masterpiece',
    'fortify-hex',
    'go-fishing',
    'trade-commodities',
    'gather-livestock',
    'harvest-azure-lily-pollen',
    'purchase-commodities',
    'improve-lifestyle',
    'craft-luxuries',
    'tap-treasury',
    'infiltration',
    'clandestine-business',
    'send-diplomatic-envoy',
    'request-foreign-aid',
    'supernatural-solution',
    'new-leadership',
    'pledge-of-fealty',
    'creative-solution',
    'build-structure',
    'repair-reputation-decay',
    'repair-reputation-corruption',
    'repair-reputation-crime',
    'repair-reputation-strife',
    'prognostication',
    'capital-investment',
    'collect-taxes',
    'build-roads',
    'clear-hex',
    'establish-settlement',
    'irrigation',
    'abandon-hex',
    'claim-hex',
    'relocate-capital',
    'manage-trade-agreements',
    'focused-attention',
    'evangelize-the-dead',
    'decadent-feasts',
    'deliberate-planning',
    'false-victory',
    'show-of-force',
    'warfare-exercises',
    'preventative-measures',
    'spread-the-legend',
    'read-all-about-it',
    'recruit-monsters',
    'process-hidden-fees',
    'supplementary-hunting',
    'disband-army',
    'offensive-gambit',
    'restore-the-temple-of-the-elk',
] as const;

export type Activity = typeof allActivities[number];

export const skillAbilities: Record<Skill, Ability> = {
    agriculture: 'stability',
    arts: 'culture',
    boating: 'economy',
    defense: 'stability',
    engineering: 'stability',
    exploration: 'economy',
    folklore: 'culture',
    industry: 'economy',
    intrigue: 'loyalty',
    magic: 'culture',
    politics: 'loyalty',
    scholarship: 'culture',
    statecraft: 'loyalty',
    trade: 'economy',
    warfare: 'loyalty',
    wilderness: 'stability',
};

export type AbilityScores = Record<Ability, number>;


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
        .map(([activity]) => activity)
);
export const trainedActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => Object.values(data.skills).every(rank => rank === 1))
        .map(([activity]) => activity)
);

export function getActivityPhase(activity: Activity): KingdomPhase {
    return activityData[activity].phase;
}

export const lockedActivities: Set<Activity> = new Set(
    (Object.entries(activityData) as [Activity, ActivityContent][])
        .filter(([, data]) => !data.enabled && data.companion)
        .map(([activity]) => activity)
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

export function getCompanionUnlockedActivities(type: 'leadership' | 'army' | 'region', unlockActivities: Set<Activity>): Activity[] {
    return activitiesByPhase[type]
        .filter(activity => {
            if (lockedActivities.has(activity)) {
                return unlockActivities.has(activity);
            } else {
                return true;
            }
        });
}

export function createActivityLabel(activity: Activity, kingdomLevel: number): string {
    let label = unslugifyActivity(activity);
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
    } else if (oncePerRoundActivities.has(activity)) {
        label += ' (once per round)';
    }
    return label;
}
