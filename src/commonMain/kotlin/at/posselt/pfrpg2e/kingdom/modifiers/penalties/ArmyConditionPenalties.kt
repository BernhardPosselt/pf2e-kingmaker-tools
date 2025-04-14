package at.posselt.pfrpg2e.kingdom.modifiers.penalties

import at.posselt.pfrpg2e.data.kingdom.KingdomPhase
import at.posselt.pfrpg2e.kingdom.modifiers.Modifier
import at.posselt.pfrpg2e.kingdom.modifiers.ModifierType
import at.posselt.pfrpg2e.kingdom.modifiers.expressions.Eq


data class ArmyConditionInfo(
    val armyName: String,
    val armyUuid: String,
    val miredValue: Int,
    val wearyValue: Int,
    val wearyLabel: String,
    val miredLabel: String,
)

fun createArmyConditionPenalties(
    info: ArmyConditionInfo,
): List<Modifier> {
    val modifiers = mutableListOf<Modifier>()
    if (info.miredValue > 0) {
        modifiers.add(
            Modifier(
                name = info.miredLabel,
                type = ModifierType.CIRCUMSTANCE,
                value = -info.miredValue,
                id = "mired-$info.armyUuid",
                applyIf = listOf(
                    Eq("@activity", "deploy-army"),
                )
            )
        )
    }
    if (info.wearyValue > 0) {
        modifiers.add(
            Modifier(
                name = info.wearyLabel,
                type = ModifierType.CIRCUMSTANCE,
                value = -info.wearyValue,
                id = "weary-$info.armyUuid",
                applyIf = listOf(
                    Eq("@phase", KingdomPhase.ARMY.value),
                )
            )
        )
    }
    return modifiers
}