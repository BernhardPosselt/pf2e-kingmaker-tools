package at.posselt.pfrpg2e

object Config {
    const val moduleId = "pf2e-kingmaker-tools"
    const val moduleName = "Kingdom Building, Camping & Weather"

    object rollTables {
        const val compendium = "$moduleId.kingmaker-tools-rolltables"
        const val weather = "Weather Events"
    }

    object items {
        const val provisionsUuid = "Compendium.${moduleId}.kingmaker-tools-camping-effects.Item.UsafuPUY2soIZhC3"
        const val specialIngredientUuid = "Compendium.pf2e.equipment-srd.Item.OCTireuX60MaPcEi"
        const val basicIngredientUuid = "Compendium.pf2e.equipment-srd.Item.kKnMlymiqZLVEAtI"
        const val rationUuid = "Compendium.pf2e.equipment-srd.Item.L9ZV076913otGtiB"
    }

    object regions {
        const val defaultRegion = "Zone 00"
    }

    object kingmakerModule {
        object weather {
            const val playlistId = "c6WJzHWMM72zP19H"
        }
    }
}



