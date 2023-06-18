import {CampingActivity} from './camping';
import {CampingActivityData, CampingActivityName} from './activities';

export interface ViewCampingData {
    actors: ViewActor[];
    prepareCamp: ViewCampingActivity;
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
    watchElapsed: string;
    subsistenceAmount: number;
    magicalSubsistenceAmount: number;
    servings: number;
    gunsToClean: number;
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

export async function toViewPrepareCamp(
    activities: CampingActivity[],
    activityData: CampingActivityData[]
): Promise<ViewCampingActivity> {
    const activity = activities.find(a => a.activity === 'Prepare Campsite');
    const data = activityData.find(a => a.name === 'Prepare Campsite')!;
    return await toViewActivity(activity, data);
}

export async function toViewActor(uuid: string): Promise<ViewActor | null> {
    const actor = await fromUuid(uuid) as Actor | null;
    if (actor) {
        return {
            image: actor.img ?? 'modules/pf2e-kingmaker-tools/static/img/add-actor.webp',
            uuid: actor.uuid!,
            name: actor.name ?? 'Unnamed Actor',
        };
    }
    return null;
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
        data
            .filter(a => a.name !== 'Prepare Campsite')
            .filter(a => !lockedActivities.has(a.name))
            .map(a => toViewActivity(activities.find(d => d.activity === a.name), a))
    ));
    result.sort((a, b) => {
        return ((b.actor ? 1 : 0) - (a.actor ? 1 : 0)) || a.name.localeCompare(b.name);
    });
    return result;
}
