package com.foundryvtt.pf2e.system

import kotlinx.js.JsPlainObject


@JsPlainObject
external interface StringArrayValue {
    var value: Array<String>
}

@JsPlainObject
external interface StringValue {
    var value: String
}

@JsPlainObject
external interface IntValue {
    var value: Int
}

@JsPlainObject
external interface MaxValue : IntValue {
    var max: Int
}

@JsPlainObject
external interface MinValue : IntValue {
    var min: Int
}

@JsPlainObject
external interface MinMaxValue : IntValue {
    var max: Int
    var min: Int
}

@JsPlainObject
external interface ItemTraits : StringArrayValue {
    val rarity: String
}