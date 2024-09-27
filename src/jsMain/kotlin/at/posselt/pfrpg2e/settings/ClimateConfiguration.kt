package at.posselt.pfrpg2e.settings

import at.posselt.pfrpg2e.app.*
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.camping.dialogs.TableHead
import at.posselt.pfrpg2e.data.regions.Month
import at.posselt.pfrpg2e.data.regions.Season
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.*
import com.foundryvtt.core.data.dsl.buildSchema
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

fun getDefaultMonths(): Array<ClimateSetting> = arrayOf(
    ClimateSetting(season = Season.WINTER.toCamelCase(), precipitationDc = 8, coldDc = 16, weatherEventDc = 18),
    ClimateSetting(season = Season.WINTER.toCamelCase(), precipitationDc = 8, coldDc = 18, weatherEventDc = 18),
    ClimateSetting(season = Season.SPRING.toCamelCase(), precipitationDc = 15, weatherEventDc = 18),
    ClimateSetting(season = Season.SPRING.toCamelCase(), precipitationDc = 15, weatherEventDc = 18),
    ClimateSetting(season = Season.SPRING.toCamelCase(), precipitationDc = 15, weatherEventDc = 18),
    ClimateSetting(season = Season.SUMMER.toCamelCase(), precipitationDc = 20, weatherEventDc = 18),
    ClimateSetting(season = Season.SUMMER.toCamelCase(), precipitationDc = 20, weatherEventDc = 18),
    ClimateSetting(season = Season.SUMMER.toCamelCase(), precipitationDc = 20, weatherEventDc = 18),
    ClimateSetting(season = Season.FALL.toCamelCase(), precipitationDc = 15, weatherEventDc = 18),
    ClimateSetting(season = Season.FALL.toCamelCase(), precipitationDc = 15, weatherEventDc = 18),
    ClimateSetting(season = Season.FALL.toCamelCase(), precipitationDc = 15, weatherEventDc = 18),
    ClimateSetting(season = Season.WINTER.toCamelCase(), precipitationDc = 8, coldDc = 18, weatherEventDc = 18),
)

@JsPlainObject
external interface ClimateSetting {
    val coldDc: Int?
    val precipitationDc: Int?
    val season: String
    val weatherEventDc: Int?
}

@JsPlainObject
external interface ClimateSettings {
    var months: Array<ClimateSetting>
}

@JsPlainObject
external interface ClimateSettingsContext : HandlebarsRenderContext {
    var heading: Array<TableHead>
    var formRows: Array<Array<Any>>
    var isValid: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class ClimateConfigurationDataModel(
    value: AnyObject? = undefined,
    context: DocumentConstructionContext? = undefined,
) : DataModel(value, context) {
    companion object {
        @Suppress("unused")
        @OptIn(ExperimentalJsStatic::class)
        @JsStatic
        fun defineSchema() = buildSchema {
            array("months") {
                options {
                    initial = getDefaultMonths()
                }
                schema {
                    int("coldDc", nullable = true)
                    int("precipitationDc", nullable = true)
                    int("weatherEventDc", nullable = true)
                    string("season")
                }
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class ClimateConfiguration : FormApp<ClimateSettingsContext, ClimateSettings>(
    title = "Climate",
    width = 1024,
    template = "applications/settings/configure-climate.hbs",
    debug = true,
    dataModel = ClimateConfigurationDataModel::class.js,
    id = "kmClimate",
) {
    private var currentSettings = game.settings.pfrpg2eKingdomCampingWeather.getClimateSettings()

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (val action = target.dataset["action"]) {
            "save" -> {
                buildPromise {
                    game.settings.pfrpg2eKingdomCampingWeather.setClimateSettings(currentSettings)
                    close()
                }
            }

            else -> console.log(action)
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ClimateSettingsContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ClimateSettingsContext(
            partId = parent.partId,
            isValid = isFormValid,
            heading = arrayOf(
                TableHead("Month"),
                TableHead("Season"),
                TableHead("Cold DC", arrayOf("number-select-heading")),
                TableHead("Precipitation DC", arrayOf("number-select-heading")),
                TableHead("Weather Event DC", arrayOf("number-select-heading")),
            ),
            formRows = currentSettings.months.mapIndexed { index, row ->
                val month = Month.entries[index]
                arrayOf(
                    month.toLabel(),
                    Select.fromEnum<Season>(
                        name = "months.$index.season",
                        label = "Season",
                        value = Season.entries.find { it.toCamelCase() == row.season },
                        hideLabel = true
                    ).toContext(),
                    Select.flatCheck(
                        name = "months.$index.coldDc",
                        label = "Cold DC",
                        value = row.coldDc,
                        hideLabel = true,
                        required = false,
                    ).toContext(),
                    Select.flatCheck(
                        name = "months.$index.precipitationDc",
                        label = "Precipitation DC",
                        value = row.precipitationDc,
                        hideLabel = true,
                        required = false,
                    ).toContext(),
                    Select.flatCheck(
                        name = "months.$index.weatherEventDc",
                        label = "Precipitation DC",
                        value = row.weatherEventDc,
                        hideLabel = true,
                        required = false,
                    ).toContext(),
                )
            }.toTypedArray()
        )
    }

    override fun fixObject(value: dynamic) {
        value["months"] = (value["months"] as Array<ClimateSetting>?) ?: getDefaultMonths()
    }

    override fun onParsedSubmit(value: ClimateSettings) = buildPromise {
        currentSettings = value
        null
    }
}