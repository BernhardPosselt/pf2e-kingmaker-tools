package com.foundryvtt.core.data.fields

import js.objects.ReadonlyRecord

typealias DataSchema<T> = ReadonlyRecord<String, DataField<T>>