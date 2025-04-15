package at.posselt.pfrpg2e.kingdom.dialogs

import at.posselt.pfrpg2e.app.prompt
import at.posselt.pfrpg2e.data.kingdom.kingdomSizeData
import at.posselt.pfrpg2e.utils.asAnyObject
import at.posselt.pfrpg2e.utils.t
import js.objects.JsPlainObject

@Suppress("unused")
@JsPlainObject
private external interface SizeContext {
    val size: String
    val type: String
    val dice: String
    val modifier: String
    val storage: String
}

@Suppress("unused")
@JsPlainObject
private external interface KingdomSizeContext {
    val data: Array<SizeContext>
}

suspend fun kingdomSizeHelp() {
    prompt<Unit, Unit>(
        title = "Kingdom Size",
        templatePath = "applications/kingdom/kingdom-size-help.hbs",
        buttonLabel = "Close",
        templateContext = KingdomSizeContext(
            data = kingdomSizeData
                .map {
                    val sizeTo = it.sizeTo
                    val size = it.sizeFrom.toString() + if (sizeTo != null) "-$sizeTo" else ""
                    SizeContext(
                        size = size,
                        type = t(it.type),
                        dice = "1" + t(it.resourceDieSize),
                        modifier = "+" + it.controlDCModifier,
                        storage = it.commodityCapacity.toString(),
                    )
                }
                .toTypedArray()
        ).asAnyObject()
    ) {
    }
}