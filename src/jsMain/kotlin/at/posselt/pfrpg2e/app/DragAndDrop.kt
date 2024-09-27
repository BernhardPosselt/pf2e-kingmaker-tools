package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.utils.fromUuidTypeSafe
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.fromUuid
import com.foundryvtt.pf2e.actor.PF2EActor
import com.foundryvtt.pf2e.item.*
import kotlinx.coroutines.await


sealed interface DocumentRef<T : Document> {
    suspend fun getDocument(): T
}

class GenericRef(
    val data: AnyObject,
    val type: String,
    val uuid: String,
    val itemType: String? = null,
    val dragstartSelector: String? = null,
)

fun toGenericRef(dropData: String): GenericRef? {
    val data = JSON.parse<AnyObject>(dropData)
    val type = data["type"]
    val uuid = data["uuid"]
    val itemType = data["itemType"]
    val selector = data["selector"]
    return if (uuid is String && type is String) {
        val src = if (selector is String) selector else null
        if (itemType is String) {
            GenericRef(data = data, type = type, uuid = uuid, itemType = itemType, dragstartSelector = src)
        } else {
            GenericRef(data = data, type = type, uuid = uuid, dragstartSelector = src)
        }
    } else {
        null
    }
}

data class ActorRef(val uuid: String) : DocumentRef<PF2EActor> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EActor>(uuid)!!
}

data class JournalEntryRef(val uuid: String) : DocumentRef<JournalEntry> {
    override suspend fun getDocument() = fromUuidTypeSafe<JournalEntry>(uuid)!!
}

data class ActionItemRef(val uuid: String) : DocumentRef<PF2EAction> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EAction>(uuid)!!
}

data class CampaignFeatureItemRef(val uuid: String) : DocumentRef<PF2ECampaignFeature> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2ECampaignFeature>(uuid)!!
}

data class ConditionItemRef(val uuid: String) : DocumentRef<PF2ECondition> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2ECondition>(uuid)!!
}

data class ConsumableItemRef(val uuid: String) : DocumentRef<PF2EConsumable> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EConsumable>(uuid)!!
}

data class EffectItemRef(val uuid: String) : DocumentRef<PF2EEffect> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EEffect>(uuid)!!
}

data class EquipmentItemRef(val uuid: String) : DocumentRef<PF2EEquipment> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EEquipment>(uuid)!!
}

data class AfflictionItemRef(val uuid: String) : DocumentRef<PF2EAffliction> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EAffliction>(uuid)!!
}

data class WeaponItemRef(val uuid: String) : DocumentRef<PF2EWeapon> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EWeapon>(uuid)!!
}

data class ArmorItemRef(val uuid: String) : DocumentRef<PF2EArmor> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EArmor>(uuid)!!
}

data class ShieldItemRef(val uuid: String) : DocumentRef<PF2EShield> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EShield>(uuid)!!
}

data class TreasureItemRef(val uuid: String) : DocumentRef<PF2ETreasure> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2ETreasure>(uuid)!!
}

data class BackpackItemRef(val uuid: String) : DocumentRef<PF2EBackpack> {
    override suspend fun getDocument() = fromUuidTypeSafe<PF2EBackpack>(uuid)!!
}

data class ItemRef(val uuid: String) : DocumentRef<dynamic> {
    override suspend fun getDocument() = fromUuid(uuid).await()
}