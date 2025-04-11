package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.AnyObject
import com.i18next.I18Next
import js.objects.Object
import js.objects.recordOf
import org.w3c.dom.Window

external class Handlebars {
    fun registerHelper(name: String, callback: dynamic)
}

inline val Window.Handlebars: Handlebars
    get() = asDynamic().Handlebars.unsafeCast<Handlebars>()

/**
 * The first option to the helper is always the path to the translation
 * Afterward, you can pass key value pairs which are passed to the t() method
 * as on object.
 * escaped string: {{t "username"}}
 * escaped string with arguments: {{t "username" greeting="hello"}}
 * As usual, you can use trippe {{{ to insert unescaped HTML
 */
fun registerI18NextHelper(handlebars: Handlebars, i18Next: I18Next) {
    handlebars.registerHelper("t", { key: String, options: AnyObject ->
        val defaults = recordOf("returnObjects" to false)
        val assign: AnyObject = if (Object.hasOwn(options, "hash")) {
            Object.assign(defaults, options["hash"])
        } else {
            defaults
        }
        i18Next.t(key, assign)
    })
}