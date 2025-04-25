@file:JsQualifier("foundry.data.regionBehaviors")
package com.foundryvtt.core.data.regionBehaviors

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.data.fields.TypeDataModel
import com.foundryvtt.core.documents.RegionBehavior
import com.foundryvtt.core.documents.RegionDocument
import com.foundryvtt.core.documents.Scene

abstract external class RegionBehaviorType(
    data: AnyObject? = definedExternally,
    options: DocumentConstructionContext? = definedExternally
): TypeDataModel {
    var events: Set<String>
    val behavior: RegionBehavior?
    val region: RegionDocument?
    val scene: Scene?
}