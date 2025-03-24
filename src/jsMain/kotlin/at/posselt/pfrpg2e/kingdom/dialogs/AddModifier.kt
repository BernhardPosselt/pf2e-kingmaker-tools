package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.data.kingdom.KingdomAbility
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.RawExpression
import at.posselt.pfrpg2e.kingdom.RawIn
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.toLabel
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.formatAsModifier
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import js.array.tupleOf
import js.core.Void
import js.objects.JsPlainObject
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsExport
class ModifierModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("name")
            enum<ModifierType>("type")
            enum<KingdomAbility>("ability", nullable = true)
            enum<KingdomSkill>("skill", nullable = true)
            enum<KingdomPhase>("phase", nullable = true)
            string("activityId", nullable = true)
            boolean("enabled")
            int("turns") {
                min = 0
            }
            boolean("isConsumedAfterRoll")
            int("value")
        }
    }
}

@Suppress("unused")
@JsPlainObject
external interface AddModifierData {
    val name: String
    val type: String
    val ability: String?
    val skill: String?
    val phase: String?
    val activityId: String?
    val enabled: Boolean
    val turns: Int
    val isConsumedAfterRoll: Boolean
    val value: Int
}

@JsPlainObject
external interface AddModifierContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val formRows: Array<FormElementContext>
}

class AddModifier(
    private val activities: Array<RawActivity>,
    private val onSave: suspend (modifier: RawModifier) -> Unit,
) : FormApp<AddModifierContext, AddModifierData>(
    title = "Add Modifier",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifierModel::class.js,
    id = "kmAddModifier",
) {
    var data: AddModifierData = AddModifierData(
        name = "",
        type = ModifierType.UNTYPED.value,
        value = 1,
        ability = null,
        skill = null,
        phase = null,
        activityId = null,
        enabled = true,
        turns = 1,
        isConsumedAfterRoll = false,
    )

    init {
        isFormValid = false
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> {
                val predicates = mutableListOf<RawExpression<Boolean>>()
                if (data.phase != null) {
                    predicates.add(RawIn(`in` = tupleOf("@phase", arrayOf(data.phase))))
                }
                if (data.ability != null) {
                    predicates.add(RawIn(`in` = tupleOf("@ability", arrayOf(data.ability))))
                }
                if (data.skill != null) {
                    predicates.add(RawIn(`in` = tupleOf("@skill", arrayOf(data.skill))))
                }
                if (data.activityId != null) {
                    predicates.add(RawIn(`in` = tupleOf("@activity", arrayOf(data.activityId))))
                }
                val activity = activities.find { it.id == data.activityId }
                val buttonLabel = listOfNotNull(
                    data.value.formatAsModifier() + " ${data.type.toLabel()}",
                    data.phase?.let { "Phase: ${it.toLabel()}" },
                    data.ability?.let { "Ability: ${it.toLabel()}" },
                    data.skill?.let { "Skill: ${it.toLabel()}" },
                    activity?.let { "Activity: ${it.title}" },
                ).joinToString(", ")
                val modifier = RawModifier(
                    type = data.type,
                    value = data.value,
                    name = data.name,
                    buttonLabel = buttonLabel,
                    enabled = data.enabled,
                    turns = data.turns,
                    isConsumedAfterRoll = data.isConsumedAfterRoll,
                    applyIf = predicates.toTypedArray(),
                )
                buildPromise {
                    onSave(modifier)
                    close()
                }
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<AddModifierContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val rows = formContext(
            TextInput(
                name = "name",
                label = "Name",
                stacked = false,
                value = data.name
            ),
            Select.range(
                name = "value",
                label = "Value",
                value = data.value,
                from = -4,
                to = 4,
                stacked = false
            ),
            Select.fromEnum<ModifierType>(
                name = "type",
                label = "Type",
                value = ModifierType.fromString(data.type),
                stacked = false
            ),
            Select.fromEnum<KingdomAbility>(
                name = "ability",
                label = "Ability",
                value = data.ability?.let { KingdomAbility.fromString(it) },
                required = false,
                stacked = false
            ),
            Select.fromEnum<KingdomSkill>(
                name = "skill",
                label = "Skill",
                value = data.skill?.let { KingdomSkill.fromString(it) },
                required = false,
                stacked = false
            ),
            Select.fromEnum<KingdomPhase>(
                name = "phase",
                label = "Phase",
                value = data.phase?.let { KingdomPhase.fromString(it) },
                required = false,
                stacked = false
            ),
            Select(
                name = "activityId",
                label = "Activity",
                value = data.activityId,
                required = false,
                options = activities.map { SelectOption(label = it.title, value = it.id) },
                stacked = false
            ),
            CheckboxInput(
                name = "enabled",
                label = "Enabled",
                value = data.enabled,
            ),
            NumberInput(
                name = "turns",
                label = "Turns",
                value = data.turns,
                stacked = false,
                help = "Use 0 for indefinite"
            ),
            CheckboxInput(
                name = "isConsumedAfterRoll",
                label = "Remove After Roll",
                value = data.isConsumedAfterRoll,
            ),
        )
        AddModifierContext(
            partId = parent.partId,
            formRows = rows,
            isFormValid = isFormValid,
        )
    }

    override fun onParsedSubmit(value: AddModifierData): Promise<Void> = buildPromise {
        data = value
        null
    }
}
