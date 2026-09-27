package com.foundryvtt.core.applications.ux

external interface ProcessedFormData<T> {
    val value: T
    val writable: Boolean
    val enumerable: Boolean
}