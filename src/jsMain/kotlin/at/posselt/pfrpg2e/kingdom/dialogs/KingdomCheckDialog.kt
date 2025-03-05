package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.CheckboxInput
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActivity
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.armies.getTargetedArmies
import at.posselt.pfrpg2e.kingdom.armies.getTargetedArmyConditions
import at.posselt.pfrpg2e.kingdom.checkModifiers
import at.posselt.pfrpg2e.kingdom.createExpressionContext
import at.posselt.pfrpg2e.kingdom.getAllActivities
import at.posselt.pfrpg2e.kingdom.getAllFeats
import at.posselt.pfrpg2e.kingdom.getAllSettlements
import at.posselt.pfrpg2e.kingdom.getRealmData
import at.posselt.pfrpg2e.kingdom.hasAssurance
import at.posselt.pfrpg2e.kingdom.increasedSkills
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateGlobalBonuses
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.evaluateModifiers
import at.posselt.pfrpg2e.kingdom.modifiers.evaluation.includeCapital
import at.posselt.pfrpg2e.kingdom.modifiers.penalties.ArmyConditionInfo
import at.posselt.pfrpg2e.kingdom.parseSkillRanks
import at.posselt.pfrpg2e.kingdom.resolveDc
import at.posselt.pfrpg2e.kingdom.setKingdom
import at.posselt.pfrpg2e.kingdom.skillRanks
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.vacancies
import at.posselt.pfrpg2e.utils.buildPromise
import at.posselt.pfrpg2e.utils.deserializeB64Json
import at.posselt.pfrpg2e.utils.formatAsModifier
import at.posselt.pfrpg2e.utils.launch
import at.posselt.pfrpg2e.utils.serializeB64Json
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.core.Void
import js.objects.ReadonlyRecord
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
import kotlin.arrayOf
import kotlin.js.Promise
import kotlin.let
import kotlin.math.max
import kotlin.to

@JsPlainObject
private external interface ModifierContext {
    val label: String
    val type: String
    val modifier: String
    val enabled: FormElementContext
    val id: FormElementContext
    val hidden: Boolean
}

@JsPlainObject
private external interface CheckContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val settlementInput: FormElementContext
    val leaderInput: FormElementContext
    val rollModeInput: FormElementContext
    val phaseInput: FormElementContext
    val skillInput: FormElementContext
    val dc: FormElementContext
    val assurance: FormElementContext
    val modifiers: Array<ModifierContext>
    val hasAssurance: Boolean
    val checkModifier: Int
    val supernaturalSolution: FormElementContext
    val creativeSolution: FormElementContext
    val encodedAssurancePills: String
    val encodedPills: String
}

@JsPlainObject
private external interface CheckData {
    var leader: String
    var rollMode: String
    var phase: String?
    var skill: String
    var dc: Int
    var assurance: Boolean
    var modifiers: ReadonlyRecord<String, Boolean>
    var supernaturalSolution: Boolean
    var creativeSolution: Boolean
}

@JsExport
class CheckModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            int("dc")
            string("settlement")
            boolean("supernaturalSolution") {
                initial = false
            }
            boolean("creativeSolution") {
                initial = false
            }
            enum<KingdomSkill>("skill")
            enum<Leader>("leader")
            enum<RollMode>("rollMode")
            enum<KingdomPhase>("phase", nullable = true)
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

private fun createSettlementOptions(
    game: Game,
    settlements: Array<RawSettlement>
): List<SelectOption> {
    return settlements.mapNotNull { settlement ->
        game.scenes.get(settlement.sceneId)?.name?.let {
            SelectOption(settlement.sceneId, it)
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

private fun getValidActivitySkills(
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
    val phase: KingdomPhase? = null,
    val structure: Structure? = null,
    val activity: KingdomActivity? = null,
    val armyConditions: ArmyConditionInfo? = null,
)

typealias AfterRollMessage = suspend (degree: DegreeOfSuccess) -> String?

private class KingdomCheckDialog(
    private val game: Game,
    private val kingdomActor: PF2ENpc,
    private val kingdom: KingdomData,
    private val baseModifiers: List<Modifier>,
    private val afterRollMessage: AfterRollMessage,
    params: CheckDialogParams,
) : FormApp<CheckContext, CheckData>(
    title = params.title,
    template = "applications/kingdom/check.hbs",
    debug = true,
    dataModel = CheckModel::class.js,
    id = "kmCheck",
) {
    val activity = params.activity
    val validSkills = params.validSkills
    val armyConditions = params.armyConditions

    var data = CheckData(
        modifiers = baseModifiers.map { it.id to it.enabled }.toRecord(),
        leader = Leader.RULER.value,
        rollMode = RollMode.PUBLICROLL.value,
        skill = params.validSkills.first().name,
        phase = params.phase?.value,
        assurance = false,
        dc = params.dc,
        supernaturalSolution = false,
        creativeSolution = false,
    )

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "roll" -> {
                buildPromise {
                    val modifier = target.dataset["modifier"]?.toInt() ?: 0
                    val pills = deserializeB64Json<Array<ModifierPill>>(target.dataset["pills"]!!)
                    roll(modifier, pills)
                    close()
                }
            }

            "rollWithAssurance" -> {
                buildPromise {
                    val modifier = target.dataset["modifier"]?.toInt() ?: 0
                    val pills = deserializeB64Json<Array<ModifierPill>>(target.dataset["pills"]!!)
                    roll(modifier, pills)
                    close()
                }
            }
        }
    }

    private suspend fun roll(modifier: Int, pills: Array<ModifierPill>) {
        rollCheck(
            dc = data.dc,
            afterRollMessage = afterRollMessage,
            rollMode = RollMode.fromString(data.rollMode),
            activity = activity,
            game = game,
            kingdomActor = kingdomActor,
            modifier = modifier,
            modifierPills = pills,
        )
        if (data.supernaturalSolution) {
            kingdom.supernaturalSolutions = max(0, kingdom.supernaturalSolutions -1)
        }
        if (data.creativeSolution) {
            kingdom.creativeSolutions = max(0, kingdom.creativeSolutions -1)
        }
        kingdomActor.setKingdom(kingdom)
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CheckContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()

        // evaluate modifiers
        val enabledModifiers = baseModifiers.map { it.copy(enabled = data.modifiers[it.id] == true) }
        val phase = data.phase?.let { fromCamelCase<KingdomPhase>(it) }
        val rollOptions = enabledModifiers.flatMap { it.rollOptions }.toSet()
        val usedSkill = KingdomSkill.fromString(data.skill)!!
        val context = kingdom.createExpressionContext(
            phase = phase,
            activity = activity,
            leader = Leader.fromString(data.leader)!!,
            usedSkill = if (data.supernaturalSolution) KingdomSkill.MAGIC else usedSkill,
            rollOptions = if (data.creativeSolution) rollOptions + "creative-solution" else rollOptions,
        )
        val selectedSkill = context.usedSkill
        val evaluatedModifiers = evaluateModifiers(enabledModifiers, context)
        val hasAssurance = kingdom.hasAssurance(selectedSkill)
        val evaluatedModifiersById = evaluatedModifiers.modifiers.associateBy { it.id }
        val encodedAssurancePills = serializeB64Json(arrayOf(
            ModifierPill(label = "Assurance", value = evaluatedModifiers.total)
        ))
        val encodedPills = serializeB64Json(evaluatedModifiers.modifiers.map {
            ModifierPill(label = it.name, value = it.value)
        }.toTypedArray())

        CheckContext(
            partId = parent.partId,
            settlementInput = Select(
                name = "settlement",
                label = "Active Settlement",
                options = createSettlementOptions(game, kingdom.settlements),
                value = kingdom.activeSettlement,
            ).toContext(),
            leaderInput = Select.fromEnum(
                name = "leader",
                label = "Leader",
                value = fromCamelCase<RollMode>(data.leader),
            ).toContext(),
            rollModeInput = Select.fromEnum<RollMode>(
                name = "rollMode",
                label = "Roll Mode",
                value = fromCamelCase<RollMode>(data.rollMode),
                labelFunction = { it.label },
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
                    val mod = evaluateModifiers(enabledModifiers, context.copy(usedSkill = it)).total
                    val label = "${it.label} (${mod.formatAsModifier()})"
                    SelectOption(label, it.value)
                },
            ).toContext(),
            dc = Select.dc(
                name = "dc",
                label = "DC",
                value = data.dc,
            ).toContext(),
            supernaturalSolution = CheckboxInput(
                name = "supernaturalSolution",
                label = "Supernatural Solution",
                value = data.supernaturalSolution,
                disabled = kingdom.supernaturalSolutions == 0
            ).toContext(),
            creativeSolution = CheckboxInput(
                name = "creativeSolution",
                label = "Creative Solution",
                value = data.creativeSolution,
                disabled = kingdom.creativeSolutions == 0
            ).toContext(),
            assurance = if (hasAssurance) {
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
            checkModifier = if (data.assurance) {
                evaluatedModifiers.assurance
            } else {
                evaluatedModifiers.total
            },
            hasAssurance = hasAssurance,
            modifiers = enabledModifiers
                .sortedBy { it.type }
                .mapIndexed { index, mod -> toModifierContext(mod, evaluatedModifiersById, index) }
                .toTypedArray(),
            encodedAssurancePills = encodedAssurancePills,
            encodedPills = encodedPills,
            isFormValid = true,
        )
    }

    fun toModifierContext(
        modifier: Modifier,
        evaluatedModifiersById: Map<String, Modifier>,
        index: Int,
    ): ModifierContext {
        val id = modifier.id
        val hidden = id !in evaluatedModifiersById
        val evaluatedModifier = evaluatedModifiersById[id]
        val value = evaluatedModifier?.value ?: 0
        val enabled = evaluatedModifier?.enabled ?: modifier.enabled
        return ModifierContext(
            label = modifier.name,
            type = modifier.type.label,
            modifier = value.formatAsModifier(),
            hidden = hidden,
            id = HiddenInput(
                name = "modifier.$index.id",
                value = id,
            ).toContext(),
            enabled = if (hidden) {
                HiddenInput(
                    name = "modifier.$index.enabled",
                    value = enabled.toString(),
                    overrideType = OverrideType.BOOLEAN,
                ).toContext()
            } else {
                CheckboxInput(
                    label = modifier.name,
                    name = "modifier.$index.enabled",
                    value = enabled,
                    hideLabel = true,
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
    value class PerformActivity(val activity: KingdomActivity) : CheckType
    value class BuildStructure(val structure: Structure) : CheckType
}

suspend fun kingdomCheckDialog(
    game: Game,
    kingdom: KingdomData,
    kingdomActor: PF2ENpc,
    check: CheckType,
    afterRollMessage: AfterRollMessage,
) {
    val params = when (check) {
        is CheckType.PerformActivity -> {
            val activity = check.activity
            val realm = game.getRealmData(kingdom)
            val vacancies = kingdom.vacancies()
            val dc = activity.resolveDc(
                kingdomLevel = kingdom.level,
                realm = realm,
                rulerVacant = vacancies.ruler,
                enemyArmyScoutingDcs = game.getTargetedArmies().map { it.system.scouting }
            )
            val skills = getValidActivitySkills(
                ranks = kingdom.parseSkillRanks(),
                activityRanks = activity.skillRanks(),
                ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
                expandMagicUse = kingdom.settings.expandMagicUse,
                activityId = activity.id,
                increaseSkills = kingdom.getAllFeats().map { it.increasedSkills() }
            )
            CheckDialogParams(
                title = activity.title,
                dc = dc ?: 0,
                validSkills = skills,
                phase = KingdomPhase.fromString(activity.phase),
                armyConditions = game.getTargetedArmyConditions()
            )
        }

        is CheckType.RollSkill -> {
            val realm = game.getRealmData(kingdom)
            val vacancies = kingdom.vacancies()
            val dc = calculateControlDC(
                kingdomLevel = kingdom.level,
                realm = realm,
                rulerVacant = vacancies.ruler,
            )
            CheckDialogParams(title = check.skill.label, dc = dc, validSkills = setOf(check.skill))
        }

        is CheckType.BuildStructure -> {
            val structure = check.structure
            val activity = kingdom.getAllActivities().find { it.id == "build-structure" }
                ?: throw IllegalArgumentException("No Build Structure Activity present")
            val dc = structure.construction.dc
            val skills = getValidActivitySkills(
                ranks = kingdom.parseSkillRanks(),
                activityRanks = structure.construction.skills,
                ignoreSkillRequirements = kingdom.settings.kingdomIgnoreSkillRequirements,
                expandMagicUse = kingdom.settings.expandMagicUse,
                activityId = activity.id,
                increaseSkills = kingdom.getAllFeats().map { it.increasedSkills() }
            )
            CheckDialogParams(
                title = activity.title,
                dc = dc,
                validSkills = skills,
                phase = KingdomPhase.fromString(activity.phase)
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
    )
    KingdomCheckDialog(
        params = params,
        game = game,
        afterRollMessage = afterRollMessage,
        kingdomActor = kingdomActor,
        kingdom = kingdom,
        baseModifiers = baseModifiers,
    ).launch()
}