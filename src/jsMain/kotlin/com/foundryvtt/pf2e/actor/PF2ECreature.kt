package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.pf2e.item.PF2EEquipment
import com.foundryvtt.pf2e.item.PF2EItem
import js.objects.ReadonlyRecord
import kotlin.js.Promise

external interface PF2ECreature {
    val skills: ReadonlyRecord<String, PF2EAttribute>
    val level: Int
    val itemTypes: ItemTypes
    val perception: PF2EAttribute
    val name: String
    val uuid: String
    val img: String?

    fun addToInventory(value: AnyObject, container: PF2EEquipment?, stack: Boolean): Promise<PF2EItem?>
}