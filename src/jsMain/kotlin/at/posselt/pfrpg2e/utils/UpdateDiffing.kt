package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.utils.MergeOptions
import com.foundryvtt.core.utils.getProperty
import com.foundryvtt.core.utils.mergeObject
import com.foundryvtt.core.utils.objectsEqual
import js.array.component1

fun filterObject(obj: AnyObject, keys: Set<String>): AnyObject =
    obj.asSequence()
        .filter { it.component1() in keys }
        .toMutableRecord()

class Changes(
    val original: AnyObject,
    val applied: AnyObject,
    val filteredOriginal: AnyObject,
    val filteredApplied: AnyObject,
)

fun parseChanges(
    original: AnyObject,
    update: AnyObject,
    relevantAttributes: Set<String>,
    path: String? = null,
): Changes? {
    val applied = mergeObject(original, update, MergeOptions(performDeletions = true, inplace = false))
    val (originalPathed, appliedPathed) = if (path != null) {
        val a = getProperty(original, path).unsafeCast<AnyObject?>() ?: return null
        val b = getProperty(applied, path).unsafeCast<AnyObject?>() ?: return null
        a to b
    } else {
        original to applied
    }
    val filteredOriginal = filterObject(originalPathed, relevantAttributes)
    val filteredApplied = filterObject(appliedPathed, relevantAttributes)
    return if (objectsEqual(filteredApplied, filteredOriginal)) {
        null
    } else {
        Changes(
            original = original,
            applied = applied,
            filteredOriginal = filteredOriginal,
            filteredApplied = filteredApplied,
        )
    }
}