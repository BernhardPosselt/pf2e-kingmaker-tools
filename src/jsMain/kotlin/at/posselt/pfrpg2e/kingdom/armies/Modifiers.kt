package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.kingdom.Modifier
import com.foundryvtt.pf2e.actor.PF2EArmy

private fun PF2EArmy.effectBadge(effectSlug: String): Int? =
    itemTypes.effect.find { it.slug == effectSlug }?.badge?.value

fun PF2EArmy.getEffectModifiers(): Array<Modifier> {
    val result = mutableListOf<Modifier>()
    effectBadge("mired")?.also {
        result.add(
            Modifier(
                name = "Mired",
                type = "circumstance",
                phases = arrayOf("army"),
                enabled = true,
                value = -it,
                activities = arrayOf("deploy-army"),
            )
        )
    }
    effectBadge("weary")?.also {
        result.add(
            Modifier(
                name = "Weary",
                type = "circumstance",
                phases = arrayOf("army"),
                enabled = true,
                value = -it,
            )
        )
    }
    return result.toTypedArray()
}