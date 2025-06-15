package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.KingdomSkillRank
import at.posselt.pfrpg2e.data.kingdom.Ruin
import at.posselt.pfrpg2e.data.kingdom.structures.AvailableItemsRule
import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.data.kingdom.structures.Construction
import at.posselt.pfrpg2e.data.kingdom.structures.IncreaseResourceDice
import at.posselt.pfrpg2e.data.kingdom.structures.ItemGroup
import at.posselt.pfrpg2e.data.kingdom.structures.ReduceUnrestBy
import at.posselt.pfrpg2e.data.kingdom.structures.RuinAmount
import at.posselt.pfrpg2e.data.kingdom.structures.Structure
import at.posselt.pfrpg2e.data.kingdom.structures.StructureBonus
import at.posselt.pfrpg2e.data.kingdom.structures.StructureTrait
import at.posselt.pfrpg2e.takeIfInstance
import at.posselt.pfrpg2e.utils.getAppFlag
import at.posselt.pfrpg2e.utils.setAppFlag
import at.posselt.pfrpg2e.utils.t
import at.posselt.pfrpg2e.utils.unsetAppFlag
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.utils.deepClone
import com.foundryvtt.core.utils.mergeObject
import com.foundryvtt.pf2e.actor.PF2ENpc
import js.array.toTypedArray
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlin.contracts.contract

typealias StructureActor = PF2ENpc

@Suppress(
    "NOTHING_TO_INLINE",
    "CANNOT_CHECK_FOR_EXTERNAL_INTERFACE",
    "CANNOT_CHECK_FOR_ERASED",
    "ERROR_IN_CONTRACT_DESCRIPTION"
)
fun isStructureRef(obj: RawStructure): Boolean {
    contract {
        returns(true) implies (obj is StructureRef)
    }
    return obj.unsafeCast<AnyObject>()["ref"] is String
}

class StructureParsingException(message: String) : Exception(message)

fun StructureActor.getRawResolvedStructureData(): RawStructureData? {
    val data = getRawStructureData()
    if (data == null) return null
    return if (isStructureRef(data)) {
        translatedStructures.find { it.id == data.ref }
            ?: throw StructureParsingException(t("kingdom.canNotFindStructureRef", recordOf("ref" to data.ref)))
    } else {
        data.unsafeCast<RawStructureData>()
    }
}

fun TokenDocument.isStructure() =
    actor?.takeIfInstance<StructureActor>()
        ?.isStructure() == true

fun StructureActor.isStructure() = getRawStructureData() != null

fun StructureActor.isSlowed() = itemTypes.condition.any { it.slug == "slowed" }

fun StructureActor.parseStructure(): Structure? {
    val baseActorUuid = parent
        ?.takeIfInstance<TokenDocument>()
        ?.baseActor
        ?.uuid
        ?: uuid
    return getRawResolvedStructureData()
        ?.parseStructure(
            inConstruction = isSlowed(),
            uuid = baseActorUuid,
            actorUuid = uuid,
            img = img,
            currentRp = hitPoints.value,
            constructedRp = hitPoints.max,
        )
}

fun RawStructureData.parseStructure(
    inConstruction: Boolean,
    uuid: String,
    actorUuid: String,
    img: String?,
    currentRp: Int,
    constructedRp: Int,
) = Structure(
    name = name,
    img = img,
    currentRp = currentRp,
    constructedRp = constructedRp,
    stacksWith = stacksWith,
    construction = Construction(
        skills = construction?.skills?.mapNotNull {
            KingdomSkill.fromString(it.skill)?.let { skill ->
                KingdomSkillRank(
                    skill = skill,
                    rank = it.proficiencyRank ?: 0,
                )
            }
        }?.toSet() ?: emptySet(),
        lumber = construction?.lumber ?: 0,
        luxuries = construction?.luxuries ?: 0,
        ore = construction?.ore ?: 0,
        stone = construction?.stone ?: 0,
        rp = construction?.rp ?: 0,
        dc = construction?.dc ?: 0,
    ),
    actorUuid = actorUuid,
    notes = notes,
    preventItemLevelPenalty = preventItemLevelPenalty == true,
    enableCapitalInvestment = enableCapitalInvestment == true,
    bonuses = (skillBonusRules?.mapNotNull { rule ->
        KingdomSkill.fromString(rule.skill)?.let { skill ->
            StructureBonus(
                skill = skill,
                activity = rule.activity,
                value = rule.value,
            )
        }
    }?.toSet() ?: emptySet()) + (activityBonusRules?.map { rule ->
        StructureBonus(
            activity = rule.activity,
            value = rule.value,
            skill = null,
        )
    }?.toSet() ?: emptySet()),
    availableItemsRules = availableItemsRules?.map { rule ->
        val group = rule.group?.let { group -> ItemGroup.fromString(group) }
        AvailableItemsRule(
            value = rule.value,
            group = group,
            maximumStacks = rule.maximumStacks ?: 3,
            alwaysStacks = rule.alwaysStacks == true,
        )
    }?.toSet() ?: emptySet(),
    settlementEventBonus = settlementEventRules?.firstOrNull()?.value ?: 0,
    leadershipActivityBonus = leadershipActivityRules?.firstOrNull()?.value ?: 0,
    storage = CommodityStorage(
        ore = storage?.ore ?: 0,
        food = storage?.food ?: 0,
        lumber = storage?.lumber ?: 0,
        stone = storage?.stone ?: 0,
        luxuries = storage?.luxuries ?: 0,
    ),
    increaseLeadershipActivities = increaseLeadershipActivities == true,
    isBridge = isBridge == true,
    consumptionReduction = consumptionReduction ?: 0,
    unlockActivities = unlockActivities?.toSet() ?: emptySet(),
    traits = traits?.mapNotNull { StructureTrait.fromString(it) }?.toSet() ?: emptySet(),
    lots = lots,
    affectsEvents = affectsEvents == true,
    affectsDowntime = affectsDowntime == true,
    reducesUnrest = reducesUnrest == true,
    reducesRuin = reducesRuin == true,
    level = level,
    upgradeFrom = upgradeFrom?.toSet() ?: emptySet(),
    reduceUnrestBy = reduceUnrestBy?.let { unrest ->
        ReduceUnrestBy(
            value = unrest.value,
            moreThanOncePerTurn = unrest.moreThanOncePerTurn == true,
            note = unrest.note,
        )
    },
    reduceRuinBy = reduceRuinBy?.let { ruin ->
        RuinAmount(
            value = ruin.value,
            ruin = Ruin.fromString(ruin.ruin),
            moreThanOncePerTurn = ruin.moreThanOncePerTurn == true,
        )
    },
    gainRuin = gainRuin?.let { ruin ->
        RuinAmount(
            value = ruin.value,
            ruin = Ruin.fromString(ruin.ruin),
            moreThanOncePerTurn = ruin.moreThanOncePerTurn == true,
        )
    },
    increaseResourceDice = IncreaseResourceDice(
        village = increaseResourceDice?.village ?: 0,
        town = increaseResourceDice?.town ?: 0,
        city = increaseResourceDice?.city ?: 0,
        metropolis = increaseResourceDice?.metropolis ?: 0,
    ),
    consumptionReductionStacks = consumptionReductionStacks == true,
    ignoreConsumptionReductionOf = ignoreConsumptionReductionOf?.toSet() ?: emptySet(),
    maximumCivicRdLimit = maximumCivicRdLimit ?: 0,
    increaseMinimumSettlementActions = increaseMinimumSettlementActions ?: 0,
    slowed = inConstruction,
    uuid = uuid,
    id = id,
)

fun Game.getImportedStructures() =
    actors.contents
        .filterIsInstance<StructureActor>()
        .mapNotNull { it.parseStructure() }

suspend fun Game.importStructures(): Array<Actor> {
    val folder = Folder.create(
        recordOf(
            "name" to "Structures",
            "type" to "Actor",
            "parent" to null,
            "color" to null,
        )
    ).await()
    val data = packs.get("${Config.moduleId}.kingmaker-tools-structures")
        ?.getDocuments()
        ?.await()
        ?.asSequence()
        ?.filterIsInstance<StructureActor>()
        ?.map {
            val obj = deepClone(it.toObject())
            val update = recordOf(
                "folder" to folder.id,
                "permission" to 0,
                "ownership" to recordOf("default" to 3),
            )
            mergeObject(obj, update)
        }
        ?.toTypedArray()
        ?: emptyArray()
    return Actor.createDocuments(data).await()
}

fun StructureActor.getRawStructureData(): RawStructure? =
    getAppFlag("structureData")

suspend fun StructureActor.setStructureData(data: RawStructure) {
    setAppFlag("structureData", data)
}

suspend fun StructureActor.unsetStructureData() {
    unsetAppFlag("structureData")
}