package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.HiddenInput
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.app.forms.OverrideType
import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.kingdom.data.RawWorkSite
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import at.posselt.pfrpg2e.toLabel
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface WorkSiteContext {
    val label: String
    val quantity: FormElementContext
    val resources: FormElementContext
    val quantityValue: Int
    val resourcesValue: Int
}

fun RawWorkSite.toContext(worksites: RealmData.WorkSite, key: String, automate: Boolean) =
    WorkSiteContext(
        label = key.toLabel(),
        quantity = if (automate) {
            HiddenInput(
                value = quantity.toString(),
                name = "workSites.$key.quantity",
                overrideType = OverrideType.NUMBER,
            ).toContext()
        } else {
            NumberInput(
                value = quantity,
                label = "Quantity",
                name = "workSites.$key.quantity",
                elementClasses = listOf("km-slim-inputs", "km-width-small"),
                hideLabel = true,
                stacked = false,
            ).toContext()
        },
        resources = if (automate) {
            HiddenInput(
                value = resources.toString(),
                name = "workSites.$key.resources",
                overrideType = OverrideType.NUMBER,
            ).toContext()
        } else {
            NumberInput(
                value = resources,
                label = "Resources",
                name = "workSites.$key.resources",
                elementClasses = listOf("km-slim-inputs", "km-width-small"),
                hideLabel = true,
                stacked = false,
            ).toContext()
        },
        quantityValue = worksites.quantity,
        resourcesValue = worksites.resources,
    )

fun RawWorkSites.toContext(worksites: RealmData.WorkSites, automate: Boolean) =
    arrayOf(
        farmlands.toContext(worksites.farmlands, "farmlands", automate),
        lumberCamps.toContext(worksites.lumberCamps, "lumberCamps", automate),
        mines.toContext(worksites.mines, "mines", automate),
        quarries.toContext(worksites.quarries, "quarries", automate),
        luxurySources.toContext(worksites.luxurySources, "luxurySources", automate),
    )