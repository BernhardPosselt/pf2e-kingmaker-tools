package com.foundryvtt.core


external interface DebugConfig {
    var hooks: Boolean
}

external interface Config {
    var debug: DebugConfig
}

