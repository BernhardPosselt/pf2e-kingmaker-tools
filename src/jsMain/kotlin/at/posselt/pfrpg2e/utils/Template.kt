package at.posselt.pfrpg2e.utils

import at.posselt.pfrpg2e.Config
import com.foundryvtt.core.loadTemplates
import com.foundryvtt.core.renderTemplate
import kotlinx.coroutines.await

private const val DIST_PATH = "modules/${Config.moduleId}/dist"

fun resolveTemplatePath(path: String) = "$DIST_PATH/$path"

suspend fun loadTpls(paths: Array<Pair<String, String>>) {
    val resolvedPaths = paths.map {
        it.first to resolveTemplatePath(it.second)
    }.toRecord()
    loadTemplates(resolvedPaths).await()
}

suspend fun tpl(path: String, ctx: Any? = null): String {
    return renderTemplate("$DIST_PATH/$path", ctx).await()
}