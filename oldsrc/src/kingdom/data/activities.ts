import {Skill} from './skills';
import {Kingdom, SkillRanks} from './kingdom';
import {getKingdomActivitiesById, KingdomActivity, KingdomActivityById} from './activityData';
import {armyStatisticsByLevel} from '../../armies/data';

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
    };
    (Object.entries(activities) as [string, KingdomActivity][])
        .forEach(([activity, data]) => {
            result[data.phase].add(activity);
            if (data.oncePerRound) result.oncePerRound.add(activity);
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

export function createActivityLabel(groupedActivities: GroupedActivityNames, activity: string, kingdom: Kingdom): string {
    const kingdomLevel = kingdom.level;
    const data = getKingdomActivitiesById(kingdom.homebrewActivities)[activity];
    const label = data.title;
    const hints = [];
    if (activity === 'claim-hex') {
        if (kingdomLevel >= 9) {
            hints.push('three times per turn');
        } else if (kingdomLevel >= 4) {
            hints.push('twice per turn');
        } else {
            hints.push('once per turn');
        }
    } else if (activity === 'train-army') {
        const maxTactics = armyStatisticsByLevel.get(kingdom.level)?.maximumTactics ?? 6;
        hints.push(`Max ${maxTactics} per Army`);
    }
    if (groupedActivities.trained.has(activity)) {
        hints.push('trained');
    } else if (groupedActivities.expert.has(activity)) {
        hints.push('expert');
    } else if (groupedActivities.master.has(activity)) {
        hints.push('master');
    }
    if (groupedActivities.oncePerRound.has(activity)) {
        hints.push('once per turn');
    }
    if (data.hint) {
        hints.push(data.hint);
    }
    const append = hints.length > 0 ? ` (${hints.join(', ')})` : '';
    return label + append;
}


export function getPerformableActivities(
    ranks: SkillRanks,
    enableCapitalInvestment: boolean,
    ignoreSkillRequirements: boolean,
    activities: KingdomActivityById,
): Record<string, boolean> {
    return Object.fromEntries(Object.values(activities).map(activity => {
        const activityRanks = activity.skills;
        const noBuildingPreventsActivity = activity.id !== 'capital-investment' || enableCapitalInvestment;
        const enabled = ignoreSkillRequirements || (Object.entries(activityRanks) as [Skill, number][])
            .some(([skill, rank]) => ranks[skill] >= rank) && noBuildingPreventsActivity;
        return [activity.id, enabled];
    })) as Record<string, boolean>;
}
