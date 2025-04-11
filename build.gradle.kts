import at.posselt.pfrpg2e.plugins.ChangeModuleVersion
import at.posselt.pfrpg2e.plugins.CombineJsonFiles
import at.posselt.pfrpg2e.plugins.JsonSchemaValidator
import at.posselt.pfrpg2e.plugins.ReleaseModule
import at.posselt.pfrpg2e.plugins.UnpackJsonFiles
import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.plain.objects)
    alias(libs.plugins.versions)
    alias(libs.plugins.download)
}

group = "at.posselt"
version = "4.8.0"

repositories {
    mavenCentral()
}

// tasks that generate code or resources need to be registered
// before referencing them in source sets below
tasks.register<CombineJsonFiles>("combineJsonFiles") {
    sourceDirectory = layout.projectDirectory.dir("data/")
    targetDirectory = layout.buildDirectory.dir("generated/resources")
}

// TODO: this task does not only need to copy strings into the resources folder but also
// needs to sanitize HTML
tasks.register<Copy>("processTranslations") {
    from(layout.projectDirectory.dir("lang/"))
    into(layout.buildDirectory.dir("generated/resources/lang"))
}

kotlin {
    js {
        useEsModules()
        compilerOptions {
            target = "es2015"
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.io.encoding.ExperimentalEncodingApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-opt-in=kotlin.js.ExperimentalJsExport",
                "-opt-in=kotlin.js.ExperimentalJsStatic",
                "-Xwhen-guards",
            )
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
        val commonMain by getting {
            resources.srcDirs(
                tasks.named("processTranslations"),
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
            dependencies {
                implementation(project.dependencies.enforcedPlatform(libs.kotlin.wrappers))
                implementation(libs.kotlin.wrappers.js)
                implementation(libs.kotlin.plain.objects)
                implementation(libs.kotlinx.html)
                implementation(libs.kotlinx.coroutines.js)
                implementation(libs.jsonschemavalidator.js)
                implementation(npm("uuid", "11.1.0"))
                implementation(npm("i18next", "24.2.3"))
                implementation(npm("i18next-http-backend", "3.0.2"))
                implementation(npm("i18next-icu", "2.3.0"))
                implementation(npm("intl-messageformat", "10.7.16"))
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
    }
    getByName("check") {
        dependsOn(
            "validateRecipes",
            "validateStructures",
            "validateCampingActivities",
            "validateFeats",
            "validateFeatures",
            "validateKingdomActivities",
            "validateCharters",
            "validateGovernments",
            "validateHeartlands",
            "validateMilestones",
            "validateKingdomEvents",
        )
    }
}

// JSON Schema validation tasks
tasks.register<JsonSchemaValidator>("validateKingdomEvents") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/event.json")
    files = layout.projectDirectory.dir("data/events")
}

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

tasks.register<JsonSchemaValidator>("validateFeats") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/feat.json")
    files = layout.projectDirectory.dir("data/feats")
}

tasks.register<JsonSchemaValidator>("validateFeatures") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/feature.json")
    files = layout.projectDirectory.dir("data/features")
}

tasks.register<JsonSchemaValidator>("validateKingdomActivities") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/kingdom-activity.json")
    files = layout.projectDirectory.dir("data/kingdom-activities")
}

tasks.register<JsonSchemaValidator>("validateCharters") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/charter.json")
    files = layout.projectDirectory.dir("data/charters")
}

tasks.register<JsonSchemaValidator>("validateGovernments") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/government.json")
    files = layout.projectDirectory.dir("data/governments")
}

tasks.register<JsonSchemaValidator>("validateHeartlands") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/heartland.json")
    files = layout.projectDirectory.dir("data/heartlands")
}

tasks.register<JsonSchemaValidator>("validateMilestones") {
    outputs.upToDateWhen { true } // no outputs, only depend on input files
    schema = layout.projectDirectory.file("src/commonMain/resources/schemas/milestone.json")
    files = layout.projectDirectory.dir("data/milestones")
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
    changelogFile = layout.projectDirectory.file("CHANGELOG.md")
    githubRepo = "BernhardPosselt/pf2e-kingmaker-tools"
}

tasks.register<UnpackJsonFiles>("unpackJson") {
    fileNameProperty = "name"
    file = layout.projectDirectory.file("data/milestones/milestones.json")
    targetDirectory = layout.projectDirectory.dir("data/milestones")
}

// transifex setup
tasks.register<Download>("downloadTxClient") {
    val current = OperatingSystem.current()
    // note that we assume the most popular architecture here
    val type = if (current.isLinux) {
        "linux-amd64"
    } else if (current.isMacOsX) {
        "darwin-arm64"
    } else {
        "windows-amd64"
    }
    src("https://github.com/transifex/cli/releases/download/v1.6.17/tx-$type.tar.gz")
    dest(layout.buildDirectory.file("tx.tar.gz"))
}

tasks.register<Copy>("extractTxClient") {
    dependsOn("downloadTxClient")
    from(tarTree(resources.gzip(layout.buildDirectory.file("tx.tar.gz"))))
    into(layout.buildDirectory.dir("transifex"))
}

tasks.register<Exec>("txPush") {
    dependsOn("extractTxClient")
    workingDir(projectDir)
    executable("build/transifex/tx")
    args(listOf("push"))
}

tasks.register<Exec>("txPull") {
    dependsOn("extractTxClient")
    workingDir(projectDir)
    executable("build/transifex/tx")
    args(listOf("pull", "-a"))
}