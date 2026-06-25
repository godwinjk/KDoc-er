package com.dartdocer.inspection

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
import com.dartdocer.action.DartDocGenerationSupport
import com.dartdocer.style.DartStyleLoader
import com.dartdocer.util.DartValidator
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartEnumDefinition
import com.jetbrains.lang.dart.psi.DartExtensionDeclaration
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartMixinDeclaration
import com.jetbrains.lang.dart.psi.DartSetterDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList

/**
 * Suggests adding DartDoc via the Alt+Enter intention bulb.
 * Enabled by default.
 */
class MissingDartDocInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is DartClassDefinition,
                    is DartEnumDefinition,
                    is DartFunctionDeclarationWithBodyOrNative,
                    is DartMethodDeclaration,
                    is DartGetterDeclaration,
                    is DartSetterDeclaration,
                    is DartExtensionDeclaration,
                    is DartMixinDeclaration,
                    is DartVarDeclarationList -> check(element)
                }
            }

            private fun check(element: PsiElement) {
                if (hasDocComment(element)) return
                if (!DartValidator.checkElementIsAllowed(element)) return

                val anchor = when (element) {
                    is DartComponent -> element.componentName ?: return
                    else -> return
                }

                holder.registerProblem(
                    anchor,
                    "Missing DartDoc comment",
                    ProblemHighlightType.INFORMATION,
                    GenerateDartDocQuickFix(),
                )
            }
        }

    private fun hasDocComment(element: PsiElement): Boolean {
        var prev = element.prevSibling
        while (prev is PsiWhiteSpace) {
            prev = prev.prevSibling
        }
        return prev is PsiComment && prev.text.trimStart().startsWith("///")
    }
}

private class GenerateDartDocQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = "Generate DartDoc"

    override fun startInWriteAction(): Boolean = false

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement?.parent ?: return
        val target = when (element) {
            is DartClassDefinition,
            is DartFunctionDeclarationWithBodyOrNative,
            is DartMethodDeclaration,
            is DartGetterDeclaration,
            is DartSetterDeclaration,
            is DartVarDeclarationList,
            is DartExtensionDeclaration,
            is DartMixinDeclaration -> element
            else -> PsiTreeUtil.getParentOfType(
                element,
                DartClassDefinition::class.java,
                DartFunctionDeclarationWithBodyOrNative::class.java,
                DartMethodDeclaration::class.java,
            ) ?: return
        }
        val policy = DartStyleLoader.resolve(project).existingDocPolicy
        DartDocGenerationSupport.applyTo(project, target, policy)
    }
}
