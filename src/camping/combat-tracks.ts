import {escapeHtml, isBlank, isFirstGm, parseSelect} from '../utils';
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

async function ifPlaylistExists(game: Game, actors: Actor[], callback: (playlist: Playlist) => Promise<void>): Promise<void> {
    if (isFirstGm(game) && getBooleanSetting(game, 'enableCombatTracks')) {
        const campingActor = getCampingActor(game);
        if (campingActor) {
            const camping = getCamping(campingActor);
            const region = camping.currentRegion;
            const actorsPlaylist = actors
                .map(a => getEntityPlaylist(a))
                .find(a => !isBlank(a));
            const sceneCombatPlaylist = game.scenes?.active ? getEntityPlaylist(game.scenes.active) : undefined;
            const regionPlaylistName = `Kingmaker.${region}`;
            const defaultPlaylistName = 'Kingmaker.Default';
            const playlist =
                (actorsPlaylist ? game.playlists?.getName(actorsPlaylist) : undefined)
                ?? (sceneCombatPlaylist ? game.playlists?.getName(sceneCombatPlaylist) : undefined)
                ?? game.playlists?.getName(regionPlaylistName)
                ?? game.playlists?.getName(defaultPlaylistName);
            console.log('Found Playlist ' + playlist?.name);
            if (playlist) {
                await callback(playlist);
            }
        }
    }
}

export async function checkBeginCombat(game: Game, combat: StoredDocument<Combat>, update: CombatUpdate): Promise<void> {
    if (combat.round === 0 && update.round === 1) {
        const actors = combat.combatants
            .map(a => a.actor)
            .filter(a => a !== null) as Actor[];
        await ifPlaylistExists(game, actors, async (playlist) => {
            await game.scenes?.active?.playlist?.stopAll();
            await playlist.playAll();
        });
    }
}

export async function stopCombat(game: Game, combat: StoredDocument<Combat> | null): Promise<void> {
    const actors = (combat?.combatants
        ?.map(a => a.actor)
        ?.filter(a => a !== null) ?? []) as Actor[];
    await ifPlaylistExists(game, actors, async (playlist) => {
        if (playlist.playing) {
            await playlist.stopAll();
            if (game.scenes?.active?.playlistSound) {
                await game.scenes.active.playlistSound.update({playing: true});
            } else {
                await game.scenes?.active?.playlist?.playAll();
            }
        }
    });
}
