package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.Scene
import kotlin.js.Promise

external class Scenes : WorldCollection<Scene> {
    companion object : WorldCollectionStatic<Scene>

    val active: Scene?
    val current: Scene?
    val viewed: Scene?

    fun preload(sceneId: String, push: Boolean = definedExternally): Promise<Unit>
}