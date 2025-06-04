package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.confirm
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.Menu
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Section
import at.posselt.pfrpg2e.app.forms.SectionsContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.resetCampsites
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.fromUuidsOfTypes
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.documents.Playlist
import com.foundryvtt.core.documents.PlaylistSound
import com.foundryvtt.core.utils.flattenObject
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.actor.PF2ELoot
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.actor.PF2EVehicle
import js.array.toTypedArray
import js.core.Void
import js.objects.Record
import js.objects.recordOf
import js.objects.unsafeJso
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


@JsPlainObject
external interface CampingSettings {
    var gunsToClean: Int
    var restRollMode: String
    var increaseWatchActorNumber: Int
    var actorUuidsNotKeepingWatch: Array<String>
    var huntAndGatherTargetActorUuid: String?
    var proxyRandomEncounterTableUuid: String?
    var randomEncounterRollMode: String
    var ignoreSkillRequirements: Boolean
    var minimumTravelSpeed: Int?
    var minimumSubsistence: Int
    var alwaysPerformActivities: Array<String>
    var restingPlaylistUuid: String?
    var restingPlaylistSoundUuid: String?
    var worldSceneId: String?
}

class CampingSettingsDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            int("gunsToClean")
            string("restRollMode") {
                choices = RestRollMode.entries.map { it.toCamelCase() }.toTypedArray()
            }
            string("worldSceneId", nullable = true)
            int("increaseWatchActorNumber")
            stringArray("actorUuidsNotKeepingWatch")
            stringArray("alwaysPerformActivities")
            string("huntAndGatherTargetActorUuid", nullable = true)
            string("proxyRandomEncounterTableUuid", nullable = true)
            string("randomEncounterRollMode")
            string("restingPlaylistUuid", nullable = true)
            string("restingPlaylistSoundUuid", nullable = true)
            boolean("ignoreSkillRequirements")
            int("minimumTravelSpeed")
            int("minimumSubsistence")
        }
    }
}

@JsPlainObject
external interface CampingSettingsContext : ValidatedHandlebarsContext, SectionsContext

enum class RestRollMode: ValueEnum, Translatable {
    NONE,
    ONE,
    ONE_EVERY_FOUR_HOURS;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "restRollMode.$value"
}

private val companionActivities = setOf(
    "blend-into-the-night",
    "bolster-confidence",
    "enhance-weapons",
    "healers-blessing",
    "intimidating-posture",
    "maintain-armor",
    "set-alarms",
    "set-traps",
    "undead-guardians",
    "water-hazards",
    "wilderness-survival",
)

@JsExport
class CampingSettingsApplication(
    private val game: Game,
    private val campingActor: CampingActor,
) : FormApp<CampingSettingsContext, CampingSettings>(
    title = t("camping.settings"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = CampingSettingsDataModel::class.js,
    id = "kmCampingSettings-${campingActor.uuid}",
    width = 600,
) {
    var settings: CampingSettings

    init {
        val camping = campingActor.getCamping()!!
        settings = CampingSettings(
            gunsToClean = camping.gunsToClean,
            restRollMode = camping.restRollMode,
            increaseWatchActorNumber = camping.increaseWatchActorNumber,
            actorUuidsNotKeepingWatch = camping.actorUuidsNotKeepingWatch,
            huntAndGatherTargetActorUuid = camping.huntAndGatherTargetActorUuid,
            proxyRandomEncounterTableUuid = camping.proxyRandomEncounterTableUuid,
            randomEncounterRollMode = camping.randomEncounterRollMode,
            ignoreSkillRequirements = camping.ignoreSkillRequirements,
            minimumTravelSpeed = camping.minimumTravelSpeed,
            minimumSubsistence = camping.cooking.minimumSubsistence,
            alwaysPerformActivities = camping.alwaysPerformActivityIds,
            restingPlaylistUuid = camping.restingTrack?.playlistUuid,
            restingPlaylistSoundUuid = camping.restingTrack?.trackUuid,
            worldSceneId = camping.worldSceneId,
        )
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CampingSettingsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val camping = campingActor.getCamping()!!
        val actors = fromUuidsOfTypes(
            camping.actorUuids,
            PF2ENpc::class,
            PF2ECharacter::class,
            PF2EVehicle::class,
            PF2ELoot::class,
        )
        val huntAndGatherUuids = (actors + listOfNotNull(campingActor))
            .mapNotNull { it.toOption(useUuid = true) }
        val uuidsNotKeepingWatch = setOf(*settings.actorUuidsNotKeepingWatch)
        val playlist = settings.restingPlaylistUuid?.let { fromUuidTypeSafe<Playlist>(it) }
        val playlistSound = settings.restingPlaylistSoundUuid?.let { fromUuidTypeSafe<PlaylistSound>(it) }
        val hexScenes = game.scenes
            .filter { it.grid.type == 2 }
            .sortedBy { it.name }
            .mapNotNull { it.id?.let { id -> SelectOption(label = it.name, value = id) } }
        CampingSettingsContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            sections = formContext(
                Section(
                    legend = t("camping.exploration"),
                    formRows = listOf(
                        Select(
                            label = t("camping.hexplorationScene"),
                            name = "worldSceneId",
                            help = t("camping.hexplorationSceneHelp"),
                            options = hexScenes,
                            required = false,
                            value = settings.worldSceneId,
                            stacked = false,
                        ),
                        Menu(
                            label = t("camping.campingPositions"),
                            name = t("camping.reset"),
                            value = "reset-camping-positions",
                            disabled = settings.worldSceneId == null,
                        ),
                        NumberInput(
                            name = "minimumTravelSpeed",
                            label = t("camping.minimumTravelSpeed"),
                            value = settings.minimumTravelSpeed ?: 0,
                            help = t("camping.minimumTravelSpeedHelp"),
                            stacked = false,
                        ),
                        Select.fromEnum<RollMode>(
                            name = "randomEncounterRollMode",
                            label = t("camping.randomEncounterRollMode"),
                            value = fromCamelCase<RollMode>(settings.randomEncounterRollMode),
                            labelFunction = { it.label },
                            stacked = false,
                        ),
                        Select(
                            name = "proxyRandomEncounterTableUuid",
                            value = settings.proxyRandomEncounterTableUuid,
                            label = t("camping.proxyRandomEncounterTableUuid"),
                            required = false,
                            options = game.tables.contents.mapNotNull { it.toOption(useUuid = true) },
                            help = t("camping.proxyRandomEncounterTableUuidHelp"),
                            stacked = false,
                        ),
                    )
                ),
                Section(
                    legend = t("camping.activities"),
                    formRows = listOf(
                        CheckboxInput(
                            name = "ignoreSkillRequirements",
                            label = t("camping.ignoreSkillRequirements"),
                            value = settings.ignoreSkillRequirements,
                        ),
                        Select(
                            name = "huntAndGatherTargetActorUuid",
                            value = settings.huntAndGatherTargetActorUuid,
                            label = t("camping.huntAndGatherTargetActorUuid"),
                            required = false,
                            options = huntAndGatherUuids,
                            help = t("camping.huntAndGatherTargetActorUuidHelp"),
                            stacked = false,
                        ),
                    )
                ),
                Section(
                    legend = t("camping.alwaysPerformedActivities"),
                    formRows = companionActivities.map { id ->
                        CheckboxInput(
                            label = t("campingActivities.$id.name"),
                            name = "alwaysPerformActivities.$id",
                            value = settings.alwaysPerformActivities.contains(id),
                            stacked = false,
                            help = t("camping.alwaysPerformActivities")
                        )
                    }
                ),
                Section(
                    legend = t("camping.cooking"),
                    formRows = listOf(
                        NumberInput(
                            name = "minimumSubsistence",
                            label = t("camping.minimumSubsistence"),
                            help = t("camping.minimumSubsistenceHelp"),
                            value = settings.minimumSubsistence,
                            stacked = false,
                        ),
                    )
                ),
                Section(
                    legend = t("camping.resting"),
                    formRows = listOf(
                        Select(
                            label = t("camping.playlist"),
                            name = "restingPlaylistUuid",
                            value = playlist?.uuid,
                            required = false,
                            stacked = false,
                            help = t("camping.playlistHelp"),
                            options = game.playlists.contents
                                .sortedBy { it.name }
                                .mapNotNull { it.toOption(useUuid = true) },
                        ),
                        Select(
                            label = t("camping.playlistTrack"),
                            name = "restingPlaylistSoundUuid",
                            value = playlistSound?.uuid,
                            required = false,
                            stacked = false,
                            options = playlist?.sounds?.contents?.mapNotNull { it.toOption(useUuid = true) }
                                ?: emptyList(),
                        ),
                        NumberInput(
                            name = "gunsToClean",
                            label = t("camping.gunsToClean"),
                            value = settings.gunsToClean,
                            stacked = false,
                            help = t("camping.gunsToCleanHelp")
                        ),
                        NumberInput(
                            name = "increaseWatchActorNumber",
                            label = t("camping.increaseWatchActorNumber"),
                            value = settings.increaseWatchActorNumber,
                            stacked = false,
                        ),
                        Select.fromEnum<RestRollMode>(
                            name = "restRollMode",
                            label = t("camping.restRollMode"),
                            value = fromCamelCase<RestRollMode>(settings.restRollMode),
                            stacked = false,
                        ),
                        *actors.mapIndexed { index, actor ->
                            CheckboxInput(
                                name = "actorUuidsNotKeepingWatch.${actor.uuid}",
                                label = t("camping.actorSkipWatch", recordOf("actorName" to actor.name)),
                                value = uuidsNotKeepingWatch.contains(actor.uuid),
                            )
                        }.toTypedArray()
                    )
                ),
            )
        )
    }

    override fun fixObject(value: dynamic) {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val actors = (value["actorUuidsNotKeepingWatch"] ?: unsafeJso()) as Record<String, Boolean>
        value["actorUuidsNotKeepingWatch"] = flattenObject(actors).asSequence()
            .filter { it.component2() == true }
            .map { it.component1() }
            .toTypedArray()
        val activities = (value["alwaysPerformActivities"] ?: unsafeJso()).unsafeCast<Record<String, Boolean>>()
        value["alwaysPerformActivities"] = flattenObject(activities).asSequence()
            .filter { it.component2() == true }
            .map { it.component1() }
            .toTypedArray()
    }

    override fun onParsedSubmit(value: CampingSettings): Promise<Void> = buildPromise {
        if (settings.restingPlaylistUuid != value.restingPlaylistUuid) {
            value.restingPlaylistSoundUuid = null
        }
        settings = value
        undefined
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (val action = target.dataset["action"]) {
            "km-save" -> {
                buildPromise {
                    campingActor.getCamping()?.let { camping ->
                        val alwaysPerformIds = settings.alwaysPerformActivities.toSet()
                        camping.gunsToClean = settings.gunsToClean
                        camping.restRollMode = settings.restRollMode
                        camping.increaseWatchActorNumber = settings.increaseWatchActorNumber
                        camping.actorUuidsNotKeepingWatch = settings.actorUuidsNotKeepingWatch
                        camping.huntAndGatherTargetActorUuid = settings.huntAndGatherTargetActorUuid
                        camping.proxyRandomEncounterTableUuid = settings.proxyRandomEncounterTableUuid
                        camping.randomEncounterRollMode = settings.randomEncounterRollMode
                        camping.ignoreSkillRequirements = settings.ignoreSkillRequirements
                        camping.minimumTravelSpeed = settings.minimumTravelSpeed
                        camping.cooking.minimumSubsistence = settings.minimumSubsistence
                        camping.alwaysPerformActivityIds = settings.alwaysPerformActivities
                        camping.restingTrack = settings.restingPlaylistUuid?.let {
                            Track(playlistUuid = it, trackUuid = settings.restingPlaylistSoundUuid)
                        }
                        camping.worldSceneId = settings.worldSceneId
                        camping.campingActivities = camping.campingActivities
                            .filter { it.activityId !in alwaysPerformIds }
                            .toTypedArray()
                        campingActor.setCamping(camping)
                    }
                    close()
                }
            }

            "reset-camping-positions" -> buildPromise {
                settings.worldSceneId
                    ?.let { worldSceneId -> game.scenes.get(worldSceneId) }
                    ?.let { scene ->
                        if (confirm(t("camping.confirmResetCampingPositions", recordOf("sceneName" to scene.name)))) {
                            scene.resetCampsites()
                        }
                    }
            }

            else -> console.log(action)
        }
    }
}