package at.posselt.pfrpg2e.kingdom.sheet.contexts

import at.posselt.pfrpg2e.app.forms.FormElementContext
import at.posselt.pfrpg2e.app.forms.NumberInput
import at.posselt.pfrpg2e.data.kingdom.RealmData
import at.posselt.pfrpg2e.kingdom.data.RawWorkSite
import at.posselt.pfrpg2e.kingdom.data.RawWorkSites
import kotlinx.js.JsPlainObject

@JsPlainObject
external interface WorkSiteContext {
    val quantity: FormElementContext
    val resources: FormElementContext
    val quantityValue: Int
    val resourcesValue: Int
}

@JsPlainObject
external interface WorkSitesContext {
    val farmlands: WorkSiteContext
    val lumberCamps: WorkSiteContext
    val mines: WorkSiteContext
    val quarries: WorkSiteContext
    val luxurySources: WorkSiteContext
}

fun RawWorkSite.toContext(worksites: RealmData.WorkSite, key: String) =
    WorkSiteContext(
        quantity = NumberInput(
            value = quantity,
            label = "Quantity",
            name = "workSites.$key.quantity"
        ).toContext(),
        resources = NumberInput(
            value = quantity,
            label = "Quantity",
            name = "workSites.$key.resources"
        ).toContext(),
        quantityValue = worksites.quantity,
        resourcesValue = worksites.resources,
    )

fun RawWorkSites.toContext(worksites: RealmData.WorkSites) =
    WorkSitesContext(
        farmlands = farmlands.toContext(worksites.farmlands, "farmlands"),
        lumberCamps = lumberCamps.toContext(worksites.lumberCamps, "lumberCamps"),
        mines = mines.toContext(worksites.mines, "mines"),
        quarries = quarries.toContext(worksites.quarries, "quarries"),
        luxurySources = luxurySources.toContext(worksites.luxurySources, "luxurySources"),
    )