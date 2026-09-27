package com.foundryvtt.core.documents

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CreateThumbnailOptions {
    val width: Int?
    val height: Int?
    val img: String?
    val format: String?
    val quality: Double
}