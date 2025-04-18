package com.foundryvtt.core

import js.objects.ReadonlyRecord
import js.objects.Record

typealias AnyObject = ReadonlyRecord<String, Any?>
typealias AnyMutableObject = Record<String, Any?>
typealias AudioContext = Any // not yet available in Kotlin yet, dom API
