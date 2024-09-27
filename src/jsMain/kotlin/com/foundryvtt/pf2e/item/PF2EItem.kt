package com.foundryvtt.pf2e.item

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.documents.Item
import com.foundryvtt.core.fromUuid
import js.objects.jso
import kotlinx.coroutines.await
import kotlin.js.Promise

// this has no class to check in an instanceof check since it's a hack in the 2e system
// you need to match against actual instances
open external class PF2EItem : Item {
    companion object : DocumentStatic<Item>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2EItem>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2EItem?>
    val sourceId: String
    val quantity: Int
    val slug: String
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2EItem.update(data: PF2EItem, operation: DatabaseUpdateOperation = jso()): Promise<PF2EItem?> =
    update(data as AnyObject, operation)

suspend fun itemFromUuid(uuid: String): PF2EItem? =
    fromUuid(uuid).await()
        ?.takeIf {
            when (it) {
                is PF2EAction -> true
                is PF2EAffliction -> true
                is PF2EArmor -> true
                is PF2EBackpack -> true
                is PF2ECampaignFeature -> true
                is PF2ECondition -> true
                is PF2EConsumable -> true
                is PF2EEffect -> true
                is PF2EEquipment -> true
                is PF2EShield -> true
                is PF2EWeapon -> true
                is PF2ETreasure -> true
                else -> false
            }
        }
        ?.unsafeCast<PF2EItem?>()