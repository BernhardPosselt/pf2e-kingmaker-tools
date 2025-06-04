package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.kingdom.RawCharter
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
external interface ModifyCharterContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyCharterData {
    val name: String
    val id: String
    val description: String
    val boost: String?
    val flaw: String?
    val freeBoosts: Int
}

class ModifyCharterDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("description")
            enum<KingdomAbility>("boost", nullable = true)
            enum<KingdomAbility>("flaw", nullable = true)
            int("freeBoosts")
        }
    }
}


class ModifyCharter(
    data: RawCharter? = null,
    private val afterSubmit: suspend (data: RawCharter) -> Unit,
) : FormApp<ModifyCharterContext, ModifyCharterData>(
    title = if (data == null) t("kingdom.addCharter") else t("kingdom.editCharter"),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyCharterDataModel::class.js,
    id = "kmModifyCharter"
) {
    private val edit: Boolean = data != null
    private var current: RawCharter = data?.let(::deepClone) ?: RawCharter(
        name = data?.name ?: "",
        id = data?.id ?: "",
        description = data?.description ?: "",
        boost = data?.boost,
        flaw = data?.flaw,
        freeBoosts = data?.freeBoosts ?: 1,
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
    ): Promise<ModifyCharterContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        ModifyCharterContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = t("applications.id"),
                    help = t("kingdom.overrideExistingElement"),
                    readonly = edit == true,
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = t("applications.name"),
                ),
                TextArea(
                    name = "description",
                    value = current.description,
                    label = t("applications.description"),
                ),
                Select.fromEnum<KingdomAbility>(
                    name = "boost",
                    value = current.boost?.let { KingdomAbility.fromString(it) },
                    label = t("kingdom.boost"),
                    required = false,
                ),
                Select.fromEnum<KingdomAbility>(
                    name = "flaw",
                    value = current.flaw?.let { KingdomAbility.fromString(it) },
                    label = t("kingdom.flaw"),
                    required = false,
                ),
                NumberInput(
                    name = "freeBoosts",
                    value = current.freeBoosts,
                    label = t("kingdom.freeBoosts"),
                ),
            )
        )
    }

    fun save(): Promise<Void> = buildPromise {
        if (isValid()) {
            close().await()
            afterSubmit(current)
        }
        undefined
    }

    override fun onParsedSubmit(value: ModifyCharterData): Promise<Void> = buildPromise {
        current = RawCharter(
            name = value.name,
            id = value.id.slugify(),
            boost = value.boost,
            description = value.description,
            flaw = value.flaw,
            freeBoosts = value.freeBoosts,
        )
        undefined
    }

}