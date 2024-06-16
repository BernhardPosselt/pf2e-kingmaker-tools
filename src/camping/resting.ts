import {
    actorHasEffectByUuid,
    getActorsByUuid,
    hasItemByUuid,
    removeActorsEffectsByName,
    removeActorsEffectsByUuid,
} from './actor';
import {Camping, getCampingActivityData, rollCampingCheck} from './camping';
import {saveCamping} from './storage';
import {getAllExpiringMealEffectUuids} from './recipes';
import {getRecipeData} from './eating';
import {getEncounterDC, rollRandomEncounter} from './random-encounters';
import {formatHours} from '../time/app';


function calculateRestSeconds(partySize: number, restDurationSeconds: number): number {
    if (partySize < 2) {
        return restDurationSeconds;
    } else {
        let seconds = restDurationSeconds + Math.floor(restDurationSeconds / (partySize - 1));
        // round up seconds to next minute
        if (seconds % 60 !== 0) {
            seconds += 60 - (seconds % 60);
        }
        return seconds;
    }
}

export function calculateDailyPreparationsSeconds(gunsToClean: number): number {
    return gunsToClean === 0 ? 30 * 60 : Math.ceil(gunsToClean / 4) * 3600;
}

async function healMoreHp(actor: Actor, data: Camping): Promise<void> {
    const healMoreHp = await hasItemByUuid(actor, 'effect', new Set([
        getRecipeData(data).find(r => r.name === 'Basic Meal')!.criticalSuccess!.effects![0].uuid!,
        getCampingActivityData(data).find(a => a.name === 'Dawnflower\'s Blessing')!.effectUuids![0]!.uuid!,
    ]));
    if (healMoreHp) {
        const currentHp = actor.attributes.hp.value;
        const maxHp = actor.attributes.hp.max;
        const conMod = actor.abilities.con.mod;
        const level = actor.level;
        const maxRestored = Math.max(conMod, 1) * level;
        const hpLost = maxHp - currentHp;
        const hpRestored = hpLost >= maxRestored ? maxRestored : hpLost;
        if (hpRestored > 0) {
            await actor.update({'system.attributes.hp.value': currentHp + hpRestored});
        }
    }
}

async function removeExpiredMealEffects(actors: Actor[], camping: Camping): Promise<void> {
    const recipeData = getRecipeData(camping);
    const expiringUuids = getAllExpiringMealEffectUuids(recipeData);
    await removeActorsEffectsByUuid(actors, new Set(expiringUuids));
}

async function getAverageRestDuration(camping: Camping): Promise<number> {
    // hardcode fish on a stick
    const fishOnAStick = getRecipeData(camping).find(r => r.name === 'Fish-On-A-Stick')!;
    const favoriteMeal = fishOnAStick.favoriteMeal!.effects![0]!.uuid;
    const criticalFailure = fishOnAStick.criticalFailure!.effects![0]!.uuid;
    const actors = await getActorsByUuid(new Set(camping.actorUuids));
    const actorsFavoriteMeal = (await Promise.all(actors
        .map(async a => (await actorHasEffectByUuid(a, favoriteMeal)) ? 1 : 0)))
        .reduce((a: number, b: number) => a + b, 0);
    const actorsCriticalFailure = (await Promise.all(actors
        .map(async a => (await actorHasEffectByUuid(a, criticalFailure)) ? 1 : 0)))
        .reduce((a: number, b: number) => a + b, 0);
    const actorsHavingNeither = actors.length - (actorsCriticalFailure + actorsFavoriteMeal);
    return Math.floor(
        ((8 * 3600 * actorsHavingNeither) +
            (7 * 3600 * actorsFavoriteMeal) +
            (9 * 3600 * actorsCriticalFailure)) / actors.length);
}

export function getActorsKeepingWatch(data: Camping, actors: Actor[]): number {
    const organizeWatchCritSuccess = data.campingActivities
        .find(a => a.activity === 'Organize Watch' && a.result === 'criticalSuccess');
    // 1 person can't keep watch, so don't increase to 2
    const additionalWatchers = actors.length > 1 ? (organizeWatchCritSuccess ? 1 : 0) : 0;
    const blacklistedActorNumber = data.actorUuidsNotKeepingWatch
        .filter(uuid => data.actorUuids.includes(uuid))
        .length;
    return actors.length + additionalWatchers + data.increaseWatchActorNumber - blacklistedActorNumber;
}

export async function getWatchSecondsDuration(actors: Actor[], data: Camping): Promise<number> {
    return calculateRestSeconds(getActorsKeepingWatch(data, actors), await getAverageRestDuration(data));
}

export interface WatchHours {
    restHours: string;
    currentRestHours: string;
}

export function getRestHours(watchSecondsDuration: number, dailyPrepsSeconds: number, watchSecondsRemaining: number): WatchHours {
    const fullDurationSeconds = watchSecondsDuration + dailyPrepsSeconds;
    const currentSeconds = watchSecondsRemaining > 0 ? Math.max(0, fullDurationSeconds - watchSecondsRemaining) : 0;
    return {
        currentRestHours: formatHours(currentSeconds),
        restHours: formatHours(fullDurationSeconds),
    };
}

interface RestAdvanceTo {
    encounter: boolean;
    advanceToSeconds: number;
    remainingSeconds: number;
}

export async function getWatchRandomEncounterData(
    game: Game,
    camping: Camping,
    watchDurationSeconds: number,
): Promise<RestAdvanceTo> {
    const dailyPrepsDuration = calculateDailyPreparationsSeconds(camping.gunsToClean);
    const mode = camping.restRollMode;
    const dc = getEncounterDC(camping, game);
    const fullRestDurationInSeconds = watchDurationSeconds + dailyPrepsDuration;
    const region = camping.currentRegion;
    if (mode === 'one') {
        if (await rollRandomEncounter(game, region, dc)) {
            const advanceToSeconds = Math.floor(Math.random() * watchDurationSeconds);
            return {
                encounter: true,
                remainingSeconds: fullRestDurationInSeconds - advanceToSeconds,
                advanceToSeconds,
            };
        }
    } else if (mode === 'one-every-4-hours') {
        const checks = Math.floor(watchDurationSeconds / (3600 * 4));
        const encounterAtSeconds = Math.floor(Math.random() * 4 * 3600);
        for (let i = 0; i < checks; i += 1) {
            if (await rollRandomEncounter(game, region, dc)) {
                const advanceToSeconds = i * 3600 * 4 + encounterAtSeconds;
                return {
                    advanceToSeconds,
                    remainingSeconds: fullRestDurationInSeconds - advanceToSeconds,
                    encounter: true,
                };
            }
        }
    }
    return {
        advanceToSeconds: fullRestDurationInSeconds,
        remainingSeconds: 0,
        encounter: false,
    };
}

export interface RestParameters {
    game: Game;
    sheetActor: Actor;
    camping: Camping;
    watchDurationSeconds: number;
}

async function playRestingPlaylist(game: Game): Promise<void> {
    const restingPlaylist = game.playlists?.getName('Kingmaker.Resting');
    if (restingPlaylist) {
        await Promise.all(restingPlaylist.sounds.map(a => a.update({playing: true})));
    }
}

export async function rest(params: RestParameters): Promise<void> {
    const {game, camping, watchDurationSeconds, sheetActor} = params;
    const actors = await getActorsByUuid(new Set(camping.actorUuids));

    // if remaining seconds are 0 from the start, we haven't started the watch yet
    if (camping.watchSecondsRemaining === 0) {
        await playRestingPlaylist(game);
        camping.watchSecondsRemaining = await startWatch(game, camping, watchDurationSeconds, actors);
    } else {
        await game.time.advance(camping.watchSecondsRemaining);
        camping.watchSecondsRemaining = 0;
    }
    await saveCamping(game, sheetActor, camping);

    if (camping.watchSecondsRemaining === 0) {
        await completeDailyPreparations(game, sheetActor, actors, camping);
    }
}

async function startWatch(game: Game, camping: Camping, watchDurationSeconds: number, actors: Actor[]): Promise<number> {
    const {
        advanceToSeconds,
        remainingSeconds,
        encounter,
    } = await getWatchRandomEncounterData(game, camping, watchDurationSeconds);
    await game.time.advance(advanceToSeconds);
    if (encounter) {
        const actorsKeepingWatch = actors
            .filter(a => !camping.actorUuidsNotKeepingWatch.includes(a.uuid) && (a.type === 'character' || a.type === 'npc'));
        if (actorsKeepingWatch.length > 0) {
            const actorKeepingWatchIndex = Math.floor(Math.random() * actorsKeepingWatch.length);
            await rollCampingCheck({
                game,
                isWatch: true,
                secret: true,
                region: camping.currentRegion,
                actor: actorsKeepingWatch[actorKeepingWatchIndex],
                skill: 'perception',
            });
        }
    }
    return remainingSeconds;
}

async function completeDailyPreparations(
    game: Game,
    sheetActor: Actor,
    actors: Actor[],
    data: Camping,
): Promise<void> {
    await Promise.all(actors.map(async actor => await healMoreHp(actor, data)));
    data.cooking.degreeOfSuccess = null;
    data.campingActivities.forEach(a => a.result = null);
    data.dailyPrepsAtTime = game.time.worldTime;
    await removeActorsEffectsByName(actors, new Set([
        'Undead Guardians (Aided)',
        'Undead Guardians (Defended)',
    ]));
    await removeActorsEffectsByUuid(actors, new Set([
        'Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.ZKJlIqyFgbKDACnG', // enhance weapons
        'Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.pbhKANmDOvwuuchk', // aided
        'Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.tERAC9KHwBjoUt5u', // defended
        'Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.2vdskbqd0VrWKR9Y', // Campfire Story: Critical Success
        'Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.wT2NzrfgmWmCMRtv', // Campfire Story: Success
        'Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.y5Jqw40SWNOgcxvC', // Campfire Story: Critical Failure
    ]));
    await removeExpiredMealEffects(actors, data);
    await saveCamping(game, sheetActor, data);
    await game.pf2e.actions.restForTheNight({actors, skipDialog: true});
}
