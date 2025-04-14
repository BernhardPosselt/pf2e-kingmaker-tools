package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SkillPicker
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toSkillContext
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.kingdom.RawKingdomEvent
import at.posselt.pfrpg2e.kingdom.RawKingdomEventOutcome
import at.posselt.pfrpg2e.kingdom.RawKingdomEventStage
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.toMutableRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.utils.deepClone
import js.core.Void
import js.objects.Object
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
external interface ModifyKingdomEventContext : ValidatedHandlebarsContext {
    val formRows: Array<FormElementContext>
}

@JsPlainObject
external interface ModifyKingdomEventData {
    val name: String
    val id: String
    val description: String
    val hex: Boolean
    val dangerous: Boolean
    val beneficial: Boolean
    val settlement: Boolean
    val continuous: Boolean
    val special: String?
    val resolution: String?
    val modifier: Int
    val location: String?
    val leader: String
    val criticalSuccessMsg: String
    val successMsg: String
    val failureMsg: String
    val criticalFailureMsg: String
}

@JsExport
class ModifyKingdomEventDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
            string("name")
            string("description")
            string("resolution", nullable = true)
            string("special", nullable = true)
            string("location", nullable = true)
            string("criticalSuccessMsg")
            string("successMsg")
            string("failureMsg")
            string("criticalFailureMsg")
            boolean("hex")
            boolean("dangerous")
            boolean("beneficial")
            boolean("settlement")
            boolean("continuous")
            int("modifier")
            enum<Leader>("leader")
        }
    }
}


class ModifyKingdomEvent(
    data: RawKingdomEvent? = null,
    private val afterSubmit: suspend (data: RawKingdomEvent) -> Unit,
) : FormApp<ModifyKingdomEventContext, ModifyKingdomEventData>(
    title = if (data == null) "Add Kingdom Event" else "Edit Kingdom Event",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ModifyKingdomEventDataModel::class.js,
    id = "kmModifyKingdomEvent",
    width = 500,
) {
    private val edit: Boolean = data != null
    private var current: RawKingdomEvent = data?.let(::deepClone) ?: RawKingdomEvent(
        id = data?.id ?: "",
        description = data?.description ?: "",
        name = data?.name ?: "",
        special = data?.special,
        resolution = data?.resolution,
        location = data?.location,
        modifier = data?.modifier ?: 0,
        modifiers = data?.modifiers,
        resolvedOn = data?.resolvedOn ?: arrayOf("criticalSuccess"),
        traits = data?.traits ?: emptyArray(),
        stages = data?.stages ?: arrayOf(
            RawKingdomEventStage(
                skills = arrayOf("agriculture"),
                leader = Leader.RULER.value,
                criticalSuccess = RawKingdomEventOutcome(
                    msg = "",
                ),
                success = RawKingdomEventOutcome(
                    msg = "",
                ),
                failure = RawKingdomEventOutcome(
                    msg = "",
                ),
                criticalFailure = RawKingdomEventOutcome(
                    msg = "",
                ),
            )
        ),
    )

    init {
        isFormValid = data != null
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "km-save" -> save()
            "edit-skills" -> KingdomSkillPicker(
                skillRanks = KingdomSkill.entries
                    .filter { it.value in (current.stages.getOrNull(0)?.skills ?: emptyArray()) }
                    .map { it.value to 0 }
                    .toMutableRecord(),
                includeProficiency = false,
                requireAtLeastOneSkill = true,
                onSave = {
                    current.stages[0].skills = Object.keys(it).map { it }.toTypedArray()
                    render()
                },
            ).launch()
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ModifyKingdomEventContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val stage = current.stages[0]
        checkNotNull(stage) {
            "No first stage for event ${current.id}"
        }
        ModifyKingdomEventContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            formRows = formContext(
                TextInput(
                    name = "id",
                    value = current.id,
                    label = "Id",
                    help = "Choose the same Id as an existing event to override it",
                    readonly = edit == true,
                    stacked = false,
                ),
                TextInput(
                    name = "name",
                    value = current.name,
                    label = "Title",
                    stacked = false,
                ),
                TextArea(
                    name = "description",
                    value = current.description,
                    label = "Description",
                    help = "Text should be wrapped in a <p></p> element",
                    stacked = false,
                ),
                SkillPicker(
                    context = toSkillContext(
                        hideProficiency = true,
                        skills = stage.skills
                            .map { it to 0 }
                            .toMutableRecord()),
                    stacked = false,
                ),
                TextInput(
                    name = "location",
                    value = current.location ?: "",
                    label = "Location",
                    required = false,
                    stacked = false,
                ),
                TextInput(
                    name = "special",
                    value = current.special ?: "",
                    label = "Special",
                    required = false,
                    stacked = false,
                ),
                TextInput(
                    name = "resolution",
                    value = current.resolution ?: "",
                    label = "Resolution",
                    required = false,
                    stacked = false,
                ),
                Select.range(
                    from = -4,
                    to = 4,
                    name = "modifier",
                    label = "Modifier",
                    value = current.modifier ?: 0,
                    stacked = false,
                ),
                CheckboxInput(
                    name = "hex",
                    label = "Trait: Hex",
                    value = "hex" in current.traits
                ),
                CheckboxInput(
                    name = "dangerous",
                    label = "Trait: Dangerous",
                    value = "dangerous" in current.traits
                ),
                CheckboxInput(
                    name = "beneficial",
                    label = "Trait: Beneficial",
                    value = "beneficial" in current.traits
                ),
                CheckboxInput(
                    name = "settlement",
                    label = "Trait: Settlement",
                    value = "settlement" in current.traits
                ),
                CheckboxInput(
                    name = "continuous",
                    label = "Trait: Continuous",
                    value = "continuous" in current.traits
                ),
                Select.fromEnum<Leader>(
                    name = "leader",
                    value = stage.leader.let { Leader.fromString(it) },
                    stacked = false,
                ),
                TextArea(
                    name = "criticalSuccessMsg",
                    value = stage.criticalSuccess?.msg ?: "",
                    label = "Critical Success Message",
                    help = "Text should be wrapped in a <p></p> element",
                    stacked = false,
                    required = false,
                ),
                TextArea(
                    name = "successMsg",
                    value = stage.success?.msg ?: "",
                    label = "Success Message",
                    help = "Text should be wrapped in a <p></p> element",
                    stacked = false,
                    required = false,
                ),
                TextArea(
                    name = "failureMsg",
                    value = stage.failure?.msg ?: "",
                    label = "Failure Message",
                    help = "Text should be wrapped in a <p></p> element",
                    stacked = false,
                    required = false,
                ),
                TextArea(
                    name = "criticalFailureMsg",
                    value = stage.criticalFailure?.msg ?: "",
                    label = "Critical Failure Message",
                    help = "Text should be wrapped in a <p></p> element",
                    stacked = false,
                    required = false,
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

    override fun onParsedSubmit(value: ModifyKingdomEventData): Promise<Void> = buildPromise {
        val stage = current.stages[0]
        checkNotNull(stage) {
            "Submitted stage is null"
        }
        val traits = listOfNotNull(
            if (value.hex) "hex" else null,
            if (value.dangerous) "dangerous" else null,
            if (value.beneficial) "beneficial" else null,
            if (value.settlement) "settlement" else null,
            if (value.continuous) "continuous" else null,
        ).toTypedArray()
        current = RawKingdomEvent(
            name = value.name,
            id = value.id.slugify(),
            description = value.description,
            location = value.location,
            special = value.special,
            resolution = value.resolution,
            modifier = value.modifier,
            resolvedOn = current.resolvedOn,
            traits = traits,
            stages = arrayOf(
                RawKingdomEventStage(
                    leader = value.leader,
                    skills = stage.skills,
                    criticalSuccess = RawKingdomEventOutcome(
                        msg = value.criticalSuccessMsg,
                        modifiers = stage.criticalSuccess?.modifiers,
                    ),
                    success = RawKingdomEventOutcome(
                        msg = value.successMsg,
                        modifiers = stage.success?.modifiers,
                    ),
                    failure = RawKingdomEventOutcome(
                        msg = value.failureMsg,
                        modifiers = stage.failure?.modifiers,
                    ),
                    criticalFailure = RawKingdomEventOutcome(
                        msg = value.criticalFailureMsg,
                        modifiers = stage.criticalFailure?.modifiers,
                    ),
                )
            ),
            modifiers = current.modifiers,
        )
        undefined
    }

}