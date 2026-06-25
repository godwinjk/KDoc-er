package com.dartdocer.generator

import com.docer.engine.generator.DocGenerator

/**
 * Base interface for all Dart doc generators. Provides helper methods for building
 * `///` triple-slash documentation lines.
 */
interface DartDocGenerator : DocGenerator {

    /** Appends a `/// text` documentation line followed by a newline. */
    fun StringBuilder.appendDocLine(text: String = ""): StringBuilder =
        if (text.isEmpty()) append("///\n") else append("/// ").append(text).append('\n')

    /** Appends an empty `///` documentation line followed by a newline. */
    fun StringBuilder.appendEmptyDocLine(): StringBuilder = append("///\n")
}
