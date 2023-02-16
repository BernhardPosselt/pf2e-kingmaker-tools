import {Activity} from './activities';
import {Skill} from './skills';
import {Leader} from './leaders';
import {Leaders, LeaderValues} from './kingdom';
import retryTimes = jest.retryTimes;
import {capitalize, mergeObjects} from '../../utils';

const allActorTypes = [
    'pc',
    'npc',
    'companion',
] as const;

export type ActorTypes = typeof allActorTypes[number];

export const allCompanions = [
    'Amiri',
    'Ekundayo',
    'Harrim',
    'Jaethal',
    'Jubilost',
    'Kalikke',
    'Kanerah',
    'Linzi',
    'Nok-Nok',
    'Octavia',
    'Regongar',
    'Tristian',
    'Valerie',
] as const;

export type Companion = typeof allCompanions[number];

export type UnlockSkills = Partial<Record<Skill, Activity[]>>;

export interface CompanionUnlock {
    activities: Activity[];
    actionSkills: UnlockSkills;
    roles: Set<Leader>;
}

export const companionActivityUnlocks: Record<Companion, CompanionUnlock> = {
    'Harrim': {activities: ['evangelize-the-dead'], actionSkills: {}, roles: new Set(['magister'])},
    'Jaethal': {activities: ['decadent-feasts'], actionSkills: {}, roles: new Set(['emissary'])},
    'Kalikke': {activities: ['deliberate-planning'], actionSkills: {}, roles: new Set(['counselor'])},
    'Kanerah': {activities: ['false-victory'], actionSkills: {}, roles: new Set(['emissary'])},
    'Regongar': {activities: ['show-of-force'], actionSkills: {}, roles: new Set(['general'])},
    'Valerie': {activities: ['warfare-exercises'], actionSkills: {}, roles: new Set(['general'])},
    'Tristian': {activities: ['preventative-measures'], actionSkills: {}, roles: new Set(['magister'])},
    'Linzi': {activities: ['spread-the-legend'], actionSkills: {}, roles: new Set(['counselor'])},
    'Jubilost': {activities: ['process-hidden-fees'], actionSkills: {}, roles: new Set(['treasurer'])},
    'Nok-Nok': {activities: ['recruit-monsters'], actionSkills: {}, roles: new Set(['emissary'])},
    'Ekundayo': {activities: ['supplementary-hunting'], actionSkills: {}, roles: new Set(['general', 'warden'])},
    'Amiri': {activities: [], actionSkills: {}, roles: new Set(['warden'])},
    'Octavia': {
        activities: [],
        actionSkills: {magic: ['celebrate-holiday', 'craft-luxuries', 'create-a-masterpiece', 'rest-and-relax']},
        roles: new Set(['magister']),
    },
};

export function getCompanionUnlock(role: Leader, values: LeaderValues): CompanionUnlock | undefined {
    const name = values.name;
    const unlock = name in companionActivityUnlocks ? companionActivityUnlocks[name as Companion] : undefined;
    if (values.type === 'companion' && unlock?.roles?.has(role)) {
        return unlock;
    } else {
        return undefined;
    }
}

export function getCompanionUnlocks(leaders: Leaders): CompanionUnlock[] {
    return (Object.entries(leaders) as [Leader, LeaderValues][])
        .flatMap(([role, values]) => {
            const unlock = getCompanionUnlock(role, values);
            if (unlock) {
                return unlock;
            } else {
                return [];
            }
        });

}

function checkCompanionRoleInvested(leader: Leader, values: LeaderValues): boolean {
    if (values.type === 'companion') {
        return getCompanionUnlock(leader, values) !== undefined;
    } else {
        return values.invested;
    }
}
export function applyLeaderCompanionRules(leaders: Leaders): Leaders {
    return {
        counselor: {...leaders.counselor, invested: checkCompanionRoleInvested('counselor', leaders.counselor)},
        emissary: {...leaders.emissary, invested: checkCompanionRoleInvested('emissary', leaders.emissary)},
        general: {...leaders.general, invested: checkCompanionRoleInvested('general', leaders.general)},
        magister: {...leaders.magister, invested: checkCompanionRoleInvested('magister', leaders.magister)},
        ruler: {...leaders.ruler, invested: checkCompanionRoleInvested('ruler', leaders.ruler)},
        treasurer: {...leaders.treasurer, invested: checkCompanionRoleInvested('treasurer', leaders.treasurer)},
        viceroy: {...leaders.viceroy, invested: checkCompanionRoleInvested('viceroy', leaders.viceroy)},
        warden: {...leaders.warden, invested: checkCompanionRoleInvested('warden', leaders.warden)},
    };
}

export function getCompanionUnlockActivities(leaders: Leaders): Activity[] {
    return getCompanionUnlocks(leaders)
        .flatMap(unlock => unlock.activities);
}

export function getCompanionUnlockSkills(leaders: Leaders): UnlockSkills {
    return getCompanionUnlocks(leaders)
        .map(unlock => unlock.actionSkills)
        .reduce((prev, curr) => mergeObjects(prev, curr, (a, b) => [...a, ...b]), {});
}
