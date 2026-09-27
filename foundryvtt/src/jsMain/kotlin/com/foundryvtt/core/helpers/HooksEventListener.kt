package com.foundryvtt.core.helpers

interface HooksEventListener {
    fun <T> on(key: String, callback: Function<T>)
}