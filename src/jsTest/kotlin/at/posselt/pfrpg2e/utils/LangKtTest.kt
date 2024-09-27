package at.posselt.pfrpg2e.utils

import js.objects.jso
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LangKtTest {
    @Test
    fun isObject() {
        assertTrue(isJsObject(jso()))
        assertFalse(isJsObject(""))
        assertFalse(isJsObject(null))
        assertFalse(isJsObject(js("undefined")))
        assertFalse(isJsObject(1))
        assertFalse(isJsObject(emptyArray<Any>()))
        assertFalse(isJsObject(js("[]")))
    }
}