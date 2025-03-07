package at.posselt.pfrpg2e.kingdom.data

import js.objects.JsPlainObject

@JsPlainObject
external interface RawLeaderValues {
    var uuid: String?
    var invested: Boolean
    var type: String
    var vacant: Boolean
}

@JsPlainObject
external interface RawLeaders {
    val ruler: RawLeaderValues
    val counselor: RawLeaderValues
    val emissary: RawLeaderValues
    val general: RawLeaderValues
    val magister: RawLeaderValues
    val treasurer: RawLeaderValues
    val viceroy: RawLeaderValues
    val warden: RawLeaderValues
}