import at.posselt.pfrpg2e.plugins.ChangeModuleVersion
import at.posselt.pfrpg2e.plugins.JsonSchemaValidator
import at.posselt.pfrpg2e.plugins.PackJsonFile
import at.posselt.pfrpg2e.plugins.ReleaseModule
import at.posselt.pfrpg2e.plugins.UnpackJsonFile
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.plain.objects)
    alias(libs.plugins.versions)
}

group = "at.posselt"
version = "2.0.1"

repositories {
    mavenCentral()
}

kotlin {
    js {
        useEsModules()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            moduleKind = JsModuleKind.MODULE_ES
            useEsClasses = true
        }
        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory = file("dist")
            }
            webpackTask {
                mainOutputFileName = "main.js"
            }
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        binaries.executable() // create a js file
    }
    sourceSets {
        // enable kotlin test for all modules
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        // define a jsMain module
        val jsMain by getting {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(libs.kotlin.wrappers))
                implementation(libs.kotlin.wrappers.js)
                implementation(libs.kotlin.plain.objects)
                implementation(libs.kotlinx.html)
                implementation(libs.kotlinx.coroutines.js)
                implementation(libs.jsonschemavalidator.js)
                api(libs.jquery)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test.js)
            }
        }
    }
}

tasks {
    getByName<Delete>("clean") {
        delete.add("dist")
    }
    getByName("jsProcessResources") {
        dependsOn("packJsonFiles")
    }
    getByName("assemble") {
        finalizedBy("copyOldJs")
    }
    getByName("check") {
        finalizedBy("validateJsonFiles")
    }
}

/**
 * At build time, validate all json files in data/
 */
tasks.register<JsonSchemaValidator>("validateJsonFiles") {
    addSchema(
        layout.projectDirectory.file("src/commonMain/resources/schemas/recipe.json"),
        layout.projectDirectory.dir("data/recipes"),
    )
    addSchema(
        layout.projectDirectory.file("src/commonMain/resources/schemas/structure.json"),
        layout.projectDirectory.dir("data/structures"),
    )
    addSchema(
        layout.projectDirectory.file("src/commonMain/resources/schemas/camping-activity.json"),
        layout.projectDirectory.dir("data/camping-activities"),
    )
}

/**
 * At build time, copy the old js file back into the dist folder
 */
tasks.register<Copy>("copyOldJs") {
    from("oldsrc/dist/main.js") {
        rename(".*", "oldmain.js")
    }
    into("dist/")
}

/**
 * Updates the version attribute in module.json when packaging the zip
 */
tasks.register<ChangeModuleVersion>("changeModuleVersion") {
    moduleVersion = project.version.toString()
}

/**
 * Split a json array in src/commonMain/resources/data/ into
 * multiple json files
 *
 * Run using ./gradlew unpackJsonFile -Pfile=recipes.json
 */
tasks.register<UnpackJsonFile>("unpackJsonFile") {
    val file = project.property("file")
    targetFile = layout.projectDirectory.file("src/commonMain/resources/data/$file")
    outputDirectory = layout.projectDirectory.dir("data")
}

/**
 * Concatenates all files in src/commonMain/resources/data/DIRECTORY into
 * one big json file in dist/
 */
tasks.register<PackJsonFile>("packJsonFiles") {
    sourceDirectory = layout.projectDirectory.dir("data/")
    targetDirectory = layout.projectDirectory.dir("src/commonMain/resources/data/")
}

tasks.register<Exec>("installOldJs") {
    workingDir = layout.projectDirectory.dir("oldsrc/").asFile
    commandLine = listOf("yarn", "install")
}

tasks.register<Exec>("compileOldJs") {
    workingDir = layout.projectDirectory.dir("oldsrc/").asFile
    commandLine = listOf("yarn", "run", "build")
}

/**
 * Run using ./gradlew package
 */
tasks.register<Zip>("package") {
    dependsOn("clean", "build", "installOldJs", "compileOldJs", "copyOldJs", "packJsonFiles", "changeModuleVersion")
    tasks.named("compileOldJs").get().mustRunAfter("installOldJs")
    tasks.named("build").get().mustRunAfter("clean", "compileOldJs")
    archiveFileName.set("release.zip")
    destinationDirectory.set(layout.buildDirectory)
    from("dist") { into("pf2e-kingmaker-tools/dist") }
    from("docs") { into("pf2e-kingmaker-tools/docs") }
    from("img") { into("pf2e-kingmaker-tools/img") }
    from("packs") { into("pf2e-kingmaker-tools/packs") }
    from("styles") { into("pf2e-kingmaker-tools/styles") }
    from("templates") { into("pf2e-kingmaker-tools/templates") }
    from("CHANGELOG.md") { into("pf2e-kingmaker-tools/") }
    from("LICENSE") { into("pf2e-kingmaker-tools/") }
    from("OpenGameLicense.md") { into("pf2e-kingmaker-tools/") }
    from("README.md") { into("pf2e-kingmaker-tools/") }
    from("token-map.json") { into("pf2e-kingmaker-tools/") }
    from("module.json") { into("pf2e-kingmaker-tools/") }
}

tasks.register<ReleaseModule>("release") {
    dependsOn("package")
    mustRunAfter("package")
    releaseZip = layout.buildDirectory.file("release.zip")
    version = project.version.toString()
    foundryVersion = "12"
    githubRepo = "BernhardPosselt/foundryvtt-kotlin"
    moduleId = "pf2e-kingmaker-tools"
}
