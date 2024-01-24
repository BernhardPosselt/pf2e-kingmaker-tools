import {Skill} from './skills';
import {unslugify} from '../../utils';
import {Kingdom, SkillRanks} from './kingdom';
import {activityHints, getKingdomActivitiesById, KingdomActivity, KingdomActivityById} from './activityData';

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

const allActivities = [
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
    // V&K
    'reconnoiter-hex',
    'take-charge',
] as const;


export function getActivitySkills(skills: Partial<SkillRanks>, skillRanks?: SkillRanks): Skill[] {
    if (skillRanks) {
        return (Object.entries(skills) as [Skill, number][])
            .filter(([skill, rank]) => rank <= skillRanks[skill])
            .map(([skill]) => skill);
    } else {
        return Object.keys(skills) as Skill[];
    }
}

interface GroupedActivityNames {
    untrained: Set<string>;
    trained: Set<string>;
    expert: Set<string>;
    master: Set<string>;
    legendary: Set<string>;
    oncePerRound: Set<string>;
    leadership: Set<string>;
    region: Set<string>;
    event: Set<string>;
    army: Set<string>;
    commerce: Set<string>;
    upkeep: Set<string>;
    civic: Set<string>;
    companion: Set<string>;
}

export function groupKingdomActivities(activities: KingdomActivityById): GroupedActivityNames {
    const result: GroupedActivityNames = {
        untrained: new Set(),
        trained: new Set(),
        expert: new Set(),
        master: new Set(),
        legendary: new Set(),
        oncePerRound: new Set(),
        leadership: new Set(),
        region: new Set(),
        event: new Set(),
        army: new Set(),
        commerce: new Set(),
        upkeep: new Set(),
        civic: new Set(),
        companion: new Set(),
    };
    (Object.entries(activities) as [string, KingdomActivity][])
        .forEach(([activity, data]) => {
            result[data.phase].add(activity);
            if (data.oncePerRound) result.oncePerRound.add(activity);
            if (data.companion) result.companion.add(activity);
            if (Object.values(data.skills).every(rank => rank === 0)) {
                result.untrained.add(activity);
            } else if (Object.values(data.skills).every(rank => rank === 1)) {
                result.trained.add(activity);
            } else if (Object.values(data.skills).every(rank => rank === 2)) {
                result.expert.add(activity);
            } else if (Object.values(data.skills).every(rank => rank === 3)) {
                result.master.add(activity);
            } else if (Object.values(data.skills).every(rank => rank === 4)) {
                result.legendary.add(activity);
                return result;
            }
        });
    return result;
}

export function enableCompanionActivities(type: KingdomPhase, unlockedCompanionActivities: Set<string>, names: GroupedActivityNames): string[] {
    return Array.from(names[type])
        .filter(activity => {
            if (names.companion.has(activity)) {
                return unlockedCompanionActivities.has(activity);
            } else {
                return true;
            }
        });
}

export function createActivityLabel(groupedActivities: GroupedActivityNames, activity: string, kingdom: Kingdom): string {
    const kingdomLevel = kingdom.level;
    let label = unslugify(activity);
    if (activity === 'claim-hex') {
        if (kingdomLevel >= 9) {
            label += ' (three times per turn)';
        } else if (kingdomLevel >= 4) {
            label += ' (twice per turn)';
        } else {
            label += ' (once per turn)';
        }
    }
    if (groupedActivities.trained.has(activity) && groupedActivities.oncePerRound.has(activity)) {
        label += ' (once per turn, trained)';
    } else if (groupedActivities.trained.has(activity)) {
        label += ' (trained)';
    } else if (groupedActivities.expert.has(activity)) {
        label += ' (expert)';
    } else if (groupedActivities.master.has(activity)) {
        label += ' (master)';
    } else if (groupedActivities.oncePerRound.has(activity)) {
        label += ' (once per turn)';
    }
    const data = getKingdomActivitiesById(kingdom.homebrewActivities)[activity];
    const hint = activityHints(data, kingdom);
    if (hint) {
        label += ` (${hint})`;
    }
    return label;
}


export function getPerformableActivities(
    ranks: SkillRanks,
    enableCapitalInvestment: boolean,
    ignoreSkillRequirements: boolean,
    activities: KingdomActivityById,
): Record<string, boolean> {
    return Object.fromEntries(allActivities.map(activity => {
        const activityRanks = activities[activity].skills;
        const noBuildingPreventsActivity = activity !== 'capital-investment' || enableCapitalInvestment;
        const enabled = ignoreSkillRequirements || (Object.entries(activityRanks) as [Skill, number][])
            .some(([skill, rank]) => ranks[skill] >= rank) && noBuildingPreventsActivity;
        return [activity, enabled];
    })) as Record<string, boolean>;
}
