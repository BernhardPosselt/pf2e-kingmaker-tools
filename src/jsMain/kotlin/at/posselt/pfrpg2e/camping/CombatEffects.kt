package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.postChatTemplate
import com.foundryvtt.core.ui.TextEditor
import com.foundryvtt.pf2e.actor.PF2EActor
import js.objects.recordOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.js.JsPlainObject


private data class CombatEffect(
    val uuid: String,
    val target: String,
    val label: String,
)

private fun getCombatEffects(partyLevel: Int, activeActivities: Set<String>): List<CombatEffect> {
    return activeActivities.mapNotNull {
        when (it) {
            "Enhance Weapons" -> CombatEffect(
                label = it,
                target = "Allies",
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.ZKJlIqyFgbKDACnG"
            )

            "Set Traps" -> CombatEffect(
                label = it,
                target = "Enemies",
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.PSBOS7ZEl9RGWBqD"
            )

            "Undead Guardians" -> CombatEffect(
                label = it,
                target = "1 Ally",
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.KysTaC245mOnSnmE"
            )

            "Water Hazards" -> CombatEffect(
                label = it,
                target = "Enemies",
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.LN6mH7Muj4hgvStt"
            )

            "Maintain Armor" -> CombatEffect(
                label = it,
                target = if (partyLevel < 3) "1 Ally" else "${1 + ((partyLevel - 1) / 2)} Allies",
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.wojV4NiAOYsnfFby"
            )

            else -> null
        }
    }
}

@JsPlainObject
private external interface CombatEffectContext {
    val label: String
    val target: String
    val link: String
}

suspend fun postCombatEffects(
    activeActivities: Set<String>,
    partyLevel: Int,
) {
    val effects = getCombatEffects(partyLevel, activeActivities)
    if (effects.isNotEmpty()) {
        postChatTemplate(
            "chatmessages/combat-effects.hbs",
            templateContext = recordOf(
                "effects" to effects.map {
                    CombatEffectContext(
                        label = it.label,
                        link = buildUuid(it.uuid),
                        target = it.target,
                    )
                }.toTypedArray(),
            )
        )
    }
}

suspend fun removeCombatEffects(actors: List<PF2EActor>) = coroutineScope {
    actors.map { actor ->
        async {
            actor.removeEffectsByName(
                setOf(
                    "Undead Guardians (Aided)",
                    "Undead Guardians (Defended)",
                )
            )
        }
    }.awaitAll()
}