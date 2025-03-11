package com.foundryvtt.core.directories

import com.foundryvtt.core.Actor
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.HooksEventListener
import com.foundryvtt.core.applications.api.ApplicationV2
import io.kvision.jquery.JQuery

external class ActorDirectory<T: Actor>: DocumentDirectory<T>

fun <O> HooksEventListener.onRenderActorDirectory(callback: (app: ApplicationV2, html: JQuery, data: AnyObject) -> O) =
    on("renderActorDirectory", callback)
