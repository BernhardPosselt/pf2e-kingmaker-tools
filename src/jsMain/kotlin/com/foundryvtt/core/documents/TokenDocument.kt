package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.data.fields.SchemaField
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface BarData {
    val attribute: String?
}


@JsPlainObject
external interface TextureData {
    var src: String
    var tint: String
    var anchorX: Double
    var anchorY: Double
    var alphaThreshold: Double
    var fit: String
    var offsetX: Int
    var offsetY: Int
    var scaleX: Double
    var scaleY: Double
}

@JsPlainObject
external interface LightData {
    val negative: Boolean
    val priority: Int
    var alpha: Boolean
    var angle: Int
}

@JsPlainObject
external interface SightData {
    val enabled: Boolean
    val range: Double
    val angle: Int
    val visionMode: String
    val color: String
    val attenuation: Double
    val brightness: Double
    val saturation: Double
    val contrast: Double
}

@JsPlainObject
external interface DetectionModeData {
    val id: String
    val enabled: Boolean
    val range: Double
}

@JsPlainObject
external interface OccludableData {
    val radius: Double
}

@JsPlainObject
external interface RingColorData {
    val ring: String
    val background: String
}

@JsPlainObject
external interface RingSubjectData {
    val scale: Double
    val texture: String
}

@JsPlainObject
external interface RingData {
    val enabled: Boolean
    val colors: RingColorData
    val effects: Double
    val subject: RingSubjectData
}

@JsPlainObject
external interface ToggleCombatantOptions {
    val active: Boolean?
    val options: AnyObject?
}

@JsPlainObject
external interface BarAttributeOptions {
    val alternative: String?
}

@JsPlainObject
external interface CreateCombatantOptions {
    val combat: Combat?
}

@JsPlainObject
external interface TrackedAttributesDescription {
    val bar: Array<String>
    var value: Array<String>
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.Token.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
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
    var x: Int
    var y: Int
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
//    val regions: JsSet<Region>

    fun getBarAttribute(options: BarAttributeOptions = definedExternally): AnyObject?
    fun hasStatusEffect(statusId: String): Boolean
    fun toggleCombatant(options: ToggleCombatantOptions = definedExternally): Promise<Boolean>
    fun updateVisionMode(visionMode: String, defaults: Boolean = definedExternally): Promise<TokenDocument>
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun TokenDocument.update(data: TokenDocument, operation: DatabaseUpdateOperation = jso()): Promise<TokenDocument?> =
    update(data as AnyObject, operation)


fun <O> HooksEventListener.onPreCreateToken(callback: PreCreateDocumentCallback<TokenDocument, O>) =
    on("preCreateToken", callback)

fun <O> HooksEventListener.onPreUpdateToken(callback: PreUpdateDocumentCallback<TokenDocument, O>): Unit =
    on("preUpdateToken", callback)

fun <O> HooksEventListener.onPreDeleteToken(callback: PreDeleteDocumentCallback<TokenDocument, O>) =
    on("preDeleteToken", callback)

fun <O> HooksEventListener.onCreateToken(callback: CreateDocumentCallback<TokenDocument, O>) =
    on("createToken", callback)

fun <O> HooksEventListener.onUpdateToken(callback: UpdateDocumentCallback<TokenDocument, O>) =
    on("updateToken", callback)

fun <O> HooksEventListener.onDeleteToken(callback: DeleteDocumentCallback<TokenDocument, O>) =
    on("deleteToken", callback)