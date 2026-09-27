@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.Scene
import com.foundryvtt.core.documents.abstract.WorldCollection
import kotlin.js.Promise

external class Scenes : WorldCollection<Scene> {
    companion object : WorldCollectionStatic<Scene>

    val active: Scene?
    val current: Scene?
    val viewed: Scene?

    fun preload(sceneId: String, push: Boolean = definedExternally): Promise<Unit>
}