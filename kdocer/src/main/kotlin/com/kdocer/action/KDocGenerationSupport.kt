package com.kdocer.action

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.kdocer.generator.ClassKDocGenerator
import com.kdocer.generator.KDocGenerator
import com.kdocer.generator.NamedFunctionKDocGenerator
import com.kdocer.generator.PropertyKDocGenerator
import com.docer.engine.merge.ExistingDocPolicy
import com.docer.engine.merge.DocMerger
import com.kdocer.util.Validator
import org.jetbrains.kotlin.idea.kdoc.KDocElementFactory
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * Shared generation logic for the actions, the inspection quick-fix and the directory
 * action. Generation, the existing-KDoc [policy][ExistingDocPolicy] (merge/keep/replace)
 * and insertion all live here so every entry point behaves identically.
 */
object KDocGenerationSupport {

    fun generatorFor(project: Project, declaration: KtDeclaration): KDocGenerator? = when (declaration) {
        is KtClassOrObject -> ClassKDocGenerator(project, declaration)
        is KtNamedFunction -> NamedFunctionKDocGenerator(project, declaration)
        is KtProperty -> PropertyKDocGenerator(project, declaration)
        else -> null
    }

    /** Every documentable, non-local declaration in [file] that passes [Validator]. */
    fun collectTargets(file: KtFile): List<KtDeclaration> =
        PsiTreeUtil.collectElementsOfType(file, KtDeclaration::class.java)
            .filter { it is KtClassOrObject || it is KtNamedFunction || it is KtProperty }
            .filter { !isLocal(it) && Validator.checkElementIsAllowed(it) }

    /**
     * Computes the KDoc text to insert (generate + merge per [policy]). May run type
     * inference (Analysis API), so it **must NOT be called inside a write action**.
     *
     * @return the text to insert, or `null` to leave the declaration untouched.
     */
    fun computeText(project: Project, declaration: KtDeclaration, policy: ExistingDocPolicy): String? {
        if (!declaration.isValid) return null
        val generator = generatorFor(project, declaration) ?: return null
        return DocMerger.resolve(declaration.docComment?.text, generator.generate(), policy)
    }

    /** Inserts pre-computed [finalText] onto [declaration]. **Must be called inside a write action.** */
    fun insert(project: Project, declaration: KtDeclaration, finalText: String) {
        if (!declaration.isValid) return
        val kdoc = KDocElementFactory(project).createKDocFromText(finalText)
        val existing = declaration.docComment
        val inserted = if (existing != null) existing.replace(kdoc) else declaration.addAfter(kdoc, null)
        CodeStyleManager.getInstance(project).reformat(inserted)
    }

    /** Convenience: compute (no write action) then insert (own write action) for one declaration. */
    fun applyTo(project: Project, declaration: KtDeclaration, policy: ExistingDocPolicy) {
        val text = computeText(project, declaration, policy) ?: return
        WriteCommandAction.runWriteCommandAction(project) { insert(project, declaration, text) }
    }

    /** Documents every qualifying declaration in [file]: compute all texts, then one write command. */
    fun documentFile(project: Project, file: KtFile, policy: ExistingDocPolicy) {
        val edits = collectTargets(file).mapNotNull { d -> computeText(project, d, policy)?.let { d to it } }
        if (edits.isEmpty()) return
        WriteCommandAction.runWriteCommandAction(project) {
            edits.forEach { (declaration, text) -> insert(project, declaration, text) }
        }
    }

    /** A declaration nested inside a code block (a local fun/class/var) is not documented. */
    private fun isLocal(declaration: KtDeclaration): Boolean =
        PsiTreeUtil.getParentOfType(declaration, KtBlockExpression::class.java) != null
}
