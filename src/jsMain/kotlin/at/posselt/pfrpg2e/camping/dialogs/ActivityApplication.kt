package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.*
import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.forms.ActivityEffects
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.SectionContext
import at.posselt.pfrpg2e.app.forms.SectionsContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SkillPicker
import at.posselt.pfrpg2e.app.forms.TextArea
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.forms.toActivityEffectContext
import at.posselt.pfrpg2e.app.forms.toOption
import at.posselt.pfrpg2e.app.forms.toSkillContext
import at.posselt.pfrpg2e.app.launchCampingSkillPicker
import at.posselt.pfrpg2e.camping.*
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.openItem
import at.posselt.pfrpg2e.utils.without
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.actor.PF2ENpc
import com.foundryvtt.pf2e.item.PF2EEffect
import js.array.push
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.String
import kotlin.js.Promise


@JsPlainObject
external interface ActivityContext : SectionsContext, HandlebarsRenderContext {
    val isFormValid: Boolean
}

@JsPlainObject
external interface ActivityOutcomeSubmitData {
    val message: String?
    val modifyRandomEncounterDc: ModifyEncounterDc
    val checkRandomEncounter: Boolean
}

@JsPlainObject
external interface ActivitySubmitData {
    val name: String
    val journalUuid: String
    val journalEntryUuid: String?
    val dc: String?
    val modifyRandomEncounterDc: ModifyEncounterDc
    val isSecret: Boolean
    val criticalSuccess: ActivityOutcomeSubmitData
    val success: ActivityOutcomeSubmitData
    val failure: ActivityOutcomeSubmitData
    val criticalFailure: ActivityOutcomeSubmitData
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class ActivityDataModel(value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @OptIn(ExperimentalJsStatic::class)
        @JsStatic
        fun defineSchema() = buildSchema {
            string("name")
            boolean("isSecret")
            string("journalUuid", nullable = true)
            string("journalEntryUuid", nullable = true)
            schema("modifyRandomEncounterDc") {
                int("day")
                int("night")
            }
            schema("criticalSuccess") {
                string("message", nullable = true)
                boolean("checkRandomEncounter")
                schema("modifyRandomEncounterDc") {
                    int("day")
                    int("night")
                }
            }
            schema("success") {
                string("message", nullable = true)
                boolean("checkRandomEncounter")
                schema("modifyRandomEncounterDc") {
                    int("day")
                    int("night")
                }
            }
            schema("failure") {
                string("message", nullable = true)
                boolean("checkRandomEncounter")
                schema("modifyRandomEncounterDc") {
                    int("day")
                    int("night")
                }
            }
            schema("criticalFailure") {
                string("message", nullable = true)
                boolean("checkRandomEncounter")
                schema("modifyRandomEncounterDc") {
                    int("day")
                    int("night")
                }
            }
        }
    }
}


private data class Journals(
    val entry: JournalEntry,
    val page: JournalEntryPage? = null
)

@OptIn(ExperimentalJsExport::class)
@JsExport
class ActivityApplication(
    private val game: Game,
    private val actor: PF2ENpc,
    data: CampingActivityData? = null,
    private val afterSubmit: () -> Unit,
) : FormApp<ActivityContext, ActivitySubmitData>(
    title = if (data == null) "Add Activity" else "Edit Activity: ${data.name}",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ActivityDataModel::class.js,
    id = "kmActivity"
) {
    private val editActivityName = data?.name
    private val editActivityLocked = data?.isLocked
    private var currentActivity: CampingActivityData = data?.let(::deepClone) ?: CampingActivityData(
        name = "",
        journalUuid = null,
        skills = emptyArray<CampingSkill>(),
        modifyRandomEncounterDc = null,
        isSecret = false,
        isLocked = false,
        effectUuids = emptyArray(),
        isHomebrew = true,
        criticalSuccess = null,
        success = null,
        failure = null,
        criticalFailure = null,
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "openDocumentLink" -> buildPromise {
                event.preventDefault()
                event.stopPropagation()
                target.dataset["uuid"]?.let { fromUuidTypeSafe<PF2EEffect>(it)?.sheet?.launch() }
            }

            "save" -> save()
            "edit-skills" -> launchCampingSkillPicker(currentActivity.getCampingSkills(expandAny = false)) {
                currentActivity.skills = it

                render()
            }

            "open-item" -> {
                event.preventDefault()
                event.stopPropagation()
                buildPromise {
                    target.dataset["uuid"]?.let { openItem(it) }
                }
            }

            "add-effect" -> target.dataset["section"]?.let { section ->
                ActivityEffectApplication(
                    game = game,
                    afterSubmit = { createEffectAt(section, it) }
                ).launch()
            }

            "delete-effect" -> {
                val index = target.dataset["index"]?.toInt()
                val section = target.dataset["section"]
                if (index != null) {
                    removeEffectAt(section, index)
                }
            }

            "edit-effect" -> {
                val index = target.dataset["index"]?.toInt()
                val section = target.dataset["section"]
                val data = index?.let { getEffectsSection(section)?.get(it) }
                if (data != null) {
                    ActivityEffectApplication(
                        game = game,
                        data = data,
                        afterSubmit = { update ->
                            data.uuid = update.uuid
                            data.doublesHealing = update.doublesHealing
                            data.target = update.target
                            render()
                        }
                    ).launch()
                }
            }
        }
    }

    private fun removeEffectAt(section: String?, index: Int) {
        if (section == "effects") {
            currentActivity.effectUuids = currentActivity
                .effectUuids
                ?.without(index)
        } else if (section == "criticalSuccess") {
            currentActivity.criticalSuccess?.effectUuids = currentActivity
                .criticalSuccess
                ?.effectUuids
                ?.without(index)
        } else if (section == "success") {
            currentActivity.success?.effectUuids = currentActivity
                .success
                ?.effectUuids
                ?.without(index)
        } else if (section == "failure") {
            currentActivity.failure?.effectUuids = currentActivity
                .failure
                ?.effectUuids
                ?.without(index)
        } else if (section == "criticalFailure") {
            currentActivity.criticalFailure?.effectUuids = currentActivity
                .criticalFailure
                ?.effectUuids
                ?.without(index)
        }
        render()
    }

    private fun createEffectAt(section: String, effect: ActivityEffect) {
        console.log(section, effect)
        getEffectsSection(section)?.push(effect)
        console.log(currentActivity)
        render()
    }

    private fun getEffectsSection(section: String?): Array<ActivityEffect>? =
        if (section == "effects") {
            currentActivity.effectUuids
        } else if (section == "criticalSuccess") {
            currentActivity.criticalSuccess?.effectUuids
        } else if (section == "success") {
            currentActivity.success?.effectUuids
        } else if (section == "failure") {
            currentActivity.failure?.effectUuids
        } else if (section == "criticalFailure") {
            currentActivity.criticalFailure?.effectUuids
        } else {
            null
        }


    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<ActivityContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val effects = game.items.contents
            .filterIsInstance<PF2EEffect>()
        val journal = currentActivity.journalUuid
            ?.let {
                when (val journal = fromUuidTypeSafe<JournalEntryPage>(it) ?: fromUuidTypeSafe<JournalEntry>(it)) {
                    is JournalEntryPage -> Journals(journal.parent!!, journal)
                    is JournalEntry -> Journals(journal)
                    else -> game.journal.contents
                        .firstOrNull()
                        ?.let { Journals(it) }
                }
            }
        val hasCheck = currentActivity.requiresACheck()
        ActivityContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            sections = arrayOf(
                SectionContext(
                    legend = "Basic",
                    formRows = formContext(
                        TextInput(
                            stacked = false,
                            label = "Name",
                            name = "name",
                            disabled = editActivityName != null,
                            value = currentActivity.name,
                            required = true,
                            help = "To override an existing activity, use the same name",
                        ),
                        Select(
                            label = "Journal",
                            name = "journalUuid",
                            value = journal?.entry?.uuid,
                            required = false,
                            options = game.journal.contents.mapNotNull { it.toOption(useUuid = true) },
                            stacked = false,
                        ),
                        Select(
                            label = "Journal Entry",
                            name = "journalEntryUuid",
                            required = false,
                            value = journal?.page?.uuid,
                            options = journal?.entry?.pages?.contents?.mapNotNull { it.toOption(useUuid = true) }
                                ?: emptyList(),
                            stacked = false,
                        ),
                        SkillPicker(
                            context = toSkillContext(currentActivity.skills ?: emptyArray()),
                            stacked = false,
                        ),
                        CheckboxInput(
                            label = "Secret Check",
                            name = "isSecret",
                            value = currentActivity.isSecret ?: false,
                        ),
                    ),
                ),
                SectionContext(
                    legend = "When Performed",
                    formRows = formContext(
                        *createEncounterModifierInputs(
                            dc = currentActivity.modifyRandomEncounterDc,
                        ),
                        ActivityEffects(
                            value = toActivityEffectContext(
                                allEffects = effects.toTypedArray(),
                                section = "effects",
                                effects = currentActivity.effectUuids ?: emptyArray()
                            )
                        ),
                    )
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = "Critical Success",
                    formRows = createActivityEffectInputs(
                        namePrefix = "criticalSuccess.",
                        outcome = currentActivity.criticalSuccess,
                        allEffects = effects,
                    ),
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = "Success",
                    formRows = createActivityEffectInputs(
                        namePrefix = "success.",
                        outcome = currentActivity.success,
                        allEffects = effects,
                    ),
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = "Failure",
                    formRows = createActivityEffectInputs(
                        namePrefix = "failure.",
                        outcome = currentActivity.failure,
                        allEffects = effects,
                    ),
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = "Critical Failure",
                    formRows = createActivityEffectInputs(
                        namePrefix = "criticalFailure.",
                        outcome = currentActivity.criticalFailure,
                        allEffects = effects,
                    ),
                ),
            )
        )
    }


    fun save(): Promise<Void> = buildPromise {
        if (isValid()) {
            actor.getCamping()?.let { camping ->
                currentActivity.let { data ->
                    camping.homebrewCampingActivities = camping.homebrewCampingActivities
                        .filter { it.name != data.name }
                        .toTypedArray()
                    camping.homebrewCampingActivities.push(data)
                    camping.campingActivities = camping.campingActivities
                        .filter { it.activity != data.name }
                        .toTypedArray()
                    actor.setCamping(camping)
                    close().await()
                    afterSubmit()
                }
            }
        }
        undefined
    }

    override fun onParsedSubmit(value: ActivitySubmitData): Promise<Void> = buildPromise {
        currentActivity = CampingActivityData(
            name = editActivityName ?: value.name,
            journalUuid = value.journalEntryUuid ?: value.journalUuid,
            skills = currentActivity.skills,
            modifyRandomEncounterDc = value.modifyRandomEncounterDc,
            isSecret = value.isSecret,
            isLocked = editActivityLocked == true,
            effectUuids = currentActivity.effectUuids,
            isHomebrew = true,
            criticalSuccess = parseOutcome(currentActivity.criticalSuccess, value.criticalSuccess),
            success = parseOutcome(currentActivity.success, value.success),
            failure = parseOutcome(currentActivity.failure, value.failure),
            criticalFailure = parseOutcome(currentActivity.criticalFailure, value.criticalFailure),
        )
        undefined
    }

}

private fun createEncounterModifierInputs(
    namePrefix: String = "",
    dc: ModifyEncounterDc?,
): Array<NumberInput> {
    return arrayOf(
        NumberInput(
            label = "Day: Encounter DC Modifier",
            name = "${namePrefix}modifyRandomEncounterDc.day",
            help = "Negative values decrease the modifier",
            value = dc?.day ?: 0,
            stacked = false,
        ),
        NumberInput(
            label = "Night: Encounter DC Modifier",
            name = "${namePrefix}modifyRandomEncounterDc.night",
            help = "Negative values decrease the modifier",
            value = dc?.night ?: 0,
            stacked = false,
        ),
    )
}

private fun createActivityEffectInputs(
    namePrefix: String,
    outcome: ActivityOutcome?,
    allEffects: List<PF2EEffect>,
): Array<FormElementContext> {
    return formContext(
        TextArea(
            name = "${namePrefix}message",
            label = "Chat Message",
            value = outcome?.message ?: "",
            required = false,
            stacked = false,
        ),
        CheckboxInput(
            name = "${namePrefix}checkRandomEncounter",
            value = outcome?.checkRandomEncounter == true,
            label = "Random Encounter Check",
        ),
        *createEncounterModifierInputs(namePrefix = namePrefix, dc = outcome?.modifyRandomEncounterDc),
        ActivityEffects(
            value = toActivityEffectContext(
                allEffects = allEffects.toTypedArray(),
                section = namePrefix.trimEnd('.'),
                effects = outcome?.effectUuids ?: emptyArray()
            )
        ),
    )
}

private fun parseOutcome(
    current: ActivityOutcome?,
    submitted: ActivityOutcomeSubmitData
) = ActivityOutcome(
    message = submitted.message,
    effectUuids = current?.effectUuids ?: emptyArray(),
    modifyRandomEncounterDc = submitted.modifyRandomEncounterDc,
    checkRandomEncounter = submitted.checkRandomEncounter,
)