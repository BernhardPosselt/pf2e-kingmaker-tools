package com.foundryvtt.core.directories

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.HooksEventListener
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.sidebar.DocumentDirectory
import com.foundryvtt.core.documents.Actor
import io.kvision.jquery.JQuery

@Suppress("unused")
external class ActorDirectory<T: Actor>: DocumentDirectory<T>

fun <O> HooksEventListener.onRenderActorDirectory(callback: (app: ApplicationV2, html: JQuery, data: AnyObject) -> O) =
    on("renderActorDirectory", callback)
