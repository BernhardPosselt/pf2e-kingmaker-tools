package com.foundryvtt.core.directories

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.applications.api.ApplicationV2
import com.foundryvtt.core.applications.sidebar.DocumentDirectory
import com.foundryvtt.core.documents.Actor
import com.foundryvtt.core.helpers.HooksEventListener
import org.w3c.dom.HTMLElement

@Suppress("unused")
external class ActorDirectory<T: Actor>: DocumentDirectory<T>

fun <O> HooksEventListener.onRenderActorDirectory(callback: (app: ApplicationV2, html: HTMLElement, data: AnyObject) -> O) =
    on("renderActorDirectory", callback)
