package com.rustdocer.generator

import com.docer.engine.generator.DocGenerator

/**
 * Base interface for all RustDoc generators. Provides helper methods for building
 * `///` outer doc comment lines.
 */
interface RustDocGenerator : DocGenerator {

    /** Appends a `/// text` documentation line followed by a newline. */
    fun StringBuilder.appendDocLine(text: String = ""): StringBuilder =
        if (text.isEmpty()) append("///\n") else append("/// ").append(text).append('\n')

    /** Appends an empty `///` documentation line followed by a newline. */
    fun StringBuilder.appendEmptyDocLine(): StringBuilder = append("///\n")
}
