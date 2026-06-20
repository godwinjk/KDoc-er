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

/**
 * Flags a qualifying declaration that has no KDoc with a weak warning, offering an
 * Alt+Enter quick-fix that generates one. "Qualifying" reuses [Validator], so the
 * inspection honours the same visibility/element-type settings as the actions.
 *
 * Enable or disable it (and tune its severity) in Settings > Editor > Inspections > KDoc-er.
 */
class MissingKDocInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) = check(function, holder)
            override fun visitClassOrObject(classOrObject: KtClassOrObject) = check(classOrObject, holder)
            override fun visitProperty(property: KtProperty) = check(property, holder)
        }

    private fun check(declaration: KtNamedDeclaration, holder: ProblemsHolder) {
        if (declaration.docComment != null) return
        if (isLocal(declaration)) return
        if (!Validator.checkElementIsAllowed(declaration)) return

        val anchor = declaration.nameIdentifier ?: return
        holder.registerProblem(
            anchor,
            "Missing KDoc comment",
            ProblemHighlightType.WEAK_WARNING,
            GenerateKDocQuickFix(),
        )
    }

    /** A declaration nested inside a code block (a local fun/class/var) is not documented. */
    private fun isLocal(declaration: KtDeclaration): Boolean =
        PsiTreeUtil.getParentOfType(declaration, KtBlockExpression::class.java) != null
}

private class GenerateKDocQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = "Generate KDoc"

    // Generation runs type inference (Analysis API), which is prohibited inside a write
    // action, so opt out — applyTo wraps the PSI edit in its own write command.
    override fun startInWriteAction(): Boolean = false

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val declaration = PsiTreeUtil.getParentOfType(descriptor.psiElement, KtDeclaration::class.java) ?: return
        val policy = StyleLoader.resolve(project).existingKDocPolicy
        KDocGenerationSupport.applyTo(project, declaration, policy)
    }
}
