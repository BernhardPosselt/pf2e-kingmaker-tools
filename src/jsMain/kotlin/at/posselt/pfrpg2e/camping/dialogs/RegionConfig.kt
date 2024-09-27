package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.*
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.combattracks.startMusic
import at.posselt.pfrpg2e.combattracks.stopMusic
import at.posselt.pfrpg2e.data.regions.Terrain
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.*
import com.foundryvtt.core.Game.scenes
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.*
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.PlaylistSound
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.array.push
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface Track {
    var playlistUuid: String
    var trackUuid: String?
}

@JsPlainObject
external interface RegionSetting {
    var name: String
    var zoneDc: Int
    var encounterDc: Int
    var level: Int
    var terrain: String
    var rollTableUuid: String?
    var combatTrack: Track?
}

@JsPlainObject
external interface RegionSettings {
    var regions: Array<RegionSetting>
}

@JsPlainObject
external interface TableHead {
    var label: String
    var classes: Array<String>?
}

@JsPlainObject
external interface RegionSettingsContext : HandlebarsRenderContext {
    var heading: Array<TableHead>
    var formRows: Array<Array<FormElementContext>>
    var isValid: Boolean
    var allowDelete: Boolean
}


@OptIn(ExperimentalJsStatic::class)
@JsExport
class RegionSettingsDataModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @OptIn(ExperimentalJsStatic::class)
        @JsStatic
        fun defineSchema() = buildSchema {
            array("regions") {
                schema {
                    string("name")
                    int("zoneDc")
                    int("encounterDc")
                    int("level")
                    string("rollTableUuid", nullable = true)
                    schema("combatTrack") {
                        string("playlistUuid", nullable = true)
                        string("trackUuid", nullable = true)
                    }
                    string("terrain")
                }
            }
        }
    }
}


@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("RegionConfig")
class RegionConfig(
    private val actor: PF2ENpc,
) : FormApp<RegionSettingsContext, RegionSettings>(
    title = "Regions",
    width = 1200,
    template = "applications/settings/configure-regions.hbs",
    dataModel = RegionSettingsDataModel::class.js,
    debug = true,
    id = "kmRegions"
) {
    private var currentSettings = actor.getCamping()!!.regionSettings

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (val action = target.dataset["action"]) {
            "save" -> {
                buildPromise {
                    actor.getCamping()?.let { camping ->
                        camping.regionSettings = currentSettings
                        if (!camping.regionSettings.regions.any { it.name == camping.currentRegion }) {
                            camping.currentRegion = currentSettings.regions.first().name
                        }
                        actor.setCamping(camping)
                    }
                    close()
                }
            }

            "add" -> {
                addDefaultRegion()
                render()
            }

            "delete" -> {
                target.dataset["index"]?.toInt()?.let {
                    currentSettings.regions = currentSettings.regions
                        .filterIndexed { index, _ -> index != it }
                        .toTypedArray()
                    render()
                }
            }

            else -> console.log(action)
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<RegionSettingsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val playlistOptions = game.playlists.contents
            .mapNotNull { it.toOption(useUuid = true) }
            .sortedBy { it.label }
        val rolltableOptions = game.tables.contents
            .mapNotNull { it.toOption(useUuid = true) }
            .sortedBy { it.label }
        RegionSettingsContext(
            partId = parent.partId,
            isValid = isFormValid,
            heading = arrayOf(
                TableHead("Name"),
                TableHead("Level", arrayOf("number-select-heading")),
                TableHead("Terrain"),
                TableHead("Zone DC", arrayOf("number-select-heading")),
                TableHead("Encounter DC", arrayOf("number-select-heading")),
                TableHead("Roll Table"),
                TableHead("Combat Playlist"),
                TableHead("Combat Track"),
                TableHead("Remove", arrayOf("small-heading"))
            ),
            allowDelete = currentSettings.regions.size > 1,
            formRows = currentSettings.regions.mapIndexed { index, row ->
                val trackOptions = row.combatTrack?.playlistUuid?.let { uuid ->
                    game.playlists.find { it.uuid == uuid }?.sounds?.contents?.mapNotNull { it.toOption(useUuid = true) }
                        ?: emptyList()
                } ?: emptyList()
                arrayOf(
                    TextInput(
                        name = "regions.$index.name",
                        label = "Name",
                        value = row.name,
                        hideLabel = true
                    ).toContext(),
                    Select.level(
                        name = "regions.$index.level",
                        label = "Level",
                        value = row.level,
                        hideLabel = true
                    ).toContext(),
                    Select.fromEnum<Terrain>(
                        name = "regions.$index.terrain",
                        label = "Terrain",
                        value = fromCamelCase<Terrain>(row.terrain),
                        hideLabel = true
                    ).toContext(),
                    Select.dc(
                        name = "regions.$index.zoneDc",
                        label = "Zone DC",
                        value = row.zoneDc,
                        hideLabel = true
                    ).toContext(),
                    Select.flatCheck(
                        name = "regions.$index.encounterDc",
                        label = "Encounter DC",
                        value = row.encounterDc,
                        hideLabel = true,
                    ).toContext(),
                    Select(
                        name = "regions.$index.rollTableUuid",
                        label = "Roll Table",
                        value = row.rollTableUuid,
                        required = false,
                        hideLabel = true,
                        options = rolltableOptions,
                    ).toContext(),
                    Select(
                        name = "regions.$index.combatTrack.playlistUuid",
                        label = "Combat Playlist",
                        value = row.combatTrack?.playlistUuid,
                        required = false,
                        hideLabel = true,
                        options = playlistOptions
                    ).toContext(),
                    Select(
                        name = "regions.$index.combatTrack.trackUuid",
                        label = "Combat Track",
                        value = row.combatTrack?.trackUuid,
                        required = false,
                        hideLabel = true,
                        options = trackOptions
                    ).toContext(),
                )
            }.toTypedArray()
        )
    }

    override fun onParsedSubmit(value: RegionSettings) = buildPromise {
        // unfortunately there is no way to make an object optional if all of its properties are null
        value.regions.forEach {
            if (it.combatTrack?.playlistUuid == null) {
                it.combatTrack = null
            }
        }
        currentSettings = value
        if (currentSettings.regions.isEmpty()) {
            addDefaultRegion()
        }
        null
    }

    private fun addDefaultRegion() {
        currentSettings.regions.push(
            RegionSetting(
                name = "New Region",
                zoneDc = 15,
                encounterDc = 12,
                level = 1,
                rollTableUuid = null,
                combatTrack = null,
                terrain = Terrain.PLAINS.toCamelCase(),
            )
        )
    }

}

suspend fun Track.play() {
    val track = trackUuid
    if (track != null) {
        fromUuidTypeSafe<PlaylistSound>(track)
            ?.typeSafeUpdate { playing = true }
    } else {
        fromUuidTypeSafe<Playlist>(playlistUuid)
            ?.playAll()
    }
}

suspend fun Track.stop() {
    val track = trackUuid
    if (track != null) {
        fromUuidTypeSafe<PlaylistSound>(track)
            ?.typeSafeUpdate { playing = false }
    } else {
        fromUuidTypeSafe<Playlist>(playlistUuid)
            ?.stopAll()
    }
}
