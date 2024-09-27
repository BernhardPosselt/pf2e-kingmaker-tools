rootProject.name = "pfrpg2eKingdomCampingWeather"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}
