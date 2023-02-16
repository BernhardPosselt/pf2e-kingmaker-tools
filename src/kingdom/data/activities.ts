import {allSkills, Skill} from './skills';
import {Ability} from './abilities';
import {unslugifyActivity} from '../../utils';

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
const activitySkills: Record<Activity, (Skill)[] | ['*']> = {
    'repair-the-flooded-mine': ['engineering'],
    'restore-the-temple-of-the-elk': ['folklore'],
    // agriculture
    'establish-farmland': ['agriculture'],
    'harvest-crops': ['agriculture'],
    // arts
    'craft-luxuries': ['arts'],
    'rest-and-relax': ['arts', 'boating', 'scholarship', 'trade', 'wilderness'],
    'quell-unrest': ['arts', 'folklore', 'intrigue', 'magic', 'politics', 'warfare'],
    'create-a-masterpiece': ['arts'],
    'repair-reputation-corruption': ['arts'],
    'harvest-azure-lily-pollen': ['agriculture'],
    // boating
    'establish-trade-agreement': ['boating', 'magic', 'trade'],
    'go-fishing': ['boating'],
    // defense
    'fortify-hex': ['defense'],
    'provide-care': ['defense'],
    // engineering
    'build-roads': ['engineering'],
    'clear-hex': ['engineering', 'exploration'],
    'demolish': ['engineering'],
    'establish-settlement': ['engineering', 'industry', 'politics', 'scholarship'],
    'establish-work-site-quarry': ['engineering'],
    'establish-work-site-lumber': ['engineering'],
    'establish-work-site-mine': ['engineering'],
    'irrigation': ['engineering'],
    'repair-reputation-decay': ['engineering'],
    // exploration
    'abandon-hex': ['exploration', 'wilderness'],
    'claim-hex': ['exploration', 'wilderness'],
    'hire-adventurers': ['exploration'],
    // folklore
    'celebrate-holiday': ['folklore'],
    // industry
    'relocate-capital': ['industry'],
    'trade-commodities': ['industry'],
    // intrigue
    'infiltration': ['intrigue'],
    'new-leadership': ['intrigue', 'politics', 'statecraft', 'warfare'],
    'clandestine-business': ['intrigue'],
    'pledge-of-fealty': ['intrigue', 'statecraft', 'warfare'],
    'repair-reputation-strife': ['intrigue'],
    // magic
    'supernatural-solution': ['magic'],
    'prognostication': ['magic'],
    // politics
    'improve-lifestyle': ['politics'],
    // scholarship
    'creative-solution': ['scholarship'],
    // statecraft
    'tap-treasury': ['statecraft'],
    'request-foreign-aid': ['statecraft'],
    'send-diplomatic-envoy': ['statecraft'],
    // trade
    'capital-investment': ['trade'],
    'manage-trade-agreements': ['trade'],
    'purchase-commodities': ['trade'],
    'collect-taxes': ['trade'],
    'repair-reputation-crime': ['trade'],
    // warfare
    'garrison-army': ['warfare'],
    'deploy-army': ['warfare'],
    'outfit-army': ['warfare'],
    'train-army': ['warfare'],
    'recover-army': ['warfare'],
    'recruit-army': ['warfare'],
    'disband-army': ['warfare'],
    'offensive-gambit': ['warfare'],

    // wilderness
    'gather-livestock': ['wilderness'],

    // other
    'build-structure': ['*'],
    'focused-attention': ['*'],

    // companions
    'evangelize-the-dead': ['folklore'],
    'decadent-feasts': ['agriculture'],
    'deliberate-planning': ['scholarship'],
    'false-victory': ['intrigue'],
    'show-of-force': ['warfare'],
    'warfare-exercises': ['warfare'],
    'preventative-measures': ['magic'],
    'spread-the-legend': ['arts'],
    'read-all-about-it': ['scholarship'],
    'recruit-monsters': ['intrigue'],
    'process-hidden-fees': ['trade'],
    'supplementary-hunting': ['wilderness'],
};

export function getActivitySkills(activity: Activity, masterInMagic : boolean): Skill[] {
    const skills = activitySkills[activity!];
    if (skills[0] === '*') {
        return [...allSkills];
    } else {
        const masterUnlockSkills: Skill[] = masterInMagic && activity === 'establish-trade-agreement' ? ['magic'] : [];
        const onlySkills = skills as Skill[];
        return masterUnlockSkills.concat(onlySkills);
    }
}

export const allLeadershipActivities: Activity[] = [
    'capital-investment',
    'celebrate-holiday',
    'clandestine-business',
    'craft-luxuries',
    'create-a-masterpiece',
    'creative-solution',
    'decadent-feasts',
    'deliberate-planning',

    'establish-trade-agreement',
    'evangelize-the-dead',
    'false-victory',

    'focused-attention',
    'hire-adventurers',
    'infiltration',
    'pledge-of-fealty',
    'process-hidden-fees',
    'prognostication',
    'provide-care',
    'preventative-measures',
    'purchase-commodities',
    'quell-unrest',
    'read-all-about-it',
    'recruit-army',
    'relocate-capital',
    'repair-reputation-corruption',
    'repair-reputation-crime',
    'repair-reputation-decay',
    'repair-reputation-strife',
    'request-foreign-aid',
    'send-diplomatic-envoy',
    'show-of-force',
    'spread-the-legend',
    'supernatural-solution',
    'warfare-exercises',
];

const leadershipActivities = new Set(allLeadershipActivities);

export const allRegionActivities: Activity[] = [
    'abandon-hex',
    'build-roads',
    'claim-hex',
    'clear-hex',
    'establish-farmland',
    'establish-settlement',
    'establish-work-site-quarry',
    'establish-work-site-lumber',
    'establish-work-site-mine',
    'go-fishing',
    'gather-livestock',
    'harvest-crops',
    'irrigation',
    'recruit-monsters',
    'supplementary-hunting',
    'harvest-azure-lily-pollen',
];

const regionActivities = new Set(allRegionActivities);

export const allWarfareActivities: Activity[] = [
    'deploy-army',
    'disband-army',
    'garrison-army',
    'offensive-gambit',
    'outfit-army',
    'recover-army',
    'recruit-army',
    'train-army',
];

const warfareActivities = new Set(allWarfareActivities);

export const  oncePerRoundActivities: Set<Activity> = new Set(['quell-unrest', 'create-a-masterpiece']);
export const trainedActivities: Set<Activity> = new Set([
    'pledge-of-fealty',
    'repair-reputation-decay',
    'repair-reputation-crime',
    'repair-reputation-corruption',
    'repair-reputation-strife',
    'create-a-masterpiece',
    'irrigation',
    'relocate-capital',
    'clandestine-business',
    'prognostication',
    'request-foreign-aid',
    'collect-taxes',
    'prognostication',
    'send-diplomatic-envoy',
]);

export function getActivityPhase(activity: Activity): KingdomPhase | undefined {
    if (regionActivities.has(activity)) {
        return 'region';
    } else if (warfareActivities.has(activity)) {
        return 'army';
    } else if (leadershipActivities.has(activity)) {
        return 'region';
    }
}

export const lockedActivities: Set<Activity> = new Set([
    'read-all-about-it',
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
]);

function filterUnlocked(activities: Activity[], unlockActivities: Set<Activity>): Activity[] {
    return activities.filter(activity => {
        if (lockedActivities.has(activity)) {
            return unlockActivities.has(activity);
        } else {
            return activity;
        }
    });
}

export function getUnlockedActivities(type: 'leadership' | 'warfare' | 'region', unlockActivities: Set<Activity>): Activity[] {
    if (type === 'leadership') {
        return filterUnlocked(allLeadershipActivities, unlockActivities);
    } else if (type === 'region') {
        return filterUnlocked(allRegionActivities, unlockActivities);
    } else if (type === 'warfare') {
        return filterUnlocked(allWarfareActivities, unlockActivities);
    } else {
        return [];
    }
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
