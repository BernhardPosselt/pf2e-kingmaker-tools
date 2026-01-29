package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.utils.MergeOptions
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
): Changes? {
    val applied = mergeObject(original, update, MergeOptions(performDeletions = true, inplace = false))
    val filteredOriginal = filterObject(original, relevantAttributes)
    val filteredApplied = filterObject(applied, relevantAttributes)
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