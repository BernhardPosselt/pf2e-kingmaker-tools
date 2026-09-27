package com.foundryvtt.core

import js.objects.ReadonlyRecord
import js.objects.Record
import js.objects.recordOf

typealias AnyObject = ReadonlyRecord<String, Any?>
typealias AnyMutableObject = Record<String, Any?>
typealias AudioContext = Any // not yet available in Kotlin yet, dom API

/**
 * Use this to serialize to foundry enum strings
 */
fun <T : Enum<T>> Enum<T>.toCamelCase(): String =
    name.split("_")
        .joinToString("") { it.lowercase().replaceFirstChar(Char::uppercase) }
        .replaceFirstChar(Char::lowercase)

fun <F : Any, S> Array<Pair<F, S>>.toRecord(): ReadonlyRecord<F, S> =
    recordOf(*this)

fun <F : Any, S> Iterable<Pair<F, S>>.toRecord(): ReadonlyRecord<F, S> =
    recordOf(*toList().toTypedArray())

fun <F : Any, S> Iterable<Pair<F, S>>.toMutableRecord(): Record<F, S> =
    recordOf(*toList().toTypedArray())

fun <F : Any, S> Map<F, S>.toRecord(): ReadonlyRecord<F, S> =
    recordOf(*map { it.key to it.value }.toTypedArray())
