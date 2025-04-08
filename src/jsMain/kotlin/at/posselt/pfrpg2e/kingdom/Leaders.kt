package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.settings.pfrpg2eKingdomCampingWeather
import com.foundryvtt.core.Game

fun Game.getActiveLeader(): Leader? =
    settings.pfrpg2eKingdomCampingWeather
        .getKingdomActiveLeader()
        ?.let { Leader.fromString(it) }
