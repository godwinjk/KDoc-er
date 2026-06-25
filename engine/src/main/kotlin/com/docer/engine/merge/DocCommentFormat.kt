package com.docer.engine.merge

data class DocCommentFormat(
    val blockStart: String?,
    val blockEnd: String?,
    val linePrefix: String,
    val subjectTags: Set<String>,
) {
    companion object {
        val KDOC = DocCommentFormat(
            blockStart = "/**",
            blockEnd = " */",
            linePrefix = " * ",
            subjectTags = setOf("param", "property", "throws", "exception"),
        )

        val TRIPLE_SLASH = DocCommentFormat(
            blockStart = null,
            blockEnd = null,
            linePrefix = "/// ",
            subjectTags = emptySet(),
        )
    }
}
