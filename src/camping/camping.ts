import {allCampingActivities, CampingActivityData, CampingActivityName} from './activities';
import {getRegionInfo} from './regions';
import {getLevelBasedDC, postDegreeOfSuccessMessage, slugify, unslugify} from '../utils';
import {DegreeOfSuccess, degreeToProperty, StringDegreeOfSuccess} from '../degree-of-success';
import {DcType} from './data';
import {RecipeData} from './recipes';
import {checkRandomEncounterMessage} from './chat';
import {getActorByUuid} from './actor';

export type RestRollMode = 'one' | 'none' | 'one-every-4-hours';

export interface CampingActivity {
    activity: CampingActivityName;
    actorUuid: string | null;
    result: StringDegreeOfSuccess | null,
    selectedSkill: string | null;
}

export type ChosenMeal = 'meal' | 'rationsOrSubsistence' | 'nothing';

export interface ActorMeal {
    actorUuid: string;
    favoriteMeal: string | null;
    chosenMeal: ChosenMeal;
}

export type CookingSkill = 'survival' | 'cooking';

export interface Cooking {
    knownRecipes: string[];
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    chosenMeal: string;
    cookingSkill: CookingSkill;
    actorMeals: ActorMeal[];
    homebrewMeals: RecipeData[];
    degreeOfSuccess: StringDegreeOfSuccess | null;
}


export interface Camping {
    actorUuids: string[];
    campingActivities: CampingActivity[];
    homebrewCampingActivities: CampingActivityData[];
    lockedActivities: CampingActivityName[];
    cooking: Cooking;
    watchSecondsRemaining: number;
    gunsToClean: number;
    dailyPrepsAtTime: number;
    currentRegion: string;
    encounterModifier: number;
    restRollMode: RestRollMode;
    increaseWatchActorNumber: number;
    actorUuidsNotKeepingWatch: string[];
    huntAndGatherTargetActorUuid?: string | null;
    ignoreSkillRequirements: boolean;
}

export function getDefaultConfiguration(game: Game, migratedRecipes: string[]): Camping {
    return {
        actorUuids: [],
        campingActivities: [],
        cooking: {
            chosenMeal: 'Basic Meal',
            actorMeals: [],
            magicalSubsistenceAmount: 0,
            subsistenceAmount: 0,
            knownRecipes: Array.from(new Set(['Basic Meal', 'Hearty Meal', ...migratedRecipes])),
            homebrewMeals: [],
            cookingSkill: 'survival',
            degreeOfSuccess: null,
        },
        restRollMode: 'one',
        currentRegion: 'Rostland Hinterlands',
        dailyPrepsAtTime: game.time.worldTime,
        homebrewCampingActivities: [],
        encounterModifier: 0,
        gunsToClean: 0,
        watchSecondsRemaining: 0,
        actorUuidsNotKeepingWatch: [],
        increaseWatchActorNumber: 0,
        lockedActivities: allCampingActivities
            .filter(a => a.isLocked)
            .map(a => a.name),
        ignoreSkillRequirements: false,
    };
}

export function getDC(game: Game, actor: Actor, dcType: DcType, region: string): number {
    if (dcType === 'zone') {
        return getRegionInfo(game, region).zoneDC;
    } else if (dcType === 'actorLevel') {
        return getLevelBasedDC(actor.level);
    } else {
        return dcType;
    }
}

export interface SkillCheckOptions {
    game: Game,
    actor: Actor,
    dc?: DcType,
    skill: string,
    secret?: boolean,
    isWatch?: boolean,
    activity?: CampingActivityData,
    region: string;
}

export async function rollCampingCheck(
    {
        game,
        actor,
        dc,
        skill,
        secret = false,
        isWatch = false,
        region,
        activity,
    }: SkillCheckOptions): Promise<DegreeOfSuccess | null> {
    const rollData: RollOptions = {
        extraRollOptions: ['camping'],
    };
    if (isWatch) {
        rollData.extraRollOptions?.push('watch');
    }
    if (activity) {
        rollData['extraRollOptions']?.push('action:' + slugify(activity.name));
    }
    if (dc) {
        rollData['dc'] = {value: getDC(game, actor, dc, region)};
    }
    if (secret) {
        rollData['rollMode'] = 'blindroll';
    }
    let result;
    const skills = actor.skills;
    const loreSkill = `${skill}-lore`;
    const skillToRoll = skill in skills ? skill : (loreSkill in skills ? loreSkill : null);
    if (skill === 'perception') {
        result = await actor.perception.roll(rollData);
    } else if (skillToRoll === null) {
        ui.notifications?.error(`${actor.name} does not have skill ${unslugify(skill)}`);
        return null;
    } else {
        result = await skills[skillToRoll].roll(rollData);
    }
    const degree = result?.degreeOfSuccess ?? null;
    if (degree !== null) {
        await postDegreeOfSuccessMessage({
            degreeOfSuccess: degree,
            messageConfig: {
                isPrivate: secret,
                critSuccess: activity?.criticalSuccess?.message,
                success: activity?.success?.message,
                failure: activity?.failure?.message,
                critFailure: activity?.criticalFailure?.message,
            },
        });
        if (activity !== undefined && activity[degreeToProperty(degree)]?.checkRandomEncounter) {
            await checkRandomEncounterMessage();
        }
    }
    return degree;
}


export function getCampingActivityData(current: Camping): CampingActivityData[] {
    const homebrewActivities = current.homebrewCampingActivities;
    const homebrewNames = new Set(homebrewActivities.map(i => i.name.toLocaleLowerCase()));
    return [
        ...allCampingActivities.filter(a => !homebrewNames.has(a.name.toLocaleLowerCase())),
        ...homebrewActivities,
    ];
}

export interface CombatEffect {
    uuid: string;
    target: string;
}

const combatEffects: Partial<Record<CampingActivityName, CombatEffect>> = {
    'Enhance Weapons':
        {
            uuid: '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.ZKJlIqyFgbKDACnG]{Enhance Weapons}',
            target: 'Allies',
        },
    'Set Traps':
        {
            uuid: '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.PSBOS7ZEl9RGWBqD]{Set Traps}',
            target: 'Enemies',
        },
    'Undead Guardians':
        {
            uuid: '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.KysTaC245mOnSnmE]{Undead Guardians}',
            target: '1 Ally',
        },
    'Water Hazards':
        {
            uuid: '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.LN6mH7Muj4hgvStt]{Water Hazards}',
            target: 'Enemies',
        },
    'Maintain Armor': {
        uuid: '@UUID[Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.wojV4NiAOYsnfFby]{Maintain Armor: Armor}',
        target: '',
    },
};

async function getMaintainArmorTarget(actorUuids: string[]): Promise<string> {
    const actors = await Promise.all(actorUuids.map(uuid => fromUuid(uuid) as Promise<Actor | null>));
    const level = Math.max(1, ...(actors.map(a => a?.system.details.level.value ?? 1)));
    if (level < 3) {
        return '1 Ally';
    } else {
        const allies = 1 + Math.floor((level - 1) / 2);
        return `${allies} Allies`;
    }
}

export async function getCombatEffects(data: Camping): Promise<Partial<Record<CampingActivityName, CombatEffect>>> {
    const result: Partial<Record<CampingActivityName, CombatEffect>> = {};
    const maintainArmorTarget = await getMaintainArmorTarget(data.actorUuids);
    data.campingActivities.forEach(a => {
        const activityName = a.activity;
        if (activityName in combatEffects && a.actorUuid) {
            const combatEffect = combatEffects[activityName]!;
            const target = activityName === 'Maintain Armor' ? maintainArmorTarget : combatEffect.target;
            result[activityName] = {
                ...combatEffect,
                target,
            };
        }
    });
    return result;
}

export function getPartyActors(game: Game): Actor[] {
    return game.actors?.filter(a => a.type === 'party') ?? [];
}

export async function getHuntAndGatherActor(game: Game, data: Camping): Promise<Actor | null> {
    const uuid = data.huntAndGatherTargetActorUuid;
    if (uuid && (data.actorUuids.includes(uuid) || getPartyActors(game).find(a => a.uuid === uuid))) {
        return await getActorByUuid(uuid);
    }
    return null;
}
