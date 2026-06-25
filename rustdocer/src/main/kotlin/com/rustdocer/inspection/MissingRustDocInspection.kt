package com.rustdocer.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.rustdocer.action.RustDocGenerationSupport
import com.rustdocer.style.RustStyleLoader
import com.rustdocer.util.RustValidator
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.RsVisitor
import org.rust.lang.core.psi.ext.RsNameIdentifierOwner

private fun buildRustDocVisitor(holder: ProblemsHolder, highlightType: ProblemHighlightType): PsiElementVisitor =
    object : RsVisitor() {
        override fun visitFunction(o: RsFunction) = check(o)
        override fun visitStructItem(o: RsStructItem) = check(o)
        override fun visitEnumItem(o: RsEnumItem) = check(o)
        override fun visitTraitItem(o: RsTraitItem) = check(o)
        override fun visitImplItem(o: RsImplItem) = checkImpl(o)

        private fun check(element: PsiElement) {
            if (element !is RsNameIdentifierOwner) return
            if (hasDocComment(element)) return
            if (!RustValidator.checkElementIsAllowed(element)) return

            val anchor = element.nameIdentifier ?: return
            holder.registerProblem(
                anchor,
                "Missing RustDoc comment",
                highlightType,
                GenerateRustDocQuickFix(),
            )
        }

        private fun checkImpl(impl: RsImplItem) {
            if (hasDocComment(impl)) return
            if (!RustValidator.checkElementIsAllowed(impl)) return

            val anchor = impl.typeReference ?: return
            holder.registerProblem(
                anchor,
                "Missing RustDoc comment",
                highlightType,
                GenerateRustDocQuickFix(),
            )
        }

        private fun hasDocComment(element: PsiElement): Boolean {
            var prev = element.prevSibling
            while (prev is PsiWhiteSpace) prev = prev.prevSibling
            return prev is PsiComment && prev.text.startsWith("///")
        }
    }

/**
 * Suggests adding RustDoc via the Alt+Enter intention bulb. Enabled by default.
 */
class MissingRustDocInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        buildRustDocVisitor(holder, ProblemHighlightType.INFORMATION)
}

private class GenerateRustDocQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = "Generate RustDoc"

    override fun startInWriteAction(): Boolean = false

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement?.parent ?: return
        val target = when (element) {
            is RsFunction, is RsStructItem, is RsEnumItem, is RsTraitItem, is RsImplItem -> element
            else -> PsiTreeUtil.getParentOfType(
                element,
                RsFunction::class.java,
                RsStructItem::class.java,
                RsEnumItem::class.java,
                RsTraitItem::class.java,
                RsImplItem::class.java,
            ) ?: return
        }
        val policy = RustStyleLoader.resolve(project).existingDocPolicy
        RustDocGenerationSupport.applyTo(project, target, policy)
    }
}
