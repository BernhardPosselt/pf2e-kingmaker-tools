package at.posselt.pfrpg2e.migrations.migrations

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill.*
import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.LeaderKingdomSkills
import at.posselt.pfrpg2e.kingdom.LeaderSkills
import com.foundryvtt.core.Game

class Migration12 : Migration(12) {
    override suspend fun migrateKingdom(game: Game, kingdom: KingdomData) {
        kingdom.activityBlacklist = kingdom.activityBlacklist + arrayOf("focused-attention-vk", "celebrate-holiday-vk", "retrain-vk", "fortify-hex-vk", "clear-hex-vk", "new-leadership-vk")
        kingdom.leaderKingdomSkills = LeaderKingdomSkills(
            ruler = arrayOf(INDUSTRY, INTRIGUE, POLITICS, STATECRAFT, WARFARE).map { it.value }.toTypedArray(),
            counselor = arrayOf(ARTS, FOLKLORE, POLITICS, SCHOLARSHIP, TRADE).map { it.value }.toTypedArray(),
            emissary = arrayOf(INTRIGUE, MAGIC, POLITICS, STATECRAFT, TRADE).map { it.value }.toTypedArray(),
            general = arrayOf(BOATING, DEFENSE, ENGINEERING, EXPLORATION, WARFARE).map { it.value }.toTypedArray(),
            magister = arrayOf(DEFENSE, FOLKLORE, MAGIC, SCHOLARSHIP, WILDERNESS).map { it.value }.toTypedArray(),
            treasurer = arrayOf(ARTS, BOATING, INDUSTRY, INTRIGUE, TRADE).map { it.value }.toTypedArray(),
            viceroy = arrayOf(AGRICULTURE, ENGINEERING, INDUSTRY, SCHOLARSHIP, WILDERNESS).map { it.value }.toTypedArray(),
            warden = arrayOf(AGRICULTURE, BOATING, DEFENSE, EXPLORATION, WILDERNESS).map { it.value }.toTypedArray(),
        )
        kingdom.leaderSkills = LeaderSkills(
            ruler = arrayOf("diplomacy", "deception", "intimidation", "performance", "society", "heraldry", "politics", "ruler"),
            counselor = arrayOf("diplomacy", "deception", "performance", "religion", "society", "academia", "art", "counselor"),
            emissary = arrayOf("diplomacy", "deception", "intimidation", "stealth", "thievery", "politics", "underworld", "emissary"),
            general = arrayOf("diplomacy", "athletics", "crafting", "intimidation", "survival", "scouting", "warfare", "general"),
            magister = arrayOf("diplomacy", "arcana", "nature", "occultism", "religion", "academia", "scribing", "magister"),
            treasurer = arrayOf("diplomacy", "crafting", "medicine", "society", "thievery", "labor", "mercantile", "treasurer"),
            viceroy = arrayOf("diplomacy", "crafting", "medicine", "nature", "society", "architecture", "engineering", "viceroy"),
            warden = arrayOf("diplomacy", "athletics", "nature", "stealth", "survival", "farming", "hunting", "warden"),
        )
    }
}