rootProject.name = "pfrpg2eKingdomCampingWeather"

includeBuild("foundryvtt-pf2e")
includeBuild("foundryvtt-module-plugin")

//pluginManagement {
//    repositories {
//        gradlePluginPortal()
//        mavenLocal()
//    }
//}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}
