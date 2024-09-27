package com.foundryvtt.core

import js.objects.ReadonlyRecord

external interface PF2EActorConfig {
    val documentClasses: ReadonlyRecord<String, JsClass<Actor>>
}

external interface Config

external val CONFIG: Config