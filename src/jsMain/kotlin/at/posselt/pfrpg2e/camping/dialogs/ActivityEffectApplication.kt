package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.camping.ActivityEffect
import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.item.PF2EEffect
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

class ActivityEffectDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("uuid")
            string("target") {
                choices = ActivityEffectTarget.entries.map { it.toCamelCase() }.toTypedArray()
            }
            boolean("doublesHealing")
        }
    }
}

@JsPlainObject
external interface ActivityEffectContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

enum class ActivityEffectTarget: ValueEnum, Translatable {
    ALLIES,
    ALL,
    SELF;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "activityEffectTarget.$value"
}

@JsPlainObject
external interface ActivityEffectSubmitData {
    val uuid: String
    val target: String
    val doublesHealing: Boolean
}


@JsExport
class ActivityEffectApplication(
    private val game: Game,
    data: ActivityEffect? = null,
    private val afterSubmit: (ActivityEffect) -> Unit,
) : FormApp<ActivityEffectContext, ActivityEffectSubmitData>(
    title = if (data == null) t("camping.addEffect") else t("camping.editEffect"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ActivityEffectDataModel::class.js,
    width = 400,
    id = "kmActivityEffect"
) {
    var currentActivityEffect: ActivityEffect? = data?.let(::deepClone)

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "openDocumentLink" -> buildPromise {
                event.preventDefault()
                event.stopPropagation()
                target.dataset["uuid"]?.let { fromUuidTypeSafe<PF2EEffect>(it)?.sheet?.launch() }
            }

            "km-save" -> save()
        }
    }

    fun save(): Promise<Void> = buildPromise {
        val effect = currentActivityEffect
        if (isValid() && effect != null) {
            close().await()
            afterSubmit(effect)
        }
        undefined
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ActivityEffectContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val effects = game.items.contents
            .filterIsInstance<PF2EEffect>()
        val item = currentActivityEffect?.uuid?.let { fromUuidTypeSafe<PF2EEffect>(it) }
            ?: effects.firstOrNull()
        if (currentActivityEffect == null) {
            currentActivityEffect = ActivityEffect(
                uuid = item?.uuid ?: "",
                target = "all",
                doublesHealing = false
            )
        }
        ActivityEffectContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                Select(
                    label = t("camping.effect"),
                    name = "uuid",
                    stacked = false,
                    options = effects.mapNotNull { it.toOption(useUuid = true) },
                    item = item,
                    value = item?.uuid,
                ),
                Select.fromEnum<ActivityEffectTarget>(
                    label = t("camping.target"),
                    name = "target",
                    stacked = false,
                    value = currentActivityEffect?.target?.let { fromCamelCase<ActivityEffectTarget>(it) }
                        ?: ActivityEffectTarget.ALL,
                    help = t("camping.targetHelp"),
                ),
                CheckboxInput(
                    name = "doublesHealing",
                    label = t("camping.doublesHealing"),
                    stacked = false,
                    value = currentActivityEffect?.doublesHealing == true,
                )
            )
        )
    }

    override fun onParsedSubmit(value: ActivityEffectSubmitData): Promise<Void> = buildPromise {
        currentActivityEffect = ActivityEffect(
            uuid = value.uuid,
            target = value.target,
            doublesHealing = value.doublesHealing,
        )
        undefined
    }

}