package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawWorkSite {
    var quantity: Int
    var resources: Int
}

@JsPlainObject
external interface RawWorkSites {
    var farmlands: RawWorkSite
    var lumberCamps: RawWorkSite
    var mines: RawWorkSite
    var quarries: RawWorkSite
    var luxurySources: RawWorkSite
}