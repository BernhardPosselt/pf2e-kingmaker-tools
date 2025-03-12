package at.posselt.pfrpg2e.kingdom.sheet.contexts

import js.objects.JsPlainObject

@JsPlainObject
external interface PhasesContext {
    val commerceEmpty: Boolean
    val leadershipEmpty: Boolean
    val regionEmpty: Boolean
    val civicEmpty: Boolean
    val armyEmpty: Boolean
}