package at.posselt.pfrpg2e.utils

import kotlinx.browser.document
import kotlinx.html.a
import kotlinx.html.dom.create
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

fun downloadJson(data: Any, fileName: String) {
    val blob = Blob(arrayOf(JSON.stringify(data, null, 2)), BlobPropertyBag(type="application/json"))
    val url = URL.createObjectURL(blob)
    val link  = document.create.a {
        href = url
        downLoad = fileName
    }
    link.click()
}