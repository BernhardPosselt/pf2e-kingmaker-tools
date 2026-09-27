package com.foundryvtt.core.documents

import com.foundryvtt.core.AnyObject
import kotlinx.js.JsPlainObject
import kotlin.js.Promise

@JsPlainObject
external interface TokenMovementData {
    val id: String
    val subpathId: String
    val chain: Array<String>
    val origin:  TokenPosition
    val destination: TokenPosition
    val passed: TokenMovementSectionData
    val pending: TokenMovementSectionData
    val history: TokenMovementSectionData
    val split: Boolean
    val constrained: Boolean
    val recorded: Boolean
    val method: String
    val constrainOptions: TokenConstraintOptions
    // terrainOptions, measureOptions
    val autoRotate: Boolean
    val showRuler: Boolean
    val user: User
    val state: String
    val updatedOptions: AnyObject
    val finished: Promise<Boolean>
    val animation: TokenMovementAnimation
}