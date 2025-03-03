package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemsRule
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.Construction
import at.posselt.pfrpg2e.data.kingdom.structures.ConstructionSkill
import at.posselt.pfrpg2e.data.kingdom.structures.IncreaseResourceDice
import at.posselt.pfrpg2e.data.kingdom.structures.ItemGroup
import at.posselt.pfrpg2e.data.kingdom.structures.ReduceUnrestBy
import at.posselt.pfrpg2e.data.kingdom.structures.RuinAmount
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.data.kingdom.structures.StructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.StructureTrait
import at.posselt.pfrpg2e.kingdom.getRawStructureData
import at.posselt.pfrpg2e.utils.asAnyObject
import com.foundryvtt.core.AnyObject
import com.foundryvtt.pf2e.actor.PF2ENpc
import kotlin.contracts.contract


data class ActorAndStructure(
    val actor: PF2ENpc,
    val structure: RawStructureData,
)

@Suppress(
    "NOTHING_TO_INLINE",
    "CANNOT_CHECK_FOR_EXTERNAL_INTERFACE",
    "CANNOT_CHECK_FOR_ERASED",
    "ERROR_IN_CONTRACT_DESCRIPTION"
)
private fun isStructureRef(obj: AnyObject): Boolean {
    contract {
        returns(true) implies (obj is StructureRef)
    }
    return obj["ref"] is String
}

class StructureParsingException(message: String) : Exception(message)

fun PF2ENpc.getRawResolvedStructureData(): RawStructureData? {
    val data = getRawStructureData()
    if (data == null) return null
    val record = data.asAnyObject()
    return if (isStructureRef(record)) {
        structures.find { it.name == record.ref }
            ?: throw StructureParsingException("Could not find existing structure with ref ${record.ref}")
    } else {
        data.unsafeCast<RawStructureData>()
    }
    return null
}

fun PF2ENpc.isStructure() = getRawStructureData() != null

fun PF2ENpc.getStructure(): Structure? {
    return getRawResolvedStructureData()?.let {
        Structure(
            name = it.name,
            stacksWith = it.stacksWith,
            construction = Construction(
                skills = it.construction?.skills?.mapNotNull {
                    KingdomSkill.fromString(it.skill)?.let { skill ->
                        ConstructionSkill(
                            skill = skill,
                            minRank = it.proficiencyRank ?: 0,
                        )
                    }
                }?.toSet() ?: emptySet(),
                lumber = it.construction?.lumber ?: 0,
                luxuries = it.construction?.luxuries ?: 0,
                ore = it.construction?.ore ?: 0,
                stone = it.construction?.stone ?: 0,
                rp = it.construction?.rp ?: 0,
                dc = it.construction?.dc ?: 0,
            ),
            notes = it.notes,
            preventItemLevelPenalty = it.preventItemLevelPenalty == true,
            enableCapitalInvestment = it.enableCapitalInvestment == true,
            bonuses = (it.skillBonusRules?.mapNotNull { rule ->
                KingdomSkill.fromString(rule.skill)?.let { skill ->
                    StructureBonus(
                        skill = skill,
                        activity = rule.activity,
                        value = rule.value,
                    )
                }
            }?.toSet() ?: emptySet()) + (it.activityBonusRules?.mapNotNull { rule ->
                StructureBonus(
                    activity = rule.activity,
                    value = rule.value,
                    skill = null,
                )
            }?.toSet() ?: emptySet()),
            availableItemsRules = it.availableItemsRules?.mapNotNull { rule ->
                val group = rule.group?.let { group -> ItemGroup.fromString(group) }
                AvailableItemsRule(
                    value = rule.value,
                    group = group,
                    maximumStacks = rule.maximumStacks,
                )
            }?.toSet() ?: emptySet(),
            settlementEventBonus = it.settlementEventRules?.firstOrNull()?.value ?: 0,
            leadershipActivityBonus = it.leadershipActivityRules?.firstOrNull()?.value ?: 0,
            storage = CommodityStorage(
                ore = it.storage?.ore ?: 0,
                food = it.storage?.food ?: 0,
                lumber = it.storage?.lumber ?: 0,
                stone = it.storage?.stone ?: 0,
                luxuries = it.storage?.luxuries ?: 0,
            ),
            increaseLeadershipActivities = it.increaseLeadershipActivities == true,
            isBridge = it.isBridge == true,
            consumptionReduction = it.consumptionReduction ?: 0,
            unlockActivities = it.unlockActivities?.toSet() ?: emptySet(),
            traits = it.traits?.mapNotNull { StructureTrait.fromString(it) }?.toSet() ?: emptySet(),
            lots = it.lots,
            affectsEvents = it.affectsEvents == true,
            affectsDowntime = it.affectsDowntime == true,
            reducesUnrest = it.reducesUnrest == true,
            reducesRuin = it.reducesRuin == true,
            level = it.level,
            upgradeFrom = it.upgradeFrom?.toSet() ?: emptySet(),
            reduceUnrestBy = it.reduceUnrestBy?.let { unrest ->
                ReduceUnrestBy(
                    value = unrest.value,
                    moreThanOncePerTurn = unrest.moreThanOncePerTurn == true,
                    note = unrest.note,
                )
            },
            reduceRuinBy = it.reduceRuinBy?.let { ruin ->
                RuinAmount(
                    value = ruin.value,
                    ruin = Ruin.fromString(ruin.ruin) ?: Ruin.CRIME,
                    moreThanOncePerTurn = ruin.moreThanOncePerTurn == true,
                )
            },
            gainRuin = it.gainRuin?.let { ruin ->
                RuinAmount(
                    value = ruin.value,
                    ruin = Ruin.fromString(ruin.ruin) ?: Ruin.CRIME,
                    moreThanOncePerTurn = ruin.moreThanOncePerTurn == true,
                )
            },
            increaseResourceDice = IncreaseResourceDice(
                village = it.increaseResourceDice?.village ?: 0,
                town = it.increaseResourceDice?.town ?: 0,
                city = it.increaseResourceDice?.city ?: 0,
                metropolis = it.increaseResourceDice?.metropolis ?: 0,
            ),
            consumptionReductionStacks = it.consumptionReductionStacks == true,
            ignoreConsumptionReductionOf = it.ignoreConsumptionReductionOf?.toSet() ?: emptySet(),
        )
    }
}

fun PF2ENpc.getActorAndStructure(): ActorAndStructure? {
    val data = getRawResolvedStructureData()
    return data?.let {
        ActorAndStructure(
            actor = this,
            structure = it,
        )
    }
}

