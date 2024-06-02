import {ActorMeal, Camping, CampingActivity, CookingSkill, getCombatEffects} from './camping';
import {CampingActivityData, CampingActivityName} from './activities';
import {StringDegreeOfSuccess} from '../degree-of-success';
import {escapeHtml, LabelAndValue, unslugify} from '../utils';
import {RecipeData} from './recipes';
import {ConsumedFood} from './eating';

interface ViewActorMeal extends ViewActor {
    favoriteMeal: string | null;
    favoriteMealUuid: string | null;
    chosenMeal: string;
}

type CssClass = StringDegreeOfSuccess | 'secret' | null;

interface ViewCampingActor extends ViewActor {
    cssClass: CssClass;
    noActivityChosen: boolean;
    activity: string | null;
}

export interface ViewCampingData {
    actorMeals: ViewActorMeal[];
    actors: ViewCampingActor[];
    needsPrepareCampsite: boolean;
    isDay: boolean;
    campingActivities: ViewCampingActivity[];
    currentRegion: string;
    regions: LabelAndValue[];
    adventuringSince: string;
    encounterDC: number;
    currentEncounterDCModifier: number;
    isGM: boolean;
    isUser: boolean;
    rations: number;
    specialIngredients: number;
    basicIngredients: number;
    watchSecondsDuration: number;
    watchDuration: string;
    dailyPrepsDuration: string;
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    knownRecipes: LabelAndValue[];
    knownFavoriteRecipes: LabelAndValue[];
    mealChoices: LabelAndValue[];
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
    showContinueRest: boolean;
    watchEnabled: boolean;
    restHours: string;
    currentRestHours: string;
    actorsKeepingWatch: number;
}

export interface ViewActor {
    uuid: string;
    image: string;
}


export interface ViewCampingActivity {
    name: string;
    slug: string;
    actor: ViewActor | null;
    journalUuid: string | null;
    isSkillCheck: boolean;
    degreeOfSuccess: StringDegreeOfSuccess | null;
    skills: LabelAndValue[];
    selectedSkill: string | null;
    isSecret: boolean;
    isHidden: boolean;
    askDc: boolean;
}

async function toViewActivity(
    activity: CampingActivity | undefined,
    activityData: CampingActivityData,
    isHidden: boolean,
): Promise<ViewCampingActivity> {
    const skills: string[] = [];
    const activityDataSkills = activityData.skills;
    if (activityDataSkills === 'any' && activity?.actorUuid) {
        const actor = await fromUuid(activity.actorUuid) as Actor | null;
        if (actor) {
            Object.keys(actor.skills).forEach(s => skills.push(s));
        }
    } else if (Array.isArray(activityDataSkills)) {
        activityDataSkills.forEach(a => skills.push(a));
    }
    return {
        name: activityData.name,
        actor: activity?.actorUuid ? await toViewActor(activity.actorUuid) : null,
        journalUuid: activityData.journalUuid ?? null,
        isSkillCheck: skills.length > 0,
        skills: skills.map(s => {
            return {value: s, label: unslugify(s)};
        }),
        degreeOfSuccess: activity?.result ?? null,
        slug: escapeHtml(activityData.name),
        isSecret: activityData.isSecret,
        selectedSkill: activity?.selectedSkill ?? (activityDataSkills.length > 0 ? activityDataSkills[0] : null),
        isHidden,
        askDc: activityData.dc === undefined,
    };
}

export async function toViewActor(uuid: string): Promise<ViewActor> {
    const actor = await fromUuid(uuid) as Actor | null;
    return {
        image: actor?.img ?? 'modules/pf2e-kingmaker-tools/static/img/add-actor.webp',
        uuid,
    };
}

function getCssClass(chosenActivity: CampingActivity | undefined, hasSkillCheck: boolean, isUser: boolean): CssClass {
    const result = chosenActivity?.result;
    if (!hasSkillCheck) {
        return 'criticalSuccess';
    } else if (result !== undefined && result !== null) {
        if (isUser) {
            return 'secret';
        } else {
            return result;
        }
    } else {
        return null;
    }
}

export async function toViewActors(
    actorUuids: string[],
    activities: CampingActivity[],
    activityData: CampingActivityData[],
    isUser: boolean,
): Promise<ViewCampingActor[]> {
    const actorInfos = await Promise.all(actorUuids.map(uuid => toViewActor(uuid)));
    return actorInfos
        .filter(a => a !== null)
        .map(actor => {
            const chosenActivity = activities
                .filter(a => a.activity !== 'Prepare Campsite')
                .find(a => a.actorUuid === actor.uuid);
            const hasSkillCheck = (activityData
                .find(a => a.name === chosenActivity?.activity && chosenActivity.activity !== undefined)
                ?.skills ?? []).length > 0;
            const cssClass = getCssClass(chosenActivity, hasSkillCheck, isUser);
            return {
                ...actor,
                noActivityChosen: !chosenActivity,
                activity: chosenActivity?.activity ?? null,
                cssClass,
            };
        });
}

export async function toViewCampingActivities(
    activities: CampingActivity[],
    data: CampingActivityData[],
    lockedActivities: Set<CampingActivityName>,
): Promise<ViewCampingActivity[]> {
    const prepareCampActivity = activities.find(a => a.activity === 'Prepare Campsite');
    const prepareCampDegreeOfSuccess = prepareCampActivity?.result ?? null;
    const canPerformActivities = prepareCampDegreeOfSuccess !== 'criticalFailure' && prepareCampDegreeOfSuccess !== null;
    const result = (await Promise.all(
        data.map(a => {
                const isHidden = (lockedActivities.has(a.name) || !canPerformActivities) && a.name !== 'Prepare Campsite';
                return toViewActivity(activities.find(d => d.activity === a.name), a, isHidden);
            },
        ),
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
        {value: 'criticalSuccess', label: 'Critical Success'},
        {value: 'success', label: 'Success'},
        {value: 'failure', label: 'Failure'},
        {value: 'criticalFailure', label: 'Critical Failure'},
    ];
}

export async function toViewActorMeals(actorUuids: string[], actorMeals: ActorMeal[], recipeData: RecipeData[]): Promise<ViewActorMeal[]> {
    return await Promise.all(actorUuids.map(async uuid => {
        const favoriteMeal = actorMeals.find(m => m.actorUuid === uuid)?.favoriteMeal ?? null;
        const favoriteMealUuid = favoriteMeal ? recipeData.find(r => r.name === favoriteMeal)?.uuid ?? null : null;
        return {
            ...(await toViewActor(uuid)),
            favoriteMeal,
            favoriteMealUuid,
            chosenMeal: actorMeals.find(m => m.actorUuid === uuid)?.chosenMeal ?? 'meal',
        };
    }));
}

export async function combatEffectsToChat(data: Camping): Promise<void> {
    const effects = await getCombatEffects(data);
    if (Object.keys(effects).length > 0) {
        const companionsAndLabels = Object.entries(effects)
            .map(([name, link]) => `<b>${name} (${link.target})<b>: ${link.uuid}`)
            .join('<br>');
        await ChatMessage.create({content: companionsAndLabels});
    }
}
