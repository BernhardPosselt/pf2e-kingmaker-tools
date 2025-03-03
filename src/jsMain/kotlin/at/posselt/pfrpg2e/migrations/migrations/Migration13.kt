package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.RawInPredicate
import at.posselt.pfrpg2e.kingdom.RawPredicate
import at.posselt.pfrpg2e.utils.asAnyObject
import com.foundryvtt.core.Game
import js.array.tupleOf
import js.reflect.Reflect

class Migration13 : Migration(13) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.modifiers.forEach {
            val predicates = it.predicates?.toMutableList() ?: mutableListOf<RawPredicate>()
            if (it.asAnyObject()["consumeId"] == "") {
                it.isConsumedAfterRoll = true
            }
            val skills = it.asDynamic().skills as Array<String>?
            val activities = it.asDynamic().activities as Array<String>?
            val abilities = it.asDynamic().abilities as Array<String>?
            val phases = it.asDynamic().phases as Array<String>?
            if (skills != null) {
                predicates.add(RawInPredicate(`in` = tupleOf("@skill", skills)))
            }
            if (activities != null) {
                predicates.add(RawInPredicate(`in` = tupleOf("@activity", activities)))
            }
            if (abilities != null) {
                predicates.add(RawInPredicate(`in` = tupleOf("@ability", abilities)))
            }
            if (phases != null) {
                predicates.add(RawInPredicate(`in` = tupleOf("@phase", phases)))
            }
            Reflect.deleteProperty(it, "skills")
            Reflect.deleteProperty(it, "activities")
            Reflect.deleteProperty(it, "abilities")
            Reflect.deleteProperty(it, "phases")
            it.predicates = predicates.toTypedArray()
        }
    }
}