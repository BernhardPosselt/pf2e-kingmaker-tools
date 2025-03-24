package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.ValidatedHandlebarsContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.TextInput
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.checks.determineDegreeOfSuccess
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActor
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.armies.getSelectedArmies
import at.posselt.pfrpg2e.kingdom.armies.getSelectedArmyConditions
import at.posselt.pfrpg2e.kingdom.checkModifiers
import at.posselt.pfrpg2e.kingdom.createExpressionContext
import at.posselt.pfrpg2e.kingdom.data.getChosenFeats
import at.posselt.pfrpg2e.kingdom.data.getChosenFeatures
import at.posselt.pfrpg2e.kingdom.data.getChosenGovernment
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getExplodedFeatures
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.hasAssurance
import at.posselt.pfrpg2e.kingdom.increasedSkills
import at.posselt.pfrpg2e.kingdom.modifiers.DowngradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.UpgradeResult
import at.posselt.pfrpg2e.kingdom.modifiers.determineDegree
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateGlobalBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.filterModifiersAndUpdateContext
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.includeCapital
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import at.posselt.pfrpg2e.kingdom.parseModifiers
import at.posselt.pfrpg2e.kingdom.parseSkillRanks
import at.posselt.pfrpg2e.kingdom.resolveDc
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.skillRanks
import at.posselt.pfrpg2e.kingdom.vacancies
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.formatAsModifier
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.postChatMessage
import at.posselt.pfrpg2e.utils.serializeB64Json
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import io.github.uuidjs.uuid.v4
import js.core.Void
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.Array
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.arrayOf
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.toSet
import kotlin.collections.toTypedArray
import kotlin.emptyArray
import kotlin.js.Promise
import kotlin.let
import kotlin.math.max
import kotlin.sequences.filter
import kotlin.sequences.map
import kotlin.sequences.toSet
import kotlin.text.toInt
import kotlin.to

@Suppress("unused")
@JsPlainObject
private external interface ModifierContext {
    val label: String
    val type: String
    val modifier: String
    val enabledInput: FormElementContext
    val idInput: FormElementContext
    val hidden: Boolean
    val id: String
    val removable: Boolean
    val fortune: Boolean
}

@Suppress("unused")
@JsPlainObject
private external interface CheckContext : ValidatedHandlebarsContext {
    val leaderInput: FormElementContext
    val rollModeInput: FormElementContext
    val phaseInput: FormElementContext
    val skillInput: FormElementContext
    val dcInput: FormElementContext
    val assuranceInput: FormElementContext
    val modifiers: Array<ModifierContext>
    val hasAssurance: Boolean
    val useAssurance: Boolean
    val checkModifier: Int
    val checkModifierLabel: String
    val supernaturalSolutionInput: FormElementContext
    val newModifierNameInput: FormElementContext
    val newModifierTypeInput: FormElementContext
    val newModifierModifierInput: FormElementContext
    val pills: String
    val upgrades: String
    val isActivity: Boolean
    val assuranceModifier: Int
    val assuranceDegree: String
    val creativeSolutionModifier: Int
    val creativeSolutionPills: String
    val fortune: Boolean
    val downgrades: String
    val consumeModifiers: String
    val rollTwiceKeepHighest: Boolean
    val rollTwiceKeepLowest: Boolean
    val supernaturalSolutionDisabled: Boolean
}

@JsPlainObject
private external interface ModifierIdEnabled {
    var id: String
    var enabled: Boolean
}


@JsPlainObject
private external interface CheckData {
    var leader: String
    var rollMode: String
    var phase: String?
    var skill: String
    var dc: Int
    var assurance: Boolean
    var modifiers: Array<ModifierIdEnabled>
    var supernaturalSolution: Boolean
    var newModifierName: String
    var newModifierType: String
    var newModifierModifier: Int
}

@JsExport
class CheckModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            int("dc")
            string("settlement")
            boolean("supernaturalSolution") {
                initial = false
            }
            enum<KingdomSkill>("skill")
            enum<Leader>("leader")
            enum<RollMode>("rollMode")
            enum<KingdomPhase>("phase", nullable = true)
            string("newModifierName")
            enum<ModifierType>("newModifierType")
            int("newModifierModifier")
            boolean("assurance")
            array("modifiers") {
                schema {
                    string("id")
                    boolean("enabled")
                }
            }
        }
    }
}

private val expandMagicActivities = setOf(
    "celebrate-holiday",
    "celebrate-holiday-vk",
    "craft-luxuries",
    "create-a-masterpiece",
    "rest-and-relax",
)

fun getValidActivitySkills(
    ranks: KingdomSkillRanks,
    activityRanks: Set<KingdomSkillRank>,
    ignoreSkillRequirements: Boolean,
    activityId: String?,
    expandMagicUse: Boolean,
    increaseSkills: List<Map<KingdomSkill, Set<KingdomSkill>>>,
): Set<KingdomSkill> {
    val skills = activityRanks.asSequence()
        .filter {
            if (ignoreSkillRequirements) {
                true
            } else {
                ranks.resolve(it.skill) >= it.rank
            }
        }
        .map { it.skill }
        .toSet() + if (expandMagicUse && activityId in expandMagicActivities) {
        setOf(KingdomSkill.MAGIC)
    } else {
        emptySet()
    }
    val groupedSkills = increaseSkills
        .fold(emptyMap<KingdomSkill, Set<KingdomSkill>>()) { prev, curr ->
            prev + curr.mapValues { (k, v) -> v + prev[k].orEmpty() }
        }
    val featSkills = skills.flatMap { groupedSkills[it] ?: emptySet() }.toSet()
    return skills + featSkills
}

private data class CheckDialogParams(
    val title: String,
    val dc: Int,
    val validSkills: Set<KingdomSkill>,
    val phase: KingdomPhase?,
    val structure: Structure?,
    val activity: RawActivity?,
    val armyConditions: ArmyConditionInfo?,
)

typealias AfterRoll = suspend (degree: DegreeOfSuccess) -> Unit

@JsPlainObject
external interface SerializedDegree {
    val degree: String
    val times: Int
}

private class KingdomCheckDialog(
    private val kingdomActor: KingdomActor,
    private val kingdom: KingdomData,
    private var baseModifiers: List<Modifier>,
    private val afterRoll: AfterRoll,
    private val params: CheckDialogParams,
    private val degreeMessages: DegreeMessages,
    private val flags: Set<String>,
    private val isSupernaturalSolution: Boolean = false,
) : FormApp<CheckContext, CheckData>(
    title = params.title,
    template = "applications/kingdom/check.hbs",
    debug = true,
    dataModel = CheckModel::class.js,
    id = "kmCheck-${kingdomActor.uuid}",
    width = 600,
) {
    val activity = params.activity
    val validSkills = params.validSkills
    val structure = params.structure
    val removableIds = mutableSetOf<String>()

    var data = CheckData(
        modifiers = baseModifiers.map { ModifierIdEnabled(it.id, it.enabled) }.toTypedArray(),
        leader = Leader.RULER.value,
        rollMode = RollMode.PUBLICROLL.value,
        skill = params.validSkills.first().value,
        phase = params.phase?.value,
        assurance = false,
        dc = params.dc,
        supernaturalSolution = false,
        newModifierName = "",
        newModifierType = "untyped",
        newModifierModifier = 0,
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "assurance" -> {
                val modifier = target.dataset["modifier"]?.toInt() ?: 0
                val pills = arrayOf("Assurance $modifier" )
                buildPromise {
                    roll(
                        modifier = modifier,
                        pills = pills,
                        upgrades = emptySet(),
                        downgrades = emptySet(),
                        fortune = false,
                        rollTwiceKeepHighest = false,
                        rollTwiceKeepLowest = false,
                        creativeSolutionModifier = 0,
                        creativeSolutionPills = emptyArray(),
                        consumedModifiers = emptySet(),
                        assurance = true,
                    )
                }
            }

            "roll" -> {
                buildPromise {
                    val rollTwiceKeepHighest = target.dataset["rollTwiceKeepHighest"] == "true"
                    val rollTwiceKeepLowest = target.dataset["rollTwiceKeepLowest"] == "true"
                    val fortune = target.dataset["fortune"] == "true"
                    val modifier = target.dataset["modifier"]?.toInt() ?: 0
                    val creativeSolutionModifier = target.dataset["creativeSolutionModifier"]?.toInt() ?: 0
                    val pills = deserializeB64Json<Array<String>>(target.dataset["pills"]!!)
                    val creativeSolutionPills =
                        deserializeB64Json<Array<String>>(target.dataset["creativeSolutionPills"]!!)
                    val upgrades = deserializeB64Json<Array<SerializedDegree>>(target.dataset["upgrades"]!!)
                        .mapNotNull { el ->
                            DegreeOfSuccess.fromString(el.degree)
                                ?.let { UpgradeResult(upgrade = it, times = el.times) }
                        }
                        .toSet()
                    val downgrades = deserializeB64Json<Array<SerializedDegree>>(target.dataset["downgrades"]!!)
                        .mapNotNull { el ->
                            DegreeOfSuccess.fromString(el.degree)
                                ?.let { DowngradeResult(downgrade = it, times = el.times) }
                        }
                        .toSet()
                    val consumedModifiers =
                        deserializeB64Json<Array<String>>(target.dataset["consumeModifiers"]!!).toSet()
                    roll(
                        modifier = modifier,
                        pills = pills,
                        upgrades = upgrades,
                        downgrades = downgrades,
                        fortune = fortune,
                        rollTwiceKeepHighest = rollTwiceKeepHighest,
                        rollTwiceKeepLowest = rollTwiceKeepLowest,
                        creativeSolutionModifier = creativeSolutionModifier,
                        creativeSolutionPills = creativeSolutionPills,
                        consumedModifiers = consumedModifiers,
                        assurance = false,
                    )
                }
            }

            "add-modifier" -> {
                val id = v4()
                data.modifiers = data.modifiers + ModifierIdEnabled(id, true)
                removableIds.add(id)
                baseModifiers = baseModifiers + Modifier(
                    id = id,
                    type = ModifierType.fromString(data.newModifierType) ?: ModifierType.UNTYPED,
                    value = data.newModifierModifier,
                    name = data.newModifierName,
                )
                data.newModifierName = ""
                data.newModifierType = ModifierType.UNTYPED.value
                data.newModifierModifier = 0
                render()
            }

            "delete-modifier" -> {
                val id = target.dataset["id"]
                if (id != null) {
                    baseModifiers = baseModifiers.filter { it.id != id }
                    removableIds.remove(id)
                    data.modifiers = data.modifiers.filter { it.id != id }.toTypedArray()
                    render()
                }
            }
        }
    }

    private suspend fun roll(
        modifier: Int,
        creativeSolutionModifier: Int,
        creativeSolutionPills: Array<String>,
        pills: Array<String>,
        upgrades: Set<UpgradeResult>,
        fortune: Boolean,
        downgrades: Set<DowngradeResult>,
        consumedModifiers: Set<String>,
        rollTwiceKeepHighest: Boolean,
        rollTwiceKeepLowest: Boolean,
        assurance: Boolean,
    ) {
        rollCheck(
            afterRoll = afterRoll,
            rollMode = RollMode.fromString(data.rollMode),
            activity = activity,
            skill = KingdomSkill.fromString(data.skill)!!,
            modifier = modifier,
            modifierWithCreativeSolution = creativeSolutionModifier,
            fortune = fortune,
            modifierPills = pills,
            dc = data.dc,
            kingdomActor = kingdomActor,
            upgrades = upgrades,
            rollTwiceKeepHighest = rollTwiceKeepHighest,
            rollTwiceKeepLowest = rollTwiceKeepLowest,
            creativeSolutionPills = creativeSolutionPills,
            downgrades = downgrades,
            degreeMessages = degreeMessages,
            useFameInfamy = false,
            assurance = assurance,
        )
        if (data.supernaturalSolution && !data.assurance) {
            KingdomCheckDialog(
                kingdomActor = kingdomActor,
                kingdom = kingdom,
                baseModifiers = baseModifiers,
                afterRoll = {
                    kingdomActor.getKingdom()?.let {
                        postChatMessage("Reduced Supernatural Solutions by 1")
                        it.supernaturalSolutions = max(0, it.supernaturalSolutions - 1)
                        kingdomActor.setKingdom(it)
                    }
                },
                params = params.copy(
                    validSkills = setOf(KingdomSkill.MAGIC),
                    title = "Supernatural Solution: ${params.title}"
                ),
                degreeMessages = degreeMessages,
                flags = flags,
                isSupernaturalSolution = true,
            ).launch()
        }

        kingdomActor.getKingdom()?.let { k ->
            k.modifiers = kingdom.modifiers.filter { it.id !in consumedModifiers }.toTypedArray()
            kingdomActor.setKingdom(k)
        }

        close()
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CheckContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val currentlyEnabledModIds = data.modifiers.filter { it.enabled }.map { it.id }.toSet()
        val enabledModifiers = baseModifiers.map { it.copy(enabled = it.id in currentlyEnabledModIds) }
        val phase = data.phase?.let { fromCamelCase<KingdomPhase>(it) }
        val usedSkill = KingdomSkill.fromString(data.skill)!!
        val context = kingdom.createExpressionContext(
            phase = phase,
            activity = activity,
            leader = Leader.fromString(data.leader)!!,
            usedSkill = usedSkill,
            rollOptions = emptySet(),
            structure = structure,
            flags = flags,
        )
        val filtered = filterModifiersAndUpdateContext(enabledModifiers, context)
        val evaluatedModifiers = evaluateModifiers(filtered)
        val creativeSolutionModifiers = evaluateModifiers(
            filterModifiersAndUpdateContext(
                enabledModifiers,
                context.copy(rollOptions = setOf("creative-solution"))
            )
        )
        val chosenFeatures = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
        val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
        val upgrades = evaluatedModifiers.upgradeResults
        val downgrades = evaluatedModifiers.downgradeResults
        val selectedSkill = context.usedSkill
        val hasAssurance = kingdom.hasAssurance(chosenFeats, selectedSkill)
        val evaluatedModifiersById = evaluatedModifiers.filteredModifiers.associateBy { it.id }
        val consumeModifierIds = evaluatedModifiers.modifiers
            .filter { it.isConsumedAfterRoll }
            .map { it.id }
            .toTypedArray()
        val pills = if (data.assurance) {
            serializeB64Json(
                arrayOf("Assurance ${evaluatedModifiers.total.formatAsModifier()}")
            )
        } else {
            serializeB64Json(evaluatedModifiers.modifiers.map {
                if (it.value == 0) it.name else "${it.name} ${it.value.formatAsModifier()}"
            }.toTypedArray())
        }
        val creativeSolutionPills = serializeB64Json(creativeSolutionModifiers.modifiers.map {
            "${it.name} ${it.value.formatAsModifier()}"
        }.toTypedArray())
        val checkModifier = if (data.assurance) {
            evaluatedModifiers.assurance
        } else {
            evaluatedModifiers.total
        }
        if (evaluatedModifiers.fortune) {
            data.supernaturalSolution = false
        }
        CheckContext(
            partId = parent.partId,
            isFormValid = isFormValid,
            leaderInput = Select.fromEnum<Leader>(
                name = "leader",
                label = "Leader",
                value = fromCamelCase<Leader>(data.leader),
            ).toContext(),
            rollModeInput = Select.fromEnum<RollMode>(
                name = "rollMode",
                label = "Roll Mode",
                value = fromCamelCase<RollMode>(data.rollMode),
                labelFunction = { it.label },
                hideLabel = true,
                stacked = false,
            ).toContext(),
            phaseInput = Select.fromEnum(
                label = "Phase",
                name = "phase",
                value = phase,
            ).toContext(),
            skillInput = Select(
                label = "Skill",
                name = "skill",
                value = selectedSkill.value,
                options = validSkills.map {
                    val skillContext = context.copy(usedSkill = it)
                    val skillFiltered = filterModifiersAndUpdateContext(
                        modifiers = enabledModifiers,
                        context = skillContext
                    )
                    val mod = evaluateModifiers(skillFiltered).total
                    val label = "${it.label} (${mod.formatAsModifier()})"
                    SelectOption(label, it.value)
                },
            ).toContext(),
            dcInput = Select.dc(
                name = "dc",
                label = "DC",
                value = data.dc,
            ).toContext(),
            supernaturalSolutionInput = CheckboxInput(
                name = "supernaturalSolution",
                label = "Supernatural Solution",
                value = data.supernaturalSolution,
                disabled = kingdom.supernaturalSolutions == 0 || evaluatedModifiers.fortune
            ).toContext(),
            assuranceInput = if (hasAssurance) {
                CheckboxInput(
                    name = "assurance",
                    label = "Assurance",
                    value = data.assurance,
                ).toContext()
            } else {
                HiddenInput(
                    name = "assurance",
                    value = data.assurance.toString(),
                    overrideType = OverrideType.BOOLEAN,
                ).toContext()
            },
            checkModifier = checkModifier,
            hasAssurance = hasAssurance,
            modifiers = enabledModifiers
                .mapIndexed { index, mod -> toModifierContext(mod, evaluatedModifiersById, index) }
                .sortedWith(compareBy<ModifierContext> { !it.hidden }.thenBy { it.type })
                .toTypedArray(),
            pills = pills,
            upgrades = serializeB64Json(upgrades.map {
                recordOf(
                    "degree" to it.upgrade.value,
                    "times" to it.times
                )
            }.toTypedArray()),
            downgrades = serializeB64Json(downgrades.map {
                recordOf(
                    "degree" to it.downgrade.value,
                    "times" to it.times
                )
            }.toTypedArray()),
            useAssurance = data.assurance,
            isActivity = activity != null,
            checkModifierLabel = checkModifier.formatAsModifier(),
            newModifierNameInput = TextInput(
                label = "Name",
                value = data.newModifierName,
                name = "newModifierName",
                required = false,
            ).toContext(),
            newModifierTypeInput = Select.fromEnum<ModifierType>(
                name = "newModifierType",
                label = "Type",
                value = ModifierType.fromString(data.newModifierType) ?: ModifierType.UNTYPED,
            ).toContext(),
            newModifierModifierInput = NumberInput(
                label = "Modifier",
                value = data.newModifierModifier,
                name = "newModifierModifier",
            ).toContext(),
            assuranceModifier = checkModifier + 10,
            assuranceDegree = determineAssuranceDegree(checkModifier, upgrades, downgrades),
            creativeSolutionModifier = creativeSolutionModifiers.total,
            creativeSolutionPills = creativeSolutionPills,
            fortune = evaluatedModifiers.fortune || data.supernaturalSolution || isSupernaturalSolution,
            supernaturalSolutionDisabled = evaluatedModifiers.fortune || isSupernaturalSolution,
            consumeModifiers = serializeB64Json(consumeModifierIds),
            rollTwiceKeepHighest = evaluatedModifiers.rollTwiceKeepHighest,
            rollTwiceKeepLowest = evaluatedModifiers.rollTwiceKeepLowest,
        )
    }

    private fun determineAssuranceDegree(
        modifier: Int,
        upgradeDegrees: Set<UpgradeResult>,
        downgradeDegrees: Set<DowngradeResult>
    ): String {
        var degree = determineDegreeOfSuccess(data.dc, modifier + 10, 10)
        return determineDegree(degree, upgradeDegrees, downgradeDegrees).changedDegree.label
    }

    fun toModifierContext(
        modifier: Modifier,
        evaluatedModifiersById: Map<String, Modifier>,
        index: Int,
    ): ModifierContext {
        val id = modifier.id
        val evaluatedModifier = evaluatedModifiersById[id]
        val value = evaluatedModifier?.value ?: 0
        val hidden = id !in evaluatedModifiersById || data.assurance && modifier.type != ModifierType.PROFICIENCY
        val enabled = evaluatedModifier?.enabled ?: modifier.enabled
        return ModifierContext(
            label = modifier.name,
            type = modifier.type.label,
            modifier = value.formatAsModifier(),
            id = modifier.id,
            removable = modifier.id in removableIds,
            fortune = modifier.fortune,
            hidden = hidden,
            idInput = HiddenInput(
                name = "modifiers.$index.id",
                value = id,
            ).toContext(),
            enabledInput = if (hidden) {
                HiddenInput(
                    name = "modifiers.$index.enabled",
                    value = enabled.toString(),
                    overrideType = OverrideType.BOOLEAN,
                ).toContext()
            } else {
                CheckboxInput(
                    label = "Enable",
                    name = "modifiers.$index.enabled",
                    value = enabled,
                ).toContext()
            }
        )
    }

    override fun onParsedSubmit(value: CheckData): Promise<Void> = buildPromise {
        data = value
        null
    }
}

sealed interface CheckType {
    value class RollSkill(val skill: KingdomSkill) : CheckType
    value class PerformActivity(val activity: RawActivity) : CheckType
    value class BuildStructure(val structure: Structure) : CheckType
}

@JsPlainObject
external interface DegreeMessages {
    val criticalSuccess: String?
    val success: String?
    val failure: String?
    val criticalFailure: String?
}

suspend fun kingdomCheckDialog(
    game: Game,
    kingdom: KingdomData,
    kingdomActor: KingdomActor,
    check: CheckType,
    afterRoll: AfterRoll = { },
    degreeMessages: DegreeMessages = DegreeMessages(),
    overrideSkills: Set<KingdomSkillRank>? = null,
    overrideDc: Int? = null,
    flags: Set<String> = emptySet(),
) {
    val params = when (check) {
        is CheckType.PerformActivity -> {
            val activity = check.activity
            val realm = game.getRealmData(kingdomActor, kingdom)
            val vacancies = kingdom.vacancies()
            val dc = overrideDc ?: activity.resolveDc(
                kingdomLevel = kingdom.level,
                realm = realm,
                rulerVacant = vacancies.ruler,
                enemyArmyScoutingDcs = game.getSelectedArmies().map { it.system.scouting }
            )
            val chosenFeatures = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
            val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
            val skills = getValidActivitySkills(
                ranks = kingdom.parseSkillRanks(
                    chosenFeatures,
                    chosenFeats,
                    kingdom.getChosenGovernment()
                ),
                activityRanks = overrideSkills ?: activity.skillRanks(),
                ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
                expandMagicUse = kingdom.settings.expandMagicUse,
                activityId = activity.id,
                increaseSkills = chosenFeats.map { it.feat.increasedSkills() }
            )
            CheckDialogParams(
                title = activity.title,
                dc = overrideDc ?: dc ?: 0,
                validSkills = skills,
                phase = KingdomPhase.fromString(activity.phase),
                armyConditions = game.getSelectedArmyConditions(),
                structure = null,
                activity = activity,
            )
        }

        is CheckType.RollSkill -> {
            val realm = game.getRealmData(kingdomActor, kingdom)
            val vacancies = kingdom.vacancies()
            val dc = overrideDc ?: calculateControlDC(
                kingdomLevel = kingdom.level,
                realm = realm,
                rulerVacant = vacancies.ruler,
            )
            CheckDialogParams(
                title = check.skill.label, dc = dc, validSkills = setOf(check.skill),
                phase = KingdomPhase.EVENT,
                structure = null,
                activity = null,
                armyConditions = null,
            )
        }

        is CheckType.BuildStructure -> {
            val structure = check.structure
            val activity = kingdom.getAllActivities().find { it.id == "build-structure" }
                ?: throw IllegalArgumentException("No Build Structure Activity present")
            val dc = structure.construction.dc
            val chosenFeatures = kingdom.getChosenFeatures(kingdom.getExplodedFeatures())
            val chosenFeats = kingdom.getChosenFeats(chosenFeatures)
            val skills = getValidActivitySkills(
                ranks = kingdom.parseSkillRanks(
                    chosenFeatures,
                    chosenFeats,
                    kingdom.getChosenGovernment()
                ),
                activityRanks = overrideSkills ?: structure.construction.skills,
                ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
                expandMagicUse = kingdom.settings.expandMagicUse,
                activityId = activity.id,
                increaseSkills = chosenFeats.map { it.feat.increasedSkills() }
            )
            CheckDialogParams(
                title = activity.title,
                dc = dc,
                validSkills = skills,
                phase = KingdomPhase.fromString(activity.phase),
                structure = structure,
                activity = activity,
                armyConditions = null,
            )
        }
    }

    val settlementResult = kingdom.getAllSettlements(game)
    val allSettlements = settlementResult.allSettlements
    val globalBonuses = evaluateGlobalBonuses(allSettlements)
    val currentSettlement = settlementResult.current?.let {
        includeCapital(
            settlement = it,
            capital = settlementResult.capital,
            capitalModifierFallbackEnabled = kingdom.settings.includeCapitalItemModifier
        )
    }
    val baseModifiers = kingdom.checkModifiers(
        globalBonuses = globalBonuses,
        currentSettlement = currentSettlement,
        allSettlements = allSettlements,
        armyConditions = params.armyConditions,
    ) + params.activity?.parseModifiers().orEmpty()
    KingdomCheckDialog(
        params = params,
        afterRoll = afterRoll,
        kingdomActor = kingdomActor,
        kingdom = kingdom,
        baseModifiers = baseModifiers,
        degreeMessages = degreeMessages,
        flags = flags,
    ).launch()
}