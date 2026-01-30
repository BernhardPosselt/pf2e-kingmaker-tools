package at.posselt.pfrpg2e.kingdom.structures

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawSettlement {
    var sceneId: String
    var lots: Int
    var level: Int
    var type: String // 'capital' | 'settlement';
    var secondaryTerritory: Boolean
    var manualSettlementLevel: Boolean?
    var waterBorders: Int
}
