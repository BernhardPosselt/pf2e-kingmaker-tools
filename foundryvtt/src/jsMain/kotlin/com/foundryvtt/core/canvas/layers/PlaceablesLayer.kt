@file:JsQualifier("foundry.canvas.layers")
package com.foundryvtt.core.canvas.layers

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.canvas.placeables.PlaceableObject
import com.foundryvtt.core.documents.Scene
import com.pixijs.Point
import js.collections.JsMap
import kotlin.js.Promise

open external class PlaceablesLayer<D : Document, P : PlaceableObject<D>> : InteractionLayer {
    companion object : CanvasLayerStatic<PlaceablesLayer<*, *>> {
        val SORT_ORDER: Int
        val documentName: String
    }

    val documentCollection: Collection<D>?
    val placeables: Array<P>
    val controlled: Array<P>
    val controlledObjects: JsMap<String, P>
    var hover: P?
    var highlightObjects: Boolean

    fun getMaxSort(): Int
    fun getSnappedPoint(point: Point): Point
    fun getDocuments(): Array<D>
    fun createObject(document: D): P
    fun clearPreviewContainer()
    fun get(value: String): P?
    fun controlAll(options: AnyObject = definedExternally): Array<P>
    fun releaseAll(options: AnyObject = definedExternally): Int
    fun rotateMany(options: RotateManyOptions = definedExternally): Promise<Array<P>>
    fun moveMany(options: MoveManyOptions = definedExternally): Promise<Array<P>>
    fun undoHistory(): Promise<Array<D>>
    fun deleteAll(): Promise<Array<D>>
    fun storeHistory(type: String, data: Array<AnyObject>)
    fun copyObjects(): Array<P>
    fun pasteObjects(position: Point, options: PasteOptions = definedExternally): Promise<Array<D>>
    fun selectObjects(options: SelectOptions = definedExternally, additionalOptions: AdditionalSelectOptions): Boolean
    fun updateAll(
        transform: (AnyObject) -> Unit,
        condition: (AnyObject) -> Boolean = definedExternally,
        options: AnyObject = definedExternally,
    ): Promise<Array<D>>

    val scene: Scene?
}