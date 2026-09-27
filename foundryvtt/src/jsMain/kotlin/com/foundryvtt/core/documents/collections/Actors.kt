@file:JsQualifier("foundry.documents.collections")
package com.foundryvtt.core.documents.collections

import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.documents.abstract.WorldCollection
import js.objects.ReadonlyRecord

external class Actors : WorldCollection<Actor> {
    companion object : WorldCollectionStatic<Actor>

    val tokens: ReadonlyRecord<String, Actor>
}