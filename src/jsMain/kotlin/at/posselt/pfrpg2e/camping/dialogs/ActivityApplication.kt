package at.posselt.pfrpg2e.camping.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
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
import at.posselt.pfrpg2e.camping.ActivityEffect
import at.posselt.pfrpg2e.camping.ActivityOutcome
import at.posselt.pfrpg2e.camping.CampingActivityData
import at.posselt.pfrpg2e.camping.CampingActor
import at.posselt.pfrpg2e.camping.ModifyEncounterDc
import at.posselt.pfrpg2e.camping.deleteCampingActivityOld
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingSkills
import at.posselt.pfrpg2e.camping.requiresACheck
import at.posselt.pfrpg2e.camping.setCamping
import at.posselt.pfrpg2e.slugify
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.openItem
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.without
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.pf2e.item.PF2EEffect
import js.core.Void
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise


@JsPlainObject
external interface ActivityContext : SectionsContext, ValidatedHandlebarsContext

@JsPlainObject
external interface ActivityOutcomeSubmitData {
    val message: String?
    val modifyRandomEncounterDc: ModifyEncounterDc
    val checkRandomEncounter: Boolean
}

@JsPlainObject
external interface ActivitySubmitData {
    val id: String
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

@JsExport
class ActivityDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("id")
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

@JsExport
class ActivityApplication(
    private val game: Game,
    private val actor: CampingActor,
    data: CampingActivityData? = null,
    private val afterSubmit: () -> Unit,
) : FormApp<ActivityContext, ActivitySubmitData>(
    title = if (data == null) t("camping.addActivity") else t("camping.editActivity", recordOf("activityName" to data.name)),
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = ActivityDataModel::class.js,
    id = "kmActivity-${actor.uuid}"
) {
    private val editActivityId = data?.id
    private val editActivityLocked = data?.isLocked
    private var currentActivity: CampingActivityData = data?.let(::deepClone) ?: CampingActivityData(
        name = "",
        journalUuid = null,
        skills = emptyArray(),
        modifyRandomEncounterDc = null,
        isSecret = false,
        isLocked = false,
        effectUuids = emptyArray(),
        isHomebrew = true,
        criticalSuccess = null,
        success = null,
        failure = null,
        criticalFailure = null,
        id = "",
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "openDocumentLink" -> buildPromise {
                event.preventDefault()
                event.stopPropagation()
                target.dataset["uuid"]?.let { fromUuidTypeSafe<PF2EEffect>(it)?.sheet?.launch() }
            }

            "km-save" -> save()
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
        when (section) {
            "effects" -> currentActivity.effectUuids = currentActivity
                .effectUuids
                ?.without(index)
            "criticalSuccess" -> currentActivity.criticalSuccess?.effectUuids = currentActivity
                .criticalSuccess
                ?.effectUuids
                ?.without(index)
            "success" -> currentActivity.success?.effectUuids = currentActivity
                .success
                ?.effectUuids
                ?.without(index)
            "failure" -> currentActivity.failure?.effectUuids = currentActivity
                .failure
                ?.effectUuids
                ?.without(index)
            "criticalFailure" -> currentActivity.criticalFailure?.effectUuids = currentActivity
                .criticalFailure
                ?.effectUuids
                ?.without(index)
        }
        render()
    }

    private fun createEffectAt(section: String, effect: ActivityEffect) {
        when (section) {
            "effects" -> currentActivity.effectUuids = currentActivity.effectUuids?.plus(effect)
            "criticalSuccess" -> currentActivity.criticalSuccess?.effectUuids = currentActivity.criticalSuccess?.effectUuids?.plus(effect)
            "success" -> currentActivity.success?.effectUuids = currentActivity.success?.effectUuids?.plus(effect)
            "failure" -> currentActivity.failure?.effectUuids = currentActivity.failure?.effectUuids?.plus(effect)
            "criticalFailure" -> currentActivity.criticalFailure?.effectUuids = currentActivity.criticalFailure?.effectUuids?.plus(effect)
        }
        render()
    }

    private fun getEffectsSection(section: String?): Array<ActivityEffect>? =
        when (section) {
            "effects" -> currentActivity.effectUuids
            "criticalSuccess" -> currentActivity.criticalSuccess?.effectUuids
            "success" -> currentActivity.success?.effectUuids
            "failure" -> currentActivity.failure?.effectUuids
            "criticalFailure" -> currentActivity.criticalFailure?.effectUuids
            else -> null
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
                    legend = t("camping.basic"),
                    formRows = formContext(
                        TextInput(
                            stacked = false,
                            label = t("applications.id"),
                            name = "id",
                            readonly = editActivityId != null,
                            value = currentActivity.id,
                            required = true,
                            help = t("camping.overrideActivityHelp"),
                        ),
                        TextInput(
                            stacked = false,
                            label = t("applications.name"),
                            name = "name",
                            value = currentActivity.name,
                            required = true,
                        ),
                        Select(
                            label = t("camping.journal"),
                            name = "journalUuid",
                            value = journal?.entry?.uuid,
                            required = false,
                            options = game.journal.contents.mapNotNull { it.toOption(useUuid = true) },
                            stacked = false,
                        ),
                        Select(
                            label = t("camping.journalEntry"),
                            name = "journalEntryUuid",
                            required = false,
                            value = journal?.page?.uuid,
                            options = journal?.entry?.pages?.contents?.mapNotNull { it.toOption(useUuid = true) }
                                ?: emptyList(),
                            stacked = false,
                        ),
                        SkillPicker(
                            context = toSkillContext(currentActivity.skills),
                            stacked = false,
                        ),
                        CheckboxInput(
                            label = t("camping.secretCheck"),
                            name = "isSecret",
                            value = currentActivity.isSecret,
                        ),
                    ),
                ),
                SectionContext(
                    legend = t("camping.whenPerformed"),
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
                    legend = t("degreeOfSuccess.criticalSuccess"),
                    formRows = createActivityEffectInputs(
                        namePrefix = "criticalSuccess.",
                        outcome = currentActivity.criticalSuccess,
                        allEffects = effects,
                    ),
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = t("degreeOfSuccess.success"),
                    formRows = createActivityEffectInputs(
                        namePrefix = "success.",
                        outcome = currentActivity.success,
                        allEffects = effects,
                    ),
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = t("degreeOfSuccess.failure"),
                    formRows = createActivityEffectInputs(
                        namePrefix = "failure.",
                        outcome = currentActivity.failure,
                        allEffects = effects,
                    ),
                ),
                SectionContext(
                    hidden = !hasCheck,
                    legend = t("degreeOfSuccess.criticalFailure"),
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
                        .filter { it.id != data.id }
                        .toTypedArray()
                    camping.homebrewCampingActivities += data
                    actor.setCamping(camping)
                    actor.deleteCampingActivityOld(data.id)
                    close().await()
                    afterSubmit()
                }
            }
        }
        undefined
    }

    override fun onParsedSubmit(value: ActivitySubmitData): Promise<Void> = buildPromise {
        currentActivity = CampingActivityData(
            id = editActivityId ?: value.id.slugify(),
            name = value.name,
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
            label = t("camping.dayEncounterDcModifier"),
            name = "${namePrefix}modifyRandomEncounterDc.day",
            help = t("camping.negativeValuesDecreaseMod"),
            value = dc?.day ?: 0,
            stacked = false,
        ),
        NumberInput(
            label = t("camping.nightEncounterDcModifier"),
            name = "${namePrefix}modifyRandomEncounterDc.night",
            help = t("camping.negativeValuesDecreaseMod"),
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
            label = t("camping.chatMessage"),
            value = outcome?.message ?: "",
            required = false,
            stacked = false,
        ),
        CheckboxInput(
            name = "${namePrefix}checkRandomEncounter",
            value = outcome?.checkRandomEncounter == true,
            label = t("camping.randomEncounterCheck"),
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