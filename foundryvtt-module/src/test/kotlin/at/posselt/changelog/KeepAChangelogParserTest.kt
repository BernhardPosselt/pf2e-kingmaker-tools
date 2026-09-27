package at.posselt.changelog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class KeepAChangelogParserTest {
    val file = File(this.javaClass.classLoader.getResource("CHANGELOG.md")!!.file)

    @Test
    fun `test parse file`() {
        val parser = KeepAChangelogParser()
        val result = file.inputStream().use(parser::parse).toList()
        assertEquals(3, result.size)
        assertEquals("Unreleased", result[0].version)
        assertEquals("", result[0].notes)
        assertEquals("6.3.1", result[1].version)
        assertEquals("""
            ### Fixed

            * Do not break camping sheet if camping actor is removed after selecting a meal; this only affects v14
            * Do not show Legendary skill rank requirement for kingdom activities if no skill checks are required, such as in Disband Army
            
            ### Added

            * Something
        """.trimIndent(), result[1].notes)
        assertEquals("6.3.0", result[2].version)
        assertEquals("""
            ### Added

            * Settlement layout can now be managed automatically as described in the rules. Existing scenes are migrated to free form layouts since automated layouts require blocks to be situated at specific spots on the map
        """.trimIndent(), result[2].notes)
    }
}