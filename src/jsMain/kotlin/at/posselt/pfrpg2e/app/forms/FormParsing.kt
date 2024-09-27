package at.posselt.pfrpg2e.app.forms

import at.posselt.pfrpg2e.utils.asSequence
import at.posselt.pfrpg2e.utils.isJsObject
import at.posselt.pfrpg2e.utils.toMutableRecord
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.utils.expandObject
import js.array.toTypedArray
import js.objects.Object
import js.objects.ReadonlyRecord


/**
 * Wrapper around expandObject that allows additional
 * transformations and fixing until the object reaches its final
 * good state
 */
fun <T> parseFormData(value: AnyObject, and: (dynamic) -> Unit): T {
    val filteredBlanks = value.asSequence()
        .filter {
            val rhs = it.component2()
            if (rhs is String) rhs.isNotEmpty() else true
        }
        .toMutableRecord()
    val expanded = expandObject(filteredBlanks)
    val result = normalizeArrays(expanded)
    and(result)
    @Suppress("UNCHECKED_CAST")
    return result as T
}


/**
 * This utility is needed to dynamically and recursively convert nested objects
 * with integer keys into arrays since that's how Foundry handles forms
 *
 * @return either a Record or an array if the top level object was an array
 */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
fun <T> normalizeArrays(obj: ReadonlyRecord<String, T>): Any {
    if (Object.hasOwn(obj, 0)) {
        return obj.asSequence()
            .sortedBy { it.component1().toInt() }
            .map {
                val value = it.component2()
                if (isJsObject(value)) {
                    normalizeArrays(value as AnyObject)
                } else {
                    value
                }
            }
            .toTypedArray()
    } else {
        return obj.asSequence()
            .map {
                val value = it.component2()
                val normalizedValue = if (isJsObject(value)) {
                    normalizeArrays(value as AnyObject)
                } else {
                    value
                }
                it.component1() to normalizedValue
            }
            .toRecord()
    }
}