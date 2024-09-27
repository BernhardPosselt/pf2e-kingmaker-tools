package com.foundryvtt.pf2e.actions

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface CheckDC {
    val slug: String?
    val statistic: StatisticDifficultyClass?
    val label: String?
    val scope: String?
    val value: Int
    val visible: Boolean?
}