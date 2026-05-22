package at.posselt.utils

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

fun createRepo(directory: File): Repository {
    return FileRepositoryBuilder()
        .setGitDir(directory)
        .readEnvironment()
        .findGitDir()
        .build()
}

fun Repository.createTag(tag: String) {
    Git(this).use {
        it.tag().setName(tag).call()
    }
}

fun Repository.currentTag(): String? {
    Git(this).use {
        it.tagList().call().asSequence()
            .forEach { println(it) }
    }
    return null
}