import at.posselt.pfrpg2e.plugins.CombineJsonFiles
import at.posselt.pfrpg2e.plugins.CopyAndSanitizeTranslations
import at.posselt.pfrpg2e.plugins.CreateDummyTranslations
import at.posselt.pfrpg2e.plugins.JsonSchemaValidator
import at.posselt.pfrpg2e.plugins.UnpackJsonFiles
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    alias(libs.plugins.foundryvtt.module)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.plain.objects)
    alias(libs.plugins.versions)
    alias(libs.plugins.download)
}

group = "at.posselt"
version = "6.3.1"

repositories {
    mavenCentral()
}

// tasks that generate code or resources need to be registered
// before referencing them in source sets below
tasks.register<CombineJsonFiles>("combineJsonFiles") {
    sourceDirectory = layout.projectDirectory.dir("data/")
    targetDirectory = layout.buildDirectory.dir("generated/data/")
}

tasks.register<CopyAndSanitizeTranslations>("processTranslations") {
    from = layout.projectDirectory.dir("lang/")
    into = layout.buildDirectory.dir("generated/lang/")
    targetFolderName = "lang"
}

kotlin {
    jvmToolchain(25)
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
                "-opt-in=kotlin.js.ExperimentalWasmJsInterop",
                "-opt-in=kotlin.js.ExperimentalJsStatic",
                "-opt-in=kotlin.time.ExperimentalTime",
                "-Xreturn-value-checker=full"
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
                implementation(libs.kotlin.wrappers.web)
                implementation(libs.kotlin.plain.objects)
                implementation(libs.kotlinx.html)
                implementation(libs.kotlinx.coroutines.js)
                implementation(libs.jsonschemavalidator.js)
                implementation(npm("uuid", "11.1.0"))
                implementation(npm("i18next", "24.2.3"))
                implementation(npm("i18next-icu", "2.3.0"))
                implementation(npm("intl-messageformat", "10.7.16"))
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
foundryvttModule {
    githubUser = "BernhardPosselt"
    githubRepo = "pf2e-kingmaker-tools"
    foundryToken = providers.environmentVariable("FOUNDRY_TOKEN")
    githubToken = providers.environmentVariable("GITHUB_TOKEN")
}

tasks.named<Zip>("foundryvttModulePackage") {
    val moduleId: String by extra
    dependsOn("clean", "build", "txPull")
    tasks.named("txPull").get().mustRunAfter("clean")
    tasks.named("build").get().mustRunAfter("txPull")
    from("dist") { into("$moduleId/dist") }
    from("docs") { into("$moduleId/docs") }
    from("img") { into("$moduleId/img") }
    from("packs") { into("$moduleId/packs") }
    from("styles") { into("$moduleId/styles") }
    from("templates") { into("$moduleId/templates") }
    from("LICENSE") { into("$moduleId/") }
    from("OpenGameLicense.md") { into("$moduleId/") }
    from("README.md") { into("$moduleId/") }
    from("token-map.json") { into("$moduleId/") }
    from("CHANGELOG.md") { into("$moduleId/") }
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
    if (current.isLinux) {
        src("https://github.com/transifex/cli/releases/download/v1.6.17/tx-linux-amd64.tar.gz")
    } else if (current.isMacOsX) {
        src("https://github.com/transifex/cli/releases/download/v1.6.17/tx-darwin-arm64.tar.gz")
    } else {
        src("https://github.com/transifex/cli/releases/download/v1.6.17/tx-windows-amd64.zip")
    }
    if (current.isLinux || current.isMacOsX) {
        dest(layout.buildDirectory.file("tx.tar.gz"))
    } else {
        dest(layout.buildDirectory.file("tx.zip"))
    }
}

tasks.register<Copy>("extractTxClient") {
    dependsOn("downloadTxClient")
    val current = OperatingSystem.current()
    if(current.isLinux || current.isMacOsX) {
        from(tarTree(resources.gzip(layout.buildDirectory.file("tx.tar.gz"))))
    } else {
        from(zipTree(layout.buildDirectory.file("tx.zip")))
    }
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

tasks.register<CreateDummyTranslations>("createDummyTranslations") {
    moduleJson = layout.projectDirectory.file("module.json")
    enTranslation = layout.projectDirectory.file("lang/en.json")
    langDirectory = layout.projectDirectory.dir("lang/")
}

// do not show beta and milestone versions as upgrades
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        val version = candidate.version
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        isStable.not()
    }
}
