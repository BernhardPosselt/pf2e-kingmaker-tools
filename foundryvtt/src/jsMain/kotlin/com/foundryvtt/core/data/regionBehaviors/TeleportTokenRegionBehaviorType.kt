@file:JsQualifier("foundry.data.regionBehaviors")
package com.foundryvtt.core.data.regionBehaviors

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DocumentConstructionContext

external class TeleportTokenRegionBehaviorType(
    data: AnyObject? = definedExternally,
    options: DocumentConstructionContext? = definedExternally
): RegionBehaviorType {
    var destination: String
    var choice: Boolean
}