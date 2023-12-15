import {escapeHtml, isBlank, isFirstGm, isKingmakerInstalled, parseSelect} from '../utils';
import {getCamping, getCampingActor} from './storage';
import {getBooleanSetting} from '../settings';

// FIXME
// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-explicit-any
type EntityDocument<T> = any;

export interface CombatUpdate {
    round: number;
}

async function setEntityPlaylist(entity: EntityDocument<unknown>, name: string | null): Promise<void> {
    await entity.setFlag('pf2e-kingmaker-tools', 'combat-track', {name});
}

function getEntityPlaylist(entity: EntityDocument<unknown>): string | null {
    const playlist = entity.getFlag('pf2e-kingmaker-tools', 'combat-track') as {
        name: string | null | undefined
    } | undefined;
    if (playlist?.name) {
        return playlist.name;
    }
    return null;
}

interface CombatPlaylistDialogOptions {
    game: Game,
    entityName: string,
    activePlaylist: string | null | undefined,
    onSubmit: (playlistName: string | null) => Promise<void>,
}

async function setSceneCombatPlaylistDialog(options: CombatPlaylistDialogOptions): Promise<void> {
    const playlistNames = options.game.playlists?.map(p => p.name ?? 'Unknown Playlist Name') ?? [];
    new Dialog({
        title: `Combat Track: ${options.entityName}`,
        content: `
        <form class="simple-dialog-form">
            <div>
                <label for="km-playlist">Playlist</label>
                <select name="playlist">
                    <option value="">-</option>
                    ${playlistNames.map(p => {
            const name = escapeHtml(p);
            return `<option value="${name}" ${name === options.activePlaylist ? 'selected' : ''}>${name}</option>`;
        }).join('')}
                </select>
            </div>
        </form>
        `,
        buttons: {
            save: {
                icon: '<i class="fa-solid fa-save"></i>',
                label: 'Save',
                callback: async (html): Promise<void> => {
                    const $html = html as HTMLElement;
                    const track = parseSelect($html, 'playlist');
                    if (isBlank(track)) {
                        await options.onSubmit(null);
                    } else {
                        await options.onSubmit(track.trim());
                    }
                },
            },
        },
        default: 'save',
    }, {
        jQuery: false,
        width: 350,
    }).render(true);
}

export async function showSetSceneCombatPlaylistDialog(game: Game, entity: EntityDocument<unknown>): Promise<void> {
    await setSceneCombatPlaylistDialog({
        game,
        activePlaylist: getEntityPlaylist(entity),
        entityName: entity.name ?? 'Unknown Entity',
        onSubmit: (playlist) => setEntityPlaylist(entity, playlist),
    });
}

const defaultKingmakerModuleSound = 'The Shrike Hills';

const kingmakerModulePlaylists = new Map<string, string>();
kingmakerModulePlaylists.set('Dunsward', 'Dunsward');
kingmakerModulePlaylists.set('Hooktongue', 'The Narlmarches');
kingmakerModulePlaylists.set('Kamelands', 'Glenebon');
kingmakerModulePlaylists.set('Narlmarches', 'The Narlmarches');
kingmakerModulePlaylists.set('Nomen Heights', 'Dunsward');
kingmakerModulePlaylists.set('Pitax', 'Capital Under Attack');
kingmakerModulePlaylists.set('Sellen Hills', 'Glenebon');
kingmakerModulePlaylists.set('Thousand Voices', 'First World');
kingmakerModulePlaylists.set('Tor of Levenies', 'Dunsward');
kingmakerModulePlaylists.set('Tuskwater', 'Glenebon');
kingmakerModulePlaylists.set('Glenebon Lowlands', 'Glenebon');
kingmakerModulePlaylists.set('Glenebon Uplands', 'Glenebon');
kingmakerModulePlaylists.set('Tiger Lords', 'Glenebon');
kingmakerModulePlaylists.set('Drelev', 'Glenebon');
kingmakerModulePlaylists.set('Numeria', 'Glenebon');
kingmakerModulePlaylists.set('Branthlend Mountains', 'Glenebon');

function getKingmakerCombatSound(game: Game, name: string): PlaylistSound | undefined {
    const combatPlaylistSourceId = '7CiwVus60FiuKFhK';
    return game.playlists
        ?.find(p => p._source._id === combatPlaylistSourceId)
        ?.sounds
        ?.getName(name);
}

/**
 * use overridden playlist first if it exists, otherwise fall back to kingmaker module one if
 * kingmaker has one available
 */
function getRegionPlaylist(game: Game, region: string): Playlist | PlaylistSound | undefined {
    const defaultRegionPlaylist = game.playlists?.getName(`Kingmaker.${region}`);
    const kingmakerModulePlaylistSound = kingmakerModulePlaylists.get(region);
    if (isKingmakerInstalled(game)
        && defaultRegionPlaylist === undefined
        && kingmakerModulePlaylistSound) {
        return getKingmakerCombatSound(game, kingmakerModulePlaylistSound);
    } else {
        return defaultRegionPlaylist;
    }
}

function getDefaultPlaylist(game: Game): Playlist | PlaylistSound | undefined {
    const defaultPlaylist = game.playlists?.getName('Kingmaker.Default');
    if (isKingmakerInstalled(game)
        && defaultPlaylist === undefined) {
        return getKingmakerCombatSound(game, defaultKingmakerModuleSound);
    } else {
        return defaultPlaylist;
    }
}

function getRegion(game: Game): string {
    const campingActor = getCampingActor(game);
    if (campingActor) {
        const camping = getCamping(campingActor);
        return camping.currentRegion;
    } else {
        return 'Brevoy';
    }
}

async function ifPlaylistExists(game: Game, actors: Actor[], callback: (playlist: Playlist | PlaylistSound) => Promise<void>): Promise<void> {
    if (isFirstGm(game) && getBooleanSetting(game, 'enableCombatTracks')) {
        const region = getRegion(game);
        const actorsPlaylist = actors
            .map(a => getEntityPlaylist(a))
            .find(a => !isBlank(a));
        const sceneCombatPlaylist = game.scenes?.active ? getEntityPlaylist(game.scenes.active) : undefined;
        const regionPlaylist = getRegionPlaylist(game, region);
        const defaultPlaylist = getDefaultPlaylist(game);
        const playlist =
            (actorsPlaylist ? game.playlists?.getName(actorsPlaylist) : undefined)
            ?? (sceneCombatPlaylist ? game.playlists?.getName(sceneCombatPlaylist) : undefined)
            ?? regionPlaylist
            ?? defaultPlaylist;
        console.log('Found Playlist ' + playlist?.name);
        if (playlist) {
            await callback(playlist);
        }
    }
}

export async function checkBeginCombat(game: Game, combat: StoredDocument<Combat>, update: CombatUpdate): Promise<void> {
    if (combat.round === 0 && update.round === 1) {
        const actors = combat.combatants
            .map(a => a.actor)
            .filter(a => a !== null) as Actor[];
        await ifPlaylistExists(game, actors, async (music) => {
            await game.scenes?.active?.playlist?.stopAll();
            if (music instanceof Playlist) {
                await music.playAll();
            } else {
                await music.update({playing: true});
            }
        });
    }
}

export async function stopCombat(game: Game, combat: StoredDocument<Combat> | null): Promise<void> {
    const actors = (combat?.combatants
        ?.map(a => a.actor)
        ?.filter(a => a !== null) ?? []) as Actor[];
    await ifPlaylistExists(game, actors, async (music) => {
        if (music.playing) {
            if (music instanceof Playlist) {
                await music.stopAll();
            } else {
                await music.update({playing: false});
            }
            if (game.scenes?.active?.playlistSound) {
                await game.scenes.active.playlistSound.update({playing: true});
            } else {
                await game.scenes?.active?.playlist?.playAll();
            }
        }
    });
}
