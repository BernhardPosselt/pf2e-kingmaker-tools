package com.foundryvtt.core.layers

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.User
import com.pixijs.Point
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface ControlOptions {
    val releaseOthers: Boolean?
}

open external class PlaceableObject<D : Document>(document: D) {
    val scene: Scene
    val document: D
    val isOwner: Boolean
    val center: Point
    val id: String
    val objectId: String
    val sourceId: String
    val isPreview: Boolean
    val hasPreview: Boolean
    val layer: PlaceablesLayer<D, *>
    val sheet: ApplicationV2
    val controlled: Boolean
    var hover: Boolean
    fun getSnappedPosition(point: Point): Point
    fun clear(): PlaceableObject<D>
    fun destroy()
    fun draw(options: AnyObject = definedExternally): Promise<PlaceableObject<D>>
    fun refresh(options: AnyObject = definedExternally): PlaceableObject<D>
    fun control(options: ControlOptions = definedExternally): Boolean
    fun release(options: AnyObject = definedExternally): Boolean
    fun clone(): PlaceableObject<D>
    fun rotate(angle: Double, snap: Boolean): Promise<PlaceableObject<D>>
    fun activateListeners()
    fun can(user: User, action: String): Boolean
}