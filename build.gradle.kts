import at.posselt.pfrpg2e.plugins.ChangeModuleVersion
import at.posselt.pfrpg2e.plugins.JsonSchemaValidator
import at.posselt.pfrpg2e.plugins.CombineJsonFiles
import at.posselt.pfrpg2e.plugins.ReleaseModule
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
version = "2.1.0"

repositories {
    mavenCentral()
}

// tasks that generate code or resources need to be registered
// before referencing them in source sets below
tasks.register<CombineJsonFiles>("combineJsonFiles") {
    sourceDirectory = layout.projectDirectory.dir("data/")
    targetDirectory = layout.buildDirectory.dir("generated/resources")
}
tasks.register<Exec>("installOldJs") {
    inputs.files(layout.projectDirectory.file("./oldsrc/package.json"))
    outputs.dir(layout.projectDirectory.dir("./oldsrc/node_modules"))
    workingDir = layout.projectDirectory.dir("oldsrc/").asFile
    commandLine = listOf("yarn", "install")
}
tasks.register<Exec>("compileOldJs") {
    dependsOn("installOldJs")
    mustRunAfter("installOldJs")
    inputs.file(layout.projectDirectory.dir("./oldsrc/webpack.config.ts"))
    inputs.dir(layout.projectDirectory.dir("./oldsrc/src/"))
    outputs.dir(layout.projectDirectory.dir("./oldsrc/dist/"))
    workingDir = layout.projectDirectory.dir("oldsrc/").asFile
    commandLine = listOf("yarn", "run", "build")
}

kotlin {
    js {
        useEsModules()
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-opt-in=kotlin.js.ExperimentalJsExport",
                "-opt-in=kotlin.js.ExperimentalJsStatic",
            )
            moduleKind = JsModuleKind.MODULE_ES
            useEsClasses = true
        }
        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory = file( "dist")
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
        val commonMain by getting {
            resources.srcDirs(
                tasks.named("combineJsonFiles"),
            )
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        // define a jsMain module
        val jsMain by getting {
            resources.srcDirs(tasks.named("compileOldJs"))
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
        delete.add(layout.projectDirectory.dir("dist"))
        delete.add(layout.projectDirectory.dir("oldsrc/dist"))
    }
    getByName("check") {
        dependsOn("validateRecipes", "validateStructures", "validateCampingActivities")
    }
}

// JSON Schema validation tasks
tasks.register<JsonSchemaValidator>("validateRecipes") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/recipe.json")
    files = layout.projectDirectory.dir("data/recipes")
}

tasks.register<JsonSchemaValidator>("validateStructures") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/structure.json")
    files = layout.projectDirectory.dir("data/structures")
}

tasks.register<JsonSchemaValidator>("validateCampingActivities") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/camping-activity.json")
    files = layout.projectDirectory.dir("data/camping-activities")
}

// release tasks
tasks.register<ChangeModuleVersion>("changeModuleVersion") {
    inputs.property("version", project.version)
    moduleVersion = project.version.toString()
    sourceFile = layout.projectDirectory.file("module.json")
    targetFile = layout.buildDirectory.file("module.json")
}

/**
 * Run using ./gradlew package
 */
tasks.register<Zip>("package") {
    dependsOn("clean", "build", "changeModuleVersion")
    tasks.named("build").get().mustRunAfter("clean")
    archiveFileName = "release.zip"
    destinationDirectory = layout.buildDirectory
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
    from("build/module.json") { into("pf2e-kingmaker-tools/") }
}

tasks.register<ReleaseModule>("release") {
    dependsOn("package")
    releaseZip = layout.buildDirectory.file("release.zip")
    releaseModuleJson = layout.buildDirectory.file("module.json")
    githubRepo = "BernhardPosselt/pf2e-kingmaker-tools"
}
