plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kotlin.serialization)
}

group = "at.posselt"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.negotiation)
    implementation(libs.ktor.client.json)
}

gradlePlugin {
    website = "https://github.com/BernhardPosselt/pf2e-kingmaker-tools/tree/master/foundryvtt-module-plugin"
    vcsUrl = "https://github.com/BernhardPosselt/pf2e-kingmaker-tools/tree/master/foundryvtt-module-plugin"
    plugins {
        create("foundryvttModule") {
            id = "at.posselt.foundryvtt-module"
            implementationClass = "at.posselt.FoundryVTTModulePlugin"
            displayName = "FoundryVTT Module Plugin"
            description = "Provides tasks to publish artifacts to github and the FoundryVTT module API"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/BernhardPosselt/pf2e-kingmaker-tools")
            credentials {
                username = "bernhardposselt"
                password = System.getenv("GITHUB_PACKAGES_TOKEN")
            }
        }
    }
}