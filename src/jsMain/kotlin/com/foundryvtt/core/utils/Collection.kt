@file:JsQualifier("foundry.utils")

package com.foundryvtt.core.utils

import js.array.JsTuple2
import js.array.ReadonlyArray
import js.iterable.JsIterable

open external class Collection<T>(
    values: ReadonlyArray<JsTuple2<String, T>> = definedExternally,
) : JsIterable<T> {
    val contents: Array<T>
    fun toJSON(): Array<Any>
    fun some(predicate: (T) -> Boolean): Boolean
    fun <O> map(transform: (T) -> O): Array<O>
    fun reduce(function: (T, T, Int) -> T, initial: T): T
    fun getName(name: String): T?
    fun get(key: String, options: GetOptions = definedExternally): T?
    fun set(key: String, value: T)
    fun forEach(action: (T) -> Unit)
    fun filter(predicate: (T) -> Boolean): Array<T>
    fun find(predicate: (T) -> Boolean): T?
    fun delete(key: String): Boolean
    fun clear()
}