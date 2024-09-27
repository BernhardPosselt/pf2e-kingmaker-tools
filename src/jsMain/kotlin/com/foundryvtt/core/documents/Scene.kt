package com.foundryvtt.core.documents

import com.foundryvtt.core.*
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.core.collections.EmbeddedCollection
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface CreateThumbnailOptions {
    val width: Int?
    val height: Int?
    val img: String?
    val format: String?
    val quality: Double
}

@JsPlainObject
external interface Rect {
    val x: Int
    val y: Int
    val width: Int
    val height: Int
}

@JsPlainObject
external interface SceneDimensions {
    val width: Int
    val height: Int
    val size: Int
    val sceneX: Int
    val sceneY: Int
    val sceneWidth: Int
    val sceneHeight: Int
    val rect: Rect
    val sceneRect: Rect
    val distance: Int
    val distancePixels: Int
    val ration: Double
    val maxR: Int
    val rows: Int
    val columns: Int
}

@JsPlainObject
external interface SceneInitial {
    val x: Int?
    val y: Int?
    val scale: Double?
}

@JsPlainObject
external interface SceneGrid {
    val type: Int
    val size: Int
    val style: String
    val thickness: Int
    val color: String
    val alpha: Double?
    val distance: Int
    val unit: String
}

@JsPlainObject
external interface EnvironmentData {
    val hue: Double
    val saturation: Double
    val intensity: Double
    val luminosity: Double
    val shadows: Double
}

@JsPlainObject
external interface SceneEnvironmentData {
    val darknessLevel: Double
    val darknessLevelLock: Boolean
    val globalLight: GlobalLightData
    val cycle: Boolean
    val base: EnvironmentData
    val dark: EnvironmentData
}

@JsName("CONFIG.Scene.documentClass")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class Scene : ClientDocument {
    companion object : DocumentStatic<Scene>

    override fun delete(operation: DatabaseDeleteOperation): Promise<Scene>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Scene?>

    var _id: String
    var name: String
    var img: String
    var active: Boolean
    var nagivation: Boolean
    var navOrder: Int
    var navName: String
    var foreground: String
    var thumb: String
    var width: Int
    var height: Int
    var padding: Double
    var initial: SceneInitial
    var grid: SceneGrid
    var environment: SceneEnvironmentData
    var drawings: EmbeddedCollection<DrawingDocument>
    var tokens: EmbeddedCollection<TokenDocument>
    var tiles: EmbeddedCollection<TileDocument>
    var notes: EmbeddedCollection<NoteDocument>

    // TODO
    //    var lights: EmbeddedCollection<Light>
    //    var sounds: EmbeddedCollection<AmbientSound>
    //    var templates: EmbeddedCollection<MeasuredTemplate>
    //    var walls : EmbeddedCollection<Wall>
    var playlist: Playlist?
    var playlistSound: PlaylistSound?

    //    var journal: Journal?
    //    var journalEntryPage: JournalEntryPage?
    var weather: String?
    var folder: Folder?
    var ownership: Ownership
    var sort: Int

    val thumbnail: String
    fun activate(): Promise<Unit>
    fun view(): Promise<Unit>
    fun getDimensions(): SceneDimensions
    fun createThumbnail(options: CreateThumbnailOptions = definedExternally): Promise<AnyObject>
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun Scene.update(data: Scene, operation: DatabaseUpdateOperation = jso()): Promise<Scene?> =
    update(data as AnyObject, operation)

fun <O> HooksEventListener.onPreCreateScene(callback: PreCreateDocumentCallback<Scene, O>) =
    on("preCreateScene", callback)

fun <O> HooksEventListener.onPreUpdateScene(callback: PreUpdateDocumentCallback<Scene, O>): Unit =
    on("preUpdateScene", callback)

fun <O> HooksEventListener.onPreDeleteScene(callback: PreDeleteDocumentCallback<Scene, O>) =
    on("preDeleteScene", callback)

fun <O> HooksEventListener.onCreateScene(callback: CreateDocumentCallback<Scene, O>) =
    on("createScene", callback)

fun <O> HooksEventListener.onUpdateScene(callback: UpdateDocumentCallback<Scene, O>) =
    on("updateScene", callback)

fun <O> HooksEventListener.onDeleteScene(callback: DeleteDocumentCallback<Scene, O>) =
    on("deleteScene", callback)