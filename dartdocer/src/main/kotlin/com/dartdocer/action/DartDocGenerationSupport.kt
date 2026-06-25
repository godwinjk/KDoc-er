package com.dartdocer.action

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.dartdocer.generator.DartClassDocGenerator
import com.dartdocer.generator.DartDocGenerator
import com.dartdocer.generator.DartFieldDocGenerator
import com.dartdocer.generator.DartFunctionDocGenerator
import com.docer.engine.merge.DocCommentFormat
import com.docer.engine.merge.DocMerger
import com.docer.engine.merge.ExistingDocPolicy
import com.dartdocer.util.DartValidator
import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartEnumDefinition
import com.jetbrains.lang.dart.psi.DartExtensionDeclaration
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartMixinDeclaration
import com.jetbrains.lang.dart.psi.DartSetterDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList

/**
 * Shared generation logic for the DartDoc actions, the inspection quick-fix and the
 * directory action. Generation, the existing-doc policy (merge/keep/replace) and insertion
 * all live here so every entry point behaves identically.
 */
object DartDocGenerationSupport {

    fun generatorFor(project: Project, element: PsiElement): DartDocGenerator? = when (element) {
        is DartClassDefinition -> DartClassDocGenerator(project, element)
        is DartEnumDefinition -> null // Could add DartEnumDocGenerator later
        is DartFunctionDeclarationWithBodyOrNative -> DartFunctionDocGenerator(project, element)
        is DartMethodDeclaration -> DartFunctionDocGenerator(project, element)
        is DartGetterDeclaration -> DartFieldDocGenerator(project, element)
        is DartSetterDeclaration -> DartFieldDocGenerator(project, element)
        is DartVarDeclarationList -> DartFieldDocGenerator(project, element)
        else -> null
    }

    /** Every documentable, non-local declaration in [file] that passes [DartValidator]. */
    fun collectTargets(file: DartFile): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()
        collectDartDeclarations(file, targets)
        return targets.filter { DartValidator.checkElementIsAllowed(it) }
    }

    private fun collectDartDeclarations(element: PsiElement, targets: MutableList<PsiElement>) {
        for (child in element.children) {
            when (child) {
                is DartClassDefinition,
                is DartEnumDefinition,
                is DartFunctionDeclarationWithBodyOrNative,
                is DartExtensionDeclaration,
                is DartMixinDeclaration -> {
                    targets.add(child)
                    // Also collect members inside classes, extensions, mixins
                    collectClassMembers(child, targets)
                }
                is DartVarDeclarationList -> targets.add(child)
                else -> collectDartDeclarations(child, targets)
            }
        }
    }

    private fun collectClassMembers(element: PsiElement, targets: MutableList<PsiElement>) {
        for (child in element.children) {
            when (child) {
                is DartMethodDeclaration -> targets.add(child)
                is DartGetterDeclaration -> targets.add(child)
                is DartSetterDeclaration -> targets.add(child)
                is DartVarDeclarationList -> targets.add(child)
                else -> {
                    // Recurse into class body, members list, etc.
                    if (child.children.isNotEmpty()) {
                        collectClassMembers(child, targets)
                    }
                }
            }
        }
    }

    /**
     * Computes the DartDoc text to insert (generate + merge per [policy]).
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
            .createFileFromText("_dummy.dart", DartFileType.INSTANCE, "$docText\nvar _x;")

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
                .createFileFromText("_nl.dart", DartFileType.INSTANCE, "\n")
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
    fun documentFile(project: Project, file: DartFile, policy: ExistingDocPolicy) {
        val edits = collectTargets(file).mapNotNull { d -> computeText(project, d, policy)?.let { d to it } }
        if (edits.isEmpty()) return
        WriteCommandAction.runWriteCommandAction(project) {
            edits.forEach { (element, text) -> insert(project, element, text) }
        }
    }

    /** Finds the existing `///` doc comment text attached to [element], or `null`. */
    private fun findExistingDocComment(element: PsiElement): String? {
        val lines = mutableListOf<String>()
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
