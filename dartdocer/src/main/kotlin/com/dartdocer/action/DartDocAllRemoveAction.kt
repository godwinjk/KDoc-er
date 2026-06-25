package com.dartdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.lang.dart.psi.DartFile

/**
 * Removes every `///` DartDoc comment in the current Dart file.
 */
class DartDocAllRemoveAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is DartFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? DartFile ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        // Collect all /// comment elements
        val docComments = mutableListOf<PsiComment>()
        collectDocComments(psiFile, docComments)
        if (docComments.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(project) {
            docComments.forEach { if (it.isValid) it.delete() }
        }
    }

    private fun collectDocComments(element: com.intellij.psi.PsiElement, result: MutableList<PsiComment>) {
        var child = element.firstChild
        while (child != null) {
            if (child is PsiComment && child.text.trimStart().startsWith("///")) {
                result.add(child)
            } else if (child !is PsiWhiteSpace) {
                collectDocComments(child, result)
            }
            child = child.nextSibling
        }
    }
}
