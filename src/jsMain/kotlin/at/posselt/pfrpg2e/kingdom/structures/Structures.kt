package at.posselt.pfrpg2e.kingdom.structures

import at.posselt.pfrpg2e.kingdom.getStructure
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

fun PF2ENpc.getParsedStructureData(): RawStructureData? {
    val data = getStructure()
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

fun PF2ENpc.getActorAndStructure(): ActorAndStructure? {
    val data = getParsedStructureData()
    return data?.let {
        ActorAndStructure(
            actor = this,
            structure = it,
        )
    }
}