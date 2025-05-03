package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.camping.translateCampingActivities
import at.posselt.pfrpg2e.camping.translateRecipes
import at.posselt.pfrpg2e.kingdom.structures.translateStructureData
import at.posselt.pfrpg2e.kingdom.translateActivities
import at.posselt.pfrpg2e.kingdom.translateCharters
import at.posselt.pfrpg2e.kingdom.translateFeats
import at.posselt.pfrpg2e.kingdom.translateGovernments
import at.posselt.pfrpg2e.kingdom.translateHeartlands
import at.posselt.pfrpg2e.kingdom.translateKingdomEvents
import at.posselt.pfrpg2e.kingdom.translateKingdomFeatures
import at.posselt.pfrpg2e.kingdom.translateMilestones
import at.posselt.pfrpg2e.localization.Translatable
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.game
import com.foundryvtt.core.ui
import com.i18next.I18Next
import com.i18next.I18NextInitOptions
import com.i18next.I18NextInterpolationOptions
import com.i18next.ICU
import com.i18next.i18next
import js.objects.Object
import js.objects.ReadonlyRecord
import js.objects.Record
import js.objects.recordOf
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.Window

external class Handlebars {
    val helpers: Record<String, Any>
    fun registerHelper(name: String, callback: dynamic)
}

inline val Window.Handlebars: Handlebars
    get() = asDynamic().Handlebars.unsafeCast<Handlebars>()

/**
 * The first option to the helper is always the path to the translation
 * Afterward, you can pass key value pairs which are passed to the t() method
 * as on object.
 * escaped string: {{localizeKM "username"}}
 * escaped string with arguments: {{localizeKM "username" greeting="hello"}}
 * As usual, you can use triple {{{ to insert unescaped HTML
 */
fun registerI18NextHelper(handlebars: Handlebars, i18Next: I18Next) {
    if (Object.hasOwn(handlebars.helpers, "localizeKM")) {
        ui.notifications.error("Handlebars helper 'localizeKM' already defined by another module, translations won't work")
    } else {
        handlebars.registerHelper("localizeKM", { key: String, options: AnyObject ->
            val defaults = recordOf("returnObjects" to false)
            val assign: AnyObject = if (Object.hasOwn(options, "hash")) {
                Object.assign(defaults, options["hash"])
            } else {
                defaults
            }
            i18Next.t(key, assign)
        })
    }
}

fun t(key: String, value: AnyObject) =
    i18next.t(key, value)

fun t(key: String) =
    i18next.t(key)

fun t(translatable: Translatable) =
    i18next.t(translatable.i18nKey)

fun unfuckFoundryTranslations(obj: ReadonlyRecord<String, Any>): ReadonlyRecord<String, Any> {
    return obj.asSequence()
        .map { (key, value) ->
            if (value is String) {
                key to value.replace("&amp;", "&")
            } else {
                key to unfuckFoundryTranslations(value.unsafeCast<Record<String, Any>>())
            }
        }
        .toRecord()
}

suspend fun initLocalization() {
    val lang = game.i18n.lang
    val trans = (game.i18n.translations[Config.moduleId] ?: englishTranslations[Config.moduleId])
        .unsafeCast<ReadonlyRecord<String, Any>>()
    val translations = unfuckFoundryTranslations(trans)
    val options = I18NextInitOptions(
        lng = lang,
        debug = false,
        defaultNS = Config.moduleId,
        resources = recordOf(
            lang to recordOf(Config.moduleId to translations)
        ),
        interpolation = I18NextInterpolationOptions(
            escapeValue = false,
        ),
    )
    i18next
        .use(ICU::class.js)
        .init(options)
        .await()
    registerI18NextHelper(window.Handlebars, i18next)
    val events = translateKingdomEvents()
    translateActivities(events)
    translateCharters()
    translateGovernments()
    translateHeartlands()
    translateCampingActivities()
    translateFeats()
    translateKingdomFeatures()
    translateMilestones()
    translateRecipes()
    translateStructureData()
}

@JsModule("./lang/en.json")
external val englishTranslations: ReadonlyRecord<String, Any>