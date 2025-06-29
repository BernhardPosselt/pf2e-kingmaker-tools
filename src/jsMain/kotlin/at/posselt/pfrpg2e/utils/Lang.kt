package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.AnyObject
import js.array.ReadonlyArray
import js.array.Tuple2
import js.array.toTypedArray
import js.array.tupleOf
import js.objects.Object
import js.objects.ReadonlyRecord
import js.objects.Record
import js.objects.recordOf
import js.reflect.Reflect
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.promise
import kotlinx.html.dom.create
import kotlinx.html.js.span
import kotlin.contracts.contract
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.encoding.Base64
import kotlin.js.Promise

/**
 * Use this whenever you'd use an async () => {} lambda in JS, e.g.
 *
 * func("do something") {
 *     buildPromise {
 *         window.fetch("https://google.com").await()
 *     }
 * }
 */
fun <T> buildPromise(
    block: suspend CoroutineScope.() -> T,
): Promise<T> =
    CoroutineScope(EmptyCoroutineContext).promise(block = block)

/**
 * Make awaitAll also work on a list of Promises instead in addition to Deferred
 */
suspend fun <T> Iterable<Promise<T>>.awaitAll(): List<T> =
    map { it.asDeferred() }.awaitAll()

suspend fun <T> Array<Promise<T>>.awaitAll(): Array<T> =
    map { it.asDeferred() }.awaitAll().toTypedArray()

fun String.stripHtml(): String? {
    val x = document.createElement("div")
    x.innerHTML = this
    return x.textContent
}

fun <F : Any, S> Array<Pair<F, S>>.toRecord(): ReadonlyRecord<F, S> =
    recordOf(*this)

fun <F : Any, S> Iterable<Pair<F, S>>.toRecord(): ReadonlyRecord<F, S> =
    recordOf(*toList().toTypedArray())

fun <F : Any, S> Iterable<Pair<F, S>>.toMutableRecord(): Record<F, S> =
    recordOf(*toList().toTypedArray())

fun <F : Any, S> Map<F, S>.toRecord(): ReadonlyRecord<F, S> =
    recordOf(*map { it.key to it.value }.toTypedArray())

@Suppress(
    "NOTHING_TO_INLINE",
    "CANNOT_CHECK_FOR_EXTERNAL_INTERFACE",
    "CANNOT_CHECK_FOR_ERASED",
    "ERROR_IN_CONTRACT_DESCRIPTION"
)
inline fun isJsObject(x: Any?): Boolean {
    contract {
        returns(true) implies (x is Record<String, Any?>)
    }
    return jsTypeOf(x) == "object" && x !is Array<*> && x != null
}

fun serializeB64Json(value: Any?): String =
    Base64.encode(JSON.stringify(value).encodeToByteArray())

fun <T> deserializeB64Json(value: String): T =
    JSON.parse(Base64.decode(value).decodeToString())


fun <T> ReadonlyRecord<String, T>.asSequence(): Sequence<Tuple2<String, T>> =
    Object.entries(this).asSequence()

fun <T> ReadonlyRecord<String, T>.toList(): List<Tuple2<String, T>> =
    Object.entries(this).toList()

fun <T> Sequence<Pair<String, T>>.toRecord(): ReadonlyRecord<String, T> =
    Object.fromEntries(map { tupleOf(it.first, it.second) }.toTypedArray())

fun <T> Sequence<Tuple2<String, T>>.toRecord(): ReadonlyRecord<String, T> =
    Object.fromEntries(toTypedArray())

fun <T> Array<T>.push(
    item: T,
) {
    asDynamic().push(item)
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun <T> Sequence<Tuple2<String, T>>.toMutableRecord(): Record<String, T> =
    Object.fromEntries(toTypedArray()) as Record<String, T>

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun <T> Sequence<Pair<String, T>>.toMutableRecord(): Record<String, T> =
    Object.fromEntries(map { tupleOf(it.first, it.second) }.toTypedArray()) as Record<String, T>

fun <T : Any> JsClass<T>.newInstance(parameters: ReadonlyArray<Any?>) =
    Reflect.construct(this, parameters)

fun <T> Array<T>.without(index: Int): Array<T> =
    filterIndexed { i, _ -> i != index }.toTypedArray()

fun Any.asAnyObject() = unsafeCast<AnyObject>()

fun <T> List<T>.asAnyObjectList() = unsafeCast<List<AnyObject>>()

fun Int.formatAsModifier() = if (this > 0) {
    "+$this"
} else {
    "$this"
}

fun escapeHtml(value: String) = document.create.span {
    +(value)
}.innerText