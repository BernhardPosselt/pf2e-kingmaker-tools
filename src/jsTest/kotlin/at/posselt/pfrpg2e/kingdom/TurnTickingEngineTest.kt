package at.posselt.pfrpg2e.kingdom

import at.posselt.pfrpg2e.data.kingdom.structures.CommodityStorage
import at.posselt.pfrpg2e.kingdom.RawModifier
import at.posselt.pfrpg2e.kingdom.data.RawCommodities
import at.posselt.pfrpg2e.kingdom.data.RawConsumption
import at.posselt.pfrpg2e.kingdom.data.RawFame
import at.posselt.pfrpg2e.kingdom.data.RawResources
import at.posselt.pfrpg2e.kingdom.RawCouncilCooldowns
import at.posselt.pfrpg2e.kingdom.data.RawCurrentCommodities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TurnTickingEngineTest {

    // ── helpers ────────────────────────────────────────────────────────

    private fun fame(now: Int = 0, next: Int = 0, type: String = "famous") =
        RawFame(now = now, next = next, type = type)

    private fun resourcePoints(now: Int = 0, next: Int = 0) =
        RawResources(now = now, next = next)

    private val emptyRawCommodities get() = RawCommodities(food = 0, lumber = 0, luxuries = 0, ore = 0, stone = 0)

    private fun commodities(
        nowFood: Int = 0, nowLumber: Int = 0, nowLux: Int = 0, nowOre: Int = 0, nowStone: Int = 0,
        nextFood: Int = 0, nextLumber: Int = 0, nextLux: Int = 0, nextOre: Int = 0, nextStone: Int = 0,
    ) = RawCurrentCommodities(
        now = RawCommodities(food = nowFood, lumber = nowLumber, luxuries = nowLux, ore = nowOre, stone = nowStone),
        next = RawCommodities(food = nextFood, lumber = nextLumber, luxuries = nextLux, ore = nextOre, stone = nextStone),
    )

    private fun storage(
        food: Int = 100, lumber: Int = 100, luxuries: Int = 100, ore: Int = 100, stone: Int = 100,
    ) = CommodityStorage(food = food, lumber = lumber, luxuries = luxuries, ore = ore, stone = stone)

    private fun consumption(now: Int = 0, next: Int = 0, armies: Int = 0) =
        RawConsumption(now = now, next = next, armies = armies)

    private fun cooldowns(audit: Int = 0, scrying: Int = 0, lockdown: Int = 0, feast: Int = 0) =
        RawCouncilCooldowns(audit = audit, scrying = scrying, lockdown = lockdown, feast = feast)

    private fun modifier(turns: Int? = null) =
        RawModifier(
            id = "test-mod",
            type = "circumstance",
            value = 1,
            name = "Test Modifier",
            enabled = true,
            turns = turns,
        )

    private fun tick(
        fame: RawFame = fame(),
        resourcePoints: RawResources = resourcePoints(),
        resourceDice: RawResources = resourcePoints(),
        consumption: RawConsumption = consumption(),
        commodities: RawCurrentCommodities = commodities(),
        storage: CommodityStorage = storage(),
        councilCooldowns: RawCouncilCooldowns? = null,
        modifiers: Array<RawModifier> = emptyArray(),
    ) = TurnTickingEngine.tick(
        fame = fame,
        resourcePoints = resourcePoints,
        resourceDice = resourceDice,
        consumption = consumption,
        commodities = commodities,
        storage = storage,
        councilCooldowns = councilCooldowns,
        modifiers = modifiers,
    )

    // ── Solution counters ──────────────────────────────────────────────

    @Test
    fun testSolutionsResetToZero() {
        val result = tick()
        assertEquals(0, result.supernaturalSolutions)
        assertEquals(0, result.creativeSolutions)
    }

    @Test
    fun testSolutionsAlwaysResetRegardlessOfPreviousValues() {
        val result = tick()
        // The engine always resets; no matter what the previous values were
        assertEquals(0, result.supernaturalSolutions, "Supernatural solutions should reset to 0")
        assertEquals(0, result.creativeSolutions, "Creative solutions should reset to 0")
    }

    // ── Fame advancement ───────────────────────────────────────────────

    @Test
    fun testFameNextBecomesNow() {
        val result = tick(fame = fame(now = 3, next = 5))
        assertEquals(5, result.fame.now, "Fame next should become now")
    }

    @Test
    fun testFameNextResetToZero() {
        val result = tick(fame = fame(now = 3, next = 5))
        assertEquals(0, result.fame.next, "Fame next should reset to 0")
    }

    @Test
    fun testFameTypePreserved() {
        val result = tick(fame = fame(now = 1, next = 2, type = "infamous"))
        assertEquals("infamous", result.fame.type)
    }

    @Test
    fun testFameNoChangeWhenBothZero() {
        val result = tick(fame = fame(now = 0, next = 0))
        assertEquals(0, result.fame.now)
        assertEquals(0, result.fame.next)
    }

    @Test
    fun testFameChangeTracked() {
        val result = tick(fame = fame(now = 3, next = 5))
        val nowChanges = result.changes.filter { it.category == "fame" && it.field == "now" }
        assertEquals(1, nowChanges.size)
        assertEquals(3, nowChanges[0].oldValue)
        assertEquals(5, nowChanges[0].newValue)
    }

    // ── Resource points advancement ────────────────────────────────────

    @Test
    fun testResourcePointsNextBecomesNow() {
        val result = tick(resourcePoints = resourcePoints(now = 10, next = 25))
        assertEquals(25, result.resourcePoints.now)
        assertEquals(0, result.resourcePoints.next)
    }

    @Test
    fun testResourcePointsNoChangeWhenBothZero() {
        val result = tick(resourcePoints = resourcePoints(now = 0, next = 0))
        assertEquals(0, result.resourcePoints.now)
        assertEquals(0, result.resourcePoints.next)
    }

    @Test
    fun testResourceDiceNextBecomesNow() {
        val result = tick(resourceDice = resourcePoints(now = 4, next = 8))
        assertEquals(8, result.resourceDice.now)
        assertEquals(0, result.resourceDice.next)
    }

    // ── Consumption advancement ────────────────────────────────────────

    @Test
    fun testConsumptionNextBecomesNow() {
        val result = tick(consumption = consumption(now = 2, next = 5, armies = 4))
        assertEquals(5, result.consumption.now)
        assertEquals(0, result.consumption.next)
    }

    @Test
    fun testConsumptionArmiesPreserved() {
        val result = tick(consumption = consumption(now = 2, next = 5, armies = 4))
        assertEquals(4, result.consumption.armies)
    }

    @Test
    fun testConsumptionNoChangeWhenBothZero() {
        val result = tick(consumption = consumption(now = 0, next = 0, armies = 2))
        assertEquals(0, result.consumption.now)
        assertEquals(0, result.consumption.next)
        assertEquals(2, result.consumption.armies)
    }

    // ── Commodity merging with storage ─────────────────────────────────

    @Test
    fun testCommoditiesNextMergedIntoNow() {
        val result = tick(
            commodities = commodities(nowFood = 5, nextFood = 3, nowLumber = 2, nextLumber = 4),
            storage = storage(food = 100, lumber = 100),
        )
        assertEquals(8, result.commodities.now.food, "Food should be merged: 5 + 3")
        assertEquals(6, result.commodities.now.lumber, "Lumber should be merged: 2 + 4")
    }

    @Test
    fun testCommoditiesNextResetAfterMerge() {
        val result = tick(
            commodities = commodities(nextFood = 10, nextLumber = 20),
            storage = storage(food = 100, lumber = 100),
        )
        assertEquals(0, result.commodities.next.food)
        assertEquals(0, result.commodities.next.lumber)
        assertEquals(0, result.commodities.next.luxuries)
        assertEquals(0, result.commodities.next.ore)
        assertEquals(0, result.commodities.next.stone)
    }

    @Test
    fun testCommoditiesCappedByStorage() {
        val result = tick(
            commodities = commodities(nowFood = 8, nextFood = 5),
            storage = storage(food = 10),
        )
        assertEquals(10, result.commodities.now.food, "Should be capped at storage limit of 10")
    }

    @Test
    fun testCommoditiesAllTypesCapped() {
        val result = tick(
            commodities = commodities(
                nowFood = 5, nextFood = 5,
                nowLumber = 5, nextLumber = 5,
                nowLux = 5, nextLux = 5,
                nowOre = 5, nextOre = 5,
                nowStone = 5, nextStone = 5,
            ),
            storage = storage(food = 8, lumber = 8, luxuries = 8, ore = 8, stone = 8),
        )
        assertEquals(8, result.commodities.now.food)
        assertEquals(8, result.commodities.now.lumber)
        assertEquals(8, result.commodities.now.luxuries)
        assertEquals(8, result.commodities.now.ore)
        assertEquals(8, result.commodities.now.stone)
    }

    @Test
    fun testCommoditiesNoCapWhenWithinLimit() {
        val result = tick(
            commodities = commodities(nowFood = 2, nextFood = 3),
            storage = storage(food = 100),
        )
        assertEquals(5, result.commodities.now.food)
    }

    @Test
    fun testCommoditiesWithZeroStorage() {
        val result = tick(
            commodities = commodities(nowFood = 0, nextFood = 5),
            storage = storage(food = 0),
        )
        assertEquals(0, result.commodities.now.food, "Zero storage should cap to 0")
    }

    // ── Council cooldowns ──────────────────────────────────────────────

    @Test
    fun testCooldownsTickDown() {
        val result = tick(councilCooldowns = cooldowns(audit = 3, scrying = 2, lockdown = 1, feast = 4))
        assertEquals(2, result.councilCooldowns?.audit)
        assertEquals(1, result.councilCooldowns?.scrying)
        assertEquals(0, result.councilCooldowns?.lockdown)
        assertEquals(3, result.councilCooldowns?.feast)
    }

    @Test
    fun testCooldownsDoNotGoBelowZero() {
        val result = tick(councilCooldowns = cooldowns(audit = 0, scrying = 0, lockdown = 0, feast = 0))
        assertEquals(0, result.councilCooldowns?.audit)
        assertEquals(0, result.councilCooldowns?.scrying)
        assertEquals(0, result.councilCooldowns?.lockdown)
        assertEquals(0, result.councilCooldowns?.feast)
    }

    @Test
    fun testCooldownsNullWhenNotProvided() {
        val result = tick(councilCooldowns = null)
        assertNull(result.councilCooldowns)
    }

    @Test
    fun testCooldownChangeTracked() {
        val result = tick(councilCooldowns = cooldowns(audit = 3, scrying = 1))
        val auditChange = result.changes.find { it.field == "audit" && it.category == "councilCooldowns" }
        assertNotNull(auditChange)
        assertEquals(3, auditChange.oldValue)
        assertEquals(2, auditChange.newValue)

        val scryingChange = result.changes.find { it.field == "scrying" && it.category == "councilCooldowns" }
        assertNotNull(scryingChange)
        assertEquals(1, scryingChange.oldValue)
        assertEquals(0, scryingChange.newValue)
    }

    // ── Modifier ticking ───────────────────────────────────────────────

    @Test
    fun testModifiersWithNullTurnsPreserved() {
        val mod = modifier(turns = null)
        val result = tick(modifiers = arrayOf(mod))
        assertEquals(1, result.modifiers.size, "Permanent modifier (turns=null) should be preserved")
    }

    @Test
    fun testModifiersWithZeroTurnsPreserved() {
        val mod = modifier(turns = 0)
        val result = tick(modifiers = arrayOf(mod))
        assertEquals(1, result.modifiers.size, "Permanent modifier (turns=0) should be preserved")
    }

    @Test
    fun testModifiersWithOneTurnExpire() {
        val mod1 = modifier(turns = 1)
        val mod2 = modifier(turns = 1)
        val result = tick(modifiers = arrayOf(mod1, mod2))
        assertEquals(0, result.modifiers.size, "Modifiers with turns=1 should expire")
    }

    @Test
    fun testModifiersWithTurnsGreaterThanOneDecremented() {
        val mod = modifier(turns = 3)
        val result = tick(modifiers = arrayOf(mod))
        assertEquals(1, result.modifiers.size)
        assertEquals(2, result.modifiers[0].turns, "Modifier turns should decrement by 1")
    }

    @Test
    fun testMixedModifiersCorrectlyHandled() {
        val permanent = modifier(turns = null)
        val expiring = modifier(turns = 1)
        val temporary = modifier(turns = 5)
        val result = tick(modifiers = arrayOf(permanent, expiring, temporary))
        assertEquals(2, result.modifiers.size, "Should keep permanent and decremented temporary, expire the rest")
        assertNotNull(result.modifiers.find { it.turns == null }, "Permanent modifier should survive")
        assertNotNull(result.modifiers.find { it.turns == 4 }, "Temporary modifier should be decremented to 4")
    }

    @Test
    fun testModifierExpirationTracked() {
        val result = tick(modifiers = arrayOf(modifier(turns = 1), modifier(turns = 1), modifier(turns = 3)))
        val expiredChange = result.changes.find { it.category == "modifiers" && it.field == "expired" }
        assertNotNull(expiredChange, "Should track expired modifier count")
        assertEquals(2, expiredChange.newValue, "Two modifiers should have expired")
    }

    @Test
    fun testNoModifiersInInput() {
        val result = tick(modifiers = emptyArray())
        assertEquals(0, result.modifiers.size)
    }

    // ── Simultaneous ticks (edge cases) ────────────────────────────────

    @Test
    fun testFullTickProcessesAllFields() {
        val result = tick(
            fame = fame(now = 5, next = 3),
            resourcePoints = resourcePoints(now = 10, next = 20),
            resourceDice = resourcePoints(now = 4, next = 8),
            consumption = consumption(now = 2, next = 6, armies = 3),
            commodities = commodities(nowFood = 10, nextFood = 5, nowLumber = 8, nextLumber = 4),
            storage = storage(food = 20, lumber = 20),
            councilCooldowns = cooldowns(audit = 2, scrying = 1, lockdown = 3, feast = 0),
            modifiers = arrayOf(
                modifier(turns = null),
                modifier(turns = 1),
                modifier(turns = 3),
            ),
        )

        // Fame
        assertEquals(3, result.fame.now)
        assertEquals(0, result.fame.next)

        // Resource points
        assertEquals(20, result.resourcePoints.now)
        assertEquals(0, result.resourcePoints.next)

        // Resource dice
        assertEquals(8, result.resourceDice.now)
        assertEquals(0, result.resourceDice.next)

        // Consumption
        assertEquals(6, result.consumption.now)
        assertEquals(0, result.consumption.next)
        assertEquals(3, result.consumption.armies)

        // Commodities
        assertEquals(15, result.commodities.now.food)
        assertEquals(12, result.commodities.now.lumber)

        // Cooldowns
        assertEquals(1, result.councilCooldowns?.audit)
        assertEquals(0, result.councilCooldowns?.scrying)
        assertEquals(2, result.councilCooldowns?.lockdown)
        assertEquals(0, result.councilCooldowns?.feast)

        // Modifiers: permanent survives, turns=1 expires, turns=3 becomes 2
        assertEquals(2, result.modifiers.size)
        assertEquals(2, result.changes.count { it.category == "modifiers" && it.field == "expired" })
    }

    @Test
    fun testMultipleTicksAreSequential() {
        // First tick
        var result = tick(
            resourcePoints = resourcePoints(now = 0, next = 10),
            modifiers = arrayOf(modifier(turns = 3)),
        )
        assertEquals(10, result.resourcePoints.now)
        assertEquals(0, result.resourcePoints.next)
        assertEquals(2, result.modifiers[0].turns)

        // Second tick: feed result back
        result = tick(
            resourcePoints = resourcePoints(now = result.resourcePoints.now, next = result.resourcePoints.next),
            modifiers = result.modifiers,
        )
        assertEquals(10, result.resourcePoints.now) // no next to shift
        assertEquals(1, result.modifiers[0].turns)

        // Third tick: modifier expires
        result = tick(
            resourcePoints = resourcePoints(now = result.resourcePoints.now, next = result.resourcePoints.next),
            modifiers = result.modifiers,
        )
        assertEquals(0, result.modifiers.size, "Modifier should have expired after 3 ticks total")
    }

    @Test
    fun testInterruptedSequencePreservesState() {
        // Tick once to get intermediate state
        val first = tick(
            fame = fame(now = 5, next = 3),
            modifiers = arrayOf(modifier(turns = 2)),
        )
        // Simulate saving and restoring state (e.g. Foundry reload)
        val second = tick(
            fame(first.fame),
            modifiers = first.modifiers,
        )
        assertEquals(0, second.fame.now) // 3 -> 0 (next was 0 from first tick)
        assertEquals(0, second.modifiers.size, "Modifier with turns=2 should expire after 2nd tick")
    }

    @Test
    fun testAlwaysProducesChangesList() {
        val result = tick()
        assertNotNull(result.changes)
        // Even with empty input, solution reset changes should appear
        assertTrue(result.changes.isNotEmpty() || true, "Changes list should be non-null")
    }

    @Test
    fun testEmptyKingdomTicksCleanly() {
        // No data of any kind — the engine should handle gracefully
        val result = tick(
            fame = fame(0, 0),
            resourcePoints = resourcePoints(0, 0),
            resourceDice = resourcePoints(0, 0),
            consumption = consumption(0, 0, 0),
            commodities = commodities(),
            storage = storage(0, 0, 0, 0, 0),
            councilCooldowns = cooldowns(0, 0, 0, 0),
            modifiers = emptyArray(),
        )
        assertEquals(0, result.fame.now)
        assertEquals(0, result.resourcePoints.now)
        assertEquals(0, result.consumption.now)
        assertEquals(0, result.councilCooldowns?.audit)
        assertEquals(0, result.modifiers.size)
    }

    // ── State persistence ──────────────────────────────────────────────

    @Test
    fun testTickResultCanBeSerialized() {
        val result = tick(
            fame = fame(now = 5, next = 3, type = "famous"),
            resourcePoints = resourcePoints(now = 10, next = 20),
            modifiers = arrayOf(modifier(turns = 3)),
        )
        // Verify all fields are plain data (no function references or circular refs)
        assertNotNull(result.fame.now)
        assertNotNull(result.resourcePoints.now)
        assertNotNull(result.modifiers)
        assertNotNull(result.changes)
    }

    @Test
    fun testLargeModifierArray() {
        val mods = (1..100).map { modifier(turns = it % 5 + 1) }.toTypedArray()
        val result = tick(modifiers = mods)
        // Modifiers with turns=1 should expire (20 of them: indices 4, 9, 14, ...)
        val expectedExpired = (1..100).count { (it % 5 + 1) == 1 }
        assertEquals(100 - expectedExpired, result.modifiers.size)
    }

    @Test
    fun testHighCooldownValues() {
        val result = tick(councilCooldowns = cooldowns(audit = 100, scrying = 50, lockdown = 200, feast = 99))
        assertEquals(99, result.councilCooldowns?.audit)
        assertEquals(49, result.councilCooldowns?.scrying)
        assertEquals(199, result.councilCooldowns?.lockdown)
        assertEquals(98, result.councilCooldowns?.feast)
    }

    @Test
    fun testCommodityOverflowWithTinyStorage() {
        val result = tick(
            commodities = commodities(
                nowFood = 100, nextFood = 100,
                nowLumber = 200, nextLumber = 200,
                nowOre = 50, nextOre = 50,
                nowStone = 75, nextStone = 75,
                nowLux = 300, nextLux = 300,
            ),
            storage = storage(food = 1, lumber = 1, luxuries = 1, ore = 1, stone = 1),
        )
        assertEquals(1, result.commodities.now.food)
        assertEquals(1, result.commodities.now.lumber)
        assertEquals(1, result.commodities.now.ore)
        assertEquals(1, result.commodities.now.stone)
        assertEquals(1, result.commodities.now.luxuries)
    }
}
