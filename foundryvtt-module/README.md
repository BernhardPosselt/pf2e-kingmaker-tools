# FoundryVTT Module Gradle Plugin

Keep in mind that this Gradle plugin is licensed under the AGPLv3

Plugin that allows you to easily package and distribute FoundryVTT modules using GitHub. The plugin makes the following assumptions:

* Your release.zip and module.json are uploaded to the releases page
* Release notes for your module will link to the release page
* If a CHANGELOG.md file is present in the module directory, it will add it to the releases page and link release notes to that location

## Usage

Set up the plugin in your plugins block and configure some base variables:

```kt
plugins {
    id("at.posselt.foundryvtt-module")
}

foundryvttModule {
    releaseModuleJson = layout.buildDirectory.file("module.json") // default if absent
    githubUser = "BernhardPosselt"
    githubRepo = "pf2e-kingmaker-tools"
    // read tokens from env variables, but can also be done differently if preferred
    foundryToken = providers.environmentVariable("FOUNDRY_TOKEN")
    githubToken = providers.environmentVariable("GITHUB_TOKEN")
}

// extend base zip task that already includes and module file
tasks.named<Zip>("foundryvttModulePackage") {
    // this variable is populated with the id parsed from the module.json file
    val moduleId: String by extra
    from("dist") { into("$moduleId/dist") }
    from("docs") { into("$moduleId/docs") }
    from("img") { into("$moduleId/img") }
    from("packs") { into("$moduleId/packs") }
    from("styles") { into("$moduleId/styles") }
    from("templates") { into("$moduleId/templates") }
    from("LICENSE") { into("$moduleId/") }
    from("README.md") { into("$moduleId/") }
    from("CHANGELOG.md") { into("$moduleId/") }
}
```

If you add a CHANGELOG.md file into the zip's module directory, it will be parsed in the [keep a changelog format](https://keepachangelog.com/en/1.1.0/) and added to the github release page

## Tasks

This adds the following Gradle Tasks to your project:

* **foundryvttModuleUpdateManifest**: modifies your module.json file with the new version and download links 
* **foundryvttModulePackage**: creates a **build/foundryvttModule/release.zip** file; includes your module.json file by default, but you'll likely want to add additional files by extending the task as noted above
* **foundryvttRelease**: Task that executes both **foundryvttModuleRelease** and **foundryvttModuleUploadGithubRelease**
* **foundryvttModuleUploadGithubRelease**:
  * Deletes version tag from local and remote if it exists
  * Tries to commit and push build.gradle.kts and module.json
  * Pushes the version as git tag
  * Uploads the package in build/foundryvttModule/release.zip to GitHub and publishes a new release on foundryvtt.com
* **foundryvttModuleCreateRelease**: Creates a new release over foundryvtt.com's REST API