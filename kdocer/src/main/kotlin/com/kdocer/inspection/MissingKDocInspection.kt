package com.kdocer.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.kdocer.action.KDocGenerationSupport
import com.kdocer.style.StyleLoader
import com.kdocer.util.Validator
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtVisitorVoid

private fun buildKDocVisitor(holder: ProblemsHolder, highlightType: ProblemHighlightType): PsiElementVisitor =
    object : KtVisitorVoid() {
        override fun visitNamedFunction(function: KtNamedFunction) = check(function)
        override fun visitClassOrObject(classOrObject: KtClassOrObject) = check(classOrObject)
        override fun visitProperty(property: KtProperty) = check(property)

        private fun check(declaration: KtNamedDeclaration) {
            if (declaration.docComment != null) return
            if (PsiTreeUtil.getParentOfType(declaration, KtBlockExpression::class.java) != null) return
            if (!Validator.checkElementIsAllowed(declaration)) return

            val anchor = declaration.nameIdentifier ?: return
            holder.registerProblem(
                anchor,
                "Missing KDoc comment",
                highlightType,
                GenerateKDocQuickFix(),
            )
        }
    }

/**
 * Suggests adding KDoc via the Alt+Enter intention bulb (no underline in the editor).
 * Enabled by default.
 */
class MissingKDocInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        buildKDocVisitor(holder, ProblemHighlightType.INFORMATION)
}

/**
 * Highlights undocumented declarations with a subtle underline.
 * Disabled by default — enable it in Settings > Editor > Inspections > KDoc-er.
 */
class MissingKDocWarningInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        buildKDocVisitor(holder, ProblemHighlightType.WEAK_WARNING)
}

private class GenerateKDocQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = "Generate KDoc"

    override fun startInWriteAction(): Boolean = false

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val declaration = PsiTreeUtil.getParentOfType(descriptor.psiElement, KtDeclaration::class.java) ?: return
        val policy = StyleLoader.resolve(project).existingDocPolicy
        KDocGenerationSupport.applyTo(project, declaration, policy)
    }
}
