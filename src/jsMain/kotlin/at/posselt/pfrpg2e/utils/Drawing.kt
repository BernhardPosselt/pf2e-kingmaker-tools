package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.documents.DrawingDocument
import com.foundryvtt.core.documents.Scene
import kotlinx.coroutines.await

/**
 * Mirror Foundry's `BaseDrawing.validateJoint` rule: a drawing must have visible text,
 * a visible fill, or a visible line. Evaluated in JS using Foundry's documented schema
 * defaults (fillType 0, fillAlpha 0.5, strokeWidth 8, strokeAlpha 1) so unset fields are
 * treated exactly as Foundry would. Returns false for a drawing Foundry would reject.
 */
private fun drawingHasVisibleComponent(datum: AnyObject): Boolean =
    js(
        """(function (d) {
            var text = d.text == null ? "" : ("" + d.text);
            var fillType = d.fillType == null ? 0 : d.fillType;
            var fillAlpha = d.fillAlpha == null ? 0.5 : d.fillAlpha;
            var strokeWidth = d.strokeWidth == null ? 8 : d.strokeWidth;
            var strokeAlpha = d.strokeAlpha == null ? 1 : d.strokeAlpha;
            var textAlpha = d.textAlpha == null ? 1 : d.textAlpha;
            var hasText = (text.trim() !== "") && (textAlpha > 0);
            // Only a SOLID fill (fillType 1) counts as visible. PATTERN (2) needs a texture
            // image and otherwise renders nothing AND fails Foundry's joint validation.
            var hasFill = (fillType === 1) && (fillAlpha > 0);
            var hasLine = (strokeWidth > 0) && (strokeAlpha > 0);
            return hasText || hasFill || hasLine;
        })(datum)"""
    ).unsafeCast<Boolean>()

/**
 * DEBUG: dump a drawing's actual runtime visibility-relevant fields, plus its computed
 * has-text/fill/line breakdown, so the console line right before a Foundry validateJoint
 * error names the culprit and shows exactly which field is wrong.
 */
private fun debugLogDrawing(datum: AnyObject) {
    js(
        """(function (d) {
            var flagType = "?";
            try {
                var f = d.flags && d.flags["pf2e-kingmaker-tools"];
                if (f) { flagType = (f.realmTile && f.realmTile.type) || (f.zoneLabel ? "zoneLabel" : "?"); }
            } catch (e) {}
            var text = d.text == null ? "" : ("" + d.text);
            var fillType = d.fillType == null ? 0 : d.fillType;
            var fillAlpha = d.fillAlpha == null ? 0.5 : d.fillAlpha;
            var strokeWidth = d.strokeWidth == null ? 8 : d.strokeWidth;
            var strokeAlpha = d.strokeAlpha == null ? 1 : d.strokeAlpha;
            var textAlpha = d.textAlpha == null ? 1 : d.textAlpha;
            var hasText = (text.trim() !== "") && (textAlpha > 0);
            var hasFill = (fillType === 1) && (fillAlpha > 0);
            var hasLine = (strokeWidth > 0) && (strokeAlpha > 0);
            console.log("[pf2e-kmt] DRAW type=" + flagType,
                "| text=" + JSON.stringify(text) + " textAlpha=" + d.textAlpha,
                "| fillType=" + d.fillType + " fillAlpha=" + d.fillAlpha,
                "| strokeWidth=" + d.strokeWidth + " strokeAlpha=" + d.strokeAlpha,
                "| shape=" + (d.shape && d.shape.type),
                "| hasText=" + hasText + " hasFill=" + hasFill + " hasLine=" + hasLine,
                "| VISIBLE=" + (hasText || hasFill || hasLine));
        })(datum)"""
    )
}

/**
 * Create Drawing documents resiliently:
 *  - Drawings that would fail Foundry's joint validation (no visible text/fill/line) are
 *    skipped *before* being sent to Foundry, so Foundry never logs a DataModelValidationError.
 *  - Each remaining drawing is created on its own and wrapped in try/catch, so any other
 *    per-document failure is logged and skipped instead of aborting the whole batch.
 * Skipped/failed data is logged with all its fields so the cause is always visible.
 */
suspend fun Scene.createDrawingsResilient(data: Array<AnyObject>): Array<DrawingDocument> {
    val created = mutableListOf<DrawingDocument>()
    for (datum in data) {
        debugLogDrawing(datum)
        if (!drawingHasVisibleComponent(datum)) {
            console.warn(
                "[pf2e-kmt] PRE-SKIP: Drawing with no visible text, fill, or line",
                datum,
            )
            continue
        }
        try {
            val docs = createEmbeddedDocuments<DrawingDocument>("Drawing", arrayOf(datum)).await()
            created.addAll(docs.asList())
        } catch (e: Throwable) {
            console.error("[pf2e-kmt] CREATE THREW for Drawing", datum, e)
        }
    }
    return created.toTypedArray()
}

/**
 * Delete Drawing documents resiliently. Foundry's EmbeddedCollection throws
 * "id [X] does not exist in the EmbeddedCollection collection" when asked to delete an id that
 * has already been removed. This happens whenever a once-captured snapshot of
 * `scene.drawings.contents` is reused across loop iterations: an earlier iteration deletes a
 * drawing from the live collection, but the stale snapshot still holds the reference, so a later
 * iteration asks to delete the same id again.
 *
 * We filter the requested ids down to those still present in the live collection (de-duplicated),
 * and wrap the call in try/catch so a concurrent removal can't abort the whole batch.
 */
suspend fun Scene.deleteDrawingsResilient(ids: Array<String>) {
    val liveIds = ids.filter { drawings.get(it) != null }.distinct().toTypedArray()
    if (liveIds.isEmpty()) return
    try {
        deleteEmbeddedDocuments<DrawingDocument>("Drawing", liveIds).await()
    } catch (e: Throwable) {
        console.error("[pf2e-kmt] DELETE THREW for Drawings", liveIds, e)
    }
}

fun DrawingDocument.getRealmTileData(): RealmTileData? =
    getAppFlag("realmTile")

suspend fun DrawingDocument.setRealmTileData(data: RealmTileData) {
    setAppFlag("realmTile", data)
}

suspend fun DrawingDocument.unsetRealmTileData() {
    unsetAppFlag("realmTile")
}