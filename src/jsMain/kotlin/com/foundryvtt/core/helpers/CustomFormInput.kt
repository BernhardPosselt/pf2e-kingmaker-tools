package com.foundryvtt.core.helpers

import com.foundryvtt.core.data.fields.DataField
import org.w3c.dom.HTMLElement

typealias CustomFormInput<T> = (field: DataField<T>, config: FormInputConfig<T>) -> HTMLElement
