package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawOngoingKingdomEvent
import at.posselt.pfrpg2e.utils.postChatMessage
import com.foundryvtt.core.Game
import kotlinx.js.JsPlainObject

private val kingdomEventIdMapping = mapOf(
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.GYO0H3yx4Izv8h7w]" to "too-close-to-home",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.SJR5W6LsquErMEhD]" to "urban-outbreak",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.oACpUsHavxM0gXje]" to "public-outbreak",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.k12fOB3Tm5l0xjEO]" to "archaeological-find",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.ijxmMlxMY1REOwJY]" to "assassination-attempt",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.N29niitpzc7GWUxN]" to "bandit-activity",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.ug1AYlKJKtSUeLYH]" to "local-disaster",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.b3XupGcqhuzuyKPw]" to "boomtown",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.Ct40OuQru3STCfyD]" to "building-demand",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.S1imtHR8xUkvPZHZ]" to "crop-failure",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.EaIsjefqXYVgfcq0]" to "cult-activity",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.bXc4KHMVR2NYrUt2]" to "diplomatic-overture",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.nxpBZrOcufzI3iJE]" to "discovery",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.l5M53xbktELh5nwz]" to "drug-den",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.YkEO6Ukf6o7olOoT]" to "economic-surge",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.rNxTrfqnm2fid8au]" to "expansion-demand",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.fGjJD3w3p4Bl4sff]" to "festive-invitation",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.FYYKdDvHrPnWnzhz]" to "feud",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.qHu7jBop1YzvEZF4]" to "food-shortage",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.89aS4xk8RYXqBYzE]" to "food-surplus",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.ZcThuH6XS88FlMqz]" to "good-weather",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.OEuqkyrjzsSjtl3G]" to "inquisition",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.H0m1LdwFOHNJTZFt]" to "justice-prevails",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.CfAXZYN88RyWWctd]" to "land-rush",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.pr1olkpuBA78fisx]" to "monster-activity",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.h8lJCzlZxEzml5zL]" to "natural-disaster",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.mN39Zf6Y8DT0MMKs]" to "natures-blessing",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.EJXLlpCCnfHLxNHN]" to "new-subjects",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.tyGnmI3RQeNGkt06]" to "noblesse-oblige",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.B9NmOtqLfEleuL3O]" to "outstanding-success",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.01jXsIhNx8Tn0BYr]" to "pilgrimage",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.9ifdy5CVK9lT5X9t]" to "plague",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.WYleHy8NAkdto1p0]" to "political-calm",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.OURUA8YrKosurVYG]" to "public-scandal",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.SC8cBkkhGX6WOzVX]" to "remarkable-treasure",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.fLcBrTB6qYEQZj47]" to "sacrifices",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.RTa6cljeJntKeVFw]" to "sensational-crime",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.q4O1wzWvplrTf8ru]" to "squatters",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.amPCmoD9IzZcQhbA]" to "undead-uprising",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.fOU85L4sMJsKngE5]" to "unexpected-find",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.B6ipXbbfumQNhlCx]" to "vandals",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.ii3XuPLxTj2pc3PE]" to "visiting-celebrity",
    "@UUID[JournalEntry.dAtoyQhSfSBvjr2W.JournalEntryPage.L1dEcsUBNS8rUCNS]" to "wealthy-immigrant",
)

@JsPlainObject
external interface OngoingEvent {
    var name: String
}

class Migration14 : Migration(14) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        val ongoingEvents = kingdom.asDynamic().ongoingEvents.unsafeCast<Array<OngoingEvent>>()
        kingdom.homebrewKingdomEvents = emptyArray()
        kingdom.kingdomEventBlacklist = emptyArray()
        kingdom.settings.kingdomCultTable = null
        kingdom.settings.kingdomEventsTable = null
        kingdom.ongoingEvents = ongoingEvents
            .mapNotNull { kingdomEventIdMapping[it.name] }
            .map {
                RawOngoingKingdomEvent(
                    stage = 0,
                    id = it
                )
            }
            .toTypedArray()
        val failedMigrationEvents = ongoingEvents.filter { it.name !in kingdomEventIdMapping }
        if (failedMigrationEvents.isNotEmpty()) {
            postChatMessage("The following events could not be migrated: " + failedMigrationEvents.joinToString(", ") { it.name })
        }
    }

    override suspend fun migrateOther(game: Game) {

    }
}