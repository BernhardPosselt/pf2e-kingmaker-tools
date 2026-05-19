rootProject.name = "pfrpg2eKingdomCampingWeather"

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
