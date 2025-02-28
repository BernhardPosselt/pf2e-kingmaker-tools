package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.utils.setAppFlag
import com.foundryvtt.core.Game
import com.foundryvtt.pf2e.actor.PF2EArmy
import com.foundryvtt.pf2e.item.PF2ECampaignFeature
import kotlinx.coroutines.await


val PF2ECampaignFeature.isArmyTactic: Boolean
    get() = system.campaign == "kingmaker" &&
            system.category == "army-tactic"

fun PF2EArmy.hasTactic(tactic: PF2ECampaignFeature): Boolean =
    itemTypes.campaignFeature.any { it.slug == tactic.slug }

suspend fun Game.getAllAvailableArmyTactics(): Array<PF2ECampaignFeature> {
    // haha
    val packTactics = packs.get("pf2e.kingmaker-features")
        ?.getDocuments()
        ?.await()
        ?.filterIsInstance<PF2ECampaignFeature>() ?: emptyList()
    val worldTactics = items.contents.asSequence()
        .filterIsInstance<PF2ECampaignFeature>()
        .filter { it.isArmyTactic }
    return (packTactics + worldTactics).toTypedArray()
}