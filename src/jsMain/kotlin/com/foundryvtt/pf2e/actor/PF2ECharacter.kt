package com.foundryvtt.pf2e.actor

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DatabaseDeleteOperation
import com.foundryvtt.core.abstract.DatabaseUpdateOperation
import com.foundryvtt.pf2e.system.IntValue
import com.foundryvtt.pf2e.system.MaxValue
import com.foundryvtt.pf2e.system.MinMaxValue
import com.foundryvtt.pf2e.system.StringValue
import js.objects.ReadonlyRecord
import js.objects.jso
import kotlinx.js.JsPlainObject
import kotlin.js.Promise


@JsPlainObject
external interface Resources {
    var heroPoints: MaxValue
}

@JsPlainObject
external interface Hp : MaxValue {
    var temp: Int
}

fun Hp.damage(): Int = max - value

@JsPlainObject
external interface Xp : MinMaxValue {
    var pct: Int
}


@JsPlainObject
external interface Details {
    var xp: Xp
    var level: IntValue
    var keyability: StringValue
    var attributes: Attributes
    var age: StringValue
    var height: StringValue
    var weight: StringValue
    var gender: StringValue
    var ethnicity: StringValue
    var nationality: StringValue
}

@JsPlainObject
external interface Speeds : IntValue {
    var otherSpeeds: Array<String>
}

@JsPlainObject
external interface Attributes {
    var hp: Hp
    var speed: Speeds
    var ac: IntValue
    var perception: IntValue
}

@JsPlainObject
external interface Saves {
    var fortitude: IntValue
    var reflex: IntValue
    var will: IntValue
}


@JsPlainObject
external interface Ability {
    var mod: Int
    var base: Int
    var label: String
    var shortLabel: String
}


@JsPlainObject
external interface Abilities {
    var str: Ability
    var dex: Ability
    var con: Ability
    var int: Ability
    var wis: Ability
    var cha: Ability
}

@JsPlainObject
external interface Traits {
    var rarity: String
}

@JsPlainObject
external interface PF2ECharacterSystem {
    var attributes: Attributes
    var resources: Resources
    var details: Details
    var exploration: Array<String>
    var abilities: Abilities
    var traits: Traits
    var saves: Saves
}

@JsPlainObject
external interface HitPoints {
    val value: Int
    val max: Int
    val temp: Int
    val unrecoverable: Int
    val negativeHealing: Boolean
    val recoveryMultiplier: Int
    val recoveryAddend: Int
}

// required to make instance of work, but since the classes are not registered here
// at page load, we can't use @file:JsQualifier
@JsName("CONFIG.PF2E.Actor.documentClasses.character")
@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
external class PF2ECharacter : PF2EActor, PF2ECreature {
    companion object : DocumentStatic<PF2ECharacter>

    override fun delete(operation: DatabaseDeleteOperation): Promise<PF2ECharacter>
    override fun update(data: AnyObject, operation: DatabaseUpdateOperation): Promise<PF2ECharacter?>

    val abilities: Abilities
    val hitPoints: HitPoints
    override val skills: ReadonlyRecord<String, PF2EAttribute>
    val system: PF2ECharacterSystem
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun PF2ECharacter.update(data: PF2ECharacter, operation: DatabaseUpdateOperation = jso()): Promise<PF2ECharacter?> =
    update(data as AnyObject, operation)