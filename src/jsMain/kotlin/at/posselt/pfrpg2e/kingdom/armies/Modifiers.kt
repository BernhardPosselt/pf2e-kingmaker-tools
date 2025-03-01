package at.posselt.pfrpg2e.kingdom.armies

import com.foundryvtt.pf2e.actor.PF2EArmy

private fun PF2EArmy.effectBadge(effectSlug: String): Int? =
    itemTypes.effect.find { it.slug == effectSlug }?.badge?.value

fun PF2EArmy.miredValue(): Int? =
    effectBadge("mired")

fun PF2EArmy.wearyValue(): Int? =
    effectBadge("weary")
