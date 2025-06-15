@file:JsQualifier("foundry.documents")
package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.data.fields.SchemaField
import kotlin.js.Promise

external class TokenDocument : Document {
    companion object : DocumentStatic<TokenDocument> {
        fun createCombatants(
            tokens: Array<TokenDocument>,
            options: CreateCombatantOptions = definedExternally
        ): Promise<Array<Combatant>>

        fun deleteCombatants(
            tokens: Array<TokenDocument>,
            options: CreateCombatantOptions = definedExternally
        ): Promise<Array<Combatant>>

        fun getTrackedAttributes(
            data: AnyObject,
            _path: Array<String> = definedExternally
        ): TrackedAttributesDescription

        fun getTrackedAttributes(data: String, _path: Array<String> = definedExternally): TrackedAttributesDescription
        fun getTrackedAttributes(
            data: DataModel,
            _path: Array<String> = definedExternally
        ): TrackedAttributesDescription

        fun getTrackedAttributes(
            data: SchemaField,
            _path: Array<String> = definedExternally
        ): TrackedAttributesDescription

        fun getTrackedAttributeChoices(attributes: AnyObject): AnyObject
    }

    override fun delete(operation: DatabaseDeleteOperation): Promise<TokenDocument>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<TokenDocument?>

    var _id: String
    var name: String
    var displayName: Int
    var displayBars: Int
    var actor: Actor?
    var actorId: String?
    var actorLink: Boolean
    var delta: Any?
    var appendNumber: Boolean
    var prependAdjective: Boolean
    var width: Int
    var height: Int
    var texture: TextureData
    var hexagonalShape: Int
    var x: Double
    var y: Double
    var elevation: Int
    var sort: Int
    var locked: Boolean
    var lockRotation: Boolean
    var alpha: Double
    var hidden: Boolean
    var disposition: Int
    var bar1: BarData
    var bar2: BarData
    var light: LightData
    var sight: SightData
    var detectionModes: Array<DetectionModeData>
    var occludable: OccludableData
    var ring: RingData

    val actors: Collection<Actor>
    val baseActor: Actor?
    val isOwner: Boolean
    val isLinked: Boolean
    val isSecret: Boolean
    val combatant: Combatant?
    val inCombat: Boolean
    val visible: Boolean
    val scene: Scene
//    val regions: JsSet<Region>

    fun getBarAttribute(options: BarAttributeOptions = definedExternally): AnyObject?
    fun hasStatusEffect(statusId: String): Boolean
    fun toggleCombatant(options: ToggleCombatantOptions = definedExternally): Promise<Boolean>
    fun updateVisionMode(visionMode: String, defaults: Boolean = definedExternally): Promise<TokenDocument>
}
