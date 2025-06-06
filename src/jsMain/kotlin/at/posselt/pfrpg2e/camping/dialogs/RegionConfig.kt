package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.data.regions.Terrain
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.typeSafeUpdate
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.PlaylistSound
import com.foundryvtt.core.game
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.collections.plus
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

@Suppress("unused")
@JsPlainObject
external interface RegionSettingsContext : ValidatedHandlebarsContext {
    var heading: Array<TableHead>
    var formRows: Array<Array<FormElementContext>>
    var allowDelete: Boolean
}

@JsExport
class RegionSettingsDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
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


@JsExport
@JsName("RegionConfig")
class RegionConfig(
    private val actor: CampingActor,
) : FormApp<RegionSettingsContext, RegionSettings>(
    title = t("camping.regions"),
    width = 1200,
    template = "applications/settings/configure-regions.hbs",
    dataModel = RegionSettingsDataModel::class.js,
    debug = true,
    id = "kmRegions-${actor.uuid}"
) {
    private var currentSettings = actor.getCamping()!!.regionSettings

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (val action = target.dataset["action"]) {
            "km-save" -> {
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
        val rollTableOptions = game.tables.contents
            .mapNotNull { it.toOption(useUuid = true) }
            .sortedBy { it.label }
        RegionSettingsContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            heading = arrayOf(
                TableHead(t("applications.name")),
                TableHead(t("applications.level"), arrayOf("number-select-heading")),
                TableHead(t("camping.terrain")),
                TableHead(t("camping.zoneDc"), arrayOf("number-select-heading")),
                TableHead(t("camping.encounterDc"), arrayOf("number-select-heading")),
                TableHead(t("camping.rollTable")),
                TableHead(t("camping.combatPlaylist")),
                TableHead(t("camping.combatTrack")),
                TableHead(t("applications.delete"), arrayOf("small-heading"))
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
                        label = t("applications.name"),
                        value = row.name,
                        hideLabel = true
                    ).toContext(),
                    Select.level(
                        name = "regions.$index.level",
                        value = row.level,
                        hideLabel = true
                    ).toContext(),
                    Select.fromEnum<Terrain>(
                        name = "regions.$index.terrain",
                        value = fromCamelCase<Terrain>(row.terrain),
                        hideLabel = true
                    ).toContext(),
                    Select.dc(
                        name = "regions.$index.zoneDc",
                        label = t("camping.zoneDc"),
                        value = row.zoneDc,
                        hideLabel = true
                    ).toContext(),
                    Select.flatCheck(
                        name = "regions.$index.encounterDc",
                        label = t("camping.encounterDc"),
                        value = row.encounterDc,
                        hideLabel = true,
                    ).toContext(),
                    Select(
                        name = "regions.$index.rollTableUuid",
                        label = t("camping.rollTable"),
                        value = row.rollTableUuid,
                        required = false,
                        hideLabel = true,
                        options = rollTableOptions,
                    ).toContext(),
                    Select(
                        name = "regions.$index.combatTrack.playlistUuid",
                        label = t("camping.combatPlaylist"),
                        value = row.combatTrack?.playlistUuid,
                        required = false,
                        hideLabel = true,
                        options = playlistOptions
                    ).toContext(),
                    Select(
                        name = "regions.$index.combatTrack.trackUuid",
                        label = t("camping.combatTrack"),
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
        currentSettings.regions = currentSettings.regions + RegionSetting(
            name = t("camping.newRegion"),
            zoneDc = 15,
            encounterDc = 12,
            level = 1,
            rollTableUuid = null,
            combatTrack = null,
            terrain = Terrain.PLAINS.toCamelCase(),
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
