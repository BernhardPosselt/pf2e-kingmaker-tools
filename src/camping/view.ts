import {CampingActivity} from './data';

export interface ViewCampingData {
    actors: ViewActor[];
    prepareCamp: ViewActor | null;
    campingActivities: ViewCampingActivities[];
    currentRegion: string;
    regions: string[];
    adventuringSince: string;
    encounterDC: number;
    isGM: boolean;
    isUser: boolean;
}

export interface ViewActor {
    uuid: string;
    image: string;
    name: string;
}

export interface ViewCampingSkill {
    dc: 'zone' | 'meal' | 'actorLevel' | number;
    skill?: string;
    proficiency?: 'trained' | 'expert' | 'master' | 'legendary';
}

export interface ViewCampingActivities {
    name: string;
    actor: ViewActor | null;
    locked: boolean;
    skills: ViewCampingSkill[];
}

export async function toViewPrepareCamp(data: { actorUuid: string | null }): Promise<ViewActor | null> {
    if (data.actorUuid) {
        return await toViewActor(data.actorUuid);
    }
    return null;
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

export async function toViewCampingActivities(activities: CampingActivity[]): Promise<ViewCampingActivities[]> {
    const result = await Promise.all(activities.map(async (activity) => {
        const actorUuid = activity.actorUuid;
        return {
            name: activity.name,
            actor: actorUuid ? await toViewActor(actorUuid) : null,
            locked: false,
            skills: [],
        };
    }));
    result.sort((a, b) => {
        return ((b.actor ? 1 : 0) - (a.actor ? 1 : 0)) || a.name.localeCompare(b.name);
    });
    return result;
}
