package at.posselt.pfrpg2e.kingdom.data

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface RawSkillRanks {
    val agriculture: Int
    val arts: Int
    val boating: Int
    val defense: Int
    val engineering: Int
    val exploration: Int
    val folklore: Int
    val industry: Int
    val intrigue: Int
    val magic: Int
    val politics: Int
    val scholarship: Int
    val statecraft: Int
    val trade: Int
    val warfare: Int
    val wilderness: Int
}