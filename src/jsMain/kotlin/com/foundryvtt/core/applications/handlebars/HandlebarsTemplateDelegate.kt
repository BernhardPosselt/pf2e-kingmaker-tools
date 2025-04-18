package com.foundryvtt.core.applications.handlebars

import com.foundryvtt.core.AnyObject
import kotlin.js.Promise

typealias HandlebarsTemplateDelegate = (AnyObject, HandlebarOptions) -> Promise<String>
