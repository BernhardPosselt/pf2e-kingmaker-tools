package com.foundryvtt.pf2e

import com.foundryvtt.core.Config
import com.foundryvtt.core.PF2EActorConfig

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
inline val Config.PF2E: PF2EConfig
    get() = asDynamic().PF2E as PF2EConfig

external interface PF2EConfig {
    val Actor: PF2EActorConfig
}