package com.foundryvtt.core

import com.foundryvtt.core.documents.Actor
import js.objects.ReadonlyRecord

external interface PF2EActorConfig {
    val documentClasses: ReadonlyRecord<String, JsClass<Actor>>
}

external interface DebugConfig {
    var hooks: Boolean
}

external interface Config {
    var debug: DebugConfig
}

external val CONFIG: Config