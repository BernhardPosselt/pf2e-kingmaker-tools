package at.posselt.pfrpg2e.macros

import at.posselt.pfrpg2e.actor.hasFeat
import at.posselt.pfrpg2e.actor.investedArmor
import at.posselt.pfrpg2e.actor.partyMembers
import at.posselt.pfrpg2e.actor.proficiency
import at.posselt.pfrpg2e.app.awaitablePrompt
import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.RadioInput
import at.posselt.pfrpg2e.app.forms.Select
import at.posselt.pfrpg2e.app.forms.SelectOption
import at.posselt.pfrpg2e.app.forms.formContext
import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.camping.findCurrentRegion
import at.posselt.pfrpg2e.camping.getCamping
import at.posselt.pfrpg2e.camping.getCampingActor
import at.posselt.pfrpg2e.data.actor.Proficiency
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import at.posselt.pfrpg2e.data.regions.Terrain
import at.posselt.pfrpg2e.fromCamelCase
import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.Actor
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.ui
import com.foundryvtt.pf2e.actions.CheckDC
import com.foundryvtt.pf2e.actions.SingleCheckActionUseOptions
import com.foundryvtt.pf2e.actor.PF2ECharacter
import com.foundryvtt.pf2e.pf2e
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.js.JsPlainObject

@JsPlainObject
private external interface SubsistData {
    val skill: String
    val dc: Int
}

@JsPlainObject
private external interface AskActorSubmitData {
    val name: String
}

@JsPlainObject
private external interface AskActorContext {
    val formRows: Array<FormElementContext>
}

private suspend fun selectActor(choices: List<PF2ECharacter>): PF2ECharacter? {
    return awaitablePrompt<AskActorSubmitData, PF2ECharacter?>(
        title = "Select Character",
        templatePath = "components/forms/form.hbs",
        templateContext = AskActorContext(
            formRows = choices
                .sortedBy { it.name }
                .mapIndexed { index, actor ->
                    RadioInput(
                        name = "name",
                        label = actor.name,
                        value = actor.name,
                        checked = index == 0,
                    ).toContext()
                }.toTypedArray()
        ).unsafeCast<AnyObject>(),
    ) { data ->
        choices.find { actor -> actor.name == data.name }
    }
}

suspend fun subsistMacro(game: Game, actor: Actor?) {
    val chosenActor = if (actor is PF2ECharacter) {
        actor
    } else {
        val ownedActors = game.partyMembers()
            .filter { it.isOwner }
        if (ownedActors.size == 1) {
            ownedActors.first()
        } else if (ownedActors.size > 1) {
            selectActor(ownedActors)
        } else {
            null
        }
    }
    if (chosenActor == null) {
        ui.notifications.error("Please select a Character")
        return
    }
    val camping = game.getCampingActor()?.getCamping()
    val skills = chosenActor.skills.asSequence()
        .map { SelectOption(label = it.component2().label, value = it.component1()) }
        .toList()
    val currentRegion = camping?.findCurrentRegion()
    val defaultDc = currentRegion?.zoneDc ?: 15
    val isUrban = currentRegion?.terrain?.let { fromCamelCase<Terrain>(it) } == Terrain.URBAN
    val defaultSkill = if (isUrban) "society" else "survival"
    prompt<SubsistData, Unit>(
        title = "Subsist",
        templatePath = "components/forms/form.hbs",
        templateContext = recordOf(
            "formRows" to formContext(
                Select(
                    name = "skill",
                    label = "Skill",
                    options = skills,
                    value = defaultSkill,
                ),
                Select.dc(
                    name = "dc",
                    label = "DC",
                    value = defaultDc,
                )
            )
        )
    ) {
        val options = SingleCheckActionUseOptions(
            difficultyClass = CheckDC(value = it.dc),
            rollOptions = arrayOf("action:subsist:after-exploration"),
            statistic = it.skill,
            actors = arrayOf(chosenActor),
        )
        val result = game.pf2e.actions.get("subsist")?.use(options)?.await()?.firstOrNull()
        val degree = fromCamelCase<DegreeOfSuccess>(result?.outcome!!)!!
        val provisions = calculateProvisions(
            survivalProficiency = chosenActor.skills["survival"]?.proficiency ?: Proficiency.UNTRAINED,
            isForager = chosenActor.hasFeat("forager"),
            hasCoyoteCloak = chosenActor.investedArmor("coyote-cloak"),
            hasCoyoteCloakGreat = chosenActor.investedArmor("coyote-cloak-greater"),
            degree = degree
        )
        if (provisions > 0) {
            postChatTemplate(
                templateContext = recordOf(
                    "provisions" to provisions,
                    "actorUuid" to chosenActor.uuid,
                    "actorName" to chosenActor.name,
                ),
                templatePath = "chatmessages/subsist.hbs",
                speaker = chosenActor
            )
        }
    }
}

private fun calculateProvisions(
    isForager: Boolean,
    hasCoyoteCloak: Boolean,
    hasCoyoteCloakGreat: Boolean,
    degree: DegreeOfSuccess,
    survivalProficiency: Proficiency = Proficiency.UNTRAINED,
): Int {
    val criticalMultiplier = if (hasCoyoteCloakGreat) {
        4
    } else if (hasCoyoteCloak) {
        2
    } else {
        1
    }

    val increaseSuccessBy = if (isForager) {
        when (survivalProficiency) {
            Proficiency.UNTRAINED -> 4
            Proficiency.TRAINED -> 4
            Proficiency.EXPERT -> 8
            Proficiency.MASTER -> 16
            Proficiency.LEGENDARY -> 32
        }
    } else {
        0
    }
    val increaseCriticalSuccessBy = if (isForager) {
        when (survivalProficiency) {
            Proficiency.UNTRAINED -> 8
            Proficiency.TRAINED -> 8
            Proficiency.EXPERT -> 16
            Proficiency.MASTER -> 32
            Proficiency.LEGENDARY -> 64
        }
    } else {
        1
    }
    return when (degree) {
        DegreeOfSuccess.CRITICAL_FAILURE -> 0
        DegreeOfSuccess.FAILURE -> 0
        DegreeOfSuccess.SUCCESS -> 1 + increaseSuccessBy
        DegreeOfSuccess.CRITICAL_SUCCESS -> 1 + increaseCriticalSuccessBy * criticalMultiplier
    }
}