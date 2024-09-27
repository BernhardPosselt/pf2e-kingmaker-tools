package at.posselt.pfrpg2e.app

import at.posselt.pfrpg2e.utils.buildPromise
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Hooks
import com.foundryvtt.core.HooksEventListener
import com.foundryvtt.core.applications.api.ApplicationRenderOptions
import com.foundryvtt.pf2e.item.*
import js.array.push
import js.objects.recordOf
import kotlinx.coroutines.await
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.*
import kotlin.js.Promise

data class AppHook<T>(
    val key: String,
    val callback: Function<T>,
)

data class AppEventListener<Event>(
    val selector: String,
    val eventType: String,
    val callback: (Event) -> Unit,
)


/**
 * Small utility class that takes care of unregistering Hooks and
 * provides a neat way of defining event listeners
 */
abstract class App<C : HandlebarsRenderContext>(
    config: HandlebarsFormApplicationOptions
) : HandlebarsApp<C>(config) {
    private val appHooks = arrayOf<AppHook<*>>()
    private val appEventListeners = arrayOf<AppEventListener<Event>>()
    protected val appHook = object : HooksEventListener {
        override fun <T> on(key: String, callback: Function<T>) {
            appHooks.push(AppHook(key = key, callback = callback))
            Hooks.on(key, callback)
        }
    }

    override fun _preClose(options: ApplicationRenderOptions): Promise<Unit> = buildPromise {
        super._preClose(options).await()
        appHooks.forEach {
            Hooks.off(it.key, it.callback)
        }
    }

    override fun _onRender(context: AnyObject, options: ApplicationRenderOptions) {
        appEventListeners.forEach {
            element.querySelectorAll(it.selector).asList()
                .forEach { el ->
                    el.addEventListener(it.eventType, it.callback)
                }
        }
        return super._onRender(context, options)
    }

    protected open fun on(selector: String, eventType: String = "click", callback: (Event) -> Unit) {
        appEventListeners.push(
            AppEventListener(
                selector = selector,
                eventType = eventType,
                callback = callback
            )
        )
    }

    protected open fun onDocumentRefDragstart(selector: String) {
        appEventListeners.push(
            AppEventListener(
                selector = selector,
                eventType = "dragstart",
                callback = { event: Event ->
                    if (event !is DragEvent) throw IllegalStateException("should never receive no DragEvent")
                    event.stopPropagation()
                    val target = event.currentTarget as HTMLElement
                    val type = target.dataset["type"]
                    val uuid = target.dataset["uuid"]
                    val itemType = target.dataset["itemType"]
                    if (type != "Actor" && type != "Item" && type != "JournalEntry") {
                        throw IllegalArgumentException("$selector has no data-type value of either Item, Actor or JournalEntry, received $type")
                    }
                    if (uuid !is String) {
                        throw IllegalArgumentException("$selector has no data-uuid set")
                    }
                    if (type == "Item" && itemType !is String) {
                        throw IllegalArgumentException("$selector has type Item but no data-item-type set")
                    }
                    val data = if (itemType == null) {
                        recordOf(
                            "type" to type,
                            "uuid" to uuid,
                            "selector" to selector,
                        )
                    } else {
                        recordOf(
                            "type" to type,
                            "uuid" to uuid,
                            "itemType" to itemType,
                            "selector" to selector,
                        )
                    }
                    event.dataTransfer?.setData("text/plain", JSON.stringify(data))
                }
            )
        )
    }

    /**
     * @param allowedDragSelectors if provided, will only allow dropping from drag selectors
     */
    protected open fun onDocumentRefDrop(
        selector: String,
        allowDrop: ((GenericRef) -> Boolean)? = null,
        callback: (DragEvent, DocumentRef<*>) -> Unit
    ) {
        appEventListeners.push(
            AppEventListener(
                selector = selector,
                eventType = "drop",
                callback = { event: Event ->
                    if (event !is DragEvent) throw IllegalStateException("should never receive no DragEvent")
                    event.dataTransfer?.getData("text/plain")
                        ?.let(::toGenericRef)
                        ?.takeIf { allowDrop?.let { f -> f(it) } ?: true }
                        ?.let {
                            val ref = when (it.type) {
                                "Actor" -> ActorRef(it.uuid)
                                "Item" -> when (it.itemType) {
                                    "action" -> ActionItemRef(it.uuid)
                                    "campaignFeature" -> CampaignFeatureItemRef(it.uuid)
                                    "condition" -> ConditionItemRef(it.uuid)
                                    "consumable" -> ConsumableItemRef(it.uuid)
                                    "effect" -> EffectItemRef(it.uuid)
                                    "equipment" -> EquipmentItemRef(it.uuid)
                                    "affliction" -> AfflictionItemRef(it.uuid)
                                    "weapon" -> WeaponItemRef(it.uuid)
                                    "armor" -> ArmorItemRef(it.uuid)
                                    "shield" -> ShieldItemRef(it.uuid)
                                    "treasure" -> TreasureItemRef(it.uuid)
                                    "backpack" -> BackpackItemRef(it.uuid)
                                    else -> ItemRef(it.uuid)
                                }

                                "JournalEntry" -> JournalEntryRef(it.uuid)
                                else -> null
                            }
                            if (ref == null) {
                                console.log("Not a reference", it.data)
                            } else {
                                callback(event, ref)
                            }
                        }
                }
            )
        )
    }
}
