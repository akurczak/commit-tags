package pl.kurczak.idea.committags.common

import com.intellij.openapi.project.Project
import pl.kurczak.idea.committags.common.settings.MainSettingsState
import pl.kurczak.idea.committags.common.settings.mainSettings

internal data class TaggedMessage(
    private val tagPrefix: String,
    private val tagSuffix: String,
    val bareMessage: String,
    val tags: List<String>
) {

    val tagsString = tags.joinToString(separator = "") { "$tagPrefix$it$tagSuffix" }

    val fullMessage = "$tagsString $bareMessage"

    val fullMessageWithDot = if (fullMessage.last() == '.') {
        fullMessage
    } else {
        "$fullMessage."
    }
}

private val messageRegexCache = mutableMapOf<Pair<String, String>, Regex>()

internal fun parseTaggedMessage(project: Project, message: String): TaggedMessage {

    val settings = project.mainSettings
    val messageRegex = messageRegexCache.getOrPut(settings.tags) {
        val escapedPrefix = Regex.escape(settings.tagPrefix)
        val escapedSuffix = Regex.escape(settings.tagSuffix)
        "^(?<tags>(${escapedPrefix}[^${escapedSuffix}]*${escapedSuffix})*)(?<message>.*)$".toRegex()
    }
    val groups = messageRegex.matchEntire(message)?.groups
    val tags = groups?.get("tags")?.value ?: ""
    val bareMessage = groups?.get("message")?.value?.trim() ?: ""
    return TaggedMessage(settings.tagPrefix, settings.tagSuffix, bareMessage, splitTags(settings.tagPrefix, settings.tagSuffix, tags))
}

private fun splitTags(tagPrefix: String, tagSuffix: String, tagsString: String): List<String> {
    val tags = tagsString.split(tagSuffix + tagPrefix).toMutableList()
    if (tags.size > 0) {
        tags[0] = tags[0].drop(1)
        tags[tags.lastIndex] = tags[tags.lastIndex].dropLast(1)
    }
    return tags
}

private val MainSettingsState.tags get() = tagPrefix to tagSuffix
