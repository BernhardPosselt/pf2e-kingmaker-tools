import {ActorMeal, CampingActivity, ConsumedFood, CookingSkill} from './camping';
import {CampingActivityData, CampingActivityName} from './activities';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {LabelAndValue} from '../utils';

interface ViewActorMeal extends ViewActor {
    favoriteMeal: string | null;
    chosenMeal: string;
}

export interface ViewCampingData {
    actorMeals: ViewActorMeal[];
    actors: ViewActor[];
    campingActivities: ViewCampingActivity[];
    currentRegion: string;
    regions: string[];
    adventuringSince: string;
    encounterDC: number;
    currentEncounterDCModifier: number;
    isGM: boolean;
    isUser: boolean;
    rations: number;
    specialIngredients: number;
    basicIngredients: number;
    watchSecondsElapsed: number;
    watchSecondsDuration: number;
    watchDuration: string;
    dailyPrepsDuration: string;
    watchElapsed: string;
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    knownRecipes: string[];
    knownFavoriteRecipes: string[];
    chosenMeal: string;
    degreesOfSuccesses: ViewDegreeOfSuccess[];
    mealDegreeOfSuccess: StringDegreeOfSuccess | null;
    time: string;
    timeMarkerPositionPx: number;
    hasCookingActor: boolean;
    consumedFood: ConsumedFood;
    chosenMealDc: number;
    cookingSkill: CookingSkill;
    cookingSkills: LabelAndValue[];
}

export interface ViewActor {
    uuid: string;
    image: string;
    name: string;
}


export interface ViewCampingActivity {
    name: string;
    actor: ViewActor | null;
    journalUuid: string;
    isSkillCheck: boolean;
    isCustomAction: boolean;
    skills: string[];
}

async function toViewActivity(activity: CampingActivity | undefined, activityData: CampingActivityData): Promise<ViewCampingActivity> {
    return {
        name: activityData.name,
        actor: activity?.actorUuid ? await toViewActor(activity.actorUuid) : null,
        journalUuid: activityData.journalUuid,
        isCustomAction: activityData.isCustomAction ?? false,
        isSkillCheck: activityData.skills.length > 0,
        skills: activityData.skills,
    };
}

export async function toViewActor(uuid: string): Promise<ViewActor> {
    const actor = await fromUuid(uuid) as Actor | null;
    return {
        image: actor?.img ?? 'modules/pf2e-kingmaker-tools/static/img/add-actor.webp',
        uuid,
        name: actor?.name ?? 'Unknown Actor',
    };
}

export async function toViewActors(actorUuids: string[]): Promise<ViewActor[]> {
    const actorInfos = await Promise.all(actorUuids.map(uuid => toViewActor(uuid)));
    return actorInfos.filter(a => a !== null) as ViewActor[];
}

export async function toViewCampingActivities(
    activities: CampingActivity[],
    data: CampingActivityData[],
    lockedActivities: Set<CampingActivityName>,
): Promise<ViewCampingActivity[]> {
    const result = (await Promise.all(
        data.filter(a => !lockedActivities.has(a.name))
            .map(a => toViewActivity(activities.find(d => d.activity === a.name), a))
    ));
    result.sort((a, b) =>
        (a.name === 'Prepare Campsite' ? -1 : 0) - (b.name === 'Prepare Campsite' ? -1 : 0)
        || a.name.localeCompare(b.name));
    return result;
}

interface ViewDegreeOfSuccess {
    value: StringDegreeOfSuccess;
    label: string;
}

export function toViewDegrees(): ViewDegreeOfSuccess[] {
    return [
        {value: 'criticalFailure', label: 'Critical Failure'},
        {value: 'failure', label: 'Failure'},
        {value: 'success', label: 'Success'},
        {value: 'criticalSuccess', label: 'Critical Success'},
    ];
}

export async function toViewActorMeals(actorUuids: string[], actorMeals: ActorMeal[]): Promise<ViewActorMeal[]> {
    return await Promise.all(actorUuids.map(async uuid => {
        return {
            ...(await toViewActor(uuid)),
            favoriteMeal: actorMeals.find(m => m.actorUuid === uuid)?.favoriteMeal ?? null,
            chosenMeal: actorMeals.find(m => m.actorUuid === uuid)?.chosenMeal ?? 'meal',
        };
    }));
}
