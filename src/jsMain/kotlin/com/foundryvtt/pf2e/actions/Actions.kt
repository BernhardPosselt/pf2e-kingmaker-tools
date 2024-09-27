package com.foundryvtt.pf2e.actions

import com.foundryvtt.core.utils.Collection
import kotlinx.js.JsPlainObject
import kotlin.js.Promise


external class Action {
    fun use(options: ActionUseOptions): Promise<Array<CheckResultCallback>>
}

@JsPlainObject
external interface RestForTheNightOptions : ActionUseOptions {
    val skipDialog: Boolean?
}

external class PF2EActionMacros : Collection<Action> {
    fun restForTheNight(options: RestForTheNightOptions): Promise<Unit>
}

