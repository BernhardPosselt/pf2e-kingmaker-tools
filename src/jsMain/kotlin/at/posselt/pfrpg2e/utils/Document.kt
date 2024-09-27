package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.takeIfInstance
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.fromUuid
import com.foundryvtt.pf2e.item.PF2EItem
import js.objects.PropertyKey
import js.objects.jso
import js.objects.recordOf
import js.reflect.Proxy
import js.reflect.ProxyHandler
import js.symbol.Symbol
import kotlinx.coroutines.await
import kotlin.reflect.KClass


val isProxy = Symbol("isProxy")


private class Handler(
    private val currentPath: String = "",
    private val updates: HashMap<String, Any?>,
) {
    private fun buildPath(p: String) = if (currentPath.isEmpty()) p else "$currentPath.$p"

    fun set(target: dynamic, p: PropertyKey, value: dynamic, receiver: Any) {
        if (value?.isProxy === isProxy)
            throw IllegalArgumentException(
                "You are assigning an attribute to a proxy. " +
                        "Did you mean to assign a value instead?"
            )
        updates[buildPath("$p")] = value
    }

    fun get(target: Any, p: PropertyKey, receiver: Any): Any {
        if (p === isProxy) return isProxy
        return Handler(buildPath("$p"), updates)
            .asProxy(jso())
    }

    fun asProxy(target: Any): Proxy<Any> {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
        val binding = recordOf("set" to ::set, "get" to ::get) as ProxyHandler<Any>
        return Proxy(target, binding)
    }
}

/**
 * Allows you to assign partial updates in a typesafe manner, e.g.:
 * pf2eActor.typeSafeUpdate {
 *     name = "test",
 *     system.details.level.value = 3
 * }.await()
 *
 * will produce {'name': 'test', 'system.details.level.value': 3}
 *
 * Note that you *must not* assign a property to itself, e.g.
 * * pf2eActor.buildUpdate {
 *  *     name = "test",
 *  *     system.details.level.value = system.details.level.value
 *  * }
 */
@Suppress("UNCHECKED_CAST")
fun <D : Document> D.buildUpdate(block: D.() -> Unit): AnyObject {
    val result = HashMap<String, Any?>()
    val proxy = Handler(updates = result)
        .asProxy(this) as D
    proxy.block()
    return result.toRecord()

}

/**
 * Same as buildUpdate, but executes it as well
 */
@Suppress("UNCHECKED_CAST")
suspend fun <D : Document> D.typeSafeUpdate(block: D.() -> Unit): D {
    val result = buildUpdate(block)
    return (update(result).await() as D?) ?: this
}


suspend fun <D : Document, T> D.setAppFlag(key: String, flag: T) =
    setFlag(Config.moduleId, key, flag).await()

suspend fun <D : Document> D.unsetAppFlag(key: String) =
    unsetFlag(Config.moduleId, key).await()

@Suppress("UNCHECKED_CAST")
fun <D : Document, T> D.getAppFlag(key: String) =
    getFlag(Config.moduleId, key) as T?

suspend inline fun <reified T> fromUuidTypeSafe(uuid: String): T? =
    fromUuid(uuid).await()
        ?.takeIfInstance<T>()

@Suppress("UNCHECKED_CAST")
suspend inline fun <T : Document> fromUuidsOfTypes(
    uuids: Array<String>,
    vararg types: KClass<out T>,
): Array<T> =
    uuids.map { fromUuid(it) }
        .awaitAll()
        .filter { document -> types.any { type -> type.isInstance(document) } }
        .toTypedArray() as Array<T>

@Suppress("UNCHECKED_CAST")
suspend inline fun <T : Document> fromUuidOfTypes(
    uuid: String,
    vararg types: KClass<out T>,
): T? =
    fromUuid(uuid)
        .await()
        ?.takeIf { document -> types.any { type -> type.isInstance(document) } } as T?

suspend inline fun <reified T> fromUuidsTypeSafe(uuids: Array<String>): Array<T> =
    uuids.map { fromUuid(it) }
        .awaitAll()
        .filterIsInstance<T>()
        .toTypedArray()

fun buildUuid(uuid: String, label: String? = null) =
    if (label == null) {
        "@UUID[$uuid]"
    } else {
        "@UUID[$uuid]{$label}"
    }
