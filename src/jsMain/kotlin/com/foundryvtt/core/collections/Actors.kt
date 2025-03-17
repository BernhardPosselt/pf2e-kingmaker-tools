package com.foundryvtt.core.collections

import com.foundryvtt.core.documents.Actor
import js.objects.ReadonlyRecord

external class Actors : WorldCollection<Actor> {
    companion object : WorldCollectionStatic<Actor>

    val tokens: ReadonlyRecord<String, Actor>
}