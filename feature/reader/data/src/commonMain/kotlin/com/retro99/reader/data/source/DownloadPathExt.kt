package com.retro99.reader.data.source

internal data class ParsedDownloadPath(
    val path: String,
    val queryParams: Map<String, String>,
)

internal fun String.parseDownloadPath(): ParsedDownloadPath {
    val questionIndex = indexOf('?')
    return if (questionIndex >= 0) {
        val pathPart = substring(0, questionIndex)
        val queryString = substring(questionIndex + 1)
        val params = queryString
            .split("&")
            .filter { it.isNotEmpty() }
            .associate { pair ->
                val eqIndex = pair.indexOf('=')
                if (eqIndex >= 0) {
                    pair.substring(0, eqIndex) to pair.substring(eqIndex + 1)
                } else {
                    pair to ""
                }
            }
        ParsedDownloadPath(pathPart, params)
    } else {
        ParsedDownloadPath(this, emptyMap())
    }
}
