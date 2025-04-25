@file:JsQualifier("foundry.utils")
package com.foundryvtt.core.utils

import com.foundryvtt.core.abstract.Document
import kotlin.js.Promise

external fun fromUuid(uuid: String, options: FromUuidOptions = definedExternally): Promise<Document?>