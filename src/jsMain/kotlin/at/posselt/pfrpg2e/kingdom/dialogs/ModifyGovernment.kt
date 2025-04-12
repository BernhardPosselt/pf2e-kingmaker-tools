package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.RawFeat
import at.posselt.pfrpg2e.kingdom.RawGovernment
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface ModifyGovernmentContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyGovernmentData {
    val name: String
    val id: String
    val description: String
    val boost: String?
    val freeBoosts: Int
    val bonusFeat: String
    val skillProficiencies: Array<Boolean>
    val boosts: Array<Boolean>
}

@JsExport
class ModifyGovernmentDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("description")
            string("bonusFeat")
            int("freeBoosts")
            booleanArray("skillProficiencies")
            booleanArray("boosts")
        }
    }
}


class ModifyGovernment(
    data: RawGovernment? = null,
    private val feats: Array<RawFeat>,
    private val afterSubmit: suspend (data: RawGovernment) -> Unit,
) : FormApp<ModifyGovernmentContext, ModifyGovernmentData>(
    title = if (data == null) "Add Government" else "Edit Government",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyGovernmentDataModel::class.js,
    id = "kmModifyGovernment"
) {
    private val edit: Boolean = data != null
    private var current: RawGovernment = data?.let(::deepClone) ?: RawGovernment(
        name = data?.name ?: "",
        id = data?.id ?: "",
        description = data?.description ?: "",
        freeBoosts = data?.freeBoosts ?: 1,
        boosts = data?.boosts ?: emptyArray(),
        bonusFeat = data?.bonusFeat ?: feats.map { it.id }.firstOrNull() ?: "",
        skillProficiencies = data?.skillProficiencies ?: emptyArray()
    )

    init {
        isFormValid = data != null
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> save()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ModifyGovernmentContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyGovernmentContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    stacked = false,
                    name = "id",
                    value = current.id,
                    label = "Id",
                    help = "Choose the same Id as an existing government to override it",
                    readonly = edit == true,
                ),
                TextInput(
                    stacked = false,
                    name = "name",
                    value = current.name,
                    label = "Name",
                ),
                TextArea(
                    stacked = false,
                    name = "description",
                    value = current.description,
                    label = "Description",
                ),
                NumberInput(
                    stacked = false,
                    name = "freeBoosts",
                    value = current.freeBoosts,
                    label = "Free Boosts",
                ),
                Select(
                    stacked = false,
                    name = "bonusFeat",
                    value = current.bonusFeat,
                    label = "Bonus Feat",
                    options = feats.map { SelectOption(it.name, it.id) },
                ),
            ) + KingdomAbility.entries.mapIndexed { index, it ->
                CheckboxInput(
                    name = "boosts.$index",
                    value = it.value in current.boosts,
                    label = "Boost: ${t(it)}",
                ).toContext()
            }.toTypedArray() + KingdomSkill.entries.mapIndexed { index, it ->
                CheckboxInput(
                    name = "skillProficiencies.$index",
                    value = it.value in current.skillProficiencies,
                    label = "Skill Training: ${t(it)}",
                ).toContext()
            }.toTypedArray()
        )
    }

    fun save(): Promise<Void> = buildPromise {
        if (isValid()) {
            close().await()
            afterSubmit(current)
        }
        undefined
    }

    override fun onParsedSubmit(value: ModifyGovernmentData): Promise<Void> = buildPromise {
        current = RawGovernment(
            name = value.name,
            id = value.id.slugify(),
            freeBoosts = value.freeBoosts,
            description = value.description,
            boosts = KingdomAbility.entries.zip(value.boosts)
                .filter { (_, enabled) -> enabled }
                .map { (value, _) -> value.value }
                .toTypedArray(),
            skillProficiencies = KingdomSkill.entries.zip(value.skillProficiencies)
                .filter { (_, enabled) -> enabled }
                .map { (value, _) -> value.value }
                .toTypedArray(),
            bonusFeat = value.bonusFeat,
        )
        undefined
    }

}