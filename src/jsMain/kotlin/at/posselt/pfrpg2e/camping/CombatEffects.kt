package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.utils.buildUuid
import at.posselt.pfrpg2e.utils.postChatTemplate
import at.posselt.pfrpg2e.utils.t
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
            "enhance-weapons" -> CombatEffect(
                label = t("camping.enhanceWeapons"),
                target = t("camping.allies"),
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.ZKJlIqyFgbKDACnG"
            )

            "set-traps" -> CombatEffect(
                label = t("camping.setTraps"),
                target = t("camping.enemies"),
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.PSBOS7ZEl9RGWBqD"
            )

            "undead-guardians" -> CombatEffect(
                label = t("camping.undeadGuardians"),
                target = t("camping.numAllies", recordOf("count" to 1)),
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.KysTaC245mOnSnmE"
            )

            "water-hazards" -> CombatEffect(
                label = t("camping.waterHazards"),
                target = t("camping.enemies"),
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.LN6mH7Muj4hgvStt"
            )

            "maintain-armor" -> CombatEffect(
                label = t("camping.maintainArmor"),
                target = if (partyLevel < 3) {
                    t("camping.numAllies", recordOf("count" to 1))
                } else {
                    t("camping.numAllies", recordOf("count" to 1 + ((partyLevel - 1) / 2)))
                },
                uuid = "Compendium.pf2e-kingmaker-tools.kingmaker-tools-camping-effects.Item.wojV4NiAOYsnfFby"
            )

            else -> null
        }
    }
}

@Suppress("unused")
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
                    t("camping.undeadGuardiansAided"),
                    t("camping.undeadGuardiansDefended"),
                )
            )
        }
    }.awaitAll()
}