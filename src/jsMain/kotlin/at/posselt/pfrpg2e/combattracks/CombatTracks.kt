package at.posselt.pfrpg2e.combattracks

import at.posselt.pfrpg2e.camping.dialogs.Track
import at.posselt.pfrpg2e.camping.dialogs.play
import at.posselt.pfrpg2e.camping.dialogs.stop
import at.posselt.pfrpg2e.camping.findCurrentRegion
import at.posselt.pfrpg2e.camping.getActiveCamping
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.Combatant
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.onDeleteCombat
import com.foundryvtt.core.documents.onPreUpdateCombat
import com.foundryvtt.core.helpers.TypedHooks
import com.foundryvtt.pf2e.actor.PF2EActor
import kotlinx.coroutines.await

fun Actor.getCombatTrack(): Track? =
    getAppFlag("combat-track")

suspend fun Actor.setCombatTrack(track: Track?) {
    setAppFlag("combat-track", track)
}

fun Scene.getCombatTrack(): Track? =
    getAppFlag("combat-track")

suspend fun Scene.setCombatTrack(track: Track?) {
    setAppFlag("combat-track", track)
}

suspend fun Scene.stopMusic() {
    playlistSound
        ?.let { sound -> sound.typeSafeUpdate { playing = false } }
        ?: playlist?.stopAll()?.await()
}


suspend fun Scene.startMusic() {
    playlistSound
        ?.let { sound -> sound.typeSafeUpdate { playing = true } }
        ?: playlist?.playAll()?.await()
}

fun Game.findCombatTrack(combatants: Array<Combatant>, active: Scene): Track? =
    // check for actor overrides
    combatants.asSequence()
        .mapNotNull(Combatant::actor)
        .filterIsInstance<PF2EActor>()
        .mapNotNull(PF2EActor::getCombatTrack)
        .firstOrNull()
        ?: active.getCombatTrack()  // or scene overrides
        ?: getActiveCamping()?.findCurrentRegion()?.combatTrack // otherwise fall back to region

suspend fun Game.startCombatTrack(combatants: Array<Combatant>, active: Scene) {
    findCombatTrack(combatants, active)?.let {
        scenes.active?.stopMusic()
        it.play()
    }
}

suspend fun Game.stopCombatTrack(combatants: Array<Combatant>, active: Scene) {
    findCombatTrack(combatants, active)?.let {
        it.stop()
        scenes.active?.startMusic()
    }
}

fun registerCombatTrackHooks(game: Game) {
    TypedHooks.onPreUpdateCombat { document, changed, _, _ ->
        if (document.round == 0 && changed["round"] == 1 && game.isFirstGM()) {
            buildPromise {
                val active = game.scenes.active
                if (game.settings.pfrpg2eKingdomCampingWeather.getEnableCombatTracks() && active != null) {
                    game.startCombatTrack(document.combatants.contents, active)
                }
            }
        }
    }
    TypedHooks.onDeleteCombat { document, _, _ ->
        buildPromise {
            val active = game.scenes.active
            if (game.settings.pfrpg2eKingdomCampingWeather.getEnableCombatTracks() && active != null && game.isFirstGM()) {
                game.stopCombatTrack(document.combatants.contents, active)
            }
        }
    }
}
