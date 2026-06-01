package at.posselt.pfrpg2e.data.kingdom.settlements

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill

data class SettlementDetailsMatrixRow(
    val workbookRow: Int,
    val label: String,
    val skill: KingdomSkill? = null,
    val activityId: String? = null,
    val isHeader: Boolean = false,
    val activityIds: Set<String> = activityId?.let { setOf(it) } ?: emptySet(),
)

private fun row(
    workbookRow: Int,
    label: String,
    skill: KingdomSkill? = null,
    activityId: String? = null,
    isHeader: Boolean = false,
    activityIds: Set<String> = activityId?.let { setOf(it) } ?: emptySet(),
) = SettlementDetailsMatrixRow(
    workbookRow = workbookRow,
    label = label,
    skill = skill,
    activityId = activityId,
    isHeader = isHeader,
    activityIds = activityIds,
)

val settlementDetailsMatrixRows = listOf(
    row(46, "Agriculture", KingdomSkill.AGRICULTURE, isHeader = true),
    row(47, "Establish Farmland", KingdomSkill.AGRICULTURE, "establish-farmland"),
    row(48, "Harvest Crops", KingdomSkill.AGRICULTURE, "harvest-crops"),
    row(50, "Arts", KingdomSkill.ARTS, isHeader = true),
    row(51, "Craft Luxuries", KingdomSkill.ARTS, "craft-luxuries"),
    row(52, "Create a Masterpiece", KingdomSkill.ARTS, "create-a-masterpiece"),
    row(53, "Repair Reputation (Corruption)", KingdomSkill.ARTS, "repair-reputation-corruption"),
    row(54, "Rest and Relax (Arts)", KingdomSkill.ARTS, "rest-and-relax"),
    row(55, "Quell Unrest (Arts)", KingdomSkill.ARTS, "quell-unrest"),
    row(57, "Boating", KingdomSkill.BOATING, isHeader = true),
    row(58, "Go Fishing", KingdomSkill.BOATING, "go-fishing"),
    row(59, "Rest and Relax (Boating)", KingdomSkill.BOATING, "rest-and-relax"),
    row(61, "Defense", KingdomSkill.DEFENSE, isHeader = true),
    row(62, "Fortify Hex", KingdomSkill.DEFENSE, "fortify-hex"),
    row(63, "Provide Care", KingdomSkill.DEFENSE, "provide-care"),
    row(65, "Engineering", KingdomSkill.ENGINEERING, isHeader = true),
    row(66, "Build Roads", KingdomSkill.ENGINEERING, "build-roads"),
    row(67, "Demolish", KingdomSkill.ENGINEERING, "demolish"),
    row(
        68,
        "Establish Work Site",
        KingdomSkill.ENGINEERING,
        "establish-work-site",
        activityIds = setOf("establish-work-site-lumber", "establish-work-site-mine", "establish-work-site-quarry"),
    ),
    row(69, "Establish Work Site (Lumber Camp)", KingdomSkill.ENGINEERING, "establish-work-site-lumber"),
    row(70, "Establish Work Site (Mine)", KingdomSkill.ENGINEERING, "establish-work-site-mine"),
    row(71, "Establish Work Site (Quarry)", KingdomSkill.ENGINEERING, "establish-work-site-quarry"),
    row(72, "Irrigation", KingdomSkill.ENGINEERING, "irrigation"),
    row(73, "Repair Reputation (Decay)", KingdomSkill.ENGINEERING, "repair-reputation-decay"),
    row(75, "Exploration", KingdomSkill.EXPLORATION, isHeader = true),
    row(76, "Hire Adventurers", KingdomSkill.EXPLORATION, "hire-adventurers"),
    row(78, "Folklore", KingdomSkill.FOLKLORE, isHeader = true),
    row(79, "Celebrate Holiday", KingdomSkill.FOLKLORE, "celebrate-holiday"),
    row(80, "Quell Unrest (Folklore)", KingdomSkill.FOLKLORE, "quell-unrest"),
    row(82, "Industry", KingdomSkill.INDUSTRY, isHeader = true),
    row(83, "Relocate Capital", KingdomSkill.INDUSTRY, "relocate-capital"),
    row(84, "Repair Reputation (Strife)", KingdomSkill.INDUSTRY, "repair-reputation-strife"),
    row(85, "Trade Commodities", KingdomSkill.INDUSTRY, "trade-commodities"),
    row(87, "Intrigue", KingdomSkill.INTRIGUE, isHeader = true),
    row(88, "Clandestine Business", KingdomSkill.INTRIGUE, "clandestine-business"),
    row(89, "Infiltration", KingdomSkill.INTRIGUE, "infiltration"),
    row(90, "Quell Unrest (Intrigue)", KingdomSkill.INTRIGUE, "quell-unrest"),
    row(92, "Magic", KingdomSkill.MAGIC, isHeader = true),
    row(93, "Prognostication", KingdomSkill.MAGIC, "prognostication"),
    row(94, "Quell Unrest (Magic)", KingdomSkill.MAGIC, "quell-unrest"),
    row(95, "Supernatural Solution", KingdomSkill.MAGIC, "supernatural-solution"),
    row(97, "Politics", KingdomSkill.POLITICS, isHeader = true),
    row(98, "Improve Lifestyle", KingdomSkill.POLITICS, "improve-lifestyle"),
    row(99, "Quell Unrest (Politics)", KingdomSkill.POLITICS, "quell-unrest"),
    row(101, "Scholarship", KingdomSkill.SCHOLARSHIP, isHeader = true),
    row(102, "Creative Solution", KingdomSkill.SCHOLARSHIP, "creative-solution"),
    row(103, "Rest and Relax (Scholarship)", KingdomSkill.SCHOLARSHIP, "rest-and-relax"),
    row(105, "Statecraft", KingdomSkill.STATECRAFT, isHeader = true),
    row(106, "Request Foreign Aid", KingdomSkill.STATECRAFT, "request-foreign-aid"),
    row(107, "Send Diplomatic Envoy", KingdomSkill.STATECRAFT, "send-diplomatic-envoy"),
    row(108, "Tap Treasury", KingdomSkill.STATECRAFT, "tap-treasury"),
    row(110, "Trade", KingdomSkill.TRADE, isHeader = true),
    row(111, "Capital Investment", KingdomSkill.TRADE, "capital-investment"),
    row(112, "Collect Taxes", KingdomSkill.TRADE, "collect-taxes"),
    row(113, "Manage Trade Agreements", KingdomSkill.TRADE, "manage-trade-agreements"),
    row(114, "Purchase Commodities", KingdomSkill.TRADE, "purchase-commodities"),
    row(115, "Repair Reputation (Crime)", KingdomSkill.TRADE, "repair-reputation-crime"),
    row(116, "Rest and Relax (Trade)", KingdomSkill.TRADE, "rest-and-relax"),
    row(118, "Warfare", KingdomSkill.WARFARE, isHeader = true),
    row(119, "Pledge of Fealty (Warfare)", KingdomSkill.WARFARE, "pledge-of-fealty"),
    row(120, "Quell Unrest (Warfare)", KingdomSkill.WARFARE, "quell-unrest"),
    row(122, "Wilderness", KingdomSkill.WILDERNESS, isHeader = true),
    row(123, "Gather Livestock", KingdomSkill.WILDERNESS, "gather-livestock"),
    row(124, "Rest and Relax (Wilderness)", KingdomSkill.WILDERNESS, "rest-and-relax"),
    row(126, "Any", isHeader = true),
    row(127, "Focused Attention", activityId = "focused-attention"),
    row(129, "General", isHeader = true),
    row(130, "Abandon Hex", activityId = "abandon-hex"),
    row(131, "Build Structure", activityId = "build-structure"),
    row(132, "Claim Hex", activityId = "claim-hex"),
    row(133, "Clear Hex", activityId = "clear-hex"),
    row(134, "Establish Settlement", activityId = "establish-settlement"),
    row(135, "Establish Trade Agreement", activityId = "establish-trade-agreement"),
    row(136, "New Leadership", activityId = "new-leadership"),
    row(137, "Pledge of Fealty", activityId = "pledge-of-fealty"),
    row(138, "Quell Unrest", activityId = "quell-unrest"),
    row(
        139,
        "Repair Reputation",
        activityId = "repair-reputation",
        activityIds = setOf(
            "repair-reputation-corruption",
            "repair-reputation-crime",
            "repair-reputation-decay",
            "repair-reputation-strife",
        ),
    ),
    row(140, "Rest and Relax", activityId = "rest-and-relax"),
    row(142, "Army", isHeader = true),
    row(
        143,
        "Recover Army",
        activityId = "recover-army",
        activityIds = setOf(
            "recover-army-damaged",
            "recover-army-defeated",
            "recover-army-lost",
            "recover-army-mired-pinned",
            "recover-army-shaken",
            "recover-army-weary",
        ),
    ),
    row(144, "Recruit Army", activityId = "recruit-army"),
    row(145, "Train Army", activityId = "train-army"),
)

val settlementActivityMatrixRows = settlementDetailsMatrixRows

fun Settlement.matrixBonusFor(row: SettlementDetailsMatrixRow): Int? {
    val activityIds = row.activityIds
    val bonuses = highestUniqueBonuses.filter { bonus ->
        val activityMatches = activityIds.isNotEmpty() && bonus.activity in activityIds
        when {
            row.skill != null && activityIds.isNotEmpty() ->
                (bonus.skill == row.skill && bonus.activity == null) ||
                        (bonus.skill == null && activityMatches) ||
                        (bonus.skill == row.skill && activityMatches)
            row.skill != null -> bonus.skill == row.skill && bonus.activity == null
            activityIds.isNotEmpty() -> activityMatches
            else -> false
        }
    }
    return bonuses.maxOfOrNull { it.value }
}
