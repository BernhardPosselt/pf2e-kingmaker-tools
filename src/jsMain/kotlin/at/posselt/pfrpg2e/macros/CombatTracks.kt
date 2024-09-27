package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.app.*
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.camping.dialogs.Track
import at.posselt.pfrpg2e.combattracks.getCombatTrack
import at.posselt.pfrpg2e.combattracks.setCombatTrack
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import com.foundryvtt.core.Game
import com.foundryvtt.core.Ui
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.PlaylistSound
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.pf2e.actor.PF2EActor
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
private external interface CombatTrackData {
    val playlistUuid: String
    val trackUuid: String?
}

@JsPlainObject
private external interface CombatTrackContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val formRows: Array<FormElementContext>
}

private class CombatTrackApplication(
    private val game: Game,
    private val scene: Scene,
    private val actor: PF2EActor?,
) : FormApp<CombatTrackContext, CombatTrackData>(
    title = "Set Combat Track: ${actor?.name ?: scene.name}",
    template = "components/forms/application-form.hbs",
    id = "kmCombatTrack",
) {
    var combatTrack: Track? = if (actor == null) {
        scene.getCombatTrack()
    } else {
        actor.getCombatTrack()
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CombatTrackContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val playlist = combatTrack?.let { fromUuidTypeSafe<Playlist>(it.playlistUuid) }
        val playlistSound = combatTrack?.trackUuid?.let { fromUuidTypeSafe<PlaylistSound>(it) }
        CombatTrackContext(
            isFormValid = isFormValid,
            partId = parent.partId,
            formRows = formContext(
                Select(
                    required = false,
                    name = "playlistUuid",
                    label = "Playlist",
                    value = playlist?.uuid,
                    options = game.playlists.contents.mapNotNull { it.toOption(useUuid = true) }
                ),
                Select(
                    required = false,
                    name = "trackUuid",
                    label = "Track",
                    value = playlistSound?.uuid,
                    options = playlist?.sounds?.contents?.mapNotNull { it.toOption(useUuid = true) } ?: emptyList()
                )
            )
        )
    }

    override fun onParsedSubmit(value: CombatTrackData): Promise<Void> = buildPromise {
        combatTrack = Track(playlistUuid = value.playlistUuid, trackUuid = value.trackUuid)
        undefined
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (val action = target.dataset["action"]) {
            "save" -> {
                buildPromise {
                    if (actor == null) {
                        scene.setCombatTrack(combatTrack)
                    } else {
                        actor.setCombatTrack(combatTrack)
                    }
                    close()
                }
            }

            else -> console.log(action)
        }
    }
}

fun combatTrackMacro(game: Game, actor: PF2EActor?) {
    val currentScene = game.scenes.current
    if (currentScene == null) {
        Ui.notifications.error("Can not run macro without a scene")
        return
    }
    CombatTrackApplication(game, currentScene, actor).launch()
}