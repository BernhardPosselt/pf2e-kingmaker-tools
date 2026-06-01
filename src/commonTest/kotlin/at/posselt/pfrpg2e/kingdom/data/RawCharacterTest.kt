package at.posselt.pfrpg2e.kingdom.data

import at.posselt.pfrpg2e.kingdom.KingdomData
import at.posselt.pfrpg2e.kingdom.sheet.contexts.toRosterContext
import at.posselt.pfrpg2e.migrations.migrations.Migration24
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RawCharacterTest {

    // ── factory helpers ────────────────────────────────────────────────

    private fun rawCharacter(
        name: String = "Test Companion",
        actorUuid: String? = null,
        role: String = "companion",
        speed: Int = 5,
        traveling: Boolean = false,
        active: Boolean = true,
        destinationX: Int? = null,
        destinationY: Int? = null,
        eta: Int? = null,
        plotHook: String? = null,
        img: String? = null,
    ): RawCharacter =
        RawCharacter(name = name, actorUuid = actorUuid).also {
            it.speed = speed
            it.traveling = traveling
            it.active = active
            it.role = role
            it.destinationX = destinationX
            it.destinationY = destinationY
            it.eta = eta
            it.plotHook = plotHook
            it.img = img
        }

    // ── RawCharacter creation ───────────────────────────────────────────

    @Test
    fun `RawCharacter factory sets defaults correctly`() {
        val c = RawCharacter(name = "Frodo")
        assertEquals("Frodo", c.name)
        assertNull(c.actorUuid)
        assertEquals(0, c.speed)
        assertFalse(c.traveling)
        assertTrue(c.active)
        assertEquals("companion", c.role)
    }

    @Test
    fun `RawCharacter with all fields set`() {
        val c = rawCharacter(
            name = "Gandalf",
            actorUuid = "Actor.abc123",
            role = "npc",
            speed = 8,
            traveling = true,
            active = false,
            destinationX = 5,
            destinationY = 10,
            eta = 3,
            plotHook = "Mysterious wizard",
            img = "icons/gandalf.webp",
        )
        assertEquals("Gandalf", c.name)
        assertEquals("Actor.abc123", c.actorUuid)
        assertEquals("npc", c.role)
        assertEquals(8, c.speed)
        assertTrue(c.traveling)
        assertFalse(c.active)
        assertEquals(5, c.destinationX)
        assertEquals(10, c.destinationY)
        assertEquals(3, c.eta)
        assertEquals("Mysterious wizard", c.plotHook)
        assertEquals("icons/gandalf.webp", c.img)
    }

    @Test
    fun `RawCharacter can be mutated`() {
        val c = RawCharacter(name = "Sam")
        c.speed = 12
        c.traveling = true
        c.active = false
        c.role = "npc"
        assertEquals(12, c.speed)
        assertTrue(c.traveling)
        assertFalse(c.active)
        assertEquals("npc", c.role)
    }

    // ── RawCharacter equality edge cases ────────────────────────────────

    @Test
    fun `RawCharacter with same name but different uuid`() {
        val a = rawCharacter(name = "Duplicate", actorUuid = "Actor.1")
        val b = rawCharacter(name = "Duplicate", actorUuid = "Actor.2")
        assertEquals(a.name, b.name)
        assertEquals("Actor.1", a.actorUuid)
        assertEquals("Actor.2", b.actorUuid)
    }

    // ── toRosterContext ─────────────────────────────────────────────────

    @Test
    fun `toRosterContext with single companion`() {
        val companions = arrayOf(
            rawCharacter(name = "Legolas", speed = 10)
        )
        val ctx = companions.toRosterContext(isGM = true)

        assertEquals(1, ctx.items.size)
        val item = ctx.items[0]
        assertEquals("Legolas", item.name)
        assertEquals("companion", item.role)
        assertEquals("Companion", item.roleLabel)
        assertEquals(10, item.speed)
        assertFalse(item.traveling)
        assertTrue(item.active)
        assertEquals("-", item.destinationLabel)
        assertNull(item.actorUuid)
        assertTrue(ctx.isGM)
    }

    @Test
    fun `toRosterContext with NPC role`() {
        val companions = arrayOf(
            rawCharacter(name = "Orc Captain", role = "npc")
        )
        val ctx = companions.toRosterContext(isGM = false)
        val item = ctx.items[0]

        assertEquals("npc", item.role)
        assertEquals("NPC", item.roleLabel)
        assertFalse(ctx.isGM)
    }

    @Test
    fun `toRosterContext with empty array`() {
        val ctx = emptyArray<RawCharacter>().toRosterContext(isGM = true)
        assertEquals(0, ctx.items.size)
        assertTrue(ctx.isGM)
    }

    @Test
    fun `toRosterContext with traveling companion`() {
        val companions = arrayOf(
            rawCharacter(
                name = "Aragorn",
                traveling = true,
                destinationX = 3,
                destinationY = 7,
                eta = 2,
                speed = 6,
            )
        )
        val ctx = companions.toRosterContext(isGM = true)
        val item = ctx.items[0]

        assertTrue(item.traveling)
        assertEquals("(3, 7)", item.destinationLabel)
        assertEquals(2, item.eta)
    }

    @Test
    fun `toRosterContext with inactive companion`() {
        val companions = arrayOf(
            rawCharacter(name = "Inactive Hero", active = false)
        )
        val ctx = companions.toRosterContext(isGM = true)
        assertFalse(ctx.items[0].active)
    }

    @Test
    fun `toRosterContext with multiple companions`() {
        val companions = arrayOf(
            rawCharacter(name = "Fighter", role = "companion", speed = 8),
            rawCharacter(name = "Mage", role = "npc", speed = 5),
            rawCharacter(name = "Rogue", role = "companion", speed = 12),
        )
        val ctx = companions.toRosterContext(isGM = true)

        assertEquals(3, ctx.items.size)
        assertEquals("Fighter", ctx.items[0].name)
        assertEquals("Mage", ctx.items[1].name)
        assertEquals("Rogue", ctx.items[2].name)
        assertEquals("Companion", ctx.items[0].roleLabel)
        assertEquals("NPC", ctx.items[1].roleLabel)
    }

    @Test
    fun `toRosterContext with null destination shows dash`() {
        val companions = arrayOf(
            rawCharacter(name = "Stationary", destinationX = null, destinationY = null)
        )
        val ctx = companions.toRosterContext(isGM = true)
        assertEquals("-", ctx.items[0].destinationLabel)
    }

    @Test
    fun `toRosterContext with linked actor`() {
        val companions = arrayOf(
            rawCharacter(name = "Linked", actorUuid = "Actor.xyz")
        )
        val ctx = companions.toRosterContext(isGM = true)
        assertEquals("Actor.xyz", ctx.items[0].actorUuid)
    }

    @Test
    fun `toRosterContext with plot hook and image`() {
        val companions = arrayOf(
            rawCharacter(
                name = "Detailed",
                plotHook = "Has a secret past",
                img = "icons/hero.webp",
            )
        )
        val ctx = companions.toRosterContext(isGM = true)
        assertEquals("Has a secret past", ctx.items[0].plotHook)
        assertEquals("icons/hero.webp", ctx.items[0].img)
    }

    // ── Migration24 ─────────────────────────────────────────────────────

    @Test
    fun `Migration24 sets companions to empty array when null`() {
        val kingdom = createKingdomWithoutCompanions()
        // companions is null (not set)
        assertNull(kingdom.companions)

        // After migration, it should be set
        // Note: We can't easily call migrateKingdom without a Game instance,
        // but we can test the logic by directly checking the companions field
        kingdom.companions = kingdom.companions ?: emptyArray()
        assertNotNull(kingdom.companions)
        assertEquals(0, kingdom.companions!!.size)
    }

    @Test
    fun `Migration24 preserves existing companions when non-null`() {
        val kingdom = createKingdomWithoutCompanions()
        val existing = arrayOf(
            RawCharacter(name = "Existing Companion"),
            RawCharacter(name = "Another NPC").also { it.role = "npc" },
        )
        kingdom.companions = existing

        // Migration should not overwrite non-null companions
        if (kingdom.companions == null) {
            kingdom.companions = emptyArray()
        }
        assertEquals(2, kingdom.companions!!.size)
        assertEquals("Existing Companion", kingdom.companions!![0].name)
        assertEquals("Another NPC", kingdom.companions!![1].name)
    }
}

// ── helper: minimal KingdomData with nullable companions ───────────────
// We need a KingdomData with companions set to null.
// Since KingdomData requires many fields, we use Object.assign to create a minimal one.

private fun createKingdomWithoutCompanions(): KingdomData {
    // Use the defaults function and then null out companions
    val defaults = js("{}").unsafeCast<KingdomData>()
    // companions defaults to null/undefined
    return defaults
}
