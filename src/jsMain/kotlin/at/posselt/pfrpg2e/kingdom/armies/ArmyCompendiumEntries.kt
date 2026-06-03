package at.posselt.pfrpg2e.kingdom.armies

import at.posselt.pfrpg2e.Config
import at.posselt.pfrpg2e.data.armies.*
import at.posselt.pfrpg2e.data.checks.getLevelBasedDC
import at.posselt.pfrpg2e.utils.isFirstGM
import at.posselt.pfrpg2e.utils.t
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.Game
import com.foundryvtt.core.abstract.DatabaseCreateOperation
import com.foundryvtt.core.abstract.Document
import com.foundryvtt.core.documents.Folder
import com.foundryvtt.core.documents.JournalEntry
import com.foundryvtt.core.documents.JournalEntryPage
import com.foundryvtt.core.documents.collections.CompendiumCollection
import js.objects.recordOf
import kotlinx.coroutines.await

private const val JOURNAL_PACK = "${Config.moduleId}.kingmaker-tools-journals"
private const val ARMY_FOLDER_NAME = "Workshop Armies"
private const val TACTIC_FOLDER_NAME = "Workshop Army Tactics"
private const val MODIFIER_FOLDER_NAME = "Workshop Army Modifiers"

/**
 * Sanitize a name for use as a stable seed.
 */
private fun slugify(name: String): String =
	name.lowercase()
		.replace("'", "")
		.replace(Regex("[^a-z0-9]+"), "-")
		.trim('-')

private const val ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

/**
 * Derive a deterministic, valid Foundry document id (`[A-Za-z0-9]{16}`) from a stable seed.
 * Same seed always yields the same id, so re-runs stay idempotent and entries get durable UUIDs.
 * Uses an FNV-1a hash to seed an xorshift32 stream (Int math compiles to `Math.imul`, preserving
 * 32-bit overflow semantics on JS).
 */
private fun foundryId(seed: String): String {
	var hash = -2128831035 // FNV-1a 32-bit offset basis (2166136261 as signed Int)
	for (ch in seed) {
		hash = hash xor ch.code
		hash *= 16777619 // FNV prime
	}
	var state = if (hash == 0) 1 else hash
	return buildString {
		repeat(16) {
			state = state xor (state shl 13)
			state = state xor (state ushr 17)
			state = state xor (state shl 5)
			append(ID_ALPHABET[(state and 0x7fffffff) % ID_ALPHABET.length])
		}
	}
}

private fun addModifierRow(
	label: String,
	value: Int,
	rows: MutableList<String>
) {
	if (value != 0) {
		val display = if (value > 0) "+$value" else "$value"
		rows.add("<tr><td><strong>$label</strong></td><td>$display</td></tr>")
	}
}

private fun buildArmyContent(army: WorkbookBasicArmy): String {
	val dc = getLevelBasedDC(army.minimumLevel)
	val statRow = armyStatistics.firstOrNull { it.level == army.minimumLevel }
	val statDc = statRow?.standardDC ?: dc

	val rows = mutableListOf<String>()
	rows.add("<tr><td><strong>Scouting</strong></td><td>${statRow?.scouting ?: 0}</td></tr>")
	rows.add("<tr><td><strong>Standard DC</strong></td><td>$statDc</td></tr>")
	rows.add("<tr><td><strong>AC</strong></td><td>${statRow?.ac ?: 0}</td></tr>")
	rows.add("<tr><td><strong>High Save</strong></td><td>${statRow?.highSave ?: 0}</td></tr>")
	rows.add("<tr><td><strong>Low Save</strong></td><td>${statRow?.lowSave ?: 0}</td></tr>")
	rows.add("<tr><td><strong>Attack Bonus</strong></td><td>${statRow?.attack ?: 0}</td></tr>")

	return buildString {
		append("<p><strong>Type:</strong> ${t("armyType.${army.type.name.lowercase()}")} | ")
		append("<strong>Level:</strong> ${army.minimumLevel} | ")
		append("<strong>DC:</strong> $dc | ")
		append("<strong>HP:</strong> ${army.hitPoints} | ")
		append("<strong>Consumption:</strong> ${army.consumption}</p>")

		append("<h3>Combat Statistics</h3>")
		append("<table>")
		rows.forEach { append(it) }
		append("</table>")

		append("<h3>Details</h3>")
		append("<p><strong>Attack Type:</strong> ${t("armyAttackType.${army.attacks.name.lowercase()}")} | ")
		append("<strong>Ranged Ammo:</strong> ${army.rangedAmmo} | ")
		append("<strong>Maneuver Save:</strong> ${t("armySaveBonus.${army.maneuverSave.name.lowercase()}")}</p>")

		if (army.specialArmyFaction != null) {
			append("<p><strong>Faction:</strong> ${army.specialArmyFaction}</p>")
		}
		append("<p><strong>Accessible at Kingdom Start:</strong> ${if (army.accessible) "Yes" else "No — Unique Army"}</p>")
		append("<p><strong>Maximum Tactics:</strong> ${statRow?.maximumTactics ?: 1}</p>")

		if (army.startingTactics.isNotEmpty()) {
			append("<p><strong>Starting Tactics:</strong> ${army.startingTactics.joinToString(", ")}</p>")
		}

		append("<h3>Description</h3>")
		append("<p>${army.description}</p>")
	}
}

private fun buildTacticContent(tactic: WorkbookArmyTactic): String {
	val dc = getLevelBasedDC(tactic.minimumLevel)

	val applicableTypes = if (tactic.uniqueArmy != null) {
		tactic.uniqueArmy!!
	} else {
		tactic.types.joinToString(", ") { t("armyType.${it.name.lowercase()}") }
	}

	return buildString {
		append("<p><strong>Level:</strong> ${tactic.minimumLevel} | <strong>DC:</strong> $dc</p>")
		append("<p><strong>Applicable To:</strong> $applicableTypes</p>")

		if (tactic.tacticalActions.isNotEmpty()) {
			append("<h3>Tactical Actions</h3>")
			append("<ul>")
			tactic.tacticalActions.forEach { action -> append("<li>$action</li>") }
			append("</ul>")
		}

		if (tactic.actionTraits.isNotEmpty()) {
			append("<p><strong>Action Traits:</strong> ${tactic.actionTraits.joinToString(", ")}</p>")
		}
		if (tactic.actionDescription != null) {
			append("<h3>Tactical Action Details</h3>")
			append("<p>${tactic.actionDescription}</p>")
		}

		append("<h3>Description</h3>")
		append("<p>${tactic.description}</p>")

		if (tactic.actionReferences.isNotEmpty()) {
			append("<h3>References</h3>")
			append("<ul>")
			tactic.actionReferences.forEach { ref ->
				append("<li><a href=\"$ref\">$ref</a></li>")
			}
			append("</ul>")
		}
	}
}

private fun buildModifierContent(modifier: WorkbookSpecializedArmyModifier): String {
	val base = workbookBasicArmies.firstOrNull { it.name == modifier.name }
	val level = base?.minimumLevel?.toString() ?: "?"
	val type = base?.type

	val rows = mutableListOf<String>()
	addModifierRow("Scouting", modifier.scouting, rows)
	addModifierRow("Standard DC", modifier.standardDc, rows)
	addModifierRow("AC", modifier.ac, rows)
	addModifierRow("High Save", modifier.highSave, rows)
	addModifierRow("Low Save", modifier.lowSave, rows)
	addModifierRow("Attack", modifier.attack, rows)
	addModifierRow("Rout Threshold", modifier.routThreshold, rows)

	return buildString {
		append("<p><strong>Army:</strong> ${modifier.name} | ")
		append("<strong>Base Level:</strong> $level")
		if (type != null) {
			append(" | <strong>Type:</strong> ${t("armyType.${type.name.lowercase()}")}")
		}
		append("</p>")

		append("<h3>Stat Modifiers</h3>")
		if (rows.isEmpty()) {
			append("<p>No stat modifiers.</p>")
		} else {
			append("<table>")
			rows.forEach { append(it) }
			append("</table>")
		}

		append("<p><em>These modifiers adjust the base statistics of ${modifier.name} armies from their standard values.</em></p>")
	}
}

private fun buildJournalEntryData(
	id: String,
	name: String,
	folderId: String,
	content: String,
	sort: Int
): AnyObject {
	val pageId = foundryId("$id-page")
	return recordOf(
		"_id" to id,
		"name" to name,
		"folder" to folderId,
		"ownership" to recordOf("default" to 2),
		"sort" to sort,
		"pages" to arrayOf(
			recordOf(
				"_id" to pageId,
				"name" to name,
				"type" to "text",
				"title" to recordOf("show" to true, "level" to 1),
				"text" to recordOf("content" to content, "format" to 1),
				"sort" to 0,
			)
		)
	)
}

private suspend fun getOrCreatePackFolder(
	pack: CompendiumCollection<Document>,
	folderName: String,
	type: String
): String {
	// Search the folders that live inside this compendium pack.
	val existingFolder = pack.folders.contents.firstOrNull { folder ->
		val f = folder.asDynamic()
		f.name as? String == folderName && f.type as? String == type
	}
	if (existingFolder != null) return existingFolder.id ?: existingFolder._id

	val folder = Folder.create(
		recordOf(
			"name" to folderName,
			"type" to type,
		),
		// Compendium folders are created by targeting the pack via the operation, not the data.
		recordOf("pack" to JOURNAL_PACK).unsafeCast<DatabaseCreateOperation>(),
	).await()
	return folder.id ?: ""
}

private suspend fun getExistingNames(pack: CompendiumCollection<Document>): Set<String> {
	// The pack's `contents` is only populated for loaded documents; the index always
	// carries every entry's `name`, so dedup by name (also catches entries created by
	// earlier builds that assigned different ids).
	val index = pack.getIndex().await()
	return index.contents.mapNotNull { entry ->
		entry.asDynamic().name as? String
	}.toSet()
}

suspend fun createArmyCompendiumEntries(game: Game) {
	if (!game.isFirstGM()) return

	val pack = game.packs.get(JOURNAL_PACK)
	if (pack == null) {
		console.warn("ArmyCompendiumEntries: pack '$JOURNAL_PACK' not found — skipping")
		return
	}

	// Reading the index does not require unlocking the pack.
	val existingNames = getExistingNames(pack)

	val missingArmies = workbookBasicArmies.filter { "Army: ${it.name}" !in existingNames }
	val missingTactics = workbookArmyTactics.filter { "Tactic: ${it.name}" !in existingNames }
	val missingModifiers = workbookSpecializedArmyModifiers.filter { "Modifier: ${it.name}" !in existingNames }

	if (missingArmies.isEmpty() && missingTactics.isEmpty() && missingModifiers.isEmpty()) {
		return  // everything already exists — never touch the locked pack on normal startups
	}

	val skipped = (workbookBasicArmies.size - missingArmies.size) +
		(workbookArmyTactics.size - missingTactics.size) +
		(workbookSpecializedArmyModifiers.size - missingModifiers.size)

	// Module compendium packs ship locked; unlock for the duration of the writes and
	// restore the original lock state afterwards.
	val wasLocked = pack.locked
	if (wasLocked) pack.configure(recordOf("locked" to false)).await()

	var created = 0
	try {
		if (missingArmies.isNotEmpty()) {
			val folderId = getOrCreatePackFolder(pack, ARMY_FOLDER_NAME, "JournalEntry")
			val docs = missingArmies.map { army ->
				buildJournalEntryData(
					id = foundryId("wb-army-${slugify(army.name)}"),
					name = "Army: ${army.name}",
					folderId = folderId,
					content = buildArmyContent(army),
					sort = army.minimumLevel * 100,
				)
			}.toTypedArray()
			JournalEntry.createDocuments(
				docs,
				recordOf("keepId" to true, "pack" to JOURNAL_PACK).unsafeCast<DatabaseCreateOperation>(),
			).await()
			created += docs.size
		}

		if (missingTactics.isNotEmpty()) {
			val folderId = getOrCreatePackFolder(pack, TACTIC_FOLDER_NAME, "JournalEntry")
			val docs = missingTactics.map { tactic ->
				buildJournalEntryData(
					id = foundryId("wb-tactic-${slugify(tactic.name)}"),
					name = "Tactic: ${tactic.name}",
					folderId = folderId,
					content = buildTacticContent(tactic),
					sort = tactic.minimumLevel * 100,
				)
			}.toTypedArray()
			JournalEntry.createDocuments(
				docs,
				recordOf("keepId" to true, "pack" to JOURNAL_PACK).unsafeCast<DatabaseCreateOperation>(),
			).await()
			created += docs.size
		}

		if (missingModifiers.isNotEmpty()) {
			val folderId = getOrCreatePackFolder(pack, MODIFIER_FOLDER_NAME, "JournalEntry")
			val docs = missingModifiers.map { mod ->
				buildJournalEntryData(
					id = foundryId("wb-modifier-${slugify(mod.name)}"),
					name = "Modifier: ${mod.name}",
					folderId = folderId,
					content = buildModifierContent(mod),
					sort = 0,
				)
			}.toTypedArray()
			JournalEntry.createDocuments(
				docs,
				recordOf("keepId" to true, "pack" to JOURNAL_PACK).unsafeCast<DatabaseCreateOperation>(),
			).await()
			created += docs.size
		}

		if (created > 0) {
			console.log("ArmyCompendiumEntries: created $created entries, $skipped already existed")
		}
	} catch (e: Throwable) {
		console.error("ArmyCompendiumEntries: failed creating army compendium entries", e)
	} finally {
		if (wasLocked) pack.configure(recordOf("locked" to true)).await()
	}
}
