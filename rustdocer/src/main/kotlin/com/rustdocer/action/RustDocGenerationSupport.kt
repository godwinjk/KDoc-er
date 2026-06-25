package com.rustdocer.action

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.docer.engine.merge.DocCommentFormat
import com.docer.engine.merge.DocMerger
import com.docer.engine.merge.ExistingDocPolicy
import com.rustdocer.generator.RustDocGenerator
import com.rustdocer.generator.RustEnumDocGenerator
import com.rustdocer.generator.RustFnDocGenerator
import com.rustdocer.generator.RustImplDocGenerator
import com.rustdocer.generator.RustStructDocGenerator
import com.rustdocer.generator.RustTraitDocGenerator
import com.rustdocer.util.RustValidator
import org.rust.lang.RsFileType
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem

/**
 * Shared generation logic for the RustDoc actions, the inspection quick-fix and the
 * directory action. Generation, the existing-doc policy (merge/keep/replace) and insertion
 * all live here so every entry point behaves identically.
 */
object RustDocGenerationSupport {

    fun generatorFor(project: Project, element: PsiElement): RustDocGenerator? = when (element) {
        is RsFunction -> RustFnDocGenerator(project, element)
        is RsStructItem -> RustStructDocGenerator(project, element)
        is RsEnumItem -> RustEnumDocGenerator(project, element)
        is RsTraitItem -> RustTraitDocGenerator(project, element)
        is RsImplItem -> RustImplDocGenerator(project, element)
        else -> null
    }

    /** Every documentable, non-local declaration in [file] that passes [RustValidator]. */
    fun collectTargets(file: RsFile): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()
        collectRustDeclarations(file, targets)
        return targets.filter { RustValidator.checkElementIsAllowed(it) }
    }

    private fun collectRustDeclarations(element: PsiElement, targets: MutableList<PsiElement>) {
        for (child in element.children) {
            when (child) {
                is RsFunction,
                is RsStructItem,
                is RsEnumItem,
                is RsTraitItem,
                is RsImplItem -> {
                    targets.add(child)
                    // Also collect members inside impl blocks, traits, etc.
                    collectMembers(child, targets)
                }
                else -> collectRustDeclarations(child, targets)
            }
        }
    }

    private fun collectMembers(element: PsiElement, targets: MutableList<PsiElement>) {
        for (child in element.children) {
            when (child) {
                is RsFunction -> targets.add(child)
                is RsStructItem -> targets.add(child)
                is RsEnumItem -> targets.add(child)
                is RsTraitItem -> targets.add(child)
                else -> {
                    // Recurse into impl body, trait members list, etc.
                    if (child.children.isNotEmpty()) {
                        collectMembers(child, targets)
                    }
                }
            }
        }
    }

    /**
     * Computes the RustDoc text to insert (generate + merge per [policy]).
     *
     * @return the text to insert, or `null` to leave the declaration untouched.
     */
    fun computeText(project: Project, element: PsiElement, policy: ExistingDocPolicy): String? {
        if (!element.isValid) return null
        val generator = generatorFor(project, element) ?: return null
        val existingDoc = findExistingDocComment(element)
        return DocMerger.resolve(existingDoc, generator.generate(), policy, DocCommentFormat.TRIPLE_SLASH)
    }

    /** Inserts pre-computed [finalText] onto [element]. **Must be called inside a write action.** */
    fun insert(project: Project, element: PsiElement, finalText: String) {
        if (!element.isValid) return

        // Remove existing doc comment if present
        removeExistingDocComment(element)

        // Create the doc comment PSI elements
        val docText = finalText.trimEnd('\n')
        val dummyFile = PsiFileFactory.getInstance(project)
            .createFileFromText("_dummy.rs", RsFileType, "$docText\nfn _x() {}")

        // Collect all the /// comment elements from the dummy file
        val commentElements = mutableListOf<PsiElement>()
        var child: PsiElement? = dummyFile.firstChild
        while (child != null) {
            if (child is PsiComment && child.text.startsWith("///")) {
                commentElements.add(child)
            } else if (child is PsiWhiteSpace && commentElements.isNotEmpty()) {
                // Include whitespace between comment lines
                commentElements.add(child)
            } else if (commentElements.isNotEmpty()) {
                break
            }
            child = child.nextSibling
        }

        if (commentElements.isEmpty()) return

        // Insert comment elements before the declaration
        val parent = element.parent
        for (commentEl in commentElements) {
            parent.addBefore(commentEl.copy(), element)
        }

        // Add a newline between comment and declaration if needed
        val prevSibling = element.prevSibling
        if (prevSibling != null && prevSibling !is PsiWhiteSpace) {
            val newline = PsiFileFactory.getInstance(project)
                .createFileFromText("_nl.rs", RsFileType, "\n")
                .firstChild
            if (newline != null) {
                parent.addBefore(newline, element)
            }
        }
    }

    /** Convenience: compute (no write action) then insert (own write action) for one element. */
    fun applyTo(project: Project, element: PsiElement, policy: ExistingDocPolicy) {
        val text = computeText(project, element, policy) ?: return
        WriteCommandAction.runWriteCommandAction(project) { insert(project, element, text) }
    }

    /** Documents every qualifying declaration in [file]: compute all texts, then one write command. */
    fun documentFile(project: Project, file: RsFile, policy: ExistingDocPolicy) {
        val edits = collectTargets(file).mapNotNull { d -> computeText(project, d, policy)?.let { d to it } }
        if (edits.isEmpty()) return
        WriteCommandAction.runWriteCommandAction(project) {
            edits.forEach { (element, text) -> insert(project, element, text) }
        }
    }

    /** Finds the existing `///` doc comment text attached to [element], or `null`. */
    private fun findExistingDocComment(element: PsiElement): String? {
        var prev = element.prevSibling

        // Skip whitespace to find comments
        while (prev is PsiWhiteSpace) {
            prev = prev.prevSibling
        }

        // Walk backwards collecting /// comment lines
        val collected = mutableListOf<PsiElement>()
        while (prev is PsiComment && prev.text.trimStart().startsWith("///")) {
            collected.add(0, prev)
            prev = prev.prevSibling
            // Skip whitespace between comment lines
            while (prev is PsiWhiteSpace && !prev.text.contains("\n\n")) {
                prev = prev.prevSibling
            }
        }

        if (collected.isEmpty()) return null
        return collected.joinToString("\n") { it.text }
    }

    /** Removes the `///` doc comment attached to [element]. */
    private fun removeExistingDocComment(element: PsiElement) {
        val toRemove = mutableListOf<PsiElement>()
        var prev = element.prevSibling

        while (prev != null) {
            when {
                prev is PsiComment && prev.text.trimStart().startsWith("///") -> {
                    toRemove.add(prev)
                    prev = prev.prevSibling
                }
                prev is PsiWhiteSpace -> {
                    if (toRemove.isNotEmpty()) toRemove.add(prev)
                    prev = prev.prevSibling
                }
                else -> break
            }
        }

        toRemove.forEach { if (it.isValid) it.delete() }
    }

    /** Removes the doc comment from [element] if present. For the remove actions. */
    fun removeDocComment(element: PsiElement) {
        removeExistingDocComment(element)
    }
}
