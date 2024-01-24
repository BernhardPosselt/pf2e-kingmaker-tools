import {Skill} from './skills';
import {Leader} from './leaders';
import {Leaders, LeaderValues} from './kingdom';
import {mergeObjects} from '../../utils';
import {getStringArraySetting} from '../../settings';

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

export function isCompanionName(name: string): name is Companion {
    return allCompanions.includes(name as Companion);
}

export type UnlockSkills = Partial<Record<Skill, string[]>>;

export interface CompanionUnlock {
    activities: string[];
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

function getCompanionUnlock(role: Leader, values: LeaderValues): CompanionUnlock | undefined {
    const leaderName = values.name;
    const unlock = leaderName in companionActivityUnlocks ? companionActivityUnlocks[leaderName as Companion] : undefined;
    if (values.type === 'companion' && unlock?.roles?.has(role)) {
        return unlock;
    } else {
        return undefined;
    }
}

function getCompanionUnlocks(leaders: Leaders, alwaysEnableCompanionNames: Set<Companion>): CompanionUnlock[] {
    const overridenUnlocks = Array.from(alwaysEnableCompanionNames)
        .map(name => companionActivityUnlocks[name]) as CompanionUnlock[];
    const leaderUnlocks = (Object.entries(leaders) as [Leader, LeaderValues][])
        .flatMap(([role, values]) => {
            const unlock = getCompanionUnlock(role, values);
            const companionName = values.name;
            const overridden = isCompanionName(companionName) && alwaysEnableCompanionNames.has(companionName);
            if (!overridden && unlock) {
                return unlock;
            } else {
                return [];
            }
        });
    return [...leaderUnlocks, ...overridenUnlocks];
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

export function getCompanionUnlockActivities(leaders: Leaders, alwaysEnableCompanionNames: Set<Companion>): string[] {
    return getCompanionUnlocks(leaders, alwaysEnableCompanionNames)
        .flatMap(unlock => unlock.activities);
}

export function getCompanionSkillUnlocks(leaders: Leaders, alwaysEnableCompanionNames: Set<Companion>): UnlockSkills {
    return getCompanionUnlocks(leaders, alwaysEnableCompanionNames)
        .map(unlock => unlock.actionSkills)
        .reduce((prev, curr) => mergeObjects(prev, curr, (a, b) => [...a, ...b]), {});
}

export function getOverrideUnlockCompanionNames(game: Game): Set<Companion> {
    const setting = getStringArraySetting(game, 'forceEnabledCompanionLeadershipBenefits')
        .filter(isCompanionName);
    return new Set<Companion>(setting);
}