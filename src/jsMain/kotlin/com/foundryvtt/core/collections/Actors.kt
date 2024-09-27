package com.foundryvtt.core.collections

import com.foundryvtt.core.Actor
import js.objects.ReadonlyRecord

external class Actors : WorldCollection<Actor> {
    companion object : WorldCollectionStatic<Actor>

    val tokens: ReadonlyRecord<String, Actor>
}