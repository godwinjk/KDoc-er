package com.rustdocer.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsFile

/**
 * Removes every `///` doc comment in the current Rust file.
 */
class RustDocAllRemoveAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(action: AnActionEvent) {
        super.update(action)
        val enabled = action.getData(PlatformDataKeys.PSI_FILE) is RsFile
        action.presentation.isVisible = enabled
        action.presentation.isEnabled = enabled
    }

    override fun actionPerformed(action: AnActionEvent) {
        val psiFile = action.getData(PlatformDataKeys.PSI_FILE) as? RsFile ?: return
        val project = psiFile.project
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val docComments = PsiTreeUtil.collectElementsOfType(psiFile, PsiComment::class.java)
            .filter { it.text.startsWith("///") }
        if (docComments.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(project) {
            docComments.forEach { if (it.isValid) it.delete() }
        }
    }
}
