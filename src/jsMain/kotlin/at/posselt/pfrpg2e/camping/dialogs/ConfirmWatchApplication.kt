package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.camping.CampingData
import at.posselt.pfrpg2e.camping.getAllRecipes
import at.posselt.pfrpg2e.camping.getCampingActorsByUuid
import at.posselt.pfrpg2e.camping.previousRestSettings
import at.posselt.pfrpg2e.resting.getTotalRestDuration
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.pf2e.actor.PF2EActor
import js.core.Void
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@Suppress("unused")
@JsPlainObject
external interface ConfirmWatchContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
    val saveLabel: String
}

@JsExport
class ConfirmWatchDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            boolean("enableWatch")
            boolean("checkRandomEncounter")
            boolean("enableDailyPreparations")
            boolean("checkWeather")
        }
    }
}

@JsPlainObject
external interface ConfirmWatchData {
    val enableWatch: Boolean
    val checkRandomEncounter: Boolean
    val enableDailyPreparations: Boolean
    val checkWeather: Boolean
}

@JsExport
class ConfirmWatchApplication(
    private val game: Game,
    private val camping: CampingData,
    private val afterSubmit: (
        enableWatch: Boolean,
        enableDailyPreparations: Boolean,
        checkRandomEncounter: Boolean,
        checkWeather: Boolean,
    ) -> Unit,
) : FormApp<ConfirmWatchContext, ConfirmWatchData>(
    title = t("camping.beginRest"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ConfirmWatchDataModel::class.js,
    width = 400,
    id = "kmConfirmWatch"
) {
    private val enableWeather = game.settings.pfrpg2eKingdomCampingWeather.getEnableWeather()
    private var enableWatch: Boolean = !camping.previousRestSettings().skipWatch
    private var enableDailyPreparations: Boolean = !camping.previousRestSettings().skipDailyPreparations
    private var checkRandomEncounter: Boolean = !camping.previousRestSettings().disableRandomEncounter
    private var checkWeather: Boolean = if(enableWeather) !camping.previousRestSettings().skipWeather else false

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> buildPromise {
                close()
                afterSubmit(enableWatch, enableDailyPreparations, checkRandomEncounter, checkWeather)
            }
        }
    }

    private suspend fun calculateWatch(): String {
        val actorsByUuid = getCampingActorsByUuid(camping.actorUuids).associateBy(PF2EActor::uuid)
        val fullRestDuration = getTotalRestDuration(
            watchers = actorsByUuid.values.filter { !camping.actorUuidsNotKeepingWatch.contains(it.uuid) },
            recipes = camping.getAllRecipes().toList(),
            gunsToClean = camping.gunsToClean,
            increaseActorsKeepingWatch = camping.increaseWatchActorNumber,
            remainingSeconds = camping.watchSecondsRemaining,
            skipWatch = !enableWatch,
            skipDailyPreparations = !enableDailyPreparations,
        )
        return fullRestDuration.total.label
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ConfirmWatchContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val calculateWatch = calculateWatch()
        ConfirmWatchContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            saveLabel = t("camping.confirmRest", recordOf("duration" to calculateWatch)),
            formRows = formContext(
                CheckboxInput(
                    label = t("camping.enableWatch"),
                    name = "enableWatch",
                    help = t("camping.enableWatchHelp"),
                    stacked = false,
                    value = enableWatch,
                ),
                CheckboxInput(
                    label = t("camping.enableDailyPreparations"),
                    name = "enableDailyPreparations",
                    help = t("camping.enableDailyPreparationsHelp"),
                    stacked = false,
                    value = enableDailyPreparations,
                ),
                CheckboxInput(
                    label = t("camping.checkRandomEncounter"),
                    name = "checkRandomEncounter",
                    help = t("camping.checkRandomEncounterHelp"),
                    stacked = false,
                    value = checkRandomEncounter,
                ),
                CheckboxInput(
                    label = t("camping.checkWeather"),
                    name = "checkWeather",
                    stacked = false,
                    value = checkWeather,
                    disabled = !enableWeather
                ),
            )
        )
    }

    override fun onParsedSubmit(value: ConfirmWatchData): Promise<Void> = buildPromise {
        enableWatch = value.enableWatch
        enableDailyPreparations = value.enableDailyPreparations
        checkRandomEncounter = value.checkRandomEncounter
        checkWeather = value.checkWeather
        undefined
    }

}