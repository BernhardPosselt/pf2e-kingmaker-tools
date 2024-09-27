package com.foundryvtt.pf2e.item

import kotlinx.js.JsPlainObject

val physicalItems = setOf(
    PF2EArmor::class,
    PF2EBackpack::class,
    PF2EConsumable::class,
    PF2EEquipment::class,
    PF2EShield::class,
    PF2EWeapon::class,
    PF2ETreasure::class,
)

@JsPlainObject
external interface PhysicalItemData {
    var quantity: Int
}

external interface PhysicalItem<T : PhysicalItemData> {
    val system: T
}

fun getPhysicalItemData(item: PF2EItem): PhysicalItemData? =
    when (item) {
        is PF2EArmor -> item.system
        is PF2EBackpack -> item.system
        is PF2EConsumable -> item.system
        is PF2EEquipment -> item.system
        is PF2EShield -> item.system
        is PF2EWeapon -> item.system
        is PF2ETreasure -> item.system
        else -> null
    }
