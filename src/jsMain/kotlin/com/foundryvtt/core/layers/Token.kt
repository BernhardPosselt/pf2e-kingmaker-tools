package com.foundryvtt.core.layers

import com.foundryvtt.core.Actor
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.Combatant
import com.foundryvtt.core.documents.TokenDocument
import com.foundryvtt.core.documents.User
import com.pixijs.Point
import js.collections.JsSet
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

@JsPlainObject
external interface OffsetOptions {
    val offsetX: Int?
    val offsetY: Int?
}

@JsPlainObject
external interface InitializeOptions {
    val deleted: Boolean?
}

external class Token(document: TokenDocument) : PlaceableObject<TokenDocument> {
    var targeted: JsSet<User>
    val actor: Actor?
    val observer: Boolean
    val name: String
    val w: Int
    val h: Int
    fun getMovementAdjustedPoint(point: Point, offset: OffsetOptions = definedExternally)
    val sourceElement: HTMLElement
    val isVideo: Boolean
    val inCombat: Boolean
    val combatant: Combatant?
    val isTargeted: Boolean
    val detectionModes: Array<AnyObject>
    val isVisible: Boolean
    val animationName: String
    val hasSight: Boolean
    val emitsDarkness: Boolean
    val emitsLight: Boolean
    val hasLimitedSourceAngle: Boolean
    val dimRadius: Double
    val brightRadius: Double
    val radius: Double
    val lightPerceptionRange: Double
    val sightRange: Double
    val optimalSightRange: Double
    fun initializeSources(options: InitializeOptions = definedExternally)
    fun initializeLightSource(options: InitializeOptions = definedExternally)
    fun initializeVisionSource(options: InitializeOptions = definedExternally)
    fun getRingEffects(): Array<Int>

    // TODO
}