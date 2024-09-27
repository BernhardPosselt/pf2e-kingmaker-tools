package com.foundryvtt.core.applications

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.applications.api.ApplicationConfiguration
import com.foundryvtt.core.applications.api.ApplicationV2
import js.array.ReadonlyArray
import js.objects.ReadonlyRecord
import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.files.File
import kotlin.js.Promise

@JsPlainObject
external interface FavoriteFolder {
    val source: String
    val path: String
    val label: String
}

@JsPlainObject
external interface BrowseOptions {
    val bucket: String?
    val extensions: Array<String>?
    val wildcard: Boolean?
}


@JsPlainObject
external interface UploadOptions {
    val notify: Boolean?
}

@JsPlainObject
external interface FilePickerOptions : ApplicationConfiguration {
    val type: String? // "image"|"audio"|"video"|"text"|"imagevideo"|"font"|"folder"|"any"
    val current: String?
    val activeSource: String?  // data, public, s3
    val callback: (() -> Unit)?
    val allowUpload: Boolean
    val field: HTMLElement?
    val button: HTMLButtonElement?
    val favorites: ReadonlyRecord<String, FavoriteFolder>?
    val displayMode: String?
    val tileSize: Boolean?
    val redirectToRoot: Array<String>
}

external class FilePicker(options: FilePickerOptions = definedExternally) : ApplicationV2 {
    companion object {
        val FILE_TYPES: ReadonlyArray<String>
        val LAST_BROWSED_DIRECTORY: String
        val LAST_TILE_SIZE: Int?
        val LAST_DISPLAY_MODE: String
        val DISPLAY_MODES: Array<String>
        val S3_BUCKETS: Array<String>?
        val favorites: ReadonlyRecord<String, FavoriteFolder>
        val uploadURL: String
        fun setFavorite(source: String, path: String): Promise<Unit>
        fun removeFavorite(source: String, path: String): Promise<Unit>
        fun matchS3URL(url: String): Array<String?>?
        fun browse(source: String, target: String, options: BrowseOptions = definedExternally): Promise<Unit>
        fun configurePath(target: String, options: AnyObject = definedExternally): Promise<Unit>
        fun createDirectory(source: String, target: String, options: AnyObject = definedExternally): Promise<AnyObject>
        fun upload(
            source: String,
            path: String,
            file: File,
            body: AnyObject = definedExternally,
            options: UploadOptions = definedExternally,
        ): Promise<AnyObject>

        fun uploadPersistent(
            packageId: String,
            file: File,
            body: AnyObject = definedExternally,
            options: UploadOptions = definedExternally,
        ): Promise<AnyObject>

        fun fromButton(button: HTMLButtonElement): FilePicker
    }

    val source: AnyObject
    val target: String
    val canUpload: Boolean
    fun browse(target: String, options: BrowseOptions = definedExternally)

}