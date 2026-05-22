package at.posselt

import at.posselt.tasks.FoundryVTTModuleCreateRelease
import at.posselt.tasks.FoundryVTTModuleUpdateManifest
import at.posselt.tasks.FoundryVTTModuleUploadGithubRelease
import at.posselt.utils.createRepo
import at.posselt.utils.currentTag
import at.posselt.utils.parseManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

abstract class FoundryVTTModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("foundryvttModule", FoundryVTTModuleExtension::class.java)
        val module = extension.moduleFile

        val moduleVersion = project.providers.provider { project.version.toString() }

        project.tasks.register<FoundryVTTModuleUpdateManifest>("foundryvttModuleUpdateManifest") {
            moduleFile.convention(module)
            targetModuleFile.convention(project.layout.buildDirectory.file("foundryvttModule/module.json"))
            githubRepo.convention(extension.githubRepo)
            githubUser.convention(extension.githubUser)
            version.convention(moduleVersion)
        }

        project.tasks.register<Zip>("foundryvttModulePackage") {
            val moduleId by extra { module.asFile.get().inputStream().use { parseManifest(it) }.id }
            dependsOn("foundryvttModuleUpdateManifest")
            archiveFileName.convention("release.zip")
            destinationDirectory.set(project.layout.buildDirectory.dir("foundryvttModule"))
            from(project.layout.buildDirectory.file("foundryvttModule/module.json")) { into("$moduleId/") }
        }

        project.tasks.register<FoundryVTTModuleUploadGithubRelease>("foundryvttModuleUploadGithubRelease") {
            dependsOn("foundryvttModulePackage")
            archive.convention(project.layout.buildDirectory.file("foundryvttModule/release.zip"))
            githubRepo.convention(extension.githubRepo)
            githubToken.convention(extension.githubToken)
            githubUser.convention(extension.githubUser)
        }

        project.tasks.register<FoundryVTTModuleCreateRelease>("foundryvttModuleCreateRelease") {
            dependsOn("foundryvttModulePackage")
            archive.convention(project.layout.buildDirectory.file("foundryvttModule/release.zip"))
            githubRepo.convention(extension.githubRepo)
            githubUser.convention(extension.githubUser)
            foundryToken.convention(extension.foundryToken)
        }

        project.tasks.register("foundryvttRelease") {
            dependsOn("foundryvttModuleUploadGithubRelease", "foundryvttModuleCreateRelease")
            project.tasks.named("foundryvttModuleCreateRelease").get()
                .mustRunAfter("foundryvttModuleUploadGithubRelease")
        }

        project.tasks.register("testGit") {
            doLast {
                createRepo(project.layout.projectDirectory.asFile.resolve(".git"))
                    .currentTag()
            }
        }
    }
}