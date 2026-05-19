package at.posselt.utils

import at.posselt.changelog.ChangelogVersion
import at.posselt.foundryvtt.model.FoundryCompatibility
import at.posselt.foundryvtt.model.Manifest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ReleaseZipParserTest {
    val file = File(this.javaClass.classLoader.getResource("release.zip")!!.file)

    @Test
    fun `test parse zip file`() {
        val parser = ReleaseZipParser()
        val result = parser.parseZip(file)

        val expected = ManifestAndChangelog(
            Manifest(
                id = "pf2e-kingmaker-tools",
                compatibility = FoundryCompatibility(
                    minimum = "14",
                    verified = "14",
                    maximum = "14",
                ),
                version = "6.3.1",
                manifest = "https://github.com/BernhardPosselt/pf2e-kingmaker-tools/releases/latest/download/module.json"
            ),
            listOf(
                ChangelogVersion(version="Unreleased", notes=""),
                ChangelogVersion(
                    "6.3.1",
                    notes = """
                    ### Fixed

                    * Do not break camping sheet if camping actor is removed after selecting a meal; this only affects v14
                    * Do not show Legendary skill rank requirement for kingdom activities if no skill checks are required, such as in Disband Army
                """.trimIndent()
                ),
            ),
            manifestText = """
                {
                    "title": "Kingdom Building, Camping & Weather",
                    "description": "A collection of utilities to run the most popular adventure for PFRPG 2e",
                    "version": "6.3.1",
                    "manifest": "https://github.com/BernhardPosselt/pf2e-kingmaker-tools/releases/latest/download/module.json",
                    "download": "https://github.com/BernhardPosselt/pf2e-kingmaker-tools/releases/download/6.3.1/release.zip",
                    "id": "pf2e-kingmaker-tools",
                    "compatibility": {
                        "minimum": "14",
                        "maximum": "14",
                        "verified": "14"
                    }
                }
            """.trimIndent()
        )
        assertEquals(expected, result)
    }
}