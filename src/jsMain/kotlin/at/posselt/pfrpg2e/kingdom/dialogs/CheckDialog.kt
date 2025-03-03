package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.FormApp
import at.posselt.pfrpg2e.app.HandlebarsRenderContext
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.data.checks.RollMode
import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.data.kingdom.calculateControlDC
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.kingdom.KingdomActivity
import at.posselt.pfrpg2e.kingdom.armies.getPlayerSelectedArmies
import at.posselt.pfrpg2e.kingdom.getActivity
import at.posselt.pfrpg2e.kingdom.getKingdom
import at.posselt.pfrpg2e.kingdom.parsedSkillRanks
import at.posselt.pfrpg2e.kingdom.resolveDc
import at.posselt.pfrpg2e.kingdom.structures.RawSettlement
import at.posselt.pfrpg2e.kingdom.vacancies
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.applications.api.HandlebarsRenderOptions
import com.foundryvtt.core.data.dsl.buildSchema
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.core.Void
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.js.Promise

@JsPlainObject
private external interface CheckContext : HandlebarsRenderContext {
    val isFormValid: Boolean
    val settlementInput: FormElementContext
    val leaderInput: FormElementContext
    val rollModeInput: FormElementContext
    val phaseInput: FormElementContext
    val skillInput: FormElementContext
    val dc: FormElementContext
}

@JsPlainObject
private external interface CheckData {
    var settlement: String
    var leader: String
    var rollMode: String
    var phase: String
    var skill: String
    var dc: Int
}

@JsExport
class CheckModel(val value: AnyObject) : DataModel(value) {
    companion object {
        @Suppress("unused")
        @JsStatic
        fun defineSchema() = buildSchema {
            int("dc")
            string("settlement")
            enum<KingdomSkill>("skill")
            enum<Leader>("leader")
            enum<RollMode>("rollMode")
            enum<KingdomPhase>("phase")
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

private fun getValidActivitySkills(
    ranks: KingdomSkillRanks,
    activity: KingdomActivity,
    ignoreSkillRequirements: Boolean,
): Set<KingdomSkill> =
    activity.skills.asSequence()
        .mapNotNull { (name, rank) ->
            KingdomSkill.fromString(name)?.let {
                it to rank
            }
        }
        .filter {
            if (ignoreSkillRequirements) {
                true
            } else {
                ranks.resolve(it.first) >= it.second
            }
        }
        .map { it.first }
        .toSet()

private class ConfigureCheck(
    private val game: Game,
    private val kingdomActor: PF2ENpc,
    private val activityId: String?,
    private val skill: KingdomSkill?,
) : FormApp<CheckContext, CheckData>(
    title = "Roll Check",
    template = "components/forms/application-form.hbs",
    debug = true,
    dataModel = CheckModel::class.js,
    id = "kmCheck",
) {
    var data: CheckData

    init {
        val kingdom = kingdomActor.getKingdom()!!
        val isRulerVacant = kingdom.vacancies().ruler
        val activity = activityId?.let { kingdom.getActivity(it) }
        val skill = if (skill != null) {
            skill
        } else if (activity != null) {
            getValidActivitySkills(
                kingdom.parsedSkillRanks(),
                activity,
                kingdom.settings.kingdomIgnoreSkillRequirements,
            ).firstOrNull() ?: throw IllegalStateException("Activity without valid skills")
        } else {
            KingdomSkill.AGRICULTURE
        }
        val dc = if (activity == null) {
            calculateControlDC(
                kingdomLevel = kingdom.level,
                kingdomSize = kingdom.size,
                rulerVacant = isRulerVacant,
            )
        } else {
            activity.resolveDc(
                kingdomLevel = kingdom.level,
                kingdomSize = kingdom.size,
                rulerVacant = isRulerVacant,
                enemyArmyScoutingDcs = game.getPlayerSelectedArmies().map { it.system.scouting }
            )
        }
        val phase = if (activity == null) {
            KingdomPhase.EVENT
        } else {
            KingdomPhase.fromString(activity.phase) ?: KingdomPhase.EVENT
        }
        data = CheckData(
            settlement = kingdom.activeSettlement,
            leader = Leader.RULER.value,
            rollMode = RollMode.PUBLICROLL.value,
            skill = skill.value,
            phase = phase.value,
            dc = dc ?: throw IllegalStateException("Check window opened for activity without check"),
        )
    }

    override fun _onClickAction(event: PointerEvent, target: HTMLElement) {
        when (target.dataset["action"]) {
            "roll" -> {
                close()
            }

            "rollWithAssurance" -> {
                close()
            }
        }
    }

    override fun _preparePartContext(
        partId: String,
        context: HandlebarsRenderContext,
        options: HandlebarsRenderOptions
    ): Promise<CheckContext> = buildPromise {
        val parent = super._preparePartContext(partId, context, options).await()
        val kingdom = kingdomActor.getKingdom()!!
//        val modifiers = createAllModifiers(
//
//        )
//        val assuranceModifier = createAssuranceModifiers(
//
//        )
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
                value = fromCamelCase<RollMode>(data.phase),
            ).toContext(),
            skillInput = Select.fromEnum<KingdomSkill>(
                label = "Skill", // TODO: add modifier
                name = "skill",
                value = fromCamelCase<KingdomSkill>(data.skill),
            ).toContext(),
            dc = Select.dc(
                name = "dc",
                label = "DC",
                value = data.dc,
            ).toContext(),
            isFormValid = true,
        )
    }

    override fun onParsedSubmit(value: CheckData): Promise<Void> = buildPromise {
        data = value
        null
    }
}