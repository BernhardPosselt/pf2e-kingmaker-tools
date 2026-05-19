package at.posselt.utils

internal fun String.isPrerelease() =
    contains("-beta") || contains("-alpha") || contains("-rc")