package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRanks
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawActivity
import at.posselt.pfrpg2e.kingdom.canBePerformed
import at.posselt.pfrpg2e.kingdom.data.ChosenFeat
import at.posselt.pfrpg2e.kingdom.data.ChosenFeature
import at.posselt.pfrpg2e.kingdom.label
import com.foundryvtt.core.ui.enrichHtml
import js.objects.JsPlainObject
import js.objects.Object
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Suppress("unused")
@JsPlainObject
external interface ActivityContext {
    val label: String
    val disabled: Boolean
    val description: String
    val actions: Int
    val id: String
    val special: String?
    val automationNotes: String?
    val requirement: String?
    val fortune: Boolean
    val criticalSuccess: String?
    val success: String?
    val failure: String?
    val criticalFailure: String?
    val isCollectTaxes: Boolean
    val order: Int?
    val open: Boolean
    val hasCheck: Boolean
}

@Suppress("unused")
@JsPlainObject
external interface ActivitiesContext {
    val commerce: Array<ActivityContext>
    val leadership: Array<ActivityContext>
    val region: Array<ActivityContext>
    val civic: Array<ActivityContext>
    val army: Array<ActivityContext>
    val upkeep: Array<ActivityContext>
}

private suspend fun toActivityContext(
    activity: RawActivity,
    kingdomLevel: Int,
    allowCapitalInvestment: Boolean,
    kingdomSkillRanks: KingdomSkillRanks,
    kingdom: KingdomData,
    chosenFeats: List<ChosenFeat>,
    chosenFeatures: List<ChosenFeature>,
    openedActivityDetails: MutableSet<String>,
): ActivityContext = coroutineScope {
    val descriptionP = async { enrichHtml(activity.description) }
    val criticalSuccessP = async { activity.criticalSuccess?.msg?.let { enrichHtml(it) } }
    val successP = async { activity.success?.msg?.let { enrichHtml(it) } }
    val failureP = async { activity.failure?.msg?.let { enrichHtml(it) } }
    val criticalFailureP = async { activity.criticalFailure?.msg?.let { enrichHtml(it) } }
    val description = descriptionP.await()
    val criticalSuccess = criticalSuccessP.await()
    val success = successP.await()
    val failure = failureP.await()
    val criticalFailure = criticalFailureP.await()
    ActivityContext(
        id = activity.id,
        label = activity.label(
            kingdomLevel = kingdomLevel,
            activity = activity,
            chosenFeatures = chosenFeatures,
        ),
        actions = activity.actions ?: 1,
        description = description,
        special = activity.special,
        automationNotes = activity.automationNotes,
        disabled = !activity.canBePerformed(
            allowCapitalInvestment = allowCapitalInvestment,
            kingdomSkillRanks = kingdomSkillRanks,
            kingdom = kingdom,
            chosenFeats = chosenFeats,
        ),
        fortune = activity.fortune,
        requirement = activity.requirement,
        criticalSuccess = criticalSuccess,
        success = success,
        failure = failure,
        criticalFailure = criticalFailure,
        isCollectTaxes = activity.id == "collect-taxes",
        order = activity.order,
        open = activity.id in openedActivityDetails,
        hasCheck = Object.keys(activity.skills).isNotEmpty(),
    )
}

suspend fun activitiesToActivityContext(
    activities: List<RawActivity>,
    allowCapitalInvestment: Boolean,
    kingdomSkillRanks: KingdomSkillRanks,
    chosenFeatures: List<ChosenFeature>,
    openedActivityDetails: MutableSet<String>,
    kingdom: KingdomData,
    chosenFeats: List<ChosenFeat>,
) = coroutineScope {
    activities
        .map {
            async {
                toActivityContext(
                    activity = it,
                    kingdomLevel = kingdom.level,
                    allowCapitalInvestment = allowCapitalInvestment,
                    kingdomSkillRanks = kingdomSkillRanks,
                    chosenFeatures = chosenFeatures,
                    openedActivityDetails = openedActivityDetails,
                    kingdom = kingdom,
                    chosenFeats = chosenFeats,
                )
            }
        }
        .awaitAll()
        .sortedWith(compareBy<ActivityContext> { it.order ?: Int.MAX_VALUE }.thenBy { it.label })
        .toTypedArray()
}

suspend fun toActivitiesContext(
    activities: List<RawActivity>,
    activityBlacklist: Set<String>,
    unlockedActivities: Set<String>,
    kingdom: KingdomData,
    chosenFeats: List<ChosenFeat>,
    allowCapitalInvestment: Boolean,
    kingdomSkillRanks: KingdomSkillRanks,
    chosenFeatures: List<ChosenFeature>,
    openedActivityDetails: MutableSet<String>,
): ActivitiesContext = coroutineScope {
    val activitiesByPhase = activities
        .asSequence()
        .filter { it.id !in activityBlacklist || it.id in unlockedActivities }
        .groupBy { it.phase }
    val commerce = activitiesToActivityContext(
        activitiesByPhase[KingdomPhase.COMMERCE.value].orEmpty(),
        allowCapitalInvestment,
        kingdomSkillRanks,
        chosenFeatures,
        openedActivityDetails,
        kingdom,
        chosenFeats,
    )
    val leadership = activitiesToActivityContext(
        activitiesByPhase[KingdomPhase.LEADERSHIP.value].orEmpty(),
        allowCapitalInvestment,
        kingdomSkillRanks,
        chosenFeatures,
        openedActivityDetails,
        kingdom,
        chosenFeats,
    )
    val civic = activitiesToActivityContext(
        activitiesByPhase[KingdomPhase.CIVIC.value].orEmpty(),
        allowCapitalInvestment,
        kingdomSkillRanks,
        chosenFeatures,
        openedActivityDetails,
        kingdom,
        chosenFeats,
    )
    val region = activitiesToActivityContext(
        activitiesByPhase[KingdomPhase.REGION.value].orEmpty(),
        allowCapitalInvestment,
        kingdomSkillRanks,
        chosenFeatures,
        openedActivityDetails,
        kingdom,
        chosenFeats,
    )
    val army = activitiesToActivityContext(
        activitiesByPhase[KingdomPhase.ARMY.value].orEmpty(),
        allowCapitalInvestment,
        kingdomSkillRanks,
        chosenFeatures,
        openedActivityDetails,
        kingdom,
        chosenFeats,
    )
    val upkeep = activitiesToActivityContext(
        activitiesByPhase[KingdomPhase.UPKEEP.value].orEmpty(),
        allowCapitalInvestment,
        kingdomSkillRanks,
        chosenFeatures,
        openedActivityDetails,
        kingdom,
        chosenFeats,
    )
    ActivitiesContext(
        upkeep = upkeep,
        commerce = commerce,
        leadership = leadership,
        region = region,
        civic = civic,
        army = army,
    )
}