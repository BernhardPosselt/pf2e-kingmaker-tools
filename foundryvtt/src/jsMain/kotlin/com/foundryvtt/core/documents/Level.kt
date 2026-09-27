@file:JsQualifier("foundry.documents")

package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

external class Level : ClientDocument {
    companion object : DocumentStatic<Level> {
        val CONFIG_SETTING: String
    }

    override fun delete(operation: DatabaseDeleteOperation): Promise<Level>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<Level?>

    var _id: String
    var elevation: Elevation
    var background: Background
    var foreground: Foreground
    var fog: Fog
    var textures: LevelTexture
    var visibility: Visibility
    var sort: Int
}

@JsPlainObject
external interface Visibility {
    var levels: Set<String>
}

@JsPlainObject
external interface Elevation {
    var bottom: Int
    var top: Int
}

@JsPlainObject
external interface Background {
    var color: String
    var src: String
    var tint: String
    var alphaThreshold: Double
}

@JsPlainObject
external interface Foreground {
    var src: String
    var tint: String
    var alphaThreshold: Double
}


@JsPlainObject
external interface Fog {
    var src: String
    var tint: String
}


@JsPlainObject
external interface LevelTexture {
    var anchorX: Double
    var anchorY: Double
    var offsetX: Int
    var offsetY: Int
    var fit: String
    var scaleX: Double
    var scaleY: Double
    var rotation: Double
}