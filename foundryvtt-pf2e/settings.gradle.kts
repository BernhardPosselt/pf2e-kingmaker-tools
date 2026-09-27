rootProject.name = "foundryvtt-pf2e"

includeBuild("../foundryvtt")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}
